package com.zjyz.designer.service;


import com.zjyz.designer.model.FieldModel;
import com.zjyz.designer.model.QueryModel;
import com.zjyz.designer.model.TableModel;

import java.util.List;


public interface MysqlService {


    /**
     * 查询指定数据源的数据库列表
     * @param connectKey
     * @return
     */
    List<String> getDbList(String connectKey) ;


    /**
     * 查询指定数据库的表列表
     * @param connectKey
     * @param dbName
     * @return
     */
    List<TableModel> getTableList(String connectKey, String dbName);

    /**
     * 异步查询指定数据库的表列表
     * @param connectKey
     * @param dbName
     * @return
     */
    List<TableModel> getTableListAsync(String connectKey, String dbName);

    /**
     * 更新表字段结构
     * @param queryModel
     * @return
     */
    Integer updateTableColumns(QueryModel queryModel) throws Exception;

    /**
     * 获取表名称列表
     * @param connectKey
     * @param dbName
     * @return
     */
    List<String> getTableNameList(String connectKey, String dbName);

    /**
     * 获取指定表的列信息
     * @param connectKey
     * @param dbName
     * @param targetName
     * @return
     */
    List<FieldModel> getTableColumnsList(String connectKey, String dbName, String targetName);
}
