package com.icbc.codeResolver.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class neo4jHotNode implements Serializable {

    public neo4jNode node;
    public Long number;

    public neo4jHotNode(neo4jNode node, Long number) {
        this.node = node;
        this.number = number;
    }
}
