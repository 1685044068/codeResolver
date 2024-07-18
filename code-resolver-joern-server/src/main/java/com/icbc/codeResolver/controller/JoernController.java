package com.icbc.codeResolver.controller;

import com.icbc.codeResolver.entity.neo4jNode;
import com.icbc.codeResolver.entity.neo4jPath;
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
    @Operation(summary = "目标一：方法追踪", description = "根据类名以及方法名进行方法追踪")
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
    @Operation(summary = "目标一：方法溯源", description = "根据类名以及方法名进行方法溯源")
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
    @Operation(summary = "目标二：url查找", description = "url的形式为/*/*/*")
    public List<String> getUrlPath(@RequestParam("url") String url) {
        String[] urlField = url.split("/");
        String Info=url.substring(urlField[1].length()+1,url.length());
        List<String> data=new ArrayList<>();
        data.add("/"+urlField[1]);
        data.add(Info);
        List<String> sbrList = joernService.getUrlPath(data);
        return sbrList;
    }

    /**
     * 查询表字段以及相关关系
     * @param dataBaseName
     * @param tableName
     * @param fieldName
     * @return
     */
    @ResponseBody
    @GetMapping("/dataBaseInfo")
    @Operation(summary = "目标三：数据库表字段关系", description = "根据数据库名，表名，字段名查询")
    public List<String> getDataBaseInfo(@RequestParam("dataBaseName")String dataBaseName, @RequestParam("tableName")String tableName, @RequestParam("fieldName")String fieldName) {
        System.out.println("数据库名"+dataBaseName);
        System.out.println("表名"+tableName);
        System.out.println("字段名查询"+fieldName);
        return joernService.getDataBaseInfo(dataBaseName,tableName,fieldName);
    }

    @ResponseBody
    @GetMapping("/showClassName")
    @Operation(summary = "目标一优化：获取包下所有类名", description = "根据前端传递过来的包名获取到该包下的所有类名")
    public List<neo4jNode> showClassName(@RequestParam("packetName")String packetName) {
        System.out.println("包名"+packetName);
        return joernService.showClassName(packetName);
    }

    @ResponseBody
    @GetMapping("/showMethodName")
    @Operation(summary = "目标一优化：获取类下所有方法名", description = "根据前端传递过来的类名获取到该类下的所有方法名以及参数")
    public List<neo4jNode> showMethodName(@RequestParam("className")String className) {
        System.out.println("类名"+className);
        return joernService.showMethodName(className);
    }

    /**
     * TODO 和getMethodNodeDown以及getMethodNodeUp的功能基本一致可以不要
     * @param className
     * @param methodName
     * @return
     */
    @ResponseBody
    @GetMapping("/showInvocationLink")
    @Operation(summary = "目标一优化：获取唯一方法的调用链路", description = "根据前端传递过来的类名以及方法名及其参数获取到该唯一方法的调用链路")
    public List<neo4jPath> showMethodName(@RequestParam("className")String className, @RequestParam("methodName")String methodName) {
        System.out.println("类名"+className);
        System.out.println("包名"+methodName);
        return joernService.showInvocationLink(className, methodName);
    }




}
