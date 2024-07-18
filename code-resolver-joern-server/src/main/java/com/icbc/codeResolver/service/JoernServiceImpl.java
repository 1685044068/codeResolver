package com.icbc.codeResolver.service;

import com.icbc.codeResolver.entity.neo4jNode;
import org.apache.dubbo.config.annotation.DubboService;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
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
    public List<neo4jNode> getMethodUp(String className, String methodName) {
        String cypherQuery = "MATCH p = (endNode:METHOD)<-[:CALL|CONTAINS*]-(prevNodes:METHOD) where (not (prevNodes)<-[:CALL]-()) and (endNode.NAME=$METHOD_NAME AND endNode.FILENAME ENDS WITH $CLASS_NAME) RETURN p";
        Collection<Map<String, Object>> result = neo4jClient.query(cypherQuery)
                .bind(methodName).to("METHOD_NAME")
                .bind(className).to("CLASS_NAME")
                .fetch()
                .all();
        List<neo4jNode> res = findRelation(result);
        return res;
    }


    /**
     * @param className
     * @param methodName
     * @return
     */
    @Override
    public List<neo4jNode> getMethodDown(String className, String methodName) {
        String cypherQuery = "MATCH p = (startNode:METHOD)-[:CALL|CONTAINS*]->(nextNodes:METHOD) WHERE (NOT (nextNodes)-[:CONTAINS]->(:CALL)) and (startNode.NAME=$METHOD_NAME AND startNode.FILENAME ENDS WITH $CLASS_NAME) RETURN p";
        Collection<Map<String, Object>> result = neo4jClient.query(cypherQuery)
                .bind(methodName).to("METHOD_NAME")
                .bind(className).to("CLASS_NAME")
                .fetch()
                .all();
        List<neo4jNode> res = findRelation(result);
        return res;
    }

    /**
     * url精确查找
     * @param url
     * @return
     */
    @Override
    public List<neo4jNode> getUrlPath(List<String> url) {
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
        List<neo4jNode> res = findRelation(result);
        return res;
    }

    @Override
    public List<neo4jNode> getDataBaseInfo(String dataBaseName, String tableName, String fieldName) {
        String cypherQuery = "match (n:ANNOTATION)<-[:AST]-(m:METHOD) where (n.CODE starts with '@Insert(' or n.CODE starts with '@Delete(' or n.CODE starts with '@Select(' or n.CODE starts with '@Update(') return n,m";
        Collection<Map<String, Object>> result = neo4jClient.query(cypherQuery)
                .fetch()
                .all();
        List<Map<String, Object>> resultList = new ArrayList<>(result);
        List<neo4jNode> ans = new ArrayList<>();
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
            if((code.contains("from "+tableName)||code.contains("update "+tableName)||code.contains("insert into "+tableName))&&(code.contains(fieldName) ||code.contains("*"))){
                //然后向上搜索
                String method_name=method_node.get("NAME").asString();
                cypherQuery = "MATCH p = (n:ANNOTATION)<-[:AST]-(endNode:METHOD)<-[:CALL|CONTAINS*]-(prevNodes:METHOD) where (not (prevNodes)<-[:CALL]-()) and (endNode.NAME=$method_name) and n.CODE=$CODE RETURN p";
                result = neo4jClient.query(cypherQuery)
                        .bind(method_name).to("method_name")
                        .bind(code).to("CODE")
                        .fetch()
                        .all();
                List<neo4jNode> res = findRelation(result);//这里的res是以$method_name结尾的方法调用
                ans.addAll(res);
            }
        }
        return ans;
    }

    /**
     * 根据pack查找
     * @param packetName
     * @return
     */
    @Override
    public List<neo4jNode> getClassName(String packetName) {
        String cypherQuery = "MATCH (n:TYPE_DECL) WHERE n.FULL_NAME STARTS WITH $PACK RETURN n";
        Collection<Map<String, Object>> result = neo4jClient.query(cypherQuery)
                .bind(packetName).to("PACK")
                .fetch()
                .all();
        List<Map<String, Object>> resultList = new ArrayList<>(result);
        List<neo4jNode> ans = new ArrayList<>();
        InternalNode class_node=null;
        for (Map<String, Object> record : resultList) {
            Object nodeObject = record.get("n");
            if (nodeObject instanceof InternalNode) {
                class_node = (InternalNode) nodeObject;
            }
            neo4jNode node=new neo4jNode(class_node.labels().iterator().next(),class_node.get("NAME").asString(),class_node.get("FULLNAME").asString(),class_node.get("CODE").asString(),class_node.get("FILENAME").asString());
            ans.add(node);
        }
        return ans;
    }
    /**
     * 根据类全路径查找
     * @param className
     * @return
     */
    @Override
    public List<neo4jNode> getMethodName(String className) {
        String cypherQuery = "MATCH (n:METHOD)<-[:AST]-(m:TYPE_DECL) WHERE m.FULL_NAME = $FULL_NAME RETURN m,n";
        Collection<Map<String, Object>> result = neo4jClient.query(cypherQuery)
                .bind(className).to("FULL_NAME")
                .fetch()
                .all();
        List<Map<String, Object>> resultList = new ArrayList<>(result);
        List<neo4jNode> ans = new ArrayList<>();
        InternalNode class_node=null;
        for (Map<String, Object> record : resultList) {
            Object nodeObject = record.get("n");
            if (nodeObject instanceof InternalNode) {
                class_node = (InternalNode) nodeObject;
            }
            neo4jNode node=new neo4jNode(class_node.labels().iterator().next(),class_node.get("NAME").asString(),class_node.get("FULLNAME").asString(),class_node.get("CODE").asString(),class_node.get("FILENAME").asString());
            ans.add(node);
        }
        return ans;
    }


    @Override
    public List<neo4jNode> getInvocationLink(String className, String methodName) {
        return null;
    }


    public List<neo4jNode> findRelation(Collection<Map<String, Object>> result) {
        List<Map<String, Object>> resultList = new ArrayList<>(result);
        List<neo4jNode> ans = new ArrayList<>();
        String label;
        for (Map<String, Object> record : resultList) {
            Path path = (Path) record.get("p");
            neo4jNode head = new neo4jNode("1", "1","1","1","1");
            neo4jNode cur = head;
            int x = 0;
            for (Path.Segment segment : path) {
                Node startNode;
                if (x == 0) {
                    startNode = segment.start();
                    label = startNode.labels().iterator().next();
                    if(label.equals("ANNOTATION")){
                        cur.next = new neo4jNode(label, startNode.get("NAME").asString(),startNode.get("FULLNAME").asString(),startNode.get("CODE").asString());
                    }
                    else{
                        cur.next = new neo4jNode(label, startNode.get("NAME").asString(),startNode.get("FULLNAME").asString(),startNode.get("CODE").asString(),startNode.get("FILENAME").asString());
                    }
                    cur = cur.next;
                    x = 1;
                }
                Node endNode = segment.end();
                label = endNode.labels().iterator().next();
                if(label.equals("METHOD")||label.equals("ANNOTATION")){
                    if(label.equals("ANNOTATION")){
                        cur.next = new neo4jNode(label, endNode.get("NAME").asString(),endNode.get("FULLNAME").asString(),endNode.get("CODE").asString());
                    }
                    else{
                        cur.next = new neo4jNode(label, endNode.get("NAME").asString(),endNode.get("FULLNAME").asString(),endNode.get("CODE").asString(),endNode.get("FILENAME").asString());
                    }
                    cur = cur.next;
                }
            }
            ans.add(head.next);
        }
        System.out.println(1);
        return ans;
    }


}
