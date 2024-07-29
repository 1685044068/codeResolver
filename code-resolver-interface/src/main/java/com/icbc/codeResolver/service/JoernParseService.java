package com.icbc.codeResolver.service;

import com.icbc.codeResolver.entity.Result;

import java.io.IOException;

/**
 * @BelongsProject: code-resolver
 * @BelongsPackage: com.icbc.codeResolver.service
 * @Author: zero
 * @CreateTime: 2024-07-22  10:05
 * @Description: TODO
 * @Version: 1.0
 */
public interface JoernParseService {
    /**
     * 核心解析业务
     * @param url
     * @return
     */
    public Result parse(String url) throws IOException;

    public Result getFileList();
}
