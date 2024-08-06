package com.icbc.codeResolver.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class neo4jAst implements Serializable {
    List<neo4jPre> nodeList;
    List<String> memberList;

    public neo4jAst(List<neo4jPre> nodeList, List<String> memberList) {
        this.nodeList = nodeList;
        this.memberList = memberList;
    }
}
