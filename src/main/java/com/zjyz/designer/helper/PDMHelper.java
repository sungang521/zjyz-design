package com.zjyz.designer.helper;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.UUID;
import com.zjyz.designer.model.*;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


/**
 * pdm文件读取和生成的辅助类
 */
@Component
public class PDMHelper implements IExport<String>{
    /**
     * 将解析出来的适配成界面的model view
     *
     * @param file
     * @return
     */
    public List<TableModel> getTableList(File file) {
        List<Table> tables = pdmConvertTables(file);
        List<TableModel> tableModels = new ArrayList<>();
        for (Table table : tables) {
            TableModel tableModel = new TableModel();
            tableModel.setTableName(table.getName());
            if (CollectionUtil.isNotEmpty(table.getColumns())) {
                List<FieldModel> fields = new ArrayList<>();
                tableModel.setFields(fields);
                for (Column column : table.getColumns()) {
                    FieldModel fieldModel = new FieldModel();
                    if(column.getPk()!=null&& column.getPk()){
                        fieldModel.setColumnKey("pk");
                    }
                    if(column.getFk()!=null&& column.getFk()){
                        fieldModel.setColumnKey("fk");
                    }
                    fieldModel.setColumnComment(column.getComment());
                    fieldModel.setColumnType(column.getType());
                    fieldModel.setColumnName(column.getName());
                    fieldModel.setIsNullable(column.getNull()+"");
                    fields.add(fieldModel);
                }
            }
            if (CollectionUtil.isNotEmpty(table.getConstraint())) {
                List<ConstraintModel> constraintModels = new ArrayList<>();
                tableModel.setConstraint(constraintModels);
                for (Reference reference : table.getConstraint()) {
                    ConstraintModel constraintModel = new ConstraintModel();
                    constraintModel.setColumnName(reference.getColumn());
                    constraintModel.setReferencedColumnName(reference.getReferenceColumn());
                    constraintModel.setReferencedTableName(reference.getTargetTable());
                    constraintModels.add(constraintModel);
                }
            }

            tableModels.add(tableModel);
        }
        return tableModels;
    }

    /**
     * 将指定的pdm文件转化成表结构
     *
     * @param file
     * @return
     */
    private List<Table> pdmConvertTables(File file) {
        Map<String, String> idMap = new HashMap<>();
        List<Table> tables = new ArrayList<>();
        SAXReader saxReader = new SAXReader();
        Document document = null;
        try {
            document = saxReader.read(file);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        Element rootElement = document.getRootElement();
        Namespace oNamespace = new Namespace("o", "object");
        Namespace cNamespace = new Namespace("c", "collection");
        Namespace aNamespace = new Namespace("a", "attribute");
        Element rootObject = rootElement.element(new QName("RootObject", oNamespace));
        Element children = rootObject.element(new QName("Children", cNamespace));
        Element model = children.element(new QName("Model", oNamespace));
        List<Element> tableEles = new ArrayList<>();
        //解析外键
        List<String> fkIds = new ArrayList<>();
        List<Reference> tempReference = new ArrayList<>();
        Element references = model.element(new QName("References", cNamespace));
        if (references != null) {
            List<Element> reference = references.elements(new QName("Reference", oNamespace));
            for (Element element : reference) {
                Reference constraintModel = new Reference();
                Element parentTable = element.element(new QName("ParentTable", cNamespace)).element(new QName("Table", oNamespace));
                Attribute parentTableValue = parentTable.attribute("Ref");
                Element childTable = element.element(new QName("ChildTable", cNamespace)).element(new QName("Table", oNamespace));
                Attribute childTableValue = childTable.attribute("Ref");
                constraintModel.setSourceTable(childTableValue.getValue());
                constraintModel.setTargetTable(parentTableValue.getValue());
                Element joins = element.element(new QName("Joins", cNamespace));
                if (joins != null) {
                    List<Element> ReferenceJoins = joins.elements(new QName("ReferenceJoin", oNamespace));
                    if (ReferenceJoins != null && ReferenceJoins.size() != 0) {
                        for (Element element1 : ReferenceJoins) {
                            Element element11 = element1.element(new QName("Object1", cNamespace)).element(new QName("Column", oNamespace));
                            Attribute ref1 = element11.attribute("Ref");
                            Element element2 = element1.element(new QName("Object2", cNamespace));
                            if(element2!=null){
                                Attribute ref = element2 .element(new QName("Column", oNamespace)).attribute("Ref");
                                fkIds.add(ref.getValue());
                                constraintModel.setColumn(ref.getValue());
                                constraintModel.setReferenceColumn(ref1.getValue());
                            }else {
                                fkIds.add(ref1.getValue());
                                constraintModel.setColumn(ref1.getValue());
                            }



                            constraintModel.setReferenceColumn(ref1.getValue());

                            // model.element(new QName("Tables", cNamespace)).element(new QName("Tables", cNamespace))
                        }
                    }
                }
                tempReference.add(constraintModel);
            }
        }
        //解析package
        Element packagesEle = model.element(new QName("Packages", cNamespace));
        if (packagesEle != null) {
            List<Element> packageEles = packagesEle.elements(new QName("Package", oNamespace));
            for (Element packageEle : packageEles) {
                Element tablesEle = packageEle.element(new QName("Tables", cNamespace));
                if (tablesEle != null) {
                    tableEles.addAll(tablesEle.elements(new QName("Table", oNamespace)));
                }
            }
        }
        //直接解析table
        Element tablesEle = model.element(new QName("Tables", cNamespace));
        if (tablesEle != null) {
            tableEles.addAll(tablesEle.elements(new QName("Table", oNamespace)));
        }

        List<Column> allCol = new ArrayList<>();
        for (Element tableElement : tableEles) {
            Table table = new Table();
            Attribute id1 = tableElement.attribute(new QName("Id"));
            table.setId(id1.getValue());
            Element name = tableElement.element(new QName("Name", aNamespace));
            table.setName(name.getText());
            //解析主键
            Element primaryKeyEle = tableElement.element(new QName("PrimaryKey", cNamespace));
            List<String> pkIds = new ArrayList<>();
            if (primaryKeyEle != null) {
                List<Element> pks = primaryKeyEle.elements(new QName("Key", oNamespace));
                for (Element pk1 : pks) {
                    pkIds.add(pk1.attribute("Ref").getValue());
                }
            }
            Element keysEle = tableElement.element(new QName("Keys", cNamespace));
            List<String> pkColumnIds = new ArrayList<>();
            if (keysEle != null) {
                List<Element> keyEleList = keysEle.elements(new QName("Key", oNamespace));
                for (Element keyEle : keyEleList) {
                    Attribute id = keyEle.attribute("Id");
                    if (pkIds.contains(id.getValue())) {
                        List<Element> list = keyEle.element(new QName("Key.Columns", cNamespace)).elements(new QName("Column", oNamespace));
                        for (Element element : list) {
                            pkColumnIds.add(element.attribute("Ref").getValue());
                        }
                    }
                }
            }
            //解析column
            Element columns2 = tableElement.element(new QName("Columns", cNamespace));
            List<Element> columns =null;
            if (columns2==null){
                columns = new ArrayList<>();
            }else {
                columns = columns2.elements(new QName("Column", oNamespace));
            }

            List<Column> columns1 = new ArrayList<>();
            for (Element columnEle : columns) {
                String columnId = columnEle.attribute("Id").getValue();
                Element cname = columnEle.element(new QName("Name", aNamespace));
                Element code = columnEle.element(new QName("Code", aNamespace));
                Element cDataType = columnEle.element(new QName("DataType", aNamespace));
                Element cLength = columnEle.element(new QName("Length", aNamespace));
                Element cComment = columnEle.element(new QName("Comment", aNamespace));
                Element nullable = columnEle.element(new QName("Column.Mandatory", aNamespace));
                Column column = new Column();
                column.setTableName(table.getName());
                column.setId(columnId);
                column.setCode(code == null ? null : code.getText());
                column.setName(cname == null ? null : cname.getText());
                column.setType(cDataType == null ? null : cDataType.getText());
                column.setLength(cLength == null ? null : Integer.parseInt(cLength.getText()));
                column.setComment(cComment == null ? null : cComment.getText());
                column.setNull(nullable == null);
                column.setPk(pkColumnIds.contains(columnId));
                column.setFk(fkIds.contains(columnId));
                columns1.add(column);
                allCol.add(column);
            }

            table.setColumns(columns1);
            // table.setConstraint(unitCon);
            tables.add(table);

        }

        Map<String, String> collect = allCol.stream().collect(Collectors.toMap(Column::getId, Column::getName));
        Map<String, String> collectTable = tables.stream().collect(Collectors.toMap(Table::getId, Table::getName));

        for (Reference reference : tempReference) {
            reference.setColumn(collect.get(reference.getColumn()));
            reference.setReferenceColumn(collect.get(reference.getReferenceColumn()));
            reference.setTargetTable(collectTable.get(reference.getTargetTable()));
            reference.setSourceTable(collectTable.get(reference.getSourceTable()));
        }

        for (Table table : tables) {
            for (Reference reference : tempReference) {
                if (table.getName().equals(reference.getSourceTable())) {
                    if (table.getConstraint() == null) {
                        table.setConstraint(new ArrayList<>());
                        table.getConstraint().add(reference);
                    } else {
                        table.getConstraint().add(reference);
                    }
                }
            }
        }
        return tables;
    }



    public static void main(String[] args) {
        PDMHelper pdmHelper = new PDMHelper();
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

        System.out.println(pdmHelper.coverModel(tables));
    }

    @Override
    public String coverModel(List<TableModel> tableModels) {
        SAXReader saxReader = new SAXReader();
        Document document = null;
        try {
            document = saxReader.read("template\\Default.pdm");
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        Element rootElement = document.getRootElement();
        Namespace oNamespace = new Namespace("o", "object");
        Namespace cNamespace = new Namespace("c", "collection");
        Namespace aNamespace = new Namespace("a", "attribute");
        Element rootObject = rootElement.element(new QName("RootObject", oNamespace));
        Element children = rootObject.element(new QName("Children", cNamespace));
        Element model = children.element(new QName("Model", oNamespace));
        Element PhysicalDiagrams = model.element(new QName("PhysicalDiagrams", cNamespace));
        Element PhysicalDiagram = PhysicalDiagrams.element(new QName("PhysicalDiagram", oNamespace));
        if (CollectionUtil.isNotEmpty(tableModels)) {
            Element symbols = DocumentHelper.createElement(new QName("Symbols", cNamespace));
            PhysicalDiagram.elements().add(symbols);
            Element tablesXml = DocumentHelper.createElement(new QName("Tables", cNamespace));
            model.elements().add(tablesXml);
            //生成table
            for (TableModel table : tableModels) {

                /**
                 * 2.生成table
                 */
                //1.Table
                String tableId = UUID.randomUUID().toString();
                Element tableXml = DocumentHelper.createElement(new QName("Table", oNamespace));
                tableXml.addAttribute(new QName("Id"), tableId);
                tablesXml.elements().add(tableXml);
                //ObjectID
                Element ObjectID = DocumentHelper.createElement(new QName("ObjectID", aNamespace));
                ObjectID.setText(UUID.randomUUID().toString());
                tableXml.add(ObjectID);
                //Name
                Element Name = DocumentHelper.createElement(new QName("Name", aNamespace));
                Name.setText(table.getTableName());
                tableXml.add(Name);
                //Code
                Element Code = DocumentHelper.createElement(new QName("Code", aNamespace));
                Code.setText("Table_1");
                tableXml.add(Code);
                //CreationDate
                Element CreationDate = DocumentHelper.createElement(new QName("CreationDate", aNamespace));
                CreationDate.setText(new Date().getTime() + "");
                tableXml.add(CreationDate);
                //Creator
                Element Creator = DocumentHelper.createElement(new QName("Creator", aNamespace));
                Creator.setText(System.getProperty("user.name"));
                tableXml.add(Creator);
                //ModificationDate
                Element ModificationDate = DocumentHelper.createElement(new QName("ModificationDate", aNamespace));
                ModificationDate.setText(new Date().getTime() + "");
                tableXml.add(ModificationDate);
                //Modifier
                Element Modifier = DocumentHelper.createElement(new QName("Modifier", aNamespace));
                Modifier.setText(System.getProperty("user.name"));
                tableXml.add(Modifier);
                //Modifier
                Element TotalSavingCurrency = DocumentHelper.createElement(new QName("TotalSavingCurrency", aNamespace));
                tableXml.add(TotalSavingCurrency);
                //Columns
                Element Columns = DocumentHelper.createElement(new QName("Columns", cNamespace));
                tableXml.elements().add(Columns);
                if (CollectionUtil.isNotEmpty(table.getFields())) {
                    for (FieldModel column : table.getFields()) {
                        Element Column = DocumentHelper.createElement(new QName("Column", oNamespace));
                        Column.addAttribute(new QName("Id"), UUID.randomUUID().toString());
                        Columns.elements().add(Column);
                        //Name
                        Element Name1 = DocumentHelper.createElement(new QName("Name", aNamespace));
                        Name1.setText(column.getColumnName());
                        Column.add(Name1);
                        //Code
                        Element Code11 = DocumentHelper.createElement(new QName("Code", aNamespace));
                        Code11.setText(column.getColumnName());
                        Column.add(Code11);
                        //Code
                        Element cdate = DocumentHelper.createElement(new QName("CreationDate", aNamespace));
                        cdate.setText(new Date().getTime()+"");
                        Column.add(cdate);
                        //user
                        Element Creator11 = DocumentHelper.createElement(new QName("Creator", aNamespace));
                        Creator11.setText(System.getProperty("user.name"));
                        Column.add(Creator11);
                        //ModificationDate
                        Element ModificationDate11 = DocumentHelper.createElement(new QName("ModificationDate", aNamespace));
                        ModificationDate11.setText(new Date().getTime()+"");
                        Column.add(ModificationDate11);
                        //Modifier
                        Element Modifier11 = DocumentHelper.createElement(new QName("Modifier", aNamespace));
                        Modifier11.setText(System.getProperty("user.name"));
                        Column.add(Modifier11);
                        //Modifier
                        Element DataType = DocumentHelper.createElement(new QName("DataType", aNamespace));
                        DataType.setText(column.getColumnType());
                        Column.add(DataType);
                    }
                }
                //keys
                Element keys = DocumentHelper.createElement(new QName("Keys", cNamespace));
                tableXml.add(keys);

                /**
                 * 1.生成Symbols
                 */
                Element tableSymbol = DocumentHelper.createElement(new QName("TableSymbol", oNamespace));
                tableSymbol.addAttribute(new QName("Id"), UUID.randomUUID().toString());
                symbols.elements().add(tableSymbol);
                List<Element> elements = tableSymbol.elements();
                //添加CreationDate
                Element creationDate = DocumentHelper.createElement(new QName("CreationDate", aNamespace));
                creationDate.setText(new Date().getTime() + "");
                elements.add(creationDate);
                //添加ModificationDate
                Element modificationDate = DocumentHelper.createElement(new QName("ModificationDate", aNamespace));
                modificationDate.setText(new Date().getTime() + "");
                elements.add(modificationDate);
                //添加IconMode
                Element iconMode = DocumentHelper.createElement(new QName("IconMode", aNamespace));
                iconMode.setText("-1");
                elements.add(iconMode);
                //Rect
                Element rect = DocumentHelper.createElement(new QName("Rect", aNamespace));
                rect.setText("((0,0), (10000,10000))");
                elements.add(rect);
                //AutoAdjustToText
                Element autoAdjustToText = DocumentHelper.createElement(new QName("AutoAdjustToText", aNamespace));
                autoAdjustToText.setText("0");
                elements.add(autoAdjustToText);
                //LineColor
                Element lineColor = DocumentHelper.createElement(new QName("LineColor", aNamespace));
                lineColor.setText("12615680");
                elements.add(lineColor);
                //FillColor
                Element fillColor = DocumentHelper.createElement(new QName("FillColor", aNamespace));
                fillColor.setText("16570034");
                elements.add(fillColor);
                //FillColor
                Element shadowColor = DocumentHelper.createElement(new QName("ShadowColor", aNamespace));
                shadowColor.setText("12632256");
                elements.add(shadowColor);
                //FillColor
                Element FontList = DocumentHelper.createElement(new QName("FontList", aNamespace));
                FontList.setText("STRN 0 新宋体,8,N\n" +
                        "DISPNAME 0 新宋体,8,N\n" +
                        "OWNRDISPNAME 0 新宋体,8,N\n" +
                        "Columns 0 新宋体,8,N\n" +
                        "TablePkColumns 0 新宋体,8,U\n" +
                        "TableFkColumns 0 新宋体,8,N\n" +
                        "Keys 0 新宋体,8,N\n" +
                        "Indexes 0 新宋体,8,N\n" +
                        "Triggers 0 新宋体,8,N\n" +
                        "LABL 0 新宋体,8,N");
                elements.add(FontList);
                //FillColor
                Element BrushStyle = DocumentHelper.createElement(new QName("BrushStyle", aNamespace));
                BrushStyle.setText("6");
                elements.add(BrushStyle);
                //GradientFillMode
                Element GradientFillMode = DocumentHelper.createElement(new QName("GradientFillMode", aNamespace));
                GradientFillMode.setText("65");
                elements.add(GradientFillMode);
                //GradientEndColor
                Element GradientEndColor = DocumentHelper.createElement(new QName("GradientEndColor", aNamespace));
                GradientEndColor.setText("16777215");
                elements.add(GradientEndColor);
                //GradientEndColor
                Element ManuallyResized = DocumentHelper.createElement(new QName("ManuallyResized", aNamespace));
                ManuallyResized.setText("1");
                elements.add(ManuallyResized);
                //Object
                Element Object = DocumentHelper.createElement(new QName("Object", cNamespace));
                elements.add(Object);
                Element objectTable = DocumentHelper.createElement(new QName("Table", oNamespace));
                objectTable.addAttribute(new QName("Ref"), tableId);
                Object.elements().add(objectTable);


            }
        }
        return document.asXML();
    }
}
