package com.icbc.codeResolver.controller;

import com.icbc.codeResolver.entity.neo4jHotNode;
import com.icbc.codeResolver.entity.neo4jNode;
import com.icbc.codeResolver.entity.neo4jPath;
import com.icbc.codeResolver.service.CodeResolverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
/**
 * 分布式调用controller
 */
@RestController
@RequestMapping(value = "/resolver")
@Tag(name = "DubboJoern", description = "Dubbojoern接口")
public class JoernController {

    //日志
    private static Logger logger = Logger.getLogger(JoernController.class);

    @DubboReference(group = "joern")
    CodeResolverService joernService;

    /**
     * 方法追踪 类名 方法名
     * @return
     */
    @GetMapping("/methodDown")
    @Operation(summary = "目标一：方法追踪", description = "根据类名以及方法名进行方法追踪")
    public List<neo4jPath> getMethodNodeDown(@RequestParam("className") String className,@RequestParam("methodName") String methodName) {
        System.out.println("目标一：方法追踪 className"+className);
        System.out.println("目标一：方法追踪 methodName"+methodName);
        return joernService.getMethodDown(className+".java",methodName);
    }

    /**
     * 方法溯源
     * @return
     */
    @GetMapping("/methodUp")
    @Operation(summary = "目标一：方法溯源", description = "根据类名以及方法名进行方法溯源")
    public List<neo4jPath> getMethodNodeUp(@RequestParam("className") String className,@RequestParam("methodName") String methodName) {
        System.out.println("目标一：方法溯源 className"+className);
        System.out.println("目标一：方法溯源 methodName"+methodName);
        return joernService.getMethodUp(className+".java",methodName);
    }


    /**
     * url精确查找 url-》斜杠分割 List<String>
     * @return
     */
    @GetMapping("/urlPath")

    @Operation(summary = "目标二：url查找", description = "url的形式为/*/*/*")
    public List<neo4jPath> getUrlPath(@RequestParam("url") String url) {
        logger.info("目标二：url查找");
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
        logger.info("目标三：数据库表字段关系 数据库名"+dataBaseName);
        logger.info("目标三：数据库表字段关系 表名"+tableName);
        logger.info("目标三：数据库表字段关系 字段名查询"+fieldName);
        return joernService.getDataBaseInfo(dataBaseName,tableName,fieldName);
    }


    @GetMapping("/showClassName")
    @Operation(summary = "目标一优化：获取包下所有类名", description = "根据前端传递过来的包名获取到该包下的所有类名")
    public List<neo4jNode> showClassName(@RequestParam("packetName")String packetName) {
        logger.info("目标一优化：获取包下所有类名 包名"+packetName);
        return joernService.showClassName(packetName);
    }


    @GetMapping("/showMethodName")
    @Operation(summary = "目标一优化：获取类下所有方法名", description = "根据前端传递过来的类名获取到该类下的所有方法名以及参数")
    public List<neo4jNode> showMethodName(@RequestParam("classFullName")String classFullName) {
        logger.info("目标一优化：获取类下所有方法名 类名"+classFullName);
        return joernService.showMethodName(classFullName);
    }

    /**
     * @param methodFullName
     * @return
     */

    @GetMapping("/showInvocationLink")
    @Operation(summary = "目标一优化：获取唯一方法的调用链路", description = "根据前端传递过来的类名以及方法名及其参数获取到该唯一方法的调用链路")
    public List<neo4jPath> showInvocationLink(@RequestParam("methodFullName")String methodFullName,@RequestParam("isDown")String isDown) {
        logger.info("目标一优化：获取唯一方法的调用链路 类名"+methodFullName);
        logger.info("目标一优化：获取唯一方法的调用链路 isDown"+isDown);
        return joernService.showInvocationLink(methodFullName,Boolean.valueOf(isDown));
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
        logger.info("目标四：获取热点节点 包名"+packetName);
        logger.info("目标四：获取热点节点 节点数"+maxNumber);
        List<neo4jHotNode> ans=joernService.getHotNode(packetName,maxNumber);
        return ans;
    }

    @GetMapping("/getSimilar")
    @Operation(summary = "目标五 获取相似方法", description = "需要包名")
    public List<neo4jSimilarNode> getSimilar(@RequestParam("packetName")String packetName, @RequestParam("identify")String identify) {
        Double threshold=0.2;
        logger.info("目标五：获取相似方法 包名"+packetName);
        List<neo4jSimilarNode> ans=joernService.getSimilar(packetName,identify,threshold);
        return ans;
    }

    @GetMapping("/getShortestPath")
    @Operation(summary = "目标六 获取最短路径", description = "需要方法代码")
    public List<neo4jPath> getShortestPath(@RequestParam("methodFullName")String methodFullName) {
        logger.info("目标六：获取最短路径 方法全路径"+methodFullName);
        List<neo4jPath> ans=joernService.getShortestPath(methodFullName);
        return ans;
    }

    @GetMapping("/getCollectionPath")
    @Operation(summary = "目标七 获取统一路径", description = "需要方法代码")
    public List<neo4jPath> getCollectionPath(@RequestBody List<String> methodList) {
        logger.info("目标七 获取统一路径 方法列表"+methodList);
        return joernService.getCollectionPath(methodList);
    }



}
