package com.icbc.codeResolver.service;

import com.icbc.codeResolver.entity.MethodNode;
import org.apache.dubbo.config.annotation.DubboService;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.neo4j.driver.types.Relationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@DubboService(group = "tabby")
public class TabbyServiceImpl implements CodeResolverService {

    public static final String ALL_FORWARD_LINK_QUERY = "match (source:Method) where source.CLASSNAME STARTS WITH 'com.icbc' AND source.NAME <> '<init>'\n" +
            "match (target:Method) where target.CLASSNAME STARTS WITH 'com.icbc' AND target.NAME <> '<init>'\n" +
            "MATCH p = (source)-[:CALL*]->(target)\n" +
            "where not (target)-[:CALL]->() AND not ((source)<-[:CALL]-())\n" +
            "RETURN p";

    public static final String FORWARD_LINK_FROM_METHOD_QUERY = "MATCH p = (source:Method {NAME:$NAME})-[:CALL*]->(target)\n" +
            "where not (target)-[:CALL]->()\n" +
            "RETURN p";

    public static final String REVERSE_LINK_FROM_METHOD_QUERY = "MATCH p = (source:Method)-[:CALL*]->(target:Method {NAME:$NAME})\n" +
            "where not (source)<-[:CALL]-()\n" +
            "RETURN p";



    @Autowired
    private Neo4jClient neo4jClient;


    @Override
    public List<String> getMethodUp(String method) {
        List<MethodNode> res = findRelation(REVERSE_LINK_FROM_METHOD_QUERY, method);
        return pathToList(res,false);
    }

    @Override
    public List<String> getMethodDown(String method) {
        List<MethodNode> res = findRelation(FORWARD_LINK_FROM_METHOD_QUERY, method);
        return pathToList(res,true);
    }

    @Override
    public List<String> getAllMethodRelation() {
        Collection<Map<String, Object>> result = neo4jClient.query(ALL_FORWARD_LINK_QUERY)
                .fetch()
                .all();
        List<Map<String, Object>> resultList = new ArrayList<>(result);
        List<MethodNode> ans = new ArrayList<>();
        String label = null;
        // 输出路径内容
        for (Map<String, Object> record : resultList) {
            Path path = (Path) record.get("p");
            MethodNode head = new MethodNode("1", "1");
            MethodNode cur = head;
            int x = 0;
            for (Path.Segment segment : path) {
                Node startNode = null;
                if (x == 0) {
                    startNode = segment.start();
                    for (String item : startNode.labels()) {
                        label = item;
                    }
                    cur.next = new MethodNode(label, startNode.get("NAME").asString());
                    cur = cur.next;
                    x = 1;
                }
                Relationship relationship = segment.relationship();
                Node endNode = segment.end();
                cur.next = new MethodNode(label, endNode.get("NAME").asString());
                cur = cur.next;
                if (x == 1) {
                    System.out.print(startNode.labels() + " - " + startNode.get("NAME") + "->");
                    x = 2;
                }
                System.out.print(relationship.type() + "->");
                System.out.print(endNode.labels() + " - " + endNode.get("NAME") + "->");

            }
            ans.add(head.next);
        }
        return pathToList(ans,true);
    }


    public List<MethodNode> findRelation(String cypherQuery, String method) {
        Collection<Map<String, Object>> result = neo4jClient.query(cypherQuery)
                .bind(method).to("NAME")
                .fetch()
                .all();
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
                stringBuilder.append(r.name + spiltChar);
                r = r.next;
            }
            sbrList.add(stringBuilder.substring(0,stringBuilder.length()-2));
        }
        return sbrList;
    }
}

