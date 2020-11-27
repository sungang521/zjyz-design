package com.zjyz.designer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
@JsonIgnoreProperties(ignoreUnknown = true)
public class EditFieldModel {


    private String tableName;

    private String columnName;

    private String columnType;

    private CheckBox  keyCheckBox;

    private String columnComment;

    private CheckBox nullCheckBox;

    private String columnDefault;


    private String oldColumnName;
    private String oldColumnType;
    private String oldColumnDefault;
    private String oldColumnComment;
    private String oldNull;
    private String oldColumnKey;

    public String getOldColumnType() {
        return oldColumnType;
    }

    public void setOldColumnType(String oldColumnType) {
        this.oldColumnType = oldColumnType;
    }

    public String getOldColumnDefault() {
        return oldColumnDefault;
    }

    public void setOldColumnDefault(String oldColumnDefault) {
        this.oldColumnDefault = oldColumnDefault;
    }

    public String getOldColumnComment() {
        return oldColumnComment;
    }

    public void setOldColumnComment(String oldColumnComment) {
        this.oldColumnComment = oldColumnComment;
    }

    public String getOldNull() {
        return oldNull;
    }

    public void setOldNull(String oldNull) {
        this.oldNull = oldNull;
    }

    public String getOldColumnKey() {
        return oldColumnKey;
    }

    public void setOldColumnKey(String oldColumnKey) {
        this.oldColumnKey = oldColumnKey;
    }

    public String getOldColumnName() {
        return oldColumnName;
    }

    public void setOldColumnName(String oldColumnName) {
        this.oldColumnName = oldColumnName;
    }

    public ObservableValue<CheckBox> getKeyCheckBoxData()
    {
        return new  ObservableValue<CheckBox>() {
            @Override
            public void addListener(ChangeListener<? super CheckBox> listener) {

            }

            @Override
            public void removeListener(ChangeListener<? super CheckBox> listener) {

            }
            @Override
            public CheckBox getValue() {
                return keyCheckBox;
            }
            @Override
            public void addListener(InvalidationListener listener) {

            }
            @Override
            public void removeListener(InvalidationListener listener) {

            }
        };
    }


    public ObservableValue<CheckBox> getNullCheckBoxData()
    {
        return new  ObservableValue<CheckBox>() {
            @Override
            public void addListener(ChangeListener<? super CheckBox> listener) {

            }

            @Override
            public void removeListener(ChangeListener<? super CheckBox> listener) {

            }

            @Override
            public CheckBox getValue() {
                return nullCheckBox;
            }

            @Override
            public void addListener(InvalidationListener listener) {

            }

            @Override
            public void removeListener(InvalidationListener listener) {

            }
        };
    }


    public EditFieldModel() {
        keyCheckBox=new CheckBox();
        nullCheckBox=new CheckBox();
    }

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

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public CheckBox getKeyCheckBox() {
        return keyCheckBox;
    }

    public void setKeyCheckBox(CheckBox keyCheckBox) {
        this.keyCheckBox = keyCheckBox;
    }

    public String getColumnComment() {
        return columnComment;
    }

    public void setColumnComment(String columnComment) {
        this.columnComment = columnComment;
    }

    public CheckBox getNullCheckBox() {
        return nullCheckBox;
    }

    public void setNullCheckBox(CheckBox nullCheckBox) {
        this.nullCheckBox = nullCheckBox;
    }

    public String getColumnDefault() {
        return columnDefault;
    }

    public void setColumnDefault(String columnDefault) {
        this.columnDefault = columnDefault;
    }
}
