package com.icbc.codeResolver.service;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.util.JdbcConstants;
import com.icbc.codeResolver.entity.*;
import com.icbc.codeResolver.mapper.JoernMapper;
import com.icbc.codeResolver.utils.CacheClient;

import java.util.*;
import java.util.concurrent.TimeUnit;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.log4j.Logger;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


@DubboService(group = "joern",timeout = 1000000)

@Component
public class JoernServiceImpl implements CodeResolverService {
    private static Logger logger = Logger.getLogger(CacheClient.class);
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
        return cacheClient.queryClassNameByPacket(packetName,100000L, TimeUnit.SECONDS);

    }

    /**
     * 前端传递类名返回方法名
     * @param classFullName
     * @return
     */
    @Override
    public List<neo4jNode> showMethodName(String classFullName) {
        return cacheClient.queryMethodNameByClass(classFullName,100000L, TimeUnit.SECONDS);

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
        return cacheClient.queryLinkByClassAndMethod(methodFullName,Boolean.FALSE, 100000L, TimeUnit.SECONDS);
//        Collection<Map<String, Object>> result=joernMapper.getMethodUp(methodFullName);
//        return linkToPath(findRelation(result));
    }


    /**
     * @param methodFullName
     * @return
     */
    @Override
    public List<neo4jPath> getMethodDown(String methodFullName) {
        return cacheClient.queryLinkByClassAndMethod(methodFullName,Boolean.TRUE, 100000L, TimeUnit.SECONDS);
//        Collection<Map<String, Object>> result=joernMapper.getMethodDown(methodFullName);
//        return linkToPath(findRelation(result));
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
     * @param tableName
     * @param fieldName
     * @return
     */
    @Override
    public List<neo4jPath> getDataBaseInfo(String tableName, String fieldName) {
        Collection<Map<String, Object>> result=joernMapper.getDataBaseInfo();
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

    /**
     * 查询相似度
     * @return
     */
    @Override
    public List<neo4jSimilarNode> getSimilar(String packetName,String methodElementId,Double threshold) {
        //直接到redis中进行查询
        logger.info("server 查询相似度一次");
        List<neo4jSimilarNode> result=cacheClient.getSimilar(packetName,methodElementId,threshold,1000L,TimeUnit.MINUTES);
        return result;
    }

    /**
     * 查询最短路径
     * @return
     */
    @Override
    public List<neo4jPath> getShortestPath(String methodFullName) {
        Collection<Map<String, Object>> result=joernMapper.getShortestPath(methodFullName);
        return linkToPath(findRelation(result));
    }

    /**
     * 查询集中路径
     * @return
     */
    @Override
    public List<neo4jPath> getCollectionPath(List<String> list){
        Collection<Map<String, Object>> result=joernMapper.getCollectionPath(list);
        return linkToPath(findRelation(result));
    }

    /**
     * 根据方法名查询方法信息
     * @return
     */
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

    @Override
    public Map<neo4jPre, neo4jAst> getDynamicInformation(String fileName,Integer lineNumber){
        Map<neo4jPre, neo4jAst> res = new HashMap<>();//最终的结果集
        List<neo4jPre> ans;//调用者集合
        //1、定位到方法
        Collection<Map<String, Object>> result=joernMapper.getMethodByLine(fileName,lineNumber);
        List<Map<String, Object>> resultList = new ArrayList<>(result);
        Object n = resultList.get(0).get("n");
        InternalNode nNode=null;
        if(n instanceof InternalNode){
            nNode=(InternalNode)n;
        }
        //2、将方法加入到队列，广度优先搜索遍历
        Queue<neo4jPre> queue = new LinkedList<>();
        neo4jNode node = new neo4jNode(nNode.labels().iterator().next(), nNode.get("NAME").asString(), nNode.get("FULL_NAME").asString(), nNode.get("CODE").asString(), nNode.get("FILENAME").asString(), nNode.elementId());
        List<String> firstPre=new ArrayList<>();
        firstPre.add("("+node.code+")");
        queue.add(new neo4jPre(node,firstPre));
        System.out.println(1);
        while(!queue.isEmpty()){
            int size= queue.size();
            for(int s=0;s<size;s++){
                neo4jPre nextNode=queue.poll();
                ans=getAstPath(nextNode.getNode().id,res,nextNode.getPrePathList());
                if(ans.size()==0){
                    System.out.println(nextNode.getPrePathList());
                }
                for(int i=0;i<ans.size();i++){
                    queue.add(ans.get(i));
                }
            }
        }
        return res;
    }
    public List<neo4jPre> getAstPath(String id,Map<neo4jPre, neo4jAst> res,List<String> pre){
        //1、根据方法id获取调用该方法的上一级ast。
        Collection<Map<String, Object>> result = joernMapper.getAstPath(id);
        List<Map<String, Object>> resultList = new ArrayList<>(result);
        List<neo4jPre> ans = new ArrayList<>();
        List<String> strControl=new ArrayList<>();
        neo4jPre key=null;

        for (Map<String, Object> record : resultList){
            //1.1获取memberall LIST
            List<String> memberAllList=new ArrayList<>();
            Object memberAll=record.get("memberAll");
            List<Object> objectList=null;
            InternalNode memberNode;
            if(memberAll instanceof List<?>){
                objectList=(List<Object>)memberAll;
            }
            for(int i=0;i<objectList.size();i++){
                Object objectNode=objectList.get(i);
                if(objectNode instanceof InternalNode){
                    memberNode = (InternalNode) objectNode;
                    memberAllList.add(memberNode.get("NAME").asString());
                }
            }
            //1.2获取nodeall list
            Object nodeAll=record.get("nodeAll");
            objectList=null;
            InternalNode methodNode;
            neo4jNode node=null;
            StringBuilder stringBuilder = new StringBuilder();
            StringBuilder controlBuilder=new StringBuilder();
            boolean elseFlag=false;
            boolean ifFlag=false;
            boolean flag=false;
            if(nodeAll instanceof List<?>){
                objectList=(List<Object>)nodeAll;
            }
            for(int i=0;i<objectList.size();i++){
                Object objectNode=objectList.get(i);
                if(objectNode instanceof InternalNode){
                    methodNode = (InternalNode) objectNode;
                    node = new neo4jNode(methodNode.labels().iterator().next(), methodNode.get("NAME").asString(), methodNode.get("FULL_NAME").asString(), methodNode.get("CODE").asString(), methodNode.get("FILENAME").asString(), methodNode.elementId());
                }
                if(i==0){
                    //把头节点加入
                    key=new neo4jPre(node,pre);
                }
                else if(i==objectList.size()-1){
                    //把尾节点加入
                    break;
                }
                else if(node.label.equals("BLOCK")){
                    //System.out.println("".equals(stringBuilder)+"  "+stringBuilder.length());
                    if(stringBuilder.length()!=0){
                        if(!flag){
                            controlBuilder.append(stringBuilder);
                            flag=true;
                        }
                        else{
                            controlBuilder.append("&&"+stringBuilder);
                        }

                    }
                    //如果是blcok，将标志位复位
                    stringBuilder = new StringBuilder();;
                    elseFlag=false;
                    ifFlag=false;
                }
                else if(node.label.equals("CONTROL_STRUCTURE")){
                    String code=node.code;
                    if(code.contains("else")){
                        if(elseFlag==false&&ifFlag==false){
                            elseFlag=true;
                        }
                    }
                    else if(code.contains("if")){
                        if(ifFlag==false){//只有前面没有if的情况才能存，存又分两种情况，前面是否存在else和if
                            if(elseFlag==false){
                                ifFlag=true;
                            }
                            String member=code.substring(code.indexOf("(")+1,code.indexOf(")"));
                            for(int j=0;j<memberAllList.size();j++){
                                if(member.contains(memberAllList.get(j))){
                                    if(!elseFlag){
                                        //if情况，正常存
                                        stringBuilder.append("("+member+")");
                                    }
                                    else{
                                        //else情况，取反，然后存，会有多个，需要用and连接
                                        if(stringBuilder.length()==0){
                                            stringBuilder.append("("+"!"+"("+member+")"+")");
                                        }else{
                                            stringBuilder.append("&&"+"("+"!"+"("+member+")"+")");
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    }

                }
            }
            String control=controlBuilder.toString();
            if(control.equals("")) control="any";
            strControl.add(control);//记录每个m的控制分支
            List<String> now=new ArrayList<>();
            //遍历pre拼接
            for(int i=0;i<pre.size();i++){
                now.add(pre.get(i)+"<<<-----"+"["+control+"]"+"----"+"("+node.code+")");
            }
            ans.add(new neo4jPre(node,now));
        }
        if(ans.size()!=0){
            res.put(key,new neo4jAst(ans,strControl));//ans是list<node>,str是list<string>，size一样对应每个方法。
        }
        return ans;
    }

    @Override
    public boolean createDatabase(String databaseName){
        return joernMapper.createDatabase(databaseName);
    }

    /**
     * 查询调用
     * @return
     */
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
