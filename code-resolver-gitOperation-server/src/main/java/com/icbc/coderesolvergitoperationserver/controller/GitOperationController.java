package com.icbc.coderesolvergitoperationserver.controller;

import com.icbc.codeResolver.entity.Result;
import com.icbc.codeResolver.entity.neo4jNode;
import com.icbc.codeResolver.service.GitOperationService;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/git")
public class GitOperationController {
    @Autowired
    private GitOperationService gitOperationService;

    @GetMapping("/pull")
    public Result pull(@RequestParam("remoteURI") String remoteURI,@RequestParam("branch") String branch) throws GitAPIException,IOException{
        List<neo4jNode> neo4jNodes = gitOperationService.connectionAndPull(remoteURI, branch);
        return Result.success(Result.success(neo4jNodes));
    }
}
