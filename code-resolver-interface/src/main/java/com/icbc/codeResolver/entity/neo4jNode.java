package com.icbc.codeResolver.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class neo4jNode implements Serializable {

    public String label;
    public String name;

    public String fullName;
    public String code;
    public String fileName;
    public neo4jNode next;

    public neo4jNode() {

    }


    public neo4jNode(String label, String name, String fullName, String code) {
        this.label = label;
        this.name = name;
        this.fullName = fullName;
        this.code = code;
    }

    public neo4jNode(String label, String name, String fullName, String code, String fileName) {
        this.label = label;
        this.name = name;
        this.fullName = fullName;
        this.code = code;
        this.fileName = fileName;
    }

}
