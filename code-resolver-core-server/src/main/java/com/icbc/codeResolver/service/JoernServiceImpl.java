package com.icbc.codeResolver.service;

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
import com.icbc.codeResolver.utils.CacheClient;

import java.util.*;
import java.util.concurrent.TimeUnit;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


@DubboService(group = "joern")

@Component
public class JoernServiceImpl implements CodeResolverService {
    @Resource
    private CacheClient cacheClient;

    @Resource
    private JoernMapper joernMapper;

    /**
     * 前端传递包名返回类名
     * @param packetName
     * @return
     */
    @Override
    public List<neo4jNode> showClassName(String packetName) {
        Collection<Map<String, Object>> result = joernMapper.getClassName(packetName);
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
        //return joernMapper.getClassName(packetName);

    }

    /**
     * 前端传递类名返回方法名
     * @param classFullName
     * @return
     */
    @Override
    public List<neo4jNode> showMethodName(String classFullName) {
        Collection<Map<String, Object>> result = joernMapper.getMethodName(classFullName);
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
        //return cacheClient.queryMethodNameByClass(classFullName,100000L, TimeUnit.SECONDS);
        return ans;
    }

    /**
     * 前端传递类名和方法名返回调用链路
     * @param methodFullName
     * @return
     */
    @Override
    public List<neo4jPath> showInvocationLink(String methodFullName,Boolean isDonw) {
        if (isDonw){
            Collection<Map<String, Object>> result=joernMapper.getMethodDown(methodFullName);
            return linkToPath(findRelation(result));
        }else {
            Collection<Map<String, Object>> result=joernMapper.getMethodUp(methodFullName);
            return linkToPath(findRelation(result));
        }
    }

    /**
     * @param methodFullName
     * @return
     */
    @Override
    public List<neo4jPath> getMethodUp(String methodFullName) {
        //return cacheClient.queryLinkByClassAndMethod(methodFullName,Boolean.FALSE, 100000L, TimeUnit.SECONDS);
        Collection<Map<String, Object>> result=joernMapper.getMethodUp(methodFullName);
        return linkToPath(findRelation(result));
    }


    /**
     * @param methodFullName
     * @return
     */
    @Override
    public List<neo4jPath> getMethodDown(String methodFullName) {
        //return cacheClient.queryLinkByClassAndMethod(methodFullName,Boolean.TRUE, 100000L, TimeUnit.SECONDS);
        Collection<Map<String, Object>> result=joernMapper.getMethodDown(methodFullName);
        return linkToPath(findRelation(result));
    }

    /**
     * url精确查找
     * @param url
     * @return
     */
    @Override
    public List<neo4jPath> getUrlPath(String url) {
        Collection<Map<String, Object>> result=joernMapper.getUrlPath(url);
        return linkToPath(findRelation(result));
    }

    /**
     * 查询表字段以及相关关系
     * @param dataBaseName
     * @param tableName
     * @param fieldName
     * @return
     */
    @Override
    public List<neo4jPath> getDataBaseInfo(String dataBaseName, String tableName, String fieldName) {
        Collection<Map<String, Object>> result=joernMapper.getDataBaseInfo(dataBaseName,tableName,fieldName);
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
                String methodFullName=method_node.get("FULL_NAME").asString();
                result = joernMapper.getMethodUp(methodFullName);
                List<neo4jNode> res = findRelation(result);//这里的res是以$method_name结尾的方法调用
                ans.addAll(res);
            }
        }
        //return cacheClient.queryDataBaseInfo(dataBaseName,tableName,fieldName,100000L, TimeUnit.SECONDS);
        return linkToPath(ans);
    }

    /**
     * 获取热点节点
     * @param packetName
     * @param maxNumber
     * @return
     */
    @Override
    public List<neo4jHotNode> getHotNode(String packetName, String maxNumber) {
        Collection<Map<String, Object>> result = joernMapper.getHotNode(packetName,maxNumber);
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
    public List<neo4jSimilarNode> getSimilar(String packetName,String methodElementId,Double threshold) {

        //这里需要先去redis查packetName下的所有方法相似度是否存在，返回全部方法相似度ans
        Collection<Map<String, Object>> result = joernMapper.getSimilar(packetName);
        List<Map<String, Object>> resultList = new ArrayList<>(result);
        List<neo4jSimilarNode> ans = new ArrayList<>();
        InternalNode class_node=null;
        Double similarity=0d;
        for (int i=0;i<resultList.size();i++){
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

        //寻找指定方法的方法相似度排序返回（也可以写进redis)
        List<neo4jSimilarNode> res = new ArrayList<>();
        for(int i=0;i<ans.size();i++){
            neo4jSimilarNode node=ans.get(i);
            if((node.from.id.equals(methodElementId))&&(node.similarity>=threshold)){
                res.add(node);
            }
        }
        Collections.sort(res);
        return res;
    }

    @Override
    public List<neo4jPath> getShortestPath(String methodFullName) {
        Collection<Map<String, Object>> result=joernMapper.getShortestPath(methodFullName);
        return linkToPath(findRelation(result));
    }

    @Override
    public List<neo4jPath> getCollectionPath(List<String> list){
        Collection<Map<String, Object>> result=joernMapper.getCollectionPath(list);
        return linkToPath(findRelation(result));
    }

    @Override
    public List<neo4jNode> getMethodInformation(String methodName){
        Collection<Map<String, Object>> result = joernMapper.getMethodInformation(methodName);
        List<Map<String, Object>> resultList = new ArrayList<>(result);
        List<neo4jNode> ans = new ArrayList<>();
        InternalNode class_node=null;

        for (Map<String, Object> record : resultList) {
            Object nodeObject = record.get("n");
            if (nodeObject instanceof InternalNode) {
                class_node = (InternalNode) nodeObject;
            }
            neo4jNode node = new neo4jNode(class_node.labels().iterator().next(), class_node.get("NAME").asString(), class_node.get("FULL_NAME").asString(), class_node.get("CODE").asString(), class_node.get("FILENAME").asString(), class_node.elementId());
            ans.add(node);
        }
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
