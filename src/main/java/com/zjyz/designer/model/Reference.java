package com.zjyz.designer.model;

/**
 * 主要用于外键，标识两表之间的连线
 */
public class Reference {
    /**
     * 连线的起始位置
     */
    private String sourceTable;
    /**
     * 箭头连接的位置
     */
    private String targetTable;
    private String column;
    private String referenceColumn;

    public String getSourceTable() {
        return sourceTable;
    }

    public void setSourceTable(String sourceTable) {
        this.sourceTable = sourceTable;
    }

    public String getTargetTable() {
        return targetTable;
    }

    public void setTargetTable(String targetTable) {
        this.targetTable = targetTable;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getReferenceColumn() {
        return referenceColumn;
    }

    public void setReferenceColumn(String referenceColumn) {
        this.referenceColumn = referenceColumn;
    }
}
