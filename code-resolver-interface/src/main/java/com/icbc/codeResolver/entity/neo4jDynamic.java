package com.icbc.codeResolver.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class neo4jDynamic implements Serializable {
    neo4jPre neo4jpre;
    neo4jAst neo4jast;

    public neo4jDynamic(neo4jPre neo4jpre, neo4jAst neo4jast) {
        this.neo4jpre = neo4jpre;
        this.neo4jast = neo4jast;
    }
}
