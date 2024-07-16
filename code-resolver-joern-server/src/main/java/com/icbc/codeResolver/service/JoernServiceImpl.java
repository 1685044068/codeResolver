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
        String cypherQuery = "MATCH p = (endNode:METHOD{NAME:$NAME})<-[:CALL|CONTAINS*]-(prevNodes:METHOD) where not (prevNodes)<-[:CALL]-() RETURN p";
        Collection<Map<String, Object>> result = neo4jClient.query(cypherQuery)
                .bind(methodName).to("NAME")
                .fetch()
                .all();
        List<MethodNode> res = findRelation(result);
        return pathToListUp(res);
    }

    @Override
    public List<String> getMethodDown(String methodName) {
        String cypherQuery = "MATCH p = (startNode:METHOD {NAME:$NAME })-[:CALL|CONTAINS*]->(nextNodes:METHOD) WHERE NOT (nextNodes)-[:CONTAINS]->(:CALL) RETURN p";
        Collection<Map<String, Object>> result = neo4jClient.query(cypherQuery)
                .bind(methodName).to("NAME")
                .fetch()
                .all();
        List<MethodNode> res = findRelation(result);
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
//        List<MethodNode> res=findRelation(cypherQuery,className);
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
//        List<MethodNode> res=findRelation(cypherQuery,className);
        return null;
    }

    /**
     * TODO url精确查找
     * @param url
     * @return
     */
    @Override
    public List<String> getUrlPath(List<String> url) {
        String class_name=url.get(0)+".java";
        String method_name=url.get(1);
        String cypherQuery = "MATCH p = (startNode:METHOD)-[:CALL|CONTAINS*]->(nextNodes:METHOD) WHERE (NOT (nextNodes)-[:CONTAINS]->(:CALL)) and (startNode.NAME=$METHOD_NAME AND startNode.FILENAME ENDS WITH $CLASS_NAME) RETURN p";
        Collection<Map<String, Object>> result = neo4jClient.query(cypherQuery)
                .bind(method_name).to("METHOD_NAME")
                .bind(class_name).to("CLASS_NAME")
                .fetch()
                .all();
        List<MethodNode> res = findRelation(result);
        return pathToListDownPara(res,url.get(0));
    }


    @Override
    public List<String> getAllMethodRelation() {
        String cypherQuery = "MATCH p = (startNode:METHOD)-[:CALL|CONTAINS*]->( endNode:METHOD) WHERE NOT (endNode)-[:CONTAINS]->(:CALL) AND NOT (startNode)<-[:CALL]-() RETURN p limit 100";
        Collection<Map<String, Object>> result = neo4jClient.query(cypherQuery)
                .fetch()
                .all();
        List<MethodNode> res = findRelation(result);
        return pathToListDown(res);
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

}
