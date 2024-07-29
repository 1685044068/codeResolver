package com.icbc.codeResolver.service;

import com.icbc.codeResolver.entity.neo4jHotNode;
import com.icbc.codeResolver.entity.neo4jNode;
import com.icbc.codeResolver.entity.neo4jPath;
import com.icbc.codeResolver.entity.neo4jSimilarNode;

import java.util.List;

public interface CodeResolverService {
    /**
     * 根据类和方法名向上溯源
     * @param methodFullName
     * @param methodCode
     * @return
     */
    public List<neo4jPath> getMethodUp(String methodFullName,String methodCode);

    /**
     * 根据类和方法名向下追踪
     * @param methodFullName
     * @param methodCode
     * @return
     */
    public List<neo4jPath> getMethodDown(String methodFullName,String methodCode);

    /**
     * 根据url查询
     * @param url
     * @return
     */
    public List<neo4jPath> getUrlPath(String url);

    /**
     * 查询表字段以及相关关系
     * @param dataBaseName
     * @param tableName
     * @param fieldName
     * @return
     */
    public List<neo4jPath> getDataBaseInfo(String dataBaseName,String tableName,String fieldName);

    /**
     * 前端传递包名返回类名
     * @param packetName
     * @return
     */
    public List<neo4jNode> showClassName(String packetName);

    /**
     * 前端传递类名返回方法名
     * @param className
     * @return
     */
    public List<neo4jNode> showMethodName(String className);

    /**
     * 前端传递类名和方法名返回调用链路
     * @param classFullName
     * @param methodCode
     * @return
     */
    public List<neo4jPath> showInvocationLink(String classFullName, String methodCode,Boolean isDonw);


    public List<neo4jHotNode> getHotNode(String packetName, String maxNumber);

    public List<neo4jSimilarNode> getSimilar(String packetName, String identify, Double threshold);

    public List<neo4jPath> getShortestPath(String methodFullName,String methodCode);

    public List<neo4jPath> getCollectionPath(List<String> list);

}
