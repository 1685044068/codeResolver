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

    @Override
    public List<String> getMethodUp(String method) {
        String cypherQuery = "MATCH p = (endNode:METHOD{NAME:$NAME})<-[:CALL|CONTAINS*]-(prevNodes:METHOD) where not (prevNodes)<-[:CALL]-() RETURN p";
        Collection<Map<String, Object>> result = neo4jClient.query(cypherQuery)
                .bind(method).to("NAME")
                .fetch()
                .all();
        List<MethodNode> res = findRelation(result);
        return pathToList(res,false);
    }

    @Override
    public List<String> getMethodDown(String method) {
        String cypherQuery = "MATCH p = (startNode:METHOD {NAME:$NAME })-[:CALL|CONTAINS*]->(nextNodes:METHOD) WHERE NOT (nextNodes)-[:CONTAINS]->(:CALL) RETURN p";
        Collection<Map<String, Object>> result = neo4jClient.query(cypherQuery)
                .bind(method).to("NAME")
                .fetch()
                .all();
        List<MethodNode> res = findRelation(result);
        return pathToList(res,true);
    }

    @Override
    public List<String> getAllMethodRelation() {
        String cypherQuery = "MATCH p = (startNode:METHOD)-[:CALL|CONTAINS*]->( endNode:METHOD) WHERE NOT (endNode)-[:CONTAINS]->(:CALL) AND NOT (startNode)<-[:CALL]-() RETURN p limit 100";
        Collection<Map<String, Object>> result = neo4jClient.query(cypherQuery)
                .fetch()
                .all();
        List<MethodNode> res = findRelation(result);
        return pathToList(res,true);
    }



    //根据指定class和指定method查找调用链（全部查出），可能存在同一个类存在相同方法的情况，重载。
    @Override
    public List<String> getUrlPathDown(List<String> url) {
        String class_name=url.get(0)+".java";
        String method_name=url.get(1);
        String cypherQuery = "MATCH p = (startNode:METHOD)-[:CALL|CONTAINS*]->(nextNodes:METHOD) WHERE (NOT (nextNodes)-[:CONTAINS]->(:CALL)) and (startNode.NAME=$METHOD_NAME AND startNode.FILENAME ENDS WITH $CLASS_NAME) RETURN p";
        Collection<Map<String, Object>> result = neo4jClient.query(cypherQuery)
                .bind(method_name).to("METHOD_NAME")
                .bind(class_name).to("CLASS_NAME")
                .fetch()
                .all();
        List<MethodNode> res = findRelation(result);
        return pathToList(res,true);
    }

    @Override
    public List<String> getUrlPathUp(List<String> url) {
        String class_name=url.get(0)+".java";
        String method_name=url.get(1);
        String cypherQuery = "MATCH p = (endNode:METHOD)<-[:CALL|CONTAINS*]-(prevNodes:METHOD) where (not (prevNodes)<-[:CALL]-()) and (endNode.NAME=$METHOD_NAME AND endNode.FILENAME ENDS WITH $CLASS_NAME) RETURN p";
        Collection<Map<String, Object>> result = neo4jClient.query(cypherQuery)
                .bind(method_name).to("METHOD_NAME")
                .bind(class_name).to("CLASS_NAME")
                .fetch()
                .all();
        List<MethodNode> res = findRelation(result);
        return pathToList(res,false);
    }

    //根据指定class和指定method，先返回method的一些情况
    @Override
    public List<String> getUrlPathAbstract(List<String> url) {
        String class_name=url.get(0)+".java";
        String method_name=url.get(1);
        String cypherQuery = "MATCH (n:METHOD) WHERE (n.NAME=$METHOD_NAME AND n.FILENAME ENDS WITH $CLASS_NAME) RETURN n";
        Collection<Map<String, Object>> result = neo4jClient.query(cypherQuery)
                .bind(method_name).to("METHOD_NAME")
                .bind(class_name).to("CLASS_NAME")
                .fetch()
                .all();
        List<Map<String, Object>> resultList = new ArrayList<>(result);
        List<String> ans = new ArrayList<>();
        //会有很多个满足条件的METHOD
        for (Map<String, Object> record : resultList){
            Object nodeObject = record.get("n");
            if (nodeObject instanceof InternalNode) {
                InternalNode node = (InternalNode) nodeObject;
                ans.add(node.get("CODE").asString());
            }
        }
        return ans;
    }

    @Override
    public List<String> getUrlPathDetailUp(List<String> url) {
        String class_name=url.get(0)+".java";
        String method_name=url.get(1);
        String code=url.get(2);
        String cypherQuery = "p=MATCH (endNode:METHOD)<-[:CALL|CONTAINS*]-(prevNodes:METHOD) where (not (prevNodes)<-[:CALL]-()) and (endNode.NAME=$METHOD_NAME AND endNode.FILENAME ENDS WITH $CLASS_NAME AND endNode.CODE=$CODE) return p";
        Collection<Map<String, Object>> result = neo4jClient.query(cypherQuery)
                .bind(method_name).to("METHOD_NAME")
                .bind(class_name).to("CLASS_NAME")
                .bind(code).to("CODE")
                .fetch()
                .all();
        List<MethodNode> res = findRelation(result);
        return pathToList(res,false);
    }

    //url传入详细的类、方法、参数
    @Override
    public List<String> getUrlPathDetailDown(List<String> url) {
        String class_name=url.get(0)+".java";
        String method_name=url.get(1);
        String code=url.get(2);
        String cypherQuery = "p=MATCH (startNode:METHOD)-[:CALL|CONTAINS*]->(nextNodes:METHOD) WHERE startNode.NAME=$METHOD_NAME AND startNode.FILENAME ENDS WITH $CLASS_NAME AND startNode.CODE=$CODE and (NOT (nextNodes)-[:CONTAINS]->(:CALL)) return p";
        Collection<Map<String, Object>> result = neo4jClient.query(cypherQuery)
                .bind(method_name).to("METHOD_NAME")
                .bind(class_name).to("CLASS_NAME")
                .bind(code).to("CODE")
                .fetch()
                .all();
        List<MethodNode> res = findRelation(result);
        return pathToList(res,true);
    }

    //根据表名和表字段字段查找调用链
    @Override
    public List<String> getSqlMember(String table,String member) {
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

            boolean x=code.contains(member) ||code.contains("*");
            boolean y=code.contains("from "+table)||code.contains("update "+table)||code.contains("insert into "+table);
            if((code.contains("from "+table)||code.contains("update "+table)||code.contains("insert into "+table))&&(code.contains(member) ||code.contains("*"))){
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

    //根据url，找Mapping注解，解析到对应方法，展示向下调用链
    @Override
    public List<String> getUrlPathMethod(String first,String left) {
        System.out.println(first);
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
