package com.icbc.codeResolver.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class neo4jPre implements Serializable {

    neo4jNode node;
    List<String> prePathList;//到达neo4jNode节点前的String路径。

    public neo4jPre(neo4jNode node, List<String> prePathList) {
        this.node = node;
        this.prePathList = prePathList;
    }
}
