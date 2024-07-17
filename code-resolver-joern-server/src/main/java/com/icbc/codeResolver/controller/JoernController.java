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

    @RequestMapping("/urlPathUp/{className}/{methodName}")
    @ResponseBody
    public List<String> getUrlPathUp(@PathVariable("className") String className, @PathVariable("methodName") String methodName) {
        List<String> url = new ArrayList<>();
        url.add(className);
        url.add(methodName);
        List<String> sbrList = joernService.getUrlPathUp(url);
        return sbrList;
    }
    @RequestMapping("/urlPathDown/{className}/{methodName}")
    @ResponseBody
    public List<String> getUrlPathDown(@PathVariable("className") String className, @PathVariable("methodName") String methodName) {
        List<String> url = new ArrayList<>();
        url.add(className);
        url.add(methodName);
        List<String> sbrList = joernService.getUrlPathDown(url);
        return sbrList;
    }
    @RequestMapping("/urlPathAbstract/{className}/{methodName}")
    @ResponseBody
    public List<String> getUrlPathAbstract(@PathVariable("className") String className, @PathVariable("methodName") String methodName) {
        List<String> url = new ArrayList<>();
        url.add(className);
        url.add(methodName);
        List<String> sbrList = joernService.getUrlPathAbstract(url);
        return sbrList;
    }

    @RequestMapping("/urlPathDetailDown/{className}/{methodName}/{code}")
    @ResponseBody
    public List<String> getUrlPathDetailDown(@PathVariable("className") String className, @PathVariable("methodName") String methodName,@PathVariable("code") String code) {
        List<String> url = new ArrayList<>();
        url.add(className);
        url.add(methodName);
        url.add(code);
        List<String> sbrList = joernService.getUrlPathDetailDown(url);
        return sbrList;
    }
    @RequestMapping("/SqlMember/{member}")
    @ResponseBody
    public List<String> getUrlPath(@PathVariable("member") String member) {
        List<String> sbrList = joernService.getSqlMember(member);
        return sbrList;
    }


}
