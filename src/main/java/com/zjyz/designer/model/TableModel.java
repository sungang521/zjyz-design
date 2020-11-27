package com.zjyz.designer.model;

import java.util.List;

public class TableModel {

    private String connectKey;
    private String dbName;
    private String tableName;
    private  String tableComment;;
    private List<FieldModel> fields;
    private List<ConstraintModel> constraint;

    public String getTableComment() {
        return tableComment;
    }

    public void setTableComment(String tableComment) {
        this.tableComment = tableComment;
    }

    public String getConnectKey() {
        return connectKey;
    }

    public void setConnectKey(String connectKey) {
        this.connectKey = connectKey;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<FieldModel> getFields() {
        return fields;
    }

    public void setFields(List<FieldModel> fields) {
        this.fields = fields;
    }

    public List<ConstraintModel> getConstraint() {
        return constraint;
    }

    public void setConstraint(List<ConstraintModel> constraint) {
        this.constraint = constraint;
    }
}
