package com.icbc.codeResolver.service;

import com.icbc.codeResolver.entity.MethodNode;

import java.util.List;

public interface CodeResolverService {
    public List<String> getMethodUp(String method);
    public List<String> getMethodDown(String method);

    public List<String> getAllMethodRelation();

    public List<String> getUrlPathUp(List<String> url);
    public List<String> getUrlPathDown(List<String> url);

    public List<String> getSqlMember(String properies);

    public List<String> getUrlPathAbstract(List<String> url);
    public List<String> getUrlPathDetailUp(List<String> url);
    public List<String> getUrlPathDetailDown(List<String> url);
}
