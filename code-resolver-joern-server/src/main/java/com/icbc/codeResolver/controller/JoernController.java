package com.icbc.codeResolver.controller;

import com.icbc.codeResolver.service.CodeResolverService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class JoernController {
    @DubboReference(group = "joern")
    CodeResolverService joernService;

    @RequestMapping("/joernMethodDown")
    @ResponseBody
    public List<String> getMethodNodeDown() {
        String methodName = "saveUser";
        List<String> sbrList = joernService.getMethodDown(methodName);
        return sbrList;
    }
    @RequestMapping("/joernMethodUp")
    @ResponseBody
    public List<String> getMethodNodeUp() {
        String methodName = "getAccessTokenExpire";
        List<String> sbrList = joernService.getMethodUp(methodName);
        return sbrList;
    }

    @RequestMapping("/joernMethodAll")
    @ResponseBody
    public List<String> getMethodNodeAll() {
        List<String> sbrList = joernService.getAllMethodRelation();
        return sbrList;
    }
}
