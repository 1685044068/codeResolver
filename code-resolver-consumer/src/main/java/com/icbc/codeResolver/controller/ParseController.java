package com.icbc.codeResolver.controller;

import com.icbc.codeResolver.entity.Result;
import com.icbc.codeResolver.service.JoernParseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "DubboParse", description = "Dubboparse接口")
public class ParseController {

    //日志
    private static Logger logger = Logger.getLogger(ParseController.class);

    @DubboReference(group = "parse")
    JoernParseService joernParseService;

    @GetMapping("/parseCode")
    @Operation(summary = "解析文件", description = "解析文件")
    public Result parseAndImport(@RequestParam("url") String url) throws IOException {
        logger.info("开始解析jar包");
        Result result=joernParseService.parse(url);
        logger.info("解析结果为 "+result);
        return result;
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
