package com.icbc.codeResolver.service;

import com.icbc.codeResolver.entity.neo4jNode;
import com.icbc.codeResolver.entity.neo4jPath;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@DubboService(group = "joern")
@Component
public class JoernServiceImpl implements CodeResolverService {

    @Resource
    private Neo4jClient neo4jClient;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

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
        List<neo4jNode> res = findRelation(result);
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
        List<neo4jNode> res = findRelation(result);
        return pathToList(res,true);
    }

    /**
     * url精确查找
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
        List<neo4jNode> res = findRelation(result);
        return pathToListDownPara(res,url.get(0));
    }

    /**
     * 查询表字段以及相关关系
     * @param dataBaseName
     * @param tableName
     * @param fieldName
     * @return
     */
    @Override
    public List<String> getDataBaseInfo(String dataBaseName, String tableName, String fieldName) {
        String cypherQuery = "match (n:ANNOTATION)<-[:AST]-(m:METHOD) where (n.CODE starts with '@Insert(' or n.CODE starts with '@Delete(' or n.CODE starts with '@Select(' or n.CODE starts with '@Update(') return n,m";
        Collection<Map<String, Object>> result = neo4jClient.query(cypherQuery)
                .fetch()
                .all();
        List<Map<String, Object>> resultList = new ArrayList<>(result);
        List<neo4jNode> ans = new ArrayList<>();
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
            neo4jNode head = new neo4jNode("1", "1");

            boolean x=code.contains(fieldName) ||code.contains("*");
            boolean y=code.contains("from "+tableName)||code.contains("update "+tableName)||code.contains("insert into "+tableName);
            if((code.contains("from "+tableName)||code.contains("update "+tableName)||code.contains("insert into "+tableName))&&(code.contains(fieldName) ||code.contains("*"))){
                label=annotation_node.labels().iterator().next();
                head = new neo4jNode(label, annotation_node.get("NAME").asString());
                //然后向上搜索
                String method_name=method_node.get("NAME").asString();
                neo4jNode cur = head;
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
        //List<MethodNode> res = findRelation(result);
        return pathToList(ans,false);
    }

    /**
     * 前端传递包名返回类名
     * @param packetName
     * @return
     */
    @Override
    public List<neo4jNode> showClassName(String packetName) {
        return getClassName(packetName);
    }

    /**
     * 前端传递类名返回方法名
     * @param className
     * @return
     */
    @Override
    public List<neo4jNode> showMethodName(String className) {
        return getMethodName(className);
    }

    /**
     * 前端传递类名和方法名返回调用链路
     * @param className
     * @param methodName
     * @return
     */
    @Override
    public List<neo4jPath> showInvocationLink(String className, String methodName) {
        return linkToPath(getInvocationLink(className,methodName));
    }


    public List<neo4jNode> findRelation(Collection<Map<String, Object>> result) {
        List<Map<String, Object>> resultList = new ArrayList<>(result);
        List<neo4jNode> ans = new ArrayList<>();
        String label = null;
        for (Map<String, Object> record : resultList) {
            Path path = (Path) record.get("p");
            neo4jNode head = new neo4jNode("1", "1");
            neo4jNode cur = head;
            int x = 0;
            for (Path.Segment segment : path) {
                Node startNode = null;
                if (x == 0) {
                    startNode = segment.start();
                    label = startNode.labels().iterator().next();
                    cur.next = new neo4jNode(label, startNode.get("NAME").asString());
                    cur = cur.next;
                    x = 1;
                }
                Node endNode = segment.end();
                label = endNode.labels().iterator().next();
                cur.next = new neo4jNode(label, endNode.get("NAME").asString());
                cur = cur.next;
            }
            ans.add(head.next);
        }
        System.out.println(1);
        return ans;
    }


    public List<String> pathToListDownPara(List<neo4jNode> path, String s) {
        List<String> sbrList = new ArrayList<>();
        for (int i = 0; i < path.size(); i++) {
            StringBuilder stringBuilder = new StringBuilder("(CLASS)"+s+"->");
            neo4jNode r = path.get(i);
            while (r != null) {
                stringBuilder.append('('+r.label+')'+r.name+"->");
                r = r.next;
            }
            sbrList.add(stringBuilder.substring(0, stringBuilder.length() - 2));
        }
        return sbrList;
    }



    public List<String> pathToList(List<neo4jNode> path, boolean direction) {
        String spiltChar = direction?"->":"<-";
        List<String> sbrList = new ArrayList<>();
        for (int i = 0; i < path.size(); i++) {
            StringBuilder stringBuilder = new StringBuilder();
            neo4jNode r = path.get(i);
            while (r != null) {
                stringBuilder.append('('+r.label+')'+r.name+spiltChar);
                r = r.next;
            }
            sbrList.add(stringBuilder.substring(0,stringBuilder.length()-2));
        }
        return sbrList;
    }

    public List<neo4jNode> getClassName(String packetName){
        return null;
    }

    public List<neo4jNode> getMethodName(String className){
        return null;
    }

    public List<neo4jNode> getInvocationLink(String className, String methodName){
        return null;
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
