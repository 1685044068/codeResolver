package com.icbc.codeResolver.controller;

import com.icbc.codeResolver.entity.neo4jNode;
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
        List<neo4jNode> sbrList = joernService.getMethodDown(className,methodName);
        return pathToList(sbrList,true);
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
        List<neo4jNode> sbrList = joernService.getMethodUp(className,methodName);
        return pathToList(sbrList,false);
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
        List<neo4jNode> sbrList = joernService.getUrlPath(data);


        return pathToList(sbrList,true);
    }

    @ResponseBody
    @GetMapping("/dataBaseInfo")
    @Operation(summary = "目标三：数据库表字段关系", description = "根据数据库名，表名，字段名查询")
    public List<String> getDataBaseInfo(@RequestParam("dataBaseName")String dataBaseName, @RequestParam("tableName")String tableName, @RequestParam("fieldName")String fieldName) {
        System.out.println("数据库名"+dataBaseName);
        System.out.println("表名"+tableName);
        System.out.println("字段名查询"+fieldName);
        List<neo4jNode> sbrList=joernService.getDataBaseInfo(dataBaseName,tableName,fieldName);
        return pathToList(sbrList,false);
    }


    @ResponseBody
    @GetMapping("/className")
    @Operation(summary = "目标一：", description = "获取类")
    public List<String> getClassName(@RequestParam("packetName")String packetName) {
        System.out.println("包名"+packetName);
        List<neo4jNode> sbrList=joernService.getClassName(packetName);
        return pathToList(sbrList,false);
    }

    @ResponseBody
    @GetMapping("/methodName")
    @Operation(summary = "目标一：", description = "获取方法")
    public List<String> getMethodName(@RequestParam("className")String className) {
        System.out.println("类全路径"+className);
        List<neo4jNode> sbrList=joernService.getMethodName(className);
        return pathToList(sbrList,false);
    }

    public List<String> pathToList(List<neo4jNode> path, boolean direction) {
        String spiltChar = direction?"->":"<-";
        List<String> sbrList = new ArrayList<>();
        for (int i = 0; i < path.size(); i++) {
            StringBuilder stringBuilder = new StringBuilder();
            neo4jNode r = path.get(i);
            while (r != null) {
                stringBuilder.append('('+r.label+')'+r.code+spiltChar);
                r = r.next;
            }
            sbrList.add(stringBuilder.substring(0,stringBuilder.length()-2));
        }
        return sbrList;
    }

}
