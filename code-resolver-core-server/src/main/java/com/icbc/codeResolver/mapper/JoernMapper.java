package com.icbc.codeResolver.mapper;

import com.icbc.codeResolver.entity.neo4jHotNode;
import com.icbc.codeResolver.entity.neo4jNode;
import com.icbc.codeResolver.entity.neo4jPath;
import com.icbc.codeResolver.entity.neo4jSimilarNode;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: code-resolver
 * @BelongsPackage: com.icbc.codeResolver.mapper
 * @Author: zero
 * @CreateTime: 2024-07-18  11:27
 * @Description: TODO
 * @Version: 1.0
 */
public interface JoernMapper {
    public Collection<Map<String, Object>> getMethodUp(String methodFullName);

    public Collection<Map<String, Object>> getMethodDown(String methodFullName);

    public Collection<Map<String, Object>> getUrlPath(String url);

    public Collection<Map<String, Object>> getDataBaseInfo();

    public Collection<Map<String, Object>> getAnnotationInfo(String methodFullName,String code);

    public Collection<Map<String, Object>> getClassName(String packetName);

    public Collection<Map<String, Object>> getMethodName(String classFullName);

    public Collection<Map<String, Object>> getHotNode(String packetName, String maxNumber);

    public Collection<Map<String, Object>> getSimilar(String packetName);

    public Collection<Map<String, Object>> getShortestPath(String methodFullName);

    public Collection<Map<String, Object>> getCollectionPath(List<String> list);

    public Collection<Map<String, Object>> getMethodInformation(String methodName);

    public Collection<Map<String, Object>> getMethodByLine(String fileName,Integer lineNumber);
    public Collection<Map<String, Object>> getAstPath(String id);
    public boolean createDatabase(String databaseName);
    public boolean changeDataBase(String databaseName);

    public Collection<Map<String, Object>> showDataBase();

    public String showCurrentDataBase();

    public Collection<Map<String, Object>> getMeteData();

}
