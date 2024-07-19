package com.icbc.codeResolver.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.icbc.codeResolver.entity.RedisData;
import com.icbc.codeResolver.entity.neo4jNode;
import com.icbc.codeResolver.entity.neo4jPath;
import com.icbc.codeResolver.mapper.JoernMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;



import static com.icbc.codeResolver.utils.RedisConstants.LOCK_SHOP_KEY;

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
            System.out.println("需要到数据库中进行查询");
            List<neo4jNode> classNeo4j = joernMapper.getClassName(packetName);
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
                        List<neo4jNode> classNeo4jRebuild = joernMapper.getClassName(packetName);
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
            System.out.println("需要到数据库中进行查询");
            List<neo4jNode> methodNeo4j = joernMapper.getMethodName(className);
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
                        List<neo4jNode> methodNeo4jRebuild = joernMapper.getMethodName(className);
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
     * @param className
     * @param methodName
     * @param isDown
     * @param time
     * @param unit
     * @return
     */
    public List<neo4jPath> queryLinkByClassAndMethod(String className,String methodName,Boolean isDown,Long time, TimeUnit unit){
        String key=className+methodName;
        List<neo4jPath> links=null;
        //1.查询缓存
        String json=stringRedisTemplate.opsForValue().get(key);
        //2.判断是否存在
        if (StrUtil.isBlank(json)){
            //3.不存在，这里应该去查数据库然后存入缓存
            System.out.println("需要到数据库中进行查询");
            if (isDown){
                links = joernMapper.getMethodDown(className,methodName);
            }else {
                links = joernMapper.getMethodUp(className,methodName);
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
                            linksRebuild = joernMapper.getMethodDown(className,methodName);
                        }else {
                            linksRebuild = joernMapper.getMethodUp(className,methodName);
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

























}
