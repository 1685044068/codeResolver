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
@RequestMapping(value = "/tabby")
public class TabbyController {
    @DubboReference(group = "tabby")
    CodeResolverService tabbyService;

    /**
     * 方法追踪
     * @return
     */
    @RequestMapping("/methodDown/{method}")
    @ResponseBody
    public List<String> getMethodNodeDown(@PathVariable("method") String method) {
        return tabbyService.getMethodDown(method);
    }

    /**
     * 方法溯源
     * @return
     */
    @RequestMapping("/methodUp/{method}")
    @ResponseBody
    public List<String> getMethodNodeUp(@PathVariable("method") String method) {
        return tabbyService.getMethodUp(method);
    }

    /**
     * 类追踪
     * @return
     */
    @RequestMapping("/classDown/{class}")
    @ResponseBody
    public List<String> getClassNodeDown(@PathVariable("class") String className) {
        List<String> sbrList = tabbyService.getClassDown(className);
        return sbrList;
    }

    /**
     * 类溯源
     * @return
     */
    @RequestMapping("/classUp/{class}")
    @ResponseBody
    public List<String> getClassNodeUp(@PathVariable("class") String className) {
        List<String> sbrList = tabbyService.getClassUp(className);
        return sbrList;
    }

    /**
     * url精确查找
     * @return
     */
    @RequestMapping("/urlPath/{className}/{methodName}")
    @ResponseBody
    public List<String> getUrlPath(@PathVariable String className,@PathVariable String methodName) {
        List<String> url = new ArrayList<>();
        url.add(className);
        url.add(methodName);
        List<String> sbrList = tabbyService.getUrlPath(url);
        return sbrList;
    }



    @RequestMapping("/methodAll")
    @ResponseBody
    public List<String> getMethodNodeAll() {
        return tabbyService.getAllMethodRelation();
    }
}
