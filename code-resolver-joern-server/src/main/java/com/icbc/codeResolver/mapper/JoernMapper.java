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
    public List<neo4jPath> getMethodUp(String methodFullName);

    public List<neo4jPath> getMethodDown(String methodFullName);

    public List<neo4jPath> getUrlPath(String url);

    public List<neo4jPath> getDataBaseInfo(String dataBaseName, String tableName, String fieldName);

    public List<neo4jNode> getClassName(String packetName);

    public List<neo4jNode> getMethodName(String classFullName);

    public List<neo4jHotNode> getHotNode(String packetName, String maxNumber);

    public List<neo4jSimilarNode> getSimilar(String packetName);

    public List<neo4jPath> getShortestPath(String methodFullName);

    public List<neo4jPath> getCollectionPath(List<String> list);

}
