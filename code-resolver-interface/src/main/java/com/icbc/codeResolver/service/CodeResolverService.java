package com.icbc.codeResolver.service;

import com.icbc.codeResolver.entity.MethodNode;

import java.util.List;

public interface CodeResolverService {
    public List<String> getMethodUp(String methodName);
    public List<String> getMethodDown(String methodName);

    public List<String> getClassUp(String className);
    public List<String> getClassDown(String className);

    public List<String> getUrlPath(List<String> url);

    public List<String> getAllMethodRelation();
}
