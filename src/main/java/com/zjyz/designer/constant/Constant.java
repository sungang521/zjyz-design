package com.zjyz.designer.constant;

public class Constant {

    /**
     * 默认测试sql
     */
    public static final  String SQL_TEST="select 1;";
    /**
     * 默认查询数据库列表sql
     */
    public static final  String SQL_SHOW_DATABASES="show databases;";
    /**
     * 默认查询数据库中所有表的sql
     */
    public static final String SQL_SHOW_TABLES="show tables from `%s`;";
    /**
     * 默认查询数据库中所有表和备注信息的sql
     */
    public static final String SQL_SHOW_TABLES_ALL="select TABLE_NAME,`ENGINE`,TABLE_COMMENT from information_schema.TABLES where TABLE_SCHEMA='%s';";
    /**
     * 查询表字段信息sql
     */
    public static final String SQL_SHOW_COLUMNS="SELECT COLUMN_NAME,COLUMN_TYPE,COLUMN_KEY,COLUMN_COMMENT,IS_NULLABLE,COLUMN_DEFAULT  FROM information_schema.Columns  where TABLE_SCHEMA='%s'  and  TABLE_NAME='%s' order by ORDINAL_POSITION ";
    /**
     * 查询表字段信息sql
     */
    public static final String SQL_SHOW_COLUMNS_ALL="SELECT TABLE_NAME,COLUMN_NAME,COLUMN_TYPE,COLUMN_KEY,COLUMN_COMMENT,IS_NULLABLE,COLUMN_DEFAULT  FROM information_schema.Columns  where TABLE_SCHEMA='%s'  order by TABLE_NAME,ORDINAL_POSITION ";

    /**
     * 查询表主外键关系
     */
    public static final String SQL_SHOW_CONSTRAINT="select  COLUMN_NAME,POSITION_IN_UNIQUE_CONSTRAINT,REFERENCED_TABLE_SCHEMA,REFERENCED_TABLE_NAME,REFERENCED_COLUMN_NAME from INFORMATION_SCHEMA.KEY_COLUMN_USAGE  where CONSTRAINT_SCHEMA ='%s' AND TABLE_NAME = '%s'";

    /**
     * 查询全部表主外键关系
     */
    public static final String SQL_SHOW_CONSTRAINT_ALL="select  TABLE_NAME,COLUMN_NAME,POSITION_IN_UNIQUE_CONSTRAINT,REFERENCED_TABLE_SCHEMA,REFERENCED_TABLE_NAME,REFERENCED_COLUMN_NAME from INFORMATION_SCHEMA.KEY_COLUMN_USAGE  where CONSTRAINT_SCHEMA ='%s' ";

    /**
     * 修改表字段
     */
    public static final String SQL_UPDATE_COLUMNS="alter table %s change  column %s %s %s default %s %s  COMMENT '%s';";


    /**
     * 修改主键索引
     */
    public static final String SQL_UPDATE_PRIMARY="alter table %s DROP  PRIMARY  KEY, add primary key (%s);";


    /**
     * 修改主键索引
     */
    public static final String SQL_UPDATE_CONSTRAINT="alter table %s add constraint %s foreign key(%s) REFERENCES %s(%s);";





    /**
     * 默认数据库列名称
     */
    public static final String FIELD_DATABASE="Database";

    /**
     * 默认表列名称
     */
    public static final String FIELD_TABKE="Tables_in_%s";


    /**
     * 用来判断是否允许为空的默认值
     */
    public  static final String FIELD_COLUMNS_YES="YES";
    /**
     * 用来判断是否允许为空的默认值
     */
    public  static final String FIELD_COLUMNS_NO="NO";
    /**
     * 用来判断是否允许为空的默认值
     */
    public  static final String FIELD_COLUMNS_PRI_KEY="PRI";




    /**
     * 用来判断默认值是否为空
     */
    public  static final String FIELD_COLUMNS_NULL="NULL";

    /**
     * 默认值为空
     */
    public  static final String FIELD_COLUMNS_DEFAULT_NULL="NULL";

    /**
     * 默认不为空
     */
    public  static final String FIELD_COLUMNS_DEFAULT_NOT_NULL="NOT NULL";



    /**
     * 点击几次触发表查询操作,从0开始计算，1表示双击
     */
    public  static final Integer SHOW_TABLE_NAX_NUMBER=1;

    /**
     * 点击事件初始化数值
     */
    public  static final Integer SHOW_TABLE_NUMBER=0;





    /**
     * 间隔距离
     */
    public  static final Integer DEFAULT_GAP=150;

    /**
     * 表之间的横向间隔
     */
    public  static final Integer DEFAULT_TABLE_GAP=450;
    /**
     * 表的默认宽度
     */
    public  static final Integer DEFAULT_TABLE_WIDTH=320;
    /**
     * 表的默认高度
     */
    public  static final Integer DEFAULT_TABLE_HEIGHT=450;
    /**
     * 默认行高
     */
    public  static final Integer DEFAULT_TABLE_LINE_HEIGHT=30;

    /**
     * 最大随机数间隙
     */
    public  static final Integer DRAW_MAX_GAP=100;

    /**
     * 最小随机数间隙
     */
    public  static final Integer DRAW_MIN_GAP=5;

    /**
     * 普通表每行显示最大的表的数量
     */
    public  static final Integer DEFAULT_TABLE_COLUMN_NUMBER=5;


}
