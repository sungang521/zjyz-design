package com.zjyz.designer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConstraintModel {




    @JsonProperty("TABLE_NAME")
    private String tableName;
    @JsonProperty("COLUMN_NAME")
    private  String columnName;
    @JsonProperty("POSITION_IN_UNIQUE_CONSTRAINT")
    private String positionInUniqueConstratnt;
    @JsonProperty("REFERENCED_TABLE_SCHEMA")
    private String ReferencedTableSchena;
    @JsonProperty("REFERENCED_TABLE_NAME")
    private String ReferencedTableName;
    @JsonProperty("REFERENCED_COLUMN_NAME")
    private String ReferencedColumnName;


    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getPositionInUniqueConstratnt() {
        return positionInUniqueConstratnt;
    }

    public void setPositionInUniqueConstratnt(String positionInUniqueConstratnt) {
        this.positionInUniqueConstratnt = positionInUniqueConstratnt;
    }

    public String getReferencedTableSchena() {
        return ReferencedTableSchena;
    }

    public void setReferencedTableSchena(String referencedTableSchena) {
        ReferencedTableSchena = referencedTableSchena;
    }

    public String getReferencedTableName() {
        return ReferencedTableName;
    }

    public void setReferencedTableName(String referencedTableName) {
        ReferencedTableName = referencedTableName;
    }

    public String getReferencedColumnName() {
        return ReferencedColumnName;
    }

    public void setReferencedColumnName(String referencedColumnName) {
        ReferencedColumnName = referencedColumnName;
    }
}
