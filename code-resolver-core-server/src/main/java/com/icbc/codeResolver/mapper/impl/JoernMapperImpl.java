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
import org.neo4j.driver.exceptions.ClientException;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.summary.ResultSummary;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${spring.data.neo4j.database}")
    private String newDataBase;

    /**
     * 创建数据库
     * @param databaseName
     * @return 是否创建成功
     */
    @Override
    public boolean createDatabase(String databaseName) {
        String query = "CREATE DATABASE " + databaseName;
        try {
            neo4jClient.query(query).run();
            System.out.println("创建数据库成功！");
            this.newDataBase=databaseName;
            return true; // 成功创建数据库
        }catch (Exception e) {
            // 处理其他未捕获的异常
            System.err.println("创建数据库失败: " + e.getMessage());
            return false; // 未成功创建数据库
        }
    }

    /**
     * 显示数据库
     * @return
     */
    @Override
    public Collection<Map<String, Object>> showDataBase() {
        String cypherQuery = "SHOW DATABASES";
        return neo4jClient.query(cypherQuery)
                .in(this.newDataBase)
                .fetch()
                .all();
    }

    /**
     * 修改数据库
     * @param databaseName
     * @return
     */
    @Override
    public boolean changeDataBase(String databaseName) {
        this.newDataBase = databaseName;
        return true;
    }

    /**
     * 返回当前数据库
     * @return
     */
    @Override
    public String showCurrentDataBase() {
        return this.newDataBase;
    }
    /**
     * 根据pack查找
     * @param packetName
     * @return
     */
    @Override
    public Collection<Map<String, Object>> getClassName(String packetName) {
        String cypherQuery = "MATCH (n:TYPE_DECL) WHERE n.FULL_NAME STARTS WITH $PACK RETURN n";
        return neo4jClient.query(cypherQuery)
                .in(this.newDataBase)
                .bind(packetName).to("PACK")
                .fetch()
                .all();
    }

    /**
     * 根据类全路径查找
     * @param classFullName
     * @return
     */
    @Override
    public Collection<Map<String, Object>> getMethodName(String classFullName) {

        String cypherQuery = "MATCH (n:METHOD)<-[:AST]-(m:TYPE_DECL) WHERE m.FULL_NAME = $FULL_NAME RETURN m,n";
        return neo4jClient.query(cypherQuery)
                .in(this.newDataBase)
                .bind(classFullName).to("FULL_NAME")
                .fetch()
                .all();
    }
    /**
     * 方法溯源【目标1】
     * @param methodFullName
     * @return
     */
    @Override
    public Collection<Map<String, Object>> getMethodUp(String methodFullName) {
        String cypherQuery = "MATCH p = (endNode:METHOD)<-[:CALL|CONTAINS*]-(prevNodes:METHOD) " +
                "where (not (prevNodes)<-[:CALL]-()) and (endNode.FULL_NAME starts with $FULL_NAME) " +
                "RETURN p ORDER BY LENGTH(p)";
        return neo4jClient.query(cypherQuery)
                .in(this.newDataBase)
                .bind(methodFullName).to("FULL_NAME")
                .fetch()
                .all();
    }
    /**
     * 方法追踪【目标1】
     * @param methodFullName
     * @return
     */
    @Override
    public Collection<Map<String, Object>> getMethodDown(String methodFullName) {
        String cypherQuery = "MATCH p = (startNode:METHOD)-[:CALL|CONTAINS*]->(nextNodes:METHOD) " +
                "WHERE (NOT (nextNodes)-[:CONTAINS]->(:CALL)) and (startNode.FULL_NAME starts with $FULL_NAME) " +
                "RETURN p";
        return neo4jClient.query(cypherQuery)
                .in(this.newDataBase)
                .bind(methodFullName).to("FULL_NAME")
                .fetch()
                .all();
    }

    /**
     * 根据url路径定位到入口方法并返回调用链路
     * @param url
     * @return 查询结果
     */
    @Override
    public Collection<Map<String, Object>> getUrlPath(String url) {
        String name="RequestMapping";//要查找的注解名称
        String like="Mapping";//要查找的注解名称包含该字符串
        String n_code= "@RequestMapping\\(\"([^\"]+)\"\\)";//正则表达式, 用于匹配 @RequestMapping("XXX")
        String n1_code="@[a-zA-Z]+Mapping\\(\"([^\"]+)\"\\)";//正则表达式, 用于匹配 @XXXMapping("XXX")
        String cypherQuery = "WITH $URL AS str"+//使用 WITH 子句定义了一个名为 $URL 的参数
                " MATCH (n:ANNOTATION)<-[:AST]-(c:TYPE_DECL)-[:AST]->(m:METHOD)-[:AST]->(n1:ANNOTATION)"+//使用 MATCH 子句查找满足以下条件的节点,存在一个 ANNOTATION 节点 n, 它有一条 AST 关系指向一个 TYPE_DECL 节点 c,c 节点有一条 AST 关系指向一个 METHOD 节点 m,m 节点有一条 AST 关系指向一个 ANNOTATION 节点 n1
                " WHERE n.NAME = $NAME"+//n 的 NAME 属性等于 $NAME (即 "RequestMapping")
                " AND n1.NAME contains $LIKE"+//n1 的 NAME 属性包含 $LIKE (即 "Mapping")
                " AND n.CODE =~$N_CODE"+//n 的 CODE 属性匹配 $N_CODE 正则表达式
                " AND n1.CODE =~$N1_CODE"+//n1 的 CODE 属性匹配 $N1_CODE 正则表达式
                " WITH n, n1, m,"+//接下来使用 WITH 子句提取 n, n1, m 节点, 以及从它们的 CODE 属性中提取的 code1 和 code2 字符串
                " substring(n.CODE, apoc.text.indexOf(n.CODE, '\"') + 1, apoc.text.indexOf(n.CODE, '\")') - apoc.text.indexOf(n.CODE, '\"') - 1) AS code1,"+
                " substring(n1.CODE, apoc.text.indexOf(n1.CODE, '\"') + 1, apoc.text.indexOf(n1.CODE, '\")') - apoc.text.indexOf(n1.CODE, '\"') - 1) AS code2"+
                " WHERE code1 + code2 = str"+
                " MATCH p=(m)-[:CALL|CONTAINS*]->(nextNodes:METHOD)"+//最后使用 MATCH 子句查找从 m 节点开始, 通过 CALL 或 CONTAINS 关系到达的nextNodes:METHOD 节点,
                " WHERE NOT (nextNodes)-[:CONTAINS]->(:CALL)"+//并且这些 nextNodes 节点没有 CONTAINS 关系指向其他 CALL 节点
                " RETURN p";

        return neo4jClient.query(cypherQuery)
                .in(this.newDataBase)
                .bind(url).to("URL")
                .bind(n_code).to("N_CODE")
                .bind(n1_code).to("N1_CODE")
                .bind(name).to("NAME")
                .bind(like).to("LIKE")
                .fetch()
                .all();
    }

    /**
     * 从mapper注解往下查询找到@Insert(类型注解
     * @return 查询结果
     */
    @Override
    public Collection<Map<String, Object>> getDataBaseInfo() {
        String cypherQuery = "match (n:ANNOTATION)<-[:AST]-(m:METHOD)<-[:AST]-(c:TYPE_DECL) " +
                "where EXISTS { MATCH (c)-[:AST]->(nn:ANNOTATION) WHERE nn.NAME=\"Mapper\"} and (n.CODE starts with '@Insert(' or n.CODE starts with '@Delete(' or n.CODE starts with '@Select(' or n.CODE starts with '@Update(') " +
                "RETURN n,m";
        return neo4jClient.query(cypherQuery)
                .in(this.newDataBase)
                .fetch()
                .all();
    }


    /**
     * 根据包名筛选业务内容，返回前maxNumber的热点节点
     * @param packetName
     * @param maxNumber
     * @return
     */
    @Override
    public Collection<Map<String, Object>> getHotNode(String packetName, String maxNumber) {
        String init=".<init>:";
        Long num=Long.valueOf(maxNumber);
        String cypherQuery="MATCH p=(n:METHOD)<-[:CALL]-(:CALL)<-[:CONTAINS]-(m:METHOD) " +
                "WHERE ALL(r IN NODES(p) where (r.FULL_NAME starts with $PACK OR r.METHOD_FULL_NAME starts with $PACK) and (NOT r.FULL_NAME CONTAINS $INIT OR NOT r.METHOD_FULL_NAME CONTAINS $INIT)) " +
                "RETURN n,collect(m) as follower,count(*) as number order by number desc limit $MAXNUMBER";
        return neo4jClient.query(cypherQuery)
                .in(this.newDataBase)
                .bind(packetName).to("PACK")
                .bind(num).to("MAXNUMBER")
                .bind(init).to("INIT")
                .fetch()
                .all();
    }
    /**
     * 根据包名返回包内两两方法的相似度
     * @param packetName
     * @return
     */
    @Override
    public Collection<Map<String, Object>> getSimilar(String packetName){
        String cypherQuery="MATCH p1 = (startNode1:METHOD)-[:CALL|CONTAINS*]->(nextNodes1:METHOD) WHERE (NOT (nextNodes1)-[:CONTAINS]->(:CALL)) and startNode1.FULL_NAME starts WITH $PACK" +
                " UNWIND nodes(p1) AS node1" +
                " with startNode1, COLLECT(ID(node1)) as nodeId1" +
                " MATCH p2 = (startNode2:METHOD)-[:CALL|CONTAINS*]->(nextNodes2:METHOD) WHERE startNode1<>startNode2 and (NOT (nextNodes2)-[:CONTAINS]->(:CALL)) and (startNode2.FULL_NAME starts WITH $PACK)" +
                " UNWIND nodes(p2) AS node2" +
                " with startNode1,startNode2,nodeId1,COLLECT(ID(node2)) as nodeId2" +
                " RETURN startNode1 AS from," +
                " startNode2 AS to," +
                " gds.similarity.jaccard(nodeId1, nodeId2) AS similarity order by similarity desc";
        return neo4jClient.query(cypherQuery)
                .in(this.newDataBase)
                .bind(packetName).to("PACK")
                .fetch()
                .all();
    }

    /**
     * 根据方法全名获取调用该方法的最短路径
     * @param methodFullName
     * @return
     */
    @Override
    public Collection<Map<String, Object>> getShortestPath(String methodFullName){
        String cypherQuery="MATCH p = (endNode:METHOD)<-[:CALL|CONTAINS*]-(prevNodes:METHOD) " +
                "where (not (prevNodes)<-[:CALL]-()) and (endNode.FULL_NAME starts with $FULL_NAME)  " +
                "RETURN p order by length(p)";
        return neo4jClient.query(cypherQuery)
                .in(this.newDataBase)
                .bind(methodFullName).to("FULL_NAME")
                .fetch()
                .all();
    }

    /**
     * 根据方法名称获取所有方法
     * @param methodName
     * @return
     */
    @Override
    public Collection<Map<String, Object>> getMethodInformation(String methodName){
        String cypherQuery="MATCH (n:METHOD) WHERE n.NAME=$NAME return n";
        return neo4jClient.query(cypherQuery)
                .in(this.newDataBase)
                .bind(methodName).to("NAME")
                .fetch()
                .all();
    }

    /**
     * 找到包含list内所有方法的链路
     * @param list
     * @return
     */
    @Override
    public Collection<Map<String, Object>> getCollectionPath(List<String> list){
        String cypherQuery="WITH $LIST AS cons" +
                " MATCH p = (endNode:METHOD)<-[:CALL|CONTAINS*]-(prevNodes:METHOD)" +
                " WHERE NOT (prevNodes)<-[:CALL]-() AND NOT (endNode)-[:CONTAINS]->(:CALL) " +
                " WITH p, cons,[consNode IN cons WHERE ANY(n IN nodes(p) WHERE elementId(n) = consNode)] AS matchedNodes" +
                " WHERE size(matchedNodes) = size(cons)" +
                " RETURN p" +
                " ORDER BY length(p) limit 1";
        return neo4jClient.query(cypherQuery)
                .in(this.newDataBase)
                .bind(list).to("LIST")
                .fetch()
                .all();
    }

    /**
     * 找到方法elementid为id的向上一级的ast调用链路
     * @param id
     * @return
     */
    @Override
    public Collection<Map<String, Object>> getAstPath(String id){
        String cypherQuery="MATCH p=(n:METHOD)<-[:CALL]-(c:CALL) <-[:AST*]-(m: METHOD) where elementId(n)=$ID and not m.NAME contains \"<\" " +
                "with nodes(p) as nodeAll,m " +
                "MATCH (m)-[:AST]->(in:METHOD_PARAMETER_IN) " +
                "return nodeAll,collect(in) as memberAll";
        return neo4jClient.query(cypherQuery)
                .in(this.newDataBase)
                .bind(id).to("ID")
                .fetch()
                .all();
    }
    /**
     * 根据变动文件名和变动行定位变动方法
     * @param fileName
     * @return
     */
    @Override
    public Collection<Map<String, Object>> getMethodByLine(String fileName,Integer lineNumber){
        String cypherQuery="MATCH (n:METHOD) where n.FILENAME ends with $FILENAME AND n. LINE_NUMBER<=$LINE AND n. LINE_NUMBER_END >=$LINE return n";
        return neo4jClient.query(cypherQuery)
                .in(this.newDataBase)
                .bind(fileName).to("FILENAME")
                .bind(lineNumber).to("LINE")
                .fetch()
                .all();
    }
}


