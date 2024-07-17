package com.icbc.codeResolver.service;

import com.icbc.codeResolver.entity.MethodNode;
import org.apache.dubbo.config.annotation.DubboService;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.neo4j.driver.types.Relationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@DubboService(group = "joern")
@Component
public class JoernServiceImpl implements CodeResolverService {

    @Autowired
    private Neo4jClient neo4jClient;

    /**
     * TODO 等待修改
     * @param className
     * @param methodName
     * @return
     */
    @Override
    public List<String> getMethodUp(String className,String methodName) {
        String cypherQuery = "MATCH p = (endNode:METHOD)<-[:CALL|CONTAINS*]-(prevNodes:METHOD) where (not (prevNodes)<-[:CALL]-()) and (endNode.NAME=$METHOD_NAME AND endNode.FILENAME ENDS WITH $CLASS_NAME) RETURN p";
        Collection<Map<String, Object>> result = neo4jClient.query(cypherQuery)
                .bind(methodName).to("METHOD_NAME")
                .bind(className).to("CLASS_NAME")
                .fetch()
                .all();
        List<MethodNode> res = findRelation(result);
        return pathToList(res,false);
    }


    /**
     * @param className
     * @param methodName
     * @return
     */
    @Override
    public List<String> getMethodDown(String className,String methodName) {
        String cypherQuery = "MATCH p = (startNode:METHOD)-[:CALL|CONTAINS*]->(nextNodes:METHOD) WHERE (NOT (nextNodes)-[:CONTAINS]->(:CALL)) and (startNode.NAME=$METHOD_NAME AND startNode.FILENAME ENDS WITH $CLASS_NAME) RETURN p";
        Collection<Map<String, Object>> result = neo4jClient.query(cypherQuery)
                .bind(methodName).to("METHOD_NAME")
                .bind(className).to("CLASS_NAME")
                .fetch()
                .all();
        List<MethodNode> res = findRelation(result);
        return pathToList(res,true);
    }

    /**
     * url精确查找
     * @param url
     * @return
     */
    @Override
    public List<String> getUrlPath(List<String> url) {
        String first=url.get(0);
        String left=url.get(1);
        String name="RequestMapping";
        String like="Mapping";
        String cypherQuery = "MATCH (n:ANNOTATION)<-[:AST]-(c:TYPE_DECL)-[:AST]->(m:METHOD)-[:AST]->(n1:ANNOTATION) WHERE n.NAME =$NAME" +
                "  AND n.CODE contains $FIRST" +
                "  AND n1.NAME contains $LIKE" +
                "  AND n1.CODE contains $LEFT" +
                " WITH m" +
                " MATCH p=(m)-[:CALL|CONTAINS*]->(nextNodes:METHOD)" +
                " WHERE NOT (nextNodes)-[:CONTAINS]->(:CALL)" +
                " RETURN p";
        Collection<Map<String, Object>> result = neo4jClient.query(cypherQuery)
                .bind(first).to("FIRST")
                .bind(left).to("LEFT")
                .bind(name).to("NAME")
                .bind(like).to("LIKE")
                .fetch()
                .all();
        List<MethodNode> res = findRelation(result);
        return pathToList(res,true);
    }

    @Override
    public List<String> getDataBaseInfo(String dataBaseName, String tableName, String fieldName) {
        String cypherQuery = "match (n:ANNOTATION)<-[:AST]-(m:METHOD) where (n.CODE starts with '@Insert(' or n.CODE starts with '@Delete(' or n.CODE starts with '@Select(' or n.CODE starts with '@Update(') return n,m";
        Collection<Map<String, Object>> result = neo4jClient.query(cypherQuery)
                .fetch()
                .all();
        List<Map<String, Object>> resultList = new ArrayList<>(result);
        List<MethodNode> ans = new ArrayList<>();
        String label = null;
        InternalNode annotation_node=null;
        InternalNode method_node=null;
        for (Map<String, Object> record : resultList){
            Object nodeObject = record.get("n");
            if (nodeObject instanceof InternalNode) {
                annotation_node = (InternalNode) nodeObject;
            }
            nodeObject = record.get("m");
            if (nodeObject instanceof InternalNode) {
                method_node = (InternalNode) nodeObject;
            }
            String code=annotation_node.get("CODE").asString();
            MethodNode head = new MethodNode("1", "1");

            boolean x=code.contains(fieldName) ||code.contains("*");
            boolean y=code.contains("from "+tableName)||code.contains("update "+tableName)||code.contains("insert into "+tableName);
            if((code.contains("from "+tableName)||code.contains("update "+tableName)||code.contains("insert into "+tableName))&&(code.contains(fieldName) ||code.contains("*"))){
                label=annotation_node.labels().iterator().next();
                head = new MethodNode(label, annotation_node.get("NAME").asString());
                //然后向上搜索
                String method_name=method_node.get("NAME").asString();
                MethodNode cur = head;
                cypherQuery = "MATCH p = (n:ANNOTATION)<-[:AST]-(endNode:METHOD)<-[:CALL|CONTAINS*]-(prevNodes:METHOD) where (not (prevNodes)<-[:CALL]-()) and (endNode.NAME=$method_name) and n.CODE=$CODE RETURN p";
                result = neo4jClient.query(cypherQuery)
                        .bind(method_name).to("method_name")
                        .bind(code).to("CODE")
                        .fetch()
                        .all();
                List<MethodNode> res = findRelation(result);//这里的res是以$method_name结尾的方法调用
                ans.addAll(res);
            }

        }
        //List<MethodNode> res = findRelation(result);
        return pathToList(ans,false);
    }


    public List<MethodNode> findRelation(Collection<Map<String, Object>> result) {
        List<Map<String, Object>> resultList = new ArrayList<>(result);
        List<MethodNode> ans = new ArrayList<>();
        String label = null;
        for (Map<String, Object> record : resultList) {
            Path path = (Path) record.get("p");
            MethodNode head = new MethodNode("1", "1");
            MethodNode cur = head;
            int x = 0;
            for (Path.Segment segment : path) {
                Node startNode = null;
                if (x == 0) {
                    startNode = segment.start();
                    label = startNode.labels().iterator().next();
                    cur.next = new MethodNode(label, startNode.get("NAME").asString());
                    cur = cur.next;
                    x = 1;
                }
                Node endNode = segment.end();
                label = endNode.labels().iterator().next();
                cur.next = new MethodNode(label, endNode.get("NAME").asString());
                cur = cur.next;
            }
            ans.add(head.next);
        }
        System.out.println(1);
        return ans;
    }


    public List<String> pathToListDownPara(List<MethodNode> path,String s) {
        List<String> sbrList = new ArrayList<>();
        for (int i = 0; i < path.size(); i++) {
            StringBuilder stringBuilder = new StringBuilder("(CLASS)"+s+"->");
            MethodNode r = path.get(i);
            while (r != null) {
                stringBuilder.append('('+r.label+')'+r.name+"->");
                r = r.next;
            }
            sbrList.add(stringBuilder.substring(0, stringBuilder.length() - 2));
        }
        return sbrList;
    }



    public List<String> pathToList(List<MethodNode> path,boolean direction) {
        String spiltChar = direction?"->":"<-";
        List<String> sbrList = new ArrayList<>();
        for (int i = 0; i < path.size(); i++) {
            StringBuilder stringBuilder = new StringBuilder();
            MethodNode r = path.get(i);
            while (r != null) {
                stringBuilder.append('('+r.label+')'+r.name+spiltChar);
                r = r.next;
            }
            sbrList.add(stringBuilder.substring(0,stringBuilder.length()-2));
        }
        return sbrList;
    }








}
