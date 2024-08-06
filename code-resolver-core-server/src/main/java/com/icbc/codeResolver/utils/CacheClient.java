package com.icbc.codeResolver.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.icbc.codeResolver.controller.JoernController;
import com.icbc.codeResolver.entity.RedisData;
import com.icbc.codeResolver.entity.neo4jNode;
import com.icbc.codeResolver.entity.neo4jPath;
import com.icbc.codeResolver.entity.neo4jSimilarNode;
import com.icbc.codeResolver.mapper.JoernMapper;
import org.apache.log4j.Logger;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


import static com.icbc.codeResolver.utils.RedisConstants.LOCK_SHOP_KEY;
import static com.icbc.codeResolver.utils.RedisConstants.LOCK_SIMILARITY_KEY;

/**
 * @BelongsProject: code-resolver
 * @BelongsPackage: com.icbc.codeResolver.utils
 * @Author: zero
 * @CreateTime: 2024-07-18  11:05
 * @Description: 缓存处理工具类
 * @Version: 1.0
 */
@Component
public class CacheClient {

    //日志
    private static Logger logger = Logger.getLogger(CacheClient.class);
    private StringRedisTemplate stringRedisTemplate;

    private JoernMapper joernMapper;

    public CacheClient(StringRedisTemplate stringRedisTemplate,JoernMapper joernMapper){
        this.stringRedisTemplate=stringRedisTemplate;
        this.joernMapper=joernMapper;
    }


    /**
     * 逻辑过期设置数据
     * @param key
     * @param value
     * @param time
     * @param unit
     */
    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit){
        //设置逻辑过期
        RedisData redisData=new RedisData();
        redisData.setData(value);//List<neo4jNode>
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        //写入redis
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(redisData));
    }

    /**
     * 获取互斥锁
     * @param key
     * @return
     */
    private boolean tryLock(String key){
        Boolean flag=stringRedisTemplate.opsForValue().setIfAbsent(key,"1",10,TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    /**
     * 释放锁
     * @param key
     */
    private void unlock(String key){
        stringRedisTemplate.delete(key);
    }

    /**
     * 创建线程池
     */
    private static final ExecutorService CACHE_REBUILD_EXECUTOR= Executors.newFixedThreadPool(10);


    /**
     * 查询class(逻辑过期版)
     * @param packetName
     * @param time
     * @param unit
     * @return
     */
    public List<neo4jNode> queryClassNameByPacket(String packetName,Long time, TimeUnit unit){
        //1.查询缓存
        String json=stringRedisTemplate.opsForValue().get(packetName);
        //2.判断是否存在
        if (StrUtil.isBlank(json)){
            //3.不存在，这里应该去查数据库然后存入缓存
            logger.info("需要到数据库中进行查询");

            Collection<Map<String, Object>> result = joernMapper.getClassName(packetName);
            List<Map<String, Object>> resultList = new ArrayList<>(result);
            List<neo4jNode> classNeo4j = new ArrayList<>();
            InternalNode class_node=null;
            for (Map<String, Object> record : resultList) {
                Object nodeObject = record.get("n");
                if (nodeObject instanceof InternalNode) {
                    class_node = (InternalNode) nodeObject;
                }
                neo4jNode node=new neo4jNode(class_node.labels().iterator().next(),class_node.get("NAME").asString(),class_node.get("FULL_NAME").asString(),class_node.get("CODE").asString(),class_node.get("FILENAME").asString(),class_node.elementId());
                classNeo4j.add(node);
            }

            //4.存入到缓存
            this.setWithLogicalExpire(packetName,classNeo4j,time,unit);
            //5.返回
            return classNeo4j;
        }else{//6.如果存在
            RedisData redisData=JSONUtil.toBean(json,RedisData.class);
            List<neo4jNode> classNeo4j=JSONUtil.toList((JSONArray) redisData.getData(),neo4jNode.class);
            LocalDateTime expireTime=redisData.getExpireTime();
            //7.判断是否过期
            if (expireTime.isAfter(LocalDateTime.now())){
                //7.1未过期，直接返回对应信息
                return classNeo4j;
            }
            //7.2已经过期，重建缓存
            String lockKey=LOCK_SHOP_KEY+packetName;
            boolean isLock=tryLock(lockKey);
            //7.3判断是否获取锁成功
            if (isLock){
                CACHE_REBUILD_EXECUTOR.submit(()->{
                    try {
                        //重建缓存
                        //1查询数据库
                        Collection<Map<String, Object>> result = joernMapper.getClassName(packetName);
                        List<Map<String, Object>> resultList = new ArrayList<>(result);
                        List<neo4jNode> classNeo4jRebuild = new ArrayList<>();
                        InternalNode class_node=null;
                        for (Map<String, Object> record : resultList) {
                            Object nodeObject = record.get("n");
                            if (nodeObject instanceof InternalNode) {
                                class_node = (InternalNode) nodeObject;
                            }
                            neo4jNode node=new neo4jNode(class_node.labels().iterator().next(),class_node.get("NAME").asString(),class_node.get("FULL_NAME").asString(),class_node.get("CODE").asString(),class_node.get("FILENAME").asString(),class_node.elementId());
                            classNeo4jRebuild.add(node);
                        }
                        //2.存储到缓存中
                        this.setWithLogicalExpire(packetName,classNeo4jRebuild,time,unit);
                    }catch (Exception e){
                        throw new RuntimeException(e);
                    }finally {
                        unlock(lockKey);
                    }
                });
            }
            return classNeo4j;
        }
    }

    /**
     * 查询method(逻辑过期版)
     * @param className
     * @param time
     * @param unit
     * @return
     */
    public List<neo4jNode> queryMethodNameByClass(String className,Long time, TimeUnit unit){
        //1.查询缓存
        String json=stringRedisTemplate.opsForValue().get(className);
        //2.判断是否存在
        if (StrUtil.isBlank(json)){
            //3.不存在，这里应该去查数据库然后存入缓存
            logger.info("需要到数据库中进行查询");
            Collection<Map<String, Object>> result = joernMapper.getMethodName(className);
            List<Map<String, Object>> resultList = new ArrayList<>(result);
            List<neo4jNode> methodNeo4j = new ArrayList<>();
            InternalNode class_node=null;
            for (Map<String, Object> record : resultList) {
                Object nodeObject = record.get("n");
                if (nodeObject instanceof InternalNode) {
                    class_node = (InternalNode) nodeObject;
                }
                neo4jNode node=new neo4jNode(class_node.labels().iterator().next(),class_node.get("NAME").asString(),class_node.get("FULL_NAME").asString(),class_node.get("CODE").asString(),class_node.get("FILENAME").asString(),class_node.elementId());
                methodNeo4j.add(node);
            }

            //4.存入到缓存
            this.setWithLogicalExpire(className,methodNeo4j,time,unit);
            //5.返回
            return methodNeo4j;
        }else{//6.如果存在
            RedisData redisData=JSONUtil.toBean(json,RedisData.class);
            List<neo4jNode> methodNeo4j=JSONUtil.toList((JSONArray) redisData.getData(),neo4jNode.class);
            LocalDateTime expireTime=redisData.getExpireTime();
            //7.判断是否过期
            if (expireTime.isAfter(LocalDateTime.now())){
                //7.1未过期，直接返回对应信息
                return methodNeo4j;
            }
            //7.2已经过期，重建缓存
            String lockKey=LOCK_SHOP_KEY+className;
            boolean isLock=tryLock(lockKey);
            //7.3判断是否获取锁成功
            if (isLock){
                CACHE_REBUILD_EXECUTOR.submit(()->{
                    try {
                        //重建缓存
                        //1查询数据库
                        //s
                        Collection<Map<String, Object>> result = joernMapper.getMethodName(className);
                        List<Map<String, Object>> resultList = new ArrayList<>(result);
                        List<neo4jNode> methodNeo4jRebuild = new ArrayList<>();
                        InternalNode class_node=null;
                        for (Map<String, Object> record : resultList) {
                            Object nodeObject = record.get("n");
                            if (nodeObject instanceof InternalNode) {
                                class_node = (InternalNode) nodeObject;
                            }
                            neo4jNode node=new neo4jNode(class_node.labels().iterator().next(),class_node.get("NAME").asString(),class_node.get("FULL_NAME").asString(),class_node.get("CODE").asString(),class_node.get("FILENAME").asString(),class_node.elementId());
                            methodNeo4jRebuild.add(node);
                        }

                        //2.存储到缓存中
                        this.setWithLogicalExpire(className,methodNeo4jRebuild,time,unit);
                    }catch (Exception e){
                        throw new RuntimeException(e);
                    }finally {
                        unlock(lockKey);
                    }
                });
            }
            return methodNeo4j;
        }
    }


    /**
     * 查询调用链路(逻辑过期版)
     * @param methodFullName
     * @param isDown
     * @param time
     * @param unit
     * @return
     */
    public List<neo4jPath> queryLinkByClassAndMethod(String methodFullName,Boolean isDown,Long time, TimeUnit unit){
        String key=methodFullName;
        List<neo4jPath> links=null;
        //1.查询缓存
        String json=stringRedisTemplate.opsForValue().get(key);
        //2.判断是否存在
        if (StrUtil.isBlank(json)){
            //3.不存在，这里应该去查数据库然后存入缓存
            logger.info("需要到数据库中进行查询");
            if (isDown){
                Collection<Map<String, Object>> result=joernMapper.getMethodDown(methodFullName);
                links= linkToPath(findRelation(result));
            }else {
                Collection<Map<String, Object>> result=joernMapper.getMethodUp(methodFullName);
                links= linkToPath(findRelation(result));
            }
            //4.存入到缓存
            this.setWithLogicalExpire(key,links,time,unit);
            //5.返回
            return links;
        }else{//6.如果存在
            RedisData redisData=JSONUtil.toBean(json,RedisData.class);
            links=JSONUtil.toList((JSONArray) redisData.getData(), neo4jPath.class);
            LocalDateTime expireTime=redisData.getExpireTime();
            //7.判断是否过期
            if (expireTime.isAfter(LocalDateTime.now())){
                //7.1未过期，直接返回对应信息
                return links;
            }
            //7.2已经过期，重建缓存
            String lockKey=LOCK_SHOP_KEY+key;
            boolean isLock=tryLock(lockKey);
            //7.3判断是否获取锁成功
            if (isLock){
                CACHE_REBUILD_EXECUTOR.submit(()->{
                    List<neo4jPath>linksRebuild=null;
                    try {
                        //重建缓存
                        //1查询数据库
                        if (isDown){
                            Collection<Map<String, Object>> result=joernMapper.getMethodDown(methodFullName);
                            linksRebuild= linkToPath(findRelation(result));
                        }else {
                            Collection<Map<String, Object>> result=joernMapper.getMethodUp(methodFullName);
                            linksRebuild= linkToPath(findRelation(result));
                        }
                        //2.存储到缓存中
                        this.setWithLogicalExpire(key,linksRebuild,time,unit);
                    }catch (Exception e){
                        throw new RuntimeException(e);
                    }finally {
                        unlock(lockKey);
                    }
                });
            }
            return links;
        }
    }

    public List<neo4jPath> queryDataBaseInfo(String dataBaseName, String tableName, String fieldName,Long time, TimeUnit unit){
        String key=dataBaseName+tableName+fieldName;
        List<neo4jPath> links=null;
        //1.查询缓存
        String json=stringRedisTemplate.opsForValue().get(key);
        //2.判断是否存在
        if (StrUtil.isBlank(json)){
            //3.不存在，这里应该去查数据库然后存入缓存
//            logger.info("需要到数据库中进行查询");
//            links=linkToPath(joernMapper.getDataBaseInfo(dataBaseName,tableName,fieldName));
//            //4.存入到缓存
//            this.setWithLogicalExpire(key,links,time,unit);
            //5.返回
            return links;
        }else{//6.如果存在
            RedisData redisData=JSONUtil.toBean(json,RedisData.class);
            links=JSONUtil.toList((JSONArray) redisData.getData(), neo4jPath.class);
            LocalDateTime expireTime=redisData.getExpireTime();
            //7.判断是否过期
            if (expireTime.isAfter(LocalDateTime.now())){
                //7.1未过期，直接返回对应信息
                return links;
            }
            //7.2已经过期，重建缓存
            String lockKey=LOCK_SHOP_KEY+key;
            boolean isLock=tryLock(lockKey);
            //7.3判断是否获取锁成功
            if (isLock){
                CACHE_REBUILD_EXECUTOR.submit(()->{
                    List<neo4jPath>linksRebuild=null;
                    try {
                        //重建缓存
                        //1查询数据库
//                        linksRebuild=linkToPath(joernMapper.getDataBaseInfo(dataBaseName,tableName,fieldName));
//                        //2.存储到缓存中
//                        this.setWithLogicalExpire(key,linksRebuild,time,unit);
                    }catch (Exception e){
                        throw new RuntimeException(e);
                    }finally {
                        unlock(lockKey);
                    }
                });
            }
            return links;
        }
    }

    /**
     * 相似度缓存
     * @param packetName
     * @param identify
     * @param time
     * @param unit
     * @return
     */
    public List<neo4jSimilarNode> getSimilar(String packetName,String identify,Double threshold,Long time, TimeUnit unit){
        //构建redis的key
        String key=LOCK_SIMILARITY_KEY+packetName+identify;
        List<neo4jSimilarNode> similarNodes=null;
        //1.查询缓存
        String json=stringRedisTemplate.opsForValue().get(key);
        //2.判断是否存在
        if (StrUtil.isBlank(json)){
            //3.不存在，重新预热后查询
            System.out.println("没有预热");
            //要加锁，估计dubbo会来问三次？
            Collection<Map<String, Object>> result = joernMapper.getSimilar(packetName);
            SimilarWarmUp(packetName,result,threshold,time,unit);
            //4.查询
            json=stringRedisTemplate.opsForValue().get(key);
            RedisData redisData=JSONUtil.toBean(json,RedisData.class);
            similarNodes=JSONUtil.toList((JSONArray) redisData.getData(), neo4jSimilarNode.class);
            if (similarNodes!=null){
                Collections.sort(similarNodes);
            }
            //5.返回
            return similarNodes;

        }else {//存在
            System.out.println("预热了");
            RedisData redisData=JSONUtil.toBean(json,RedisData.class);
            similarNodes=JSONUtil.toList((JSONArray) redisData.getData(), neo4jSimilarNode.class);
            LocalDateTime expireTime=redisData.getExpireTime();
            //判断是否过期
            if (expireTime.isAfter(LocalDateTime.now())){
                //未过期，直接返回对应信息
                return similarNodes;
            }
            //已经过期，重建缓存
            boolean isLock=tryLock(key);
            if (isLock){
                CACHE_REBUILD_EXECUTOR.submit(()->{
                    try {
                        //重建缓存
                        Collection<Map<String, Object>> result = joernMapper.getSimilar(packetName);
                        SimilarWarmUp(packetName,result,threshold,1000L,TimeUnit.SECONDS);
                    }catch (Exception e){
                        throw new RuntimeException(e);
                    }finally {
                        unlock(key);
                    }
                });
            }
            return similarNodes;
        }
    }




    /**
     * 相似度预热
     * @param packetName
     * @param result
     * @param time
     * @param unit
     */
    public void SimilarWarmUp(String packetName,Collection<Map<String, Object>> result,Double threshold,Long time, TimeUnit unit){
        String lockKey_pre=LOCK_SIMILARITY_KEY+packetName;
        List<Map<String, Object>> resultList = new ArrayList<>(result);
        //resultList的数量为相似对对数，list的每一个节点表示一个map，一个map有三个键值对分别是from和对应的值   to和对应的值   similarity和对应的值
        InternalNode class_node=null;
        Double similarity=0d;
        Map<String,List<neo4jSimilarNode>> map=new HashMap<>();
        for (int i=0;i<resultList.size();i++){
            Map<String, Object> record=resultList.get(i);//遍历获取map
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
            if (resNode.similarity<threshold) continue;
            //判断map中是否有名为from对应method名的key，有的话取出key对应的list加入，没有就创建list
            if (map.containsKey(nodeFrom.getId())){//如果已经有key
                map.get(nodeFrom.getId()).add(resNode);
            }else {
                List<neo4jSimilarNode> list=new ArrayList<>();
                list.add(resNode);
                map.put(nodeFrom.getId(),list);
            }
        }
        //遍历存入redis
        for (String method:map.keySet()){
            String key=lockKey_pre+method;
            try {
                System.out.println("------------------------");
                System.out.println(key);
                List<neo4jSimilarNode> nodes=map.get(method);
                //排序
                Collections.sort(nodes);
                //存入缓存
                this.setWithLogicalExpire(key,nodes,time,unit);
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }
        System.out.println("预热完成");
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
