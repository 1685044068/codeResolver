package com.icbc.codeResolver.controller;

import com.icbc.codeResolver.entity.Result;
import com.icbc.codeResolver.service.JoernParseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dubbo.config.annotation.DubboReference;
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
@RequestMapping(value = "/parse")
@Tag(name = "DubboParse", description = "Dubboparse接口")
public class ParseController {
    @DubboReference(group = "parse")
    JoernParseService joernParseService;


    @GetMapping("/parseCode")
    @Operation(summary = "解析文件", description = "解析文件")
    public Result parseAndImport(@RequestParam("url") String url) throws IOException {
        Result result=joernParseService.parse(url);
        System.out.println(result);
        return result;
    }

    /**
     * 解析路径下的所有文件
     * TODO 做一下过滤，只保留jar文件
     * @return
     */
    @GetMapping("/getFileList")
    @ResponseBody
    @Operation(summary = "获取解析目录下的所有文件", description = "获取解析目录下的所有文件")
    public Result getFileList(){
        Result result=joernParseService.getFileList();
        System.out.println(result);
        return result;
    }

}
