package com.icbc.codeResolver.controller;

import cn.hutool.core.lang.UUID;
import com.icbc.codeResolver.entity.AsyncTaskProgress;
import com.icbc.codeResolver.entity.Result;
import com.icbc.codeResolver.service.JoernParseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * @BelongsProject: code-resolver
 * @BelongsPackage: com.icbc.coderesolverconsumber.controller
 * @Author: zero
 * @CreateTime: 2024-07-22  09:20
 * @Description: 分布式调用controller
 * @Version: 1.0
 */
@RestController
@RequestMapping(value = "/parser")
@Slf4j
@Tag(name = "DubboParse", description = "Dubboparse接口")
public class ParseController {

    /**
     * 生成随机的UUID
     * @return
     */
    public static String generateTaskId() {
        return UUID.randomUUID().toString();
    }

    /**
     * 日志
     */
    private static Logger logger = Logger.getLogger(ParseController.class);

    @DubboReference(group = "parse")
    JoernParseService joernParseService;

    /**
     * 同步单线程进行解析
     * @param url
     * @return
     * @throws IOException
     */
    @GetMapping("/parseCode")
    @Operation(summary = "同步单线程进行解析", description = "同步单线程进行解析")
    public Result parseAndImport(@RequestParam("url") String url) throws IOException {
        return joernParseService.parse(url);
    }


    /**
     * 异步多线程进行解析
     * @param url
     * @return
     * @throws IOException
     */
    @GetMapping("/AsyncParseCode")
    @Operation(summary = "异步多线程进行解析", description = "异步多线程进行解析")
    public Result AsyncparseAndImport(@RequestParam("url") String url) throws IOException {
        String taskId=generateTaskId();
        joernParseService.AsyncParse(url,taskId);
        return Result.processing("等待任务结束",taskId);
    }

    /**
     * 前端通过该接口查询进度
     * @param taskId
     * @return
     */
    @GetMapping("/progress")
    @Operation(summary = "查看后端导入进度", description = "查看后端导入进度")
    public AsyncTaskProgress getAsyncTaskProgress(@RequestParam("taskId") String taskId) {
        return joernParseService.getAsyncTaskProgress(taskId);
    }

    /**
     * 获取路径下的所有jar包
     * @return
     */
    @GetMapping("/getFileList")
    @ResponseBody
    @Operation(summary = "获取解析目录下的所有文件", description = "获取解析目录下的所有文件")
    public Result getFileList(){
        Result result=joernParseService.getFileList();
        logger.info("获取到的文件为"+result);
        return result;
    }

}
