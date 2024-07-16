package com.icbc.codeResolver.service;

import com.icbc.codeResolver.entity.MethodNode;
import org.apache.dubbo.config.annotation.DubboService;
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
    public List<String> getMethodUp(String methodName) {
        String cypherQuery = "MATCH p = (endNode:METHOD{NAME:$NAME})<-[:CALL|CONTAINS*]-(prevNodes:METHOD)  WHERE (prevNodes)<-[:CALL|CONTAINS]-() RETURN p";
        List<MethodNode> res = findRelation(cypherQuery, methodName);
        return pathToListUp(res);
    }

    @Override
    public List<String> getMethodDown(String methodName) {
        String cypherQuery = "MATCH p = (startNode:METHOD{NAME:$NAME})-[:CALL|CONTAINS*]->(nextNodes:METHOD) WHERE (nextNodes)-[:CALL|CONTAINS]->() RETURN p";
        List<MethodNode> res = findRelation(cypherQuery, methodName);
        return pathToListDown(res);
    }

    /**
     * TODO 类溯源
     * @param className
     * @return
     */
    @Override
    public List<String> getClassUp(String className) {
        String cypherQuery="";
        List<MethodNode> res=findRelation(cypherQuery,className);
        return null;
    }

    /**
     * TODO 类追踪
     * @param className
     * @return
     */
    @Override
    public List<String> getClassDown(String className) {
        String cypherQuery="";
        List<MethodNode> res=findRelation(cypherQuery,className);
        return null;
    }

    /**
     * TODO url精确查找
     * @param url
     * @return
     */
    @Override
    public List<String> getUrlPath(List<String> url) {
        return null;
    }


    @Override
    public List<String> getAllMethodRelation() {
        List<MethodNode> res = findMethodRelation();
        return pathToListDown(res);
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

    public List<MethodNode> findMethodRelation() {
        String cypherQuery = "MATCH p=(m:METHOD)-[r:CONTAINS]->(k:CALL)-[r2:CALL]->(n:METHOD) RETURN p LIMIT 25";
        Collection<Map<String, Object>> result = neo4jClient.query(cypherQuery)
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
        return ans;
    }

    public List<String> pathToListDown(List<MethodNode> path) {
        List<String> sbrList = new ArrayList<>();
        for (int i = 0; i < path.size(); i++) {
            StringBuilder stringBuilder = new StringBuilder();
            MethodNode r = path.get(i);
            while (r != null) {
                stringBuilder.append(r.name+'('+r.label+')'+"->");
                r = r.next;
            }
            sbrList.add(stringBuilder.substring(0, stringBuilder.length() - 2));
        }
        return sbrList;
    }

    public List<String> pathToListUp(List<MethodNode> path) {
        List<String> sbrList = new ArrayList<>();
        for (int i = 0; i < path.size(); i++) {
            StringBuilder stringBuilder = new StringBuilder();
            MethodNode r = path.get(i);
            while (r != null) {
                stringBuilder.append(r.name+'('+r.label+')'+"<-");

                r = r.next;
            }
            sbrList.add(stringBuilder.substring(0, stringBuilder.length() - 2));
        }
        return sbrList;
    }

}
