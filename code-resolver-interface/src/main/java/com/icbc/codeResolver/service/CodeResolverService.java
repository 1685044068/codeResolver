package com.icbc.codeResolver.service;

import com.icbc.codeResolver.entity.*;

import java.util.List;
import java.util.Map;

public interface CodeResolverService {
    /**
     * 根据类和方法名向上溯源
     * @param methodFullName
     * @return
     */
    public List<neo4jPath> getMethodUp(String methodFullName);

    /**
     * 根据类和方法名向下追踪
     * @param methodFullName
     * @return
     */
    public List<neo4jPath> getMethodDown(String methodFullName);

    /**
     * 根据url查询
     * @param url
     * @return
     */
    public List<neo4jPath> getUrlPath(String url);

    /**
     * 查询表字段以及相关关系
     * @param tableName
     * @param fieldName
     * @return
     */
    public List<neo4jPath> getDataBaseInfo(String tableName,String fieldName);

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
     * @param methodFullName
     * @return
     */
    public List<neo4jPath> showInvocationLink(String methodFullName, Boolean isDonw);



    /**
     * 获取热点节点
     * @param packetName
     * @param maxNumber
     * @return
     */
    public List<neo4jHotNode> getHotNode(String packetName, String maxNumber);

    /**
     * 获取指定节点相似度
     * @param packetName
     * @param identify
     * @param threshold
     * @return
     */
    public List<neo4jSimilarNode> getSimilar(String packetName, String identify, Double threshold);

    /**
     * 获取最短路径
     * @param methodFullName
     * @return
     */
    public List<neo4jPath> getShortestPath(String methodFullName);

    /**
     * 获取存在多个指定节点的多个链路
     * @param list
     * @return
     */
    public List<neo4jPath> getCollectionPath(List<String> list);

    /**
     * 获取方法的信息
     * @param methodName
     * @return
     */
    public List<neo4jNode> getMethodInformation(String methodName);



    public boolean createDatabase(String databaseName);

    public boolean changeDataBase(String databaseName);

    public List<String> showDataBase();

    public String showCurrentDataBase();

    public List<neo4jNode> getDynamic(Map<String,List<Integer>> lineInformation);
    public List<neo4jDynamic> getChangeMethodInfo(Integer id);

    public List<neo4jNode> getMeteData();

}
