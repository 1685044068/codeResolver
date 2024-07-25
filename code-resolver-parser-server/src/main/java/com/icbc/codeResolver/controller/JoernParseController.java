package com.icbc.codeResolver.controller;

import com.icbc.codeResolver.entity.Result;
import com.icbc.codeResolver.service.JoernParseService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/parser")
@RestController
@Slf4j
public class JoernParseController {
    @Autowired
    JoernParseService joernParseService;
    @GetMapping("/parseCode")
    public Result parseAndImport(@RequestParam("url") String url){
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
