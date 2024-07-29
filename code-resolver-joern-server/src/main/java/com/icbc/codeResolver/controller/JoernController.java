package com.icbc.codeResolver.controller;

import com.icbc.codeResolver.entity.neo4jHotNode;
import com.icbc.codeResolver.entity.neo4jNode;
import com.icbc.codeResolver.entity.neo4jPath;
import com.icbc.codeResolver.entity.neo4jSimilarNode;
import com.icbc.codeResolver.service.CodeResolverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 本地测试controller
 */
@RestController
@RequestMapping(value = "/joern")
@Tag(name = "Joern", description = "joern接口")
public class JoernController {
    @DubboReference(group = "joern")
    CodeResolverService joernService;

    /**
     * url精确查找 url-》斜杠分割 List<String>
     * @return
     */
    @GetMapping("/urlPath")
    
    @Operation(summary = "目标二：url查找", description = "url的形式为/*/*/*")
    public List<neo4jPath> getUrlPath(@RequestParam("url") String url) {
        return joernService.getUrlPath(url);
    }

    /**
     * 查询表字段以及相关关系
     * @param dataBaseName
     * @param tableName
     * @param fieldName
     * @return
     */
    
    @GetMapping("/dataBaseInfo")
    @Operation(summary = "目标三：数据库表字段关系", description = "根据数据库名，表名，字段名查询")
    public List<neo4jPath> getDataBaseInfo(@RequestParam("dataBaseName")String dataBaseName, @RequestParam("tableName")String tableName, @RequestParam("fieldName")String fieldName) {
        System.out.println("目标三：数据库表字段关系 数据库名"+dataBaseName);
        System.out.println("目标三：数据库表字段关系 表名"+tableName);
        System.out.println("目标三：数据库表字段关系 字段名查询"+fieldName);
        return joernService.getDataBaseInfo(dataBaseName,tableName,fieldName);
    }

    
    @GetMapping("/showClassName")
    @Operation(summary = "目标一优化：获取包下所有类名", description = "根据前端传递过来的包名获取到该包下的所有类名")
    public List<neo4jNode> showClassName(@RequestParam("packetName")String packetName) {
        System.out.println("目标一优化：获取包下所有类名 包名"+packetName);
        return joernService.showClassName(packetName);
    }

    
    @GetMapping("/showMethodName")
    @Operation(summary = "目标一优化：获取类下所有方法名", description = "根据前端传递过来的类名获取到该类下的所有方法名以及参数")
    public List<neo4jNode> showMethodName(@RequestParam("classFullName")String classFullName) {
        System.out.println("目标一优化：获取类下所有方法名 类名"+classFullName);
        return joernService.showMethodName(classFullName);
    }

    /**
     * @param methodFullName
     * @param methodCode
     * @return
     */
    
    @GetMapping("/showInvocationLink")
    @Operation(summary = "目标一优化：获取唯一方法的调用链路", description = "根据前端传递过来的类名以及方法名及其参数获取到该唯一方法的调用链路")
    public List<neo4jPath> showMethodName(@RequestParam("methodFullName")String methodFullName, @RequestParam("methodCode")String methodCode,@RequestParam("isDown")String isDown) {
        System.out.println("目标一优化：获取唯一方法的调用链路 类名"+methodFullName);
        System.out.println("目标一优化：获取唯一方法的调用链路 方法名"+methodCode);
        System.out.println("目标一优化：获取唯一方法的调用链路 isDown"+isDown);
        return joernService.showInvocationLink(methodFullName, methodCode,Boolean.valueOf(isDown));
    }

    /**
     * 获取热点节点
     * @param packetName
     * @param maxNumber
     * @return
     */
    
    @GetMapping("/getHotNode")
    @Operation(summary = "目标四 获取热点节点", description = "需要包名以及top maxNumber个热点节点")
    public List<neo4jHotNode> getHotNode(@RequestParam("packetName")String packetName, @RequestParam("maxNumber")String maxNumber) {
        System.out.println("目标四：获取热点节点 包名"+packetName);
        System.out.println("目标四：获取热点节点 节点数"+maxNumber);
        List<neo4jHotNode> ans=joernService.getHotNode(packetName,maxNumber);
        return ans;
    }

    @GetMapping("/getSimilar")
    @Operation(summary = "目标五 获取相似方法", description = "需要包名")
    public List<neo4jSimilarNode> getSimilar(@RequestParam("packetName")String packetName,@RequestParam("identify")String identify) {
        Double threshold=0.2;
        System.out.println("目标五：获取相似方法 包名"+packetName);
        List<neo4jSimilarNode> ans=joernService.getSimilar(packetName,identify,threshold);
        return ans;
    }

    @GetMapping("/getShortestPath")
    @Operation(summary = "目标六 获取最短路径", description = "需要方法代码")
    public List<neo4jPath> getShortestPath(@RequestParam("methodFullName")String methodFullName,@RequestParam("methodCode")String methodCode) {
        System.out.println("目标六：获取最短路径 方法全路径"+methodFullName);
        System.out.println("目标六：获取最短路径 方法代码"+methodCode);
        List<neo4jPath> ans=joernService.getShortestPath(methodFullName,methodCode);
        return ans;
    }

    @GetMapping("/getCollectionPath")
    @Operation(summary = "目标七 获取统一路径", description = "需要方法代码")
    public List<neo4jPath> getCollectionPath(@RequestBody List<neo4jNode> methodList) {
        List<String> list=new ArrayList<>();
        for(int i=0;i<methodList.size();i++){
            neo4jNode node=methodList.get(i);
            list.add(node.fullName+node.code);
        }
        return joernService.getCollectionPath(list);
    }

    public List<String> nodeToString(List<neo4jHotNode> path) {
        List<String> sbrList = new ArrayList<>();
        for (int i = 0; i < path.size(); i++) {
            neo4jHotNode r = path.get(i);
            System.out.println(r.number);
            String str=('('+r.node.label+')'+r.node.fullName+"     "+r.number);
            sbrList.add(str);
        }
        return sbrList;
    }
}
