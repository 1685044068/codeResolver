package com.icbc.codeResolver.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class neo4jNode implements Serializable {

    public String label;
    public String name;
    public String fullName;
    public String code;
    public neo4jNode next;
    public neo4jNode pre;

    public neo4jNode() {

    }

    public neo4jNode(String label, String name,String fullName,String code) {
        this.label = label;
        this.name = name;
        this.code=code;
        this.fullName=fullName;
    }
    public neo4jNode(String label, String name) {
        this.label = label;
        this.name = name;
    }

    public neo4jNode(String label, String name, neo4jNode next) {
        this.label = label;
        this.name = name;
        this.next = next;
    }

}
