package com.icbc.codeResolver.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class neo4jAst implements Serializable {
    List<neo4jPre> nodeList;//调用了neo4jPre节点的节点list
    List<String> memberList;//nodeList调用neo4jPre节点需要的参数

    public neo4jAst(List<neo4jPre> nodeList, List<String> memberList) {
        this.nodeList = nodeList;
        this.memberList = memberList;
    }
}
