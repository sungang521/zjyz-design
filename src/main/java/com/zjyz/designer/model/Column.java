package com.zjyz.designer.model;

/**
 * 字段
 */
public class Column {
    private String tableName;
    private String id;
    /**
     * 字段名
     */
    private String name;
    private String code;
    /**
     * 字段类型
     */
    private String type;
    /**
     * 字段长度
     */
    private Integer length;
    /**
     * 是不是主键
     */
    private Boolean isPk;
    /**
     * 是不是外键
     */
    private Boolean isFk;
    /**
     * 是否为null
     */
    private Boolean isNull;
    /**
     * 注释
     */
    private String comment;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Boolean getPk() {
        return isPk;
    }

    public void setPk(Boolean pk) {
        isPk = pk;
    }

    public Boolean getFk() {
        return isFk;
    }

    public void setFk(Boolean fk) {
        isFk = fk;
    }

    public Boolean getNull() {
        return isNull;
    }

    public void setNull(Boolean aNull) {
        isNull = aNull;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
