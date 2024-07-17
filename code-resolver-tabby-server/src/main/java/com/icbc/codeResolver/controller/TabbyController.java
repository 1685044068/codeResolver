package com.icbc.codeResolver.controller;

import com.icbc.codeResolver.service.CodeResolverService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("/methodDown/{className}/{methodName}")
    @ResponseBody
    public List<String> getMethodNodeDown(@PathVariable("className") String className,@PathVariable("methodName") String methodName) {
        return tabbyService.getMethodDown(className,methodName);
    }

    /**
     * 方法溯源
     * @return
     */
    @GetMapping("/methodDown/{className}/{methodName}")
    @ResponseBody
    public List<String> getMethodNodeUp(@PathVariable("className") String className,@PathVariable("methodName") String methodName) {
        return tabbyService.getMethodUp(className,methodName);
    }


    /**
     * url精确查找
     * @return
     */
    @GetMapping("/urlPath/{url}")
    @ResponseBody
    public List<String> getUrlPath(@PathVariable("url") String url) {
        String[] urlField = url.split("/");
        List<String> sbrList = tabbyService.getUrlPath(new ArrayList<>(List.of(urlField)));
        return sbrList;
    }

}
