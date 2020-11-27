package com.zjyz.designer.helper;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import com.zjyz.designer.model.FieldModel;
import com.zjyz.designer.model.TableModel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class HtmlExportHelper implements IExport<String> {
    private static final String EMPTY = "";
    private static final Integer CYCLIC = 3;

    @Override
    public String coverModel(List<TableModel> tableModelList) {
        if (CollectionUtil.isNotEmpty(tableModelList)) {
            String htmlStr = FileUtil.readString("template/default.html", "utf-8");
            Document doc = Jsoup.parse(htmlStr);
            Element container = doc.body().getElementById("container");
            Element outCyclic = container.appendElement("table");
            outCyclic.attr("cellspacing", "150");
            Element tr = null;
            for (int i = 0; i < tableModelList.size(); i++) {
                if (i % CYCLIC == 0) {
                    tr = outCyclic.appendElement("tr");
                }
                tr.appendElement("td").appendChild(unitTable(tableModelList.get(i)));
            }
            return doc.html();
        }
        return EMPTY;
    }

    /**
     * 单个table生成的html
     *
     * @param tableModel
     * @return
     */
    private Element unitTable(TableModel tableModel) {
        Element element = new Element("table");
        element.attr("class", "altrowstable");
        //设置表名
        element.appendElement("tr").appendElement("td").attr("colspan", "6").attr("style", "text-align:center").text(tableModel.getTableName());
        //设置列说明
        Element tr = element.appendElement("tr");
        tr.appendElement("th").text("字段名");
        tr.appendElement("th").text("字段类型");
        tr.appendElement("th").text("约束");
        tr.appendElement("th").text("是否为空");
        tr.appendElement("th").text("注释");
        tr.appendElement("th").text("默认值");
        //遍历每一个字段
        if (CollectionUtil.isNotEmpty(tableModel.getFields())) {
            for (FieldModel fieldModel : tableModel.getFields()) {
                Element row = element.appendElement("tr");
                row.appendElement("td").text(fieldModel.getColumnName());
                row.appendElement("td").text(fieldModel.getColumnType());
                row.appendElement("td").text(fieldModel.getColumnKey());
                row.appendElement("td").text(fieldModel.getIsNullable() == null ? EMPTY : fieldModel.getIsNullable());
                row.appendElement("td").text(fieldModel.getColumnComment() == null ? EMPTY : fieldModel.getColumnComment());
                row.appendElement("td").text(fieldModel.getColumnDefault() == null ? EMPTY : fieldModel.getColumnDefault());
            }
        }

        return element;
    }

    public static void main(String[] args) {
        HtmlExportHelper htmlExportHelper = new HtmlExportHelper();
        List<TableModel> tables = new ArrayList<>();
        List<FieldModel> fieldModels = new ArrayList<>();

        TableModel tableModel = new TableModel();
        tableModel.setFields(fieldModels);
        tableModel.setTableName("sungang");
        FieldModel fieldModel = new FieldModel();
        fieldModel.setColumnName("id");
        fieldModel.setColumnType("int");
        fieldModel.setColumnComment("test");
        fieldModel.setColumnKey("id");
        fieldModels.add(fieldModel);
        tables.add(tableModel);

        TableModel tableMode2 = new TableModel();
        tableMode2.setFields(fieldModels);
        tableMode2.setTableName("sungang1");
        FieldModel fieldModel2 = new FieldModel();
        fieldModel2.setColumnName("id");
        fieldModel2.setColumnType("int");
        fieldModel2.setColumnComment("test11");
        fieldModel2.setColumnKey("id");
        fieldModels.add(fieldModel2);
        tables.add(tableMode2);

        TableModel tableMode3 = new TableModel();
        tableMode3.setFields(fieldModels);
        tableMode3.setTableName("sungang2");
        tables.add(tableMode3);

        TableModel tableMode4 = new TableModel();
        tableMode4.setFields(fieldModels);
        tableMode4.setTableName("sungang2");
        tables.add(tableMode4);

        System.out.println(htmlExportHelper.coverModel(tables));
    }
}
