package com.icbc.codeResolver.service;

import com.icbc.codeResolver.entity.MethodNode;

import java.util.List;

public interface CodeResolverService {
    public List<String> getMethodUp(String className,String methodName);
    public List<String> getMethodDown(String className,String methodName);

    public List<String> getUrlPath(List<String> url);

    public List<String> getDataBaseInfo(String dataBaseName,String tableName,String fieldName);

}
