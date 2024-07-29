package com.icbc.codeResolver.service;

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
     * @param className
     * @param methodName
     * @return
     */
    @Override
    public List<neo4jPath> getMethodUp(String className,String methodName) {
//        return cacheClient.queryLinkByClassAndMethod(className,methodName,Boolean.FALSE, 100000L, TimeUnit.SECONDS);
        return joernMapper.getMethodUp(className,methodName);
    }


    /**
     * @param className
     * @param methodName
     * @return
     */
    @Override
    public List<neo4jPath> getMethodDown(String className,String methodName) {
//        return cacheClient.queryLinkByClassAndMethod(className,methodName,Boolean.TRUE, 100000L, TimeUnit.SECONDS);
        return joernMapper.getMethodDown(className,methodName);
    }

    /**
     * url精确查找
     * @param url
     * @return
     */
    @Override
    public List<neo4jPath> getUrlPath(String url) {
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

        return cacheClient.queryDataBaseInfo(dataBaseName,tableName,fieldName,100000L, TimeUnit.SECONDS);
        //return joernMapper.getDataBaseInfo(dataBaseName,tableName,fieldName);
    }

    /**
     * 前端传递包名返回类名
     * @param packetName
     * @return
     */
    @Override
    public List<neo4jNode> showClassName(String packetName) {
//        return cacheClient.queryClassNameByPacket(packetName,100000L, TimeUnit.SECONDS);
        return joernMapper.getClassName(packetName);
    }

    /**
     * 前端传递类名返回方法名
     * @param className
     * @return
     */
    @Override
    public List<neo4jNode> showMethodName(String className) {
//        return cacheClient.queryMethodNameByClass(className,100000L, TimeUnit.SECONDS);
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

    /**
     * 获取热点节点
     * @param packetName
     * @param maxNumber
     * @return
     */
    @Override
    public List<neo4jHotNode> getHotNode(String packetName, String maxNumber) {
        return joernMapper.getHotNode(packetName,maxNumber);
    }

    @Override
    public List<neo4jSimilarNode> getSimilar(String packetName,String identify,Double threshold) {
        List<neo4jSimilarNode> ans=joernMapper.getSimilar(packetName);
        List<neo4jSimilarNode> res = new ArrayList<>();
        for(int i=0;i<ans.size();i++){
            neo4jSimilarNode node=ans.get(i);
            if((node.from.id.equals(identify))&&(node.similarity>=threshold)){
                res.add(node);
            }
        }
        Collections.sort(res);
        return res;
    }
}
