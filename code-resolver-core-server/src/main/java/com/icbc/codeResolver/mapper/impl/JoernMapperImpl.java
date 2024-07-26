package com.icbc.codeResolver.mapper.impl;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.util.JdbcConstants;
import com.icbc.codeResolver.entity.neo4jHotNode;
import com.icbc.codeResolver.entity.neo4jNode;
import com.icbc.codeResolver.entity.neo4jPath;
import com.icbc.codeResolver.entity.neo4jSimilarNode;
import com.icbc.codeResolver.mapper.JoernMapper;
import jakarta.annotation.Resource;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.Mapping;

import java.util.*;

/**
 * @BelongsProject: code-resolver
 * @BelongsPackage: com.icbc.codeResolver.mapper.impl
 * @Author: zero
 * @CreateTime: 2024-07-18  11:28
 * @Description: TODO
 * @Version: 1.0
 */
@Component
public class JoernMapperImpl implements JoernMapper {
    @Resource
    private Neo4jClient neo4jClient;

    @Override
    public List<neo4jPath> getMethodUp(String className, String methodName) {
        String cypherQuery = "MATCH p = (endNode:METHOD)<-[:CALL|CONTAINS*]-(prevNodes:METHOD) where (not (prevNodes)<-[:CALL]-()) and (endNode.NAME=$METHOD_NAME AND endNode.FILENAME ENDS WITH $CLASS_NAME) RETURN p";
        Collection<Map<String, Object>> result = neo4jClient.query(cypherQuery)
                .bind(methodName).to("METHOD_NAME")
                .bind(className).to("CLASS_NAME")
                .fetch()
                .all();
        List<neo4jNode> res = findRelation(result);
        return linkToPath(res);
    }

    @Override
    public List<neo4jPath> getMethodDown(String className, String methodName) {
        String cypherQuery = "MATCH p = (startNode:METHOD)-[:CALL|CONTAINS*]->(nextNodes:METHOD) WHERE (NOT (nextNodes)-[:CONTAINS]->(:CALL)) and (startNode.NAME=$METHOD_NAME AND startNode.FILENAME ENDS WITH $CLASS_NAME) RETURN p";
        Collection<Map<String, Object>> result = neo4jClient.query(cypherQuery)
                .bind(methodName).to("METHOD_NAME")
                .bind(className).to("CLASS_NAME")
                .fetch()
                .all();
        List<neo4jNode> res = findRelation(result);
        return linkToPath(res);
    }

    @Override
    public List<neo4jPath> getUrlPath(String url) {
        String name="RequestMapping";
        String like="Mapping";
        String n_code= "@RequestMapping\\(\"([^\"]+)\"\\)";
        String n1_code="@[a-zA-Z]+Mapping\\(\"([^\"]+)\"\\)";
        String cypherQuery = "WITH $URL AS str"+
                " MATCH (n:ANNOTATION)<-[:AST]-(c:TYPE_DECL)-[:AST]->(m:METHOD)-[:AST]->(n1:ANNOTATION)"+
                " WHERE n.NAME = $NAME"+
                " AND n1.NAME contains $LIKE"+
                " AND n.CODE =~$N_CODE"+
                " AND n1.CODE =~$N1_CODE"+
//                " RETURN n,c,m,n1";
                " WITH n, n1, m,"+
                " substring(n.CODE, apoc.text.indexOf(n.CODE, '\"') + 1, apoc.text.indexOf(n.CODE, '\")') - apoc.text.indexOf(n.CODE, '\"') - 1) AS code1,"+
                " substring(n1.CODE, apoc.text.indexOf(n1.CODE, '\"') + 1, apoc.text.indexOf(n1.CODE, '\")') - apoc.text.indexOf(n1.CODE, '\"') - 1) AS code2"+
                " WHERE code1 + code2 = str"+
                " MATCH p=(m)-[:CALL|CONTAINS*]->(nextNodes:METHOD)"+
                " WHERE NOT (nextNodes)-[:CONTAINS]->(:CALL)"+
                " RETURN p";

        System.out.println(cypherQuery);
        Collection<Map<String, Object>> result = neo4jClient.query(cypherQuery)
                .bind(url).to("URL")
                .bind(n_code).to("N_CODE")
                .bind(n1_code).to("N1_CODE")
                .bind(name).to("NAME")
                .bind(like).to("LIKE")
                .fetch()
                .all();
        List<neo4jNode> res = findRelation(result);
        return linkToPath(res);

    }

    @Override
    public List<neo4jPath> getDataBaseInfo(String dataBaseName, String tableName, String fieldName) {
        String cypherQuery = "match (n:ANNOTATION)<-[:AST]-(m:METHOD)<-[:AST]-(c:TYPE_DECL) where EXISTS { MATCH (c)-[:AST]->(nn:ANNOTATION) WHERE nn.NAME=\"Mapper\"} and (n.CODE starts with '@Insert(' or n.CODE starts with '@Delete(' or n.CODE starts with '@Select(' or n.CODE starts with '@Update(') RETURN n,m";
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
            System.out.println(code);
            //对注解上的code进行分解
            String sql=code.substring(code.indexOf("\"")+1,code.lastIndexOf("\""));
            System.out.println(sql);
            sql = sql.replaceAll("\" \\+ \"", "").replaceAll("\\s+", " ").trim();
            System.out.println("Transformed String: " + sql);
            SQLStatement sqlStatement = SQLUtils.parseStatements(sql, JdbcConstants.MYSQL).get(0);
            MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
            sqlStatement.accept(visitor);
            //获取表名称
            Map<TableStat.Name,TableStat> tables=visitor.getTables();
            boolean flag_table=false,flag_field=false;
            for (Map.Entry <TableStat.Name,TableStat>  entry : tables.entrySet()) {
                System.out.println("表名 = " + entry.getKey());
                if(entry.getKey().toString().equals(tableName)){
                    flag_table=true;
                    break;
                }
            }
            Collection<TableStat.Column> columns_first=visitor.getColumns();
            Set<TableStat.Column> columns=new HashSet<>(columns_first);
            Iterator iterator = columns.iterator();
            while(iterator.hasNext()){
                String str=iterator.next().toString();
                System.out.println("列名 = " + str);
                if(str.equals(tableName+"."+fieldName)||str.equals(tableName+".*")){
                    flag_field=true;
                    break;
                }
            }
            if(flag_table&&flag_field){
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
        return linkToPath(ans);
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
            neo4jNode node=new neo4jNode(class_node.labels().iterator().next(),class_node.get("NAME").asString(),class_node.get("FULL_NAME").asString(),class_node.get("CODE").asString(),class_node.get("FILENAME").asString(),class_node.elementId());
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
            neo4jNode node=new neo4jNode(class_node.labels().iterator().next(),class_node.get("NAME").asString(),class_node.get("FULL_NAME").asString(),class_node.get("CODE").asString(),class_node.get("FILENAME").asString(),class_node.elementId());
            ans.add(node);
            String class_method_key="CLASS:"+className;


        }
        return ans;
    }
    /**
     * 根据包名筛选业务内容，返回前maxNumber的热点节点
     * @return
     */
    @Override
    public List<neo4jHotNode> getHotNode(String packetName, String maxNumber) {
        String init=".<init>:";
        Long num=Long.valueOf(maxNumber);
        String cypherQuery="MATCH p=(n:METHOD)<-[:CALL]-(:CALL)<-[:CONTAINS]-(m:METHOD) WHERE ALL(r IN NODES(p) where (r.FULL_NAME starts with $PACK OR r.METHOD_FULL_NAME starts with $PACK) and (NOT r.FULL_NAME CONTAINS $INIT OR NOT r.METHOD_FULL_NAME CONTAINS $INIT)) RETURN n,collect(m) as follower,count(*) as number order by number desc limit $MAXNUMBER";
        Collection<Map<String, Object>> result = neo4jClient.query(cypherQuery)
                .bind(packetName).to("PACK")
                .bind(num).to("MAXNUMBER")
                .bind(init).to("INIT")
                .fetch()
                .all();
        List<Map<String, Object>> resultList = new ArrayList<>(result);
        List<neo4jHotNode> ans = new ArrayList<>();
        InternalNode class_node=null;

        for (Map<String, Object> record : resultList) {
            Object nodeObject = record.get("n");
            if (nodeObject instanceof InternalNode) {
                class_node = (InternalNode) nodeObject;
            }
            neo4jNode node=new neo4jNode(class_node.labels().iterator().next(), class_node.get("NAME").asString(),class_node.get("FULL_NAME").asString(),class_node.get("CODE").asString(),class_node.get("FILENAME").asString(),class_node.elementId());
            Long number =(Long) record.get("number");
            //获取列表follower
            List<?> list_followers=new ArrayList<>();
            List<neo4jNode> followers_node=new ArrayList<>();
            Object followers=record.get("follower");
            if(followers instanceof List<?>){
                list_followers=(List<?>)followers;
            }
            for(int i=0;i<list_followers.size();i++){
                nodeObject=list_followers.get(i);
                if(nodeObject instanceof InternalNode){
                    class_node = (InternalNode) nodeObject;
                    neo4jNode node1=new neo4jNode(class_node.labels().iterator().next(), class_node.get("NAME").asString(),class_node.get("FULL_NAME").asString(),class_node.get("CODE").asString(),class_node.get("FILENAME").asString(),class_node.elementId());
                    followers_node.add(node1);
                }
            }
            ans.add(new neo4jHotNode(node,number,followers_node));
        }
        return ans;
    }
    @Override
    public List<neo4jSimilarNode> getSimilar(String packetName){
        String cypherQuery="MATCH p1 = (startNode1:METHOD)-[:CALL|CONTAINS*]->(nextNodes1:METHOD) WHERE (NOT (nextNodes1)-[:CONTAINS]->(:CALL)) and startNode1.FULL_NAME starts WITH $PACK" +
                " UNWIND nodes(p1) AS node1" +
                " with startNode1, COLLECT(ID(node1)) as nodeId1" +
                " MATCH p2 = (startNode2:METHOD)-[:CALL|CONTAINS*]->(nextNodes2:METHOD) WHERE startNode1<>startNode2 and (NOT (nextNodes2)-[:CONTAINS]->(:CALL)) and (startNode2.FULL_NAME starts WITH $PACK)" +
                " UNWIND nodes(p2) AS node2" +
                " with startNode1,startNode2,nodeId1,COLLECT(ID(node2)) as nodeId2" +
                " RETURN startNode1 AS from," +
                " startNode2 AS to," +
                " gds.similarity.jaccard(nodeId1, nodeId2) AS similarity order by similarity desc";
        Collection<Map<String, Object>> result = neo4jClient.query(cypherQuery)
                .bind(packetName).to("PACK")
                .fetch()
                .all();
        List<Map<String, Object>> resultList = new ArrayList<>(result);
        List<neo4jSimilarNode> ans = new ArrayList<>();
        InternalNode class_node=null;
        Double similarity=0d;
        for (int i=0;i<resultList.size();i+=2){
            Map<String, Object> record=resultList.get(i);
            Object nodeObject = record.get("from");
            if (nodeObject instanceof InternalNode) {
                class_node = (InternalNode) nodeObject;
            }
            neo4jNode nodeFrom=new neo4jNode(class_node.labels().iterator().next(), class_node.get("NAME").asString(),class_node.get("FULL_NAME").asString(),class_node.get("CODE").asString(),class_node.get("FILENAME").asString(),class_node.elementId());
            nodeObject = record.get("to");
            if (nodeObject instanceof InternalNode) {
                class_node = (InternalNode) nodeObject;
            }
            neo4jNode nodeTo=new neo4jNode(class_node.labels().iterator().next(), class_node.get("NAME").asString(),class_node.get("FULL_NAME").asString(),class_node.get("CODE").asString(),class_node.get("FILENAME").asString(),class_node.elementId());
            Object object=record.get("similarity");
            if(object instanceof Double){
                similarity=(Double)object;
            }
            neo4jSimilarNode resNode=new neo4jSimilarNode(nodeFrom,nodeTo,similarity);
            ans.add(resNode);
        }
        System.out.println("方法对数量："+ans.size());
        return ans;
    }

    public List<neo4jNode> findRelation(Collection<Map<String, Object>> result) {
        List<Map<String, Object>> resultList = new ArrayList<>(result);
        List<neo4jNode> ans = new ArrayList<>();
        String label;
        for (Map<String, Object> record : resultList) {
            Path path = (Path) record.get("p");
            neo4jNode head = new neo4jNode("1", "1","1","1","1","1");
            neo4jNode cur = head;
            int x = 0;
            for (Path.Segment segment : path) {
                Node startNode;
                if (x == 0) {
                    startNode = segment.start();
                    label = startNode.labels().iterator().next();
                    if(label.equals("ANNOTATION")){
                        cur.next = new neo4jNode(label, startNode.get("NAME").asString(),startNode.get("FULL_NAME").asString(),startNode.get("CODE").asString(),startNode.elementId());
                    }
                    else{
                        cur.next = new neo4jNode(label, startNode.get("NAME").asString(),startNode.get("FULL_NAME").asString(),startNode.get("CODE").asString(),startNode.get("FILENAME").asString(), startNode.elementId());
                    }
                    cur = cur.next;
                    x = 1;
                }
                Node endNode = segment.end();
                label = endNode.labels().iterator().next();
                if(label.equals("METHOD")||label.equals("ANNOTATION")){
                    if(label.equals("ANNOTATION")){
                        cur.next = new neo4jNode(label, endNode.get("NAME").asString(),endNode.get("FULL_NAME").asString(),endNode.get("CODE").asString(),endNode.elementId());
                    }
                    else{
                        cur.next = new neo4jNode(label, endNode.get("NAME").asString(),endNode.get("FULL_NAME").asString(),endNode.get("CODE").asString(),endNode.get("FILENAME").asString(),endNode.elementId());
                    }
                    cur = cur.next;
                }
            }
            ans.add(head.next);
        }
        return ans;
    }

    public List<neo4jPath> linkToPath(List<neo4jNode> links){
        List<neo4jPath> paths=new ArrayList<>();
        for (neo4jNode node:links){
            neo4jPath path=new neo4jPath();
            int len=0;
            while(node!=null){
                len++;
                path.pathMember.add(node);
                node=node.next;
            }
            path.setPathLen(len);
            paths.add(path);
        }
        return paths;
    }
}



