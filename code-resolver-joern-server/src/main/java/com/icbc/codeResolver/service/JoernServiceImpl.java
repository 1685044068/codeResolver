package com.icbc.codeResolver.service;

import com.icbc.codeResolver.entity.neo4jNode;
import com.icbc.codeResolver.entity.neo4jPath;
import com.icbc.codeResolver.mapper.JoernMapper;
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
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private JoernMapper joernMapper;

    /**
     * TODO 等待修改
     * @param className
     * @param methodName
     * @return
     */
    @Override
    public List<neo4jPath> getMethodUp(String className,String methodName) {
        return joernMapper.getMethodUp(className,methodName);
    }


    /**
     * @param className
     * @param methodName
     * @return
     */
    @Override
    public List<neo4jPath> getMethodDown(String className,String methodName) {
        return joernMapper.getMethodDown(className,methodName);
    }

    /**
     * url精确查找
     * @param url
     * @return
     */
    @Override
    public List<neo4jPath> getUrlPath(List<String> url) {
        return joernMapper.getUrlPath(url);
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
        return joernMapper.getDataBaseInfo(dataBaseName,tableName,fieldName);
    }

    /**
     * 前端传递包名返回类名
     * @param packetName
     * @return
     */
    @Override
    public List<neo4jNode> showClassName(String packetName) {
        //1.首先根据包名向redis中查询数据
        //1.1如果查询到直接返回
        //2.如果没有查询到调用neo4j部分代码进行查询
        //2.1存入redis，返回数据
        return joernMapper.getClassName(packetName);
    }

    /**
     * 前端传递类名返回方法名
     * @param className
     * @return
     */
    @Override
    public List<neo4jNode> showMethodName(String className) {
        return joernMapper.getMethodName(className);
    }

    /**
     * 前端传递类名和方法名返回调用链路
     * @param className
     * @param methodName
     * @return
     */
    @Override
    public List<neo4jPath> showInvocationLink(String className, String methodName,Boolean isDonw) {
        if (isDonw){
            return joernMapper.getMethodDown(className,methodName);
        }else {
            return joernMapper.getMethodUp(className,methodName);
        }

    }
}
