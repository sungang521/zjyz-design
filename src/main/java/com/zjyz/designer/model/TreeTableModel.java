package com.zjyz.designer.model;

import java.util.List;

public class TreeTableModel {

    private  String name;
    private List<String> relation;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getRelation() {
        return relation;
    }

    public void setRelation(List<String> relation) {
        this.relation = relation;
    }
}
