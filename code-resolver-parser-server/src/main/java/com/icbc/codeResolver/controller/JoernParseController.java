package com.icbc.codeResolver.controller;

import com.icbc.codeResolver.entity.Result;
import com.icbc.codeResolver.service.JoernParseService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RequestMapping("/parser")
@RestController
@Slf4j
public class JoernParseController {
    @Autowired
    JoernParseService joernParseService;
    @GetMapping("/parseCode")
    public Result parseAndImport(@RequestParam("url") String url) throws IOException {
        Result result=joernParseService.parse(url);
        System.out.println(result);
        return result;
    }

    @GetMapping("/getFileList")
    @Operation(summary = "获取解析目录下的所有文件", description = "获取解析目录下的所有文件")
    public Result getFileList(){
        Result result=joernParseService.getFileList();
        System.out.println(result);
        return result;
    }
}
