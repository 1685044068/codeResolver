package com.icbc.codeResolver.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class neo4jHotNode implements Serializable {

    public neo4jNode node;
    public Long number;

    public List<neo4jNode> followNode;

    public neo4jHotNode(neo4jNode node, Long number, List<neo4jNode> followNode) {
        this.node = node;
        this.number = number;
        this.followNode = followNode;
    }

    public neo4jHotNode(neo4jNode node, Long number) {
        this.node = node;
        this.number = number;
    }
}
