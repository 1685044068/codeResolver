package com.icbc.codeResolver.controller;

import cn.hutool.core.lang.UUID;
import com.icbc.codeResolver.entity.AsyncTaskProgress;
import com.icbc.codeResolver.entity.FileDto;
import com.icbc.codeResolver.entity.Result;
import com.icbc.codeResolver.service.JoernParseService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RequestMapping("/parser")
@RestController
public class JoernParseController {
    @Autowired
    JoernParseService joernParseService;

    public static String generateTaskId() {
        return UUID.randomUUID().toString();
    }
    @GetMapping("/parseCode")
    @Operation(summary = "同步单线程进行解析", description = "同步单线程进行解析")
    public Result parseAndImport(@RequestParam("url") String url) throws IOException {
        String res=joernParseService.parse(url);
        return Result.success(res);
    }

    @GetMapping("/AsyncParseCode")
    @Operation(summary = "异步多线程进行解析", description = "异步多线程进行解析")
    public Result AsyncparseAndImport(@RequestParam("url") String url) throws IOException {
        String taskId=generateTaskId();
        joernParseService.AsyncParse(url,taskId);
        return Result.processing("等待任务结束",taskId);
    }

    @GetMapping("/getFileList")
    @Operation(summary = "获取解析目录下的所有文件", description = "获取解析目录下的所有文件")
    public Result getFileList(){
        List<FileDto> result=joernParseService.getFileList();
        return Result.success(result);
    }

    /**
     * 前端通过该接口查询进度
     * @param taskId
     * @return
     */
    @GetMapping("/progress")
    @Operation(summary = "查看后端导入进度", description = "查看后端导入进度")
    public Result getAsyncTaskProgress(@RequestParam("taskId") String taskId) {
        AsyncTaskProgress res=joernParseService.getAsyncTaskProgress(taskId);
        return Result.success(res);
    }
}
