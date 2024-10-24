package com.icbc.codeResolver.service;

import com.icbc.codeResolver.entity.AsyncTaskProgress;
import com.icbc.codeResolver.entity.FileDto;

import java.io.IOException;
import java.util.List;

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
    public void AsyncParse(String url,String taskId) throws IOException;
    public String parse(String url) throws IOException;

    public List<FileDto> getFileList();

    public AsyncTaskProgress getAsyncTaskProgress(String taskId);
}
