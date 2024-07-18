package com.icbc.codeResolver.service;

import com.icbc.codeResolver.entity.neo4jNode;
import com.icbc.codeResolver.entity.neo4jPath;

import java.util.List;

public interface CodeResolverService {
    /**
     * 根据类和方法名向上溯源
     * @param className
     * @param methodName
     * @return
     */
    public List<String> getMethodUp(String className,String methodName);

    /**
     * 根据类和方法名向下追踪
     * @param className
     * @param methodName
     * @return
     */
    public List<String> getMethodDown(String className,String methodName);

    /**
     * 根据url查询
     * @param url
     * @return
     */
    public List<String> getUrlPath(List<String> url);

    /**
     * 查询表字段以及相关关系
     * @param dataBaseName
     * @param tableName
     * @param fieldName
     * @return
     */
    public List<String> getDataBaseInfo(String dataBaseName,String tableName,String fieldName);

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
     * @param className
     * @param methodName
     * @return
     */
    public List<neo4jPath> showInvocationLink(String className, String methodName);



}
