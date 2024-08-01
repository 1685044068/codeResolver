package com.icbc.codeResolver.controller;

import cn.hutool.core.lang.UUID;
import com.icbc.codeResolver.entity.Result;
import com.icbc.codeResolver.service.JoernParseService;
import com.icbc.codeResolver.entity.AsyncTaskProgress;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RequestMapping("/parser")
@RestController
@Slf4j
public class JoernParseController {
    @Autowired
    JoernParseService joernParseService;

    public static String generateTaskId() {
        return UUID.randomUUID().toString();
    }
    @GetMapping("/parseCode")
    public Result parseAndImport(@RequestParam("url") String url) throws IOException {
        String taskId = generateTaskId();
        joernParseService.AsyncParse(url,taskId);
        return Result.processing("任务进行中.....",taskId);
    }

    @GetMapping("/getFileList")
    @Operation(summary = "获取解析目录下的所有文件", description = "获取解析目录下的所有文件")
    public Result getFileList(){
        Result result=joernParseService.getFileList();
        System.out.println(result);
        return result;
    }

    /**
     * 前端通过该接口查询进度
     * @param taskId
     * @return
     */
    @GetMapping("/progress")
    public AsyncTaskProgress getAsyncTaskProgress(@RequestParam("taskId") String taskId) {
        return joernParseService.getAsyncTaskProgress(taskId);
    }
}
