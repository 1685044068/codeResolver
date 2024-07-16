package com.icbc.codeResolver.controller;

import com.icbc.codeResolver.service.CodeResolverService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/joern")
public class JoernController {
    @DubboReference(group = "joern")
    CodeResolverService joernService;

    /**
     * 方法追踪
     * @return
     */
    @RequestMapping("/methodDown/{method}")
    @ResponseBody
    public List<String> getMethodNodeDown(@PathVariable("method") String methodName) {
        List<String> sbrList = joernService.getMethodDown(methodName);
        return sbrList;
    }

    /**
     * 方法溯源
     * @return
     */
    @RequestMapping("/methodUp/{method}")
    @ResponseBody
    public List<String> getMethodNodeUp(@PathVariable("method") String methodName) {
        List<String> sbrList = joernService.getMethodUp(methodName);
        return sbrList;
    }

    /**
     * 类追踪
     * @return
     */
    @RequestMapping("/classDown/{class}")
    @ResponseBody
    public List<String> getClassNodeDown(@PathVariable("class") String className) {
        List<String> sbrList = joernService.getClassDown(className);
        return sbrList;
    }

    /**
     * 类溯源
     * @return
     */
    @RequestMapping("/classUp/{class}")
    @ResponseBody
    public List<String> getClassNodeUp(@PathVariable("class") String className) {
        List<String> sbrList = joernService.getClassUp(className);
        return sbrList;
    }

    /**
     * url精确查找
     * @return
     */
    @RequestMapping("/urlPath/{className}/{methodName}")
    @ResponseBody
    public List<String> getUrlPath(@PathVariable("className") String className,@PathVariable("methodName") String methodName) {
        List<String> url = new ArrayList<>();
        url.add(className);
        url.add(methodName);
        List<String> sbrList = joernService.getUrlPath(url);
        return sbrList;
    }


    @RequestMapping("/methodAll")
    @ResponseBody
    public List<String> getMethodNodeAll() {
        return joernService.getAllMethodRelation();
    }
}
