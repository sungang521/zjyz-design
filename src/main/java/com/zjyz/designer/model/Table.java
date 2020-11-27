package com.zjyz.designer.model;

import java.util.List;

/**
 * 表信息
 */
public class Table {
    private String id;
    /**
     * 表名
     */
    private String name;
    /**
     * 字段
     */
    private List<Column> columns;
    /**
     * 约束集合
     */
    private List<Reference> constraint;
    /**
     * 说明
     */
    private String comment;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public List<Reference> getConstraint() {
        return constraint;
    }

    public void setConstraint(List<Reference> constraint) {
        this.constraint = constraint;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
