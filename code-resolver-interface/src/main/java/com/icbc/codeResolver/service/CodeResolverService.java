package com.icbc.codeResolver.service;

import com.icbc.codeResolver.entity.MethodNode;

import java.util.List;

public interface CodeResolverService {
    public List<String> getMethodUp(String method);
    public List<String> getMethodDown(String method);

    public List<String> getAllMethodRelation();
}
