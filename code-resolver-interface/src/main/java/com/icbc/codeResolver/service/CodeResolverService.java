package com.icbc.codeResolver.service;

import com.icbc.codeResolver.entity.neo4jNode;

import java.util.List;

public interface CodeResolverService {
    public List<neo4jNode> getMethodUp(String className, String methodName);
    public List<neo4jNode> getMethodDown(String className, String methodName);

    public List<neo4jNode> getUrlPath(List<String> url);

    public List<neo4jNode> getDataBaseInfo(String dataBaseName, String tableName, String fieldName);

    public List<neo4jNode> getClassName(String packetName);

    public List<neo4jNode> getMethodName(String className);

    public List<neo4jNode> getInvocationLink(String className, String methodName);

}
