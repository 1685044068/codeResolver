package com.icbc.codeResolver.service;

import com.icbc.codeResolver.entity.Result;
import com.icbc.codeResolver.entity.neo4jNode;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface GitOperationService {
    List<neo4jNode> connectionAndPull(String remoteURI, String branch) throws GitAPIException,IOException;
}
