package com.icbc.codeResolver.controller;

import com.icbc.codeResolver.service.CodeResolverService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/tabby")
public class TabbyController {
    @DubboReference(group = "tabby")
    CodeResolverService tabbyService;


    @RequestMapping("/methodDown/{method}")
    @ResponseBody
    public List<String> getMethodNodeDown(@PathVariable("method") String method) {
        return tabbyService.getMethodDown(method);
    }
    @RequestMapping("/methodUp/{method}")
    @ResponseBody
    public List<String> getMethodNodeUp(@PathVariable("method") String method) {
        return tabbyService.getMethodUp(method);
    }

    @RequestMapping("/methodAll")
    @ResponseBody
    public List<String> getMethodNodeAll() {
        return tabbyService.getAllMethodRelation();
    }
}
