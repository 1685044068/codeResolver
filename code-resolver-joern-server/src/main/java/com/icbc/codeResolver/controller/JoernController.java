package com.icbc.codeResolver.controller;

import com.icbc.codeResolver.service.CodeResolverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/joern")
@Tag(name = "joern", description = "joern接口")
public class JoernController {
    @DubboReference(group = "joern")
    CodeResolverService joernService;

    /**
     * 方法追踪 类名 方法名
     * @return
     */
    @GetMapping("/methodDown/{method}")
    @ResponseBody
    @Operation(summary = "方法追踪", description = "方法追踪")
    public List<String> getMethodNodeDown(@PathVariable("method") String methodName) {
        List<String> sbrList = joernService.getMethodDown(methodName);
        return sbrList;
    }

    /**
     * 方法溯源
     * @return
     */
    @GetMapping("/methodUp/{method}")
    @ResponseBody
    @Operation(summary = "方法溯源", description = "方法溯源")
    public List<String> getMethodNodeUp(@PathVariable("method") String methodName) {
        List<String> sbrList = joernService.getMethodUp(methodName);
        return sbrList;
    }

    /**
     * 类追踪
     * @return
     */
    @GetMapping("/classDown/{class}")
    @ResponseBody
    @Operation(summary = "类追踪", description = "类追踪")
    public List<String> getClassNodeDown(@PathVariable("class") String className) {
        List<String> sbrList = joernService.getClassDown(className);
        return sbrList;
    }

    /**
     * 类溯源
     * @return
     */
    @GetMapping("/classUp/{class}")
    @ResponseBody
    @Operation(summary = "类溯源", description = "类溯源")
    public List<String> getClassNodeUp(@PathVariable("class") String className) {
        List<String> sbrList = joernService.getClassUp(className);
        return sbrList;
    }

    /**
     * url精确查找 url-》斜杠分割 List<String>
     * @return
     */
    @GetMapping("/urlPath/{url}")
    @ResponseBody
    @Operation(summary = "url精确查找", description = "url精确查找")
    public List<String> getUrlPath(@PathVariable("className") String className,@PathVariable("methodName") String methodName) {
        List<String> url = new ArrayList<>();
        url.add(className);
        url.add(methodName);
        List<String> sbrList = joernService.getUrlPath(url);
        return sbrList;
    }

    /**
     * 表名和字段
     * @return
     */



    @GetMapping("/methodAll")
    @ResponseBody
    @Operation(summary = "methodAll", description = "methodAll")
    public List<String> getMethodNodeAll() {
        return joernService.getAllMethodRelation();
    }







}
