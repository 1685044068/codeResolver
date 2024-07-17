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
@Tag(name = "Joern", description = "joern接口")
public class JoernController {
    @DubboReference(group = "joern")
    CodeResolverService joernService;

    /**
     * 方法追踪 类名 方法名
     * @return
     */
    @GetMapping("/methodDown")
    @ResponseBody
    @Operation(summary = "方法追踪", description = "根据类名以及方法名进行方法追踪")
    public List<String> getMethodNodeDown(@RequestParam("className") String className,@RequestParam("methodName") String methodName) {
        System.out.println("方法追踪"+className);
        System.out.println("方法追踪"+methodName);
        List<String> sbrList = joernService.getMethodDown(className+".java",methodName);
        return sbrList;
    }

    /**
     * 方法溯源
     * @return
     */
    @GetMapping("/methodUp")
    @ResponseBody
    @Operation(summary = "方法溯源", description = "根据类名以及方法名进行方法溯源")
    public List<String> getMethodNodeUp(@RequestParam("className") String className,@RequestParam("methodName") String methodName) {
        System.out.println("方法溯源"+className);
        System.out.println("方法溯源"+methodName);
        List<String> sbrList = joernService.getMethodUp(className+".java",methodName);
        return sbrList;
    }


    /**
     * url精确查找 url-》斜杠分割 List<String>
     * @return
     */
    @GetMapping("/urlPath")
    @ResponseBody
    @Operation(summary = "url查找", description = "url的形式为/*/*/*")
    public List<String> getUrlPath(@RequestParam("url") String url) {
        String[] urlField = url.split("/");
        String Info=url.substring(urlField[0].length()+1,url.length());
        List<String> data=new ArrayList<>();
        data.add("/"+urlField[0]);
        data.add(Info);
        List<String> sbrList = joernService.getUrlPath(data);
        return sbrList;
    }

    @ResponseBody
    @GetMapping("/dataBaseInfo")
    @Operation(summary = "数据库表字段关系", description = "根据数据库名，表名，字段名查询")
    public List<String> getDataBaseInfo(@RequestParam("dataBaseName")String dataBaseName, @RequestParam("tableName")String tableName, @RequestParam("fieldName")String fieldName) {
        System.out.println("数据库名"+dataBaseName);
        System.out.println("表名"+tableName);
        System.out.println("字段名查询"+fieldName);
        return joernService.getDataBaseInfo(dataBaseName,tableName,fieldName);
    }

}
