package com.icbc.codeResolver.controller;

import com.icbc.codeResolver.entity.Result;
import com.icbc.codeResolver.entity.neo4jNode;
import com.icbc.codeResolver.service.GitOperationService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

//@RestController
@RequestMapping("/git")
public class GitOperationController {
    @DubboReference(group = "git")
    private GitOperationService gitOperationService;
    @GetMapping("/pull")
    public Result pull(@RequestParam("remoteURI") String remoteURI, @RequestParam("branch") String branch) throws GitAPIException,IOException{
        List<neo4jNode> res = gitOperationService.connectionAndPull(remoteURI, branch);
        return Result.success(res);
    }
}
