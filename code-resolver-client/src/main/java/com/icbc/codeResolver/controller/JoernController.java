package com.icbc.codeResolver.controller;

import com.icbc.codeResolver.service.CodeResolverService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/joern")
public class JoernController {
    @DubboReference(group = "joern")
    CodeResolverService joernService;
    @RequestMapping("/methodDown/{method}")
    @ResponseBody
    public List<String> getMethodNodeDown(@PathVariable("method")  String method) {
        System.out.println("method: " + method);
        return joernService.getMethodDown(method);
    }
    @RequestMapping("/methodUp/{method}")
    @ResponseBody
    public List<String> getMethodNodeUp(@PathVariable("method")  String method) {
        System.out.println("method: " + method);
        return joernService.getMethodUp(method);
    }

    @RequestMapping("/methodAll")
    @ResponseBody
    public List<String> getMethodNodeAll() {
        return joernService.getAllMethodRelation();
    }
}
