package com.icbc.codeResolver.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class MethodNode implements Serializable {

    public String label;
    public String name;
    public MethodNode next;
    public MethodNode pre;

    public MethodNode() {

    }

    public MethodNode(String label, String name) {
        this.label = label;
        this.name = name;
    }

    public MethodNode(String label, String name, MethodNode next) {
        this.label = label;
        this.name = name;
        this.next = next;
    }

}
