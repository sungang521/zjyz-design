package com.zjyz.designer.helper;

import com.zjyz.designer.model.TableModel;

import java.util.List;

/**
 *
 */
public interface IExport<T> {
    /**
     * 将table转化成指定的格式导出
     * @param tableModelList
     * @return
     */
    T coverModel(List<TableModel> tableModelList);
}
