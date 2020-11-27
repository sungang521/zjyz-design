package com.zjyz.designer.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfoenix.animation.alert.JFXAlertAnimation;
import com.jfoenix.controls.*;
import com.zjyz.designer.conf.MysqlConf;
import com.zjyz.designer.constant.Constant;
import com.zjyz.designer.controller.compent.LoadingTab;
import com.zjyz.designer.helper.HtmlExportHelper;
import com.zjyz.designer.helper.PDMHelper;
import com.zjyz.designer.model.*;
import com.zjyz.designer.service.MysqlService;
import com.zjyz.designer.utils.LocalCacheUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.zjyz.designer.MainApp.loadFxml;

/**
 * @Author libingsi
 * @Description
 * @Date 2020/11/13 10:37
 * @Version 1.0
 **/
@Component
public class MainController  implements Initializable {
    @Autowired
    private PDMHelper pdmHelper;
    @Autowired
    private HtmlExportHelper htmlExportHelper;
    @FXML
    JFXRadioButton pdmStyle;
    @FXML
    JFXRadioButton htmlStyle;
    @Autowired
    private MysqlService mysqlService;

    @FXML
    private SplitPane splitPane;

    @FXML
    private TreeView tree;

    //连接数据源属性字段 connection.fxml
    @FXML
    public TextField connectionName;
    @FXML
    public TextField iPName;
    @FXML
    public TextField port;
    @FXML
    public TextField username;
    @FXML
    public PasswordField password;

    @FXML
    private TableView<EditFieldModel> editTableView;





    @FXML
    private JFXComboBox<Label> sourceFieldComboBox;


    @FXML
    private JFXComboBox<Label>  targetFieldComboBox;

    @FXML
    private JFXTextField sourceNameField;
    @FXML
    private JFXTextField targetNameField;


    private static final String FX_TEXT_FILL_WHITE = "-fx-text-fill:BLACK";
    private static final String ANIMATED_OPTION_BUTTON = "animated-option-button";
    private static final String ANIMATED_OPTION_SUB_BUTTON2 = "animated-option-sub-button2";

    private List<TableModel> tableModels;
    /**
     * 新建窗口
     */
    private  Stage stage =null;

    /**
     * 编辑表窗口
     */
    private  Stage editStage =null;
    /**
     * 编辑表窗口
     */
    private  Stage relationStage =null;



    /**
     * 数据源树
     */
    private TreeItem<String> rootItem=null ;


    /**
     * 计数器,标记用户操作的具体的数据名称
     */
    Map<String,Integer> counter=new HashMap<>();


    /**
     * 计算图的高度使用
     */
    List<Integer> tableHeight=new LinkedList<>();

    /**
     * 存储表的位置
     */
    Map<String,PositionModel> relationMap=new HashMap<>();

    /**
     * 存储两表之间的关系
     */
    Map<String,List<String >> relationTableMap=new HashMap<>();

    /**
     * 保持所有的链接信息
     */
    List<DbCheckModel> dbCheckModelList=new ArrayList<>();


    /**
     * 保持不同tab下的Pane
     */
    Map<String,Pane> contentPane=new HashMap<>();

    Map<String,Tab> tabMap = new HashMap<>();

    /**
     * 是否添加过tab
     */
    private boolean isAdd = false;

    JFXTabPane jfxTabPane = new JFXTabPane();


    /**
     * 默认调用方法
     */
    public void initialize(URL location, ResourceBundle resources){


        if(CollectionUtil.isEmpty(dbCheckModelList)){
            String dbCache= LocalCacheUtil.get();
            if(!StringUtils.isEmpty(dbCache)){

                try {
                    ObjectMapper objectMapper=new ObjectMapper();
                    List<Object> list=objectMapper.readValue(dbCache,ArrayList.class);
                    if(CollectionUtil.isNotEmpty(list)){
                        dbCheckModelList=list.stream().map(v->{
                            DbCheckModel checkModel=objectMapper.convertValue(v,DbCheckModel.class);
                            return checkModel;
                        }).collect(Collectors.toList());

                        if(CollectionUtil.isNotEmpty(dbCheckModelList)){
                            dbCheckModelList.forEach(v->{
                                //文件的特殊處理
                                if(v.getHost().equals("file")){
                                    initFileDb(v.getConnectKey());
                                }else {
                                    //建立数据库连接
                                    MysqlConf.init(v.getConnectKey(), v.getHost(), v.getPort(), v.getUser(), v.getPwd());
                                    //初始化左侧列表
                                    showDbList(v.getConnectKey());
                                }
                            });
                        }
                    }



                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    /**
     * 测试连接是否可用
     */
    public void DbTest(){

        DbCheckModel checkModel=dbInit();
        String connectKey=checkModel.getConnectKey();
        //建立数据库连接
        if(checkModel.getStatus()){
            Alert.AlertType alertType;
            String msg="";
            if(MysqlConf.test(connectKey)){
                alertType=Alert.AlertType.INFORMATION;
                msg="连接成功";
            }else{
                alertType=Alert.AlertType.ERROR;
                msg="连接失败";
            }

            Alert alert=new Alert(alertType);
            alert.setTitle("连接提示");
            alert.setContentText(msg);
            alert.showAndWait();
        }
        Optional<DbCheckModel> dbCheckModel=dbCheckModelList.stream().filter(v->connectKey.equals(v.getConnectKey())).findFirst();
        if(!dbCheckModel.isPresent()){
            MysqlConf.remove(connectKey);
        }
    }

    /**
     * 初始化数据库连接
     * @return
     */
    private DbCheckModel dbInit(){
        String connectKey=connectionName.getText().trim();
        String host=iPName.getText().trim();

        String portValue=port.getText().trim();
        String user=username.getText().trim();
        String pwd=password.getText().trim();
        DbCheckModel dbCheckModel=new DbCheckModel();
        dbCheckModel.setStatus(false);
        dbCheckModel.setConnectKey(connectKey);
        dbCheckModel.setHost(host);
        dbCheckModel.setPort(portValue);
        dbCheckModel.setUser(user);
        dbCheckModel.setPwd(pwd);

        if(connectKey.length()>0&&host.length()>0&&portValue.length()>0&&user.length()>0){


            ObjectMapper objectMapper=new ObjectMapper();
            try {
                //查询是否有同名的连接
                Optional<DbCheckModel> optional=dbCheckModelList.stream().filter(v->v.getConnectKey().equals(dbCheckModel.getConnectKey())).findFirst();

                if(!optional.isPresent()){
                    //缓存连接信息
                    dbCheckModelList.add(dbCheckModel);
                    String dbCache=objectMapper.writeValueAsString(dbCheckModelList);
                    LocalCacheUtil.set(dbCache);
                    //建立数据库连接
                    MysqlConf.init(connectKey,host,portValue,user,pwd);
                    dbCheckModel.setStatus(true);
                }else{
                    alert(Alert.AlertType.ERROR,"已存在相同的连接名");
                }

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

        }else{
            alert(Alert.AlertType.ERROR,"缺少必须参数.");
        }
        return dbCheckModel;
    }
    /**
     * 连接数据源,并加载数据源中的数据库列表
     */
    public  void dbConnection(){

        DbCheckModel checkModel=dbInit();
        String connectKey=checkModel.getConnectKey();
        if(checkModel.getStatus()){
            //关闭新建窗口
            if(ObjectUtil.isNotNull(stage)){
                stage.close();
            }
            //加载左侧数据源列表
            showDbList(connectKey);
        }




    }
    /**
     * 加载左侧数据源和数据库列表
     */
    public void showDbList(String connectKey){
        //获取数据库列表
        List<String> list =mysqlService.getDbList(connectKey);
        if(!list.isEmpty()){
            //初始化数据源数据节点
            if(ObjectUtil.isNull(rootItem)){
                rootItem = new TreeItem<> ();
                tree.setRoot(rootItem);
                tree.setShowRoot(false);
            }

            //列表组件数据组装
            TreeItem connectItem=new TreeItem<> (connectKey);

            rootItem.getChildren().add(connectItem);
            list.forEach(v->{
                ImageView imageView=new ImageView(new Image(getClass().getResourceAsStream("/style/pdb.png")) );
                imageView.setDisable(false);
                imageView.setFitHeight(20);
                imageView.setFitWidth(20);
                TreeItem dbItem=new TreeItem<> (v,imageView);
                connectItem.getChildren().add(dbItem);
            });

            tree.addEventFilter(MouseEvent.MOUSE_CLICKED,(e)->{

                if (!isAdd) {
                    splitPane.getItems().add(jfxTabPane);
                    jfxTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
                    isAdd = true;
                }
                // showDbEr(list);
                new Thread(() -> showDbEr(list)).start();
            });
        }
    }
    /**
     * 生成数据库对应的ER图
     * @param list
     */
    private void showDbEr(List<String> list){
        //双击数据库名获取数据库中对应的表生成ER图
        TreeItem treeItem= (TreeItem) tree.getSelectionModel().getSelectedItem();


        if(!ObjectUtils.isEmpty(treeItem)){

            String dbName=treeItem.getValue().toString();
            if(list.indexOf(dbName)!=-1){
                if(counter.containsKey(dbName)){
                    if(counter.get(dbName)>= Constant.SHOW_TABLE_NAX_NUMBER){

                        String connectKey=treeItem.getParent().getValue().toString();
                        //开始绘制最外层框架和初始化数据
                        Pane pane=new Pane();
                        if(contentPane.containsKey(dbName)){
                            contentPane.remove(dbName);
                        }
                        contentPane.put(dbName,pane);
                        StackPane result = new StackPane(pane);
                        result.setAlignment(Pos.TOP_LEFT);
                        LoadingTab tab = null;
                        try {
                            tab = (LoadingTab) ShowTabScrollPane(result, dbName);
                            LoadingTab finalTab = tab;
                            Platform.runLater(() ->
                            {
                                List<Tab> collect = jfxTabPane.getTabs().stream().filter(tab1 -> tab1.getText().equals(finalTab.getTab().getText())).collect(Collectors.toList());
                                if(CollectionUtil.isEmpty(collect)){
                                    jfxTabPane.getTabs().add(finalTab.getTab());
                                    System.out.println("数据库tab添加了,一共"+jfxTabPane.getTabs().size());
                                }

                            });

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        //获取表的列表信息,只加载一次
                        if(!(treeItem.getChildren().size()>0)){
                            List<String>  tableNameList=mysqlService.getTableNameList(connectKey,dbName);
                            if(CollectionUtil.isNotEmpty(tableNameList)){
                                treeItem.getChildren().clear();
                                tableNameList.forEach(v->{
                                    TreeItem tableItem=new TreeItem<> (v);
                                    treeItem.getChildren().add(tableItem);
                                });
                            }
                        }

                        //获取表结构信息,组装数据
                        List<TableModel>  tableList=mysqlService.getTableList(connectKey,dbName);
                        this.tableModels = tableList;
                        Map<String,TableModel> tableMap=tableList.stream().collect(Collectors.toMap(TableModel::getTableName,a->a,(a,b)->a));
                        List<TreeTableModel> tableRelation=new ArrayList<>();
                        tableList.forEach(v->{
                            if(CollectionUtil.isNotEmpty(v.getConstraint())){
                                v.getConstraint().forEach(c->{
                                    if(!StringUtils.isEmpty(c.getReferencedTableName())){
                                        TreeTableModel treeTableModel;
                                        Optional<TreeTableModel> optional=tableRelation.stream().filter(t->t.getName().equals(c.getReferencedTableName())).findAny();
                                        if(optional.isPresent()){
                                            treeTableModel=optional.get();

                                        }else{
                                            treeTableModel=new TreeTableModel();
                                            treeTableModel.setName(c.getReferencedTableName());
                                            treeTableModel.setRelation(new ArrayList<>());
                                            tableRelation.add(treeTableModel);
                                        }
                                        treeTableModel.getRelation().add(v.getTableName());

                                    }

                                });
                            }
                        });









                        tableHeight.clear();
                        relationMap.clear();
                        AtomicInteger level=new AtomicInteger(0);

                        //绘制有关联关系的表
                        while(true){
                            if(tableRelation.isEmpty()){
                                break;
                            }else{
                                TreeTableModel treeTableModel=tableRelation.remove(0);
                                relationProcess(tableMap,level,pane,treeTableModel);

                            }
                        }
                        //绘制没有关联关系的表
                        if(!tableMap.isEmpty()){
                            AtomicInteger horizontal=new AtomicInteger(0);
                            tableMap.forEach((k,v)->{
                                //控制换行
                                if(horizontal.get()>=Constant.DEFAULT_TABLE_COLUMN_NUMBER){
                                    horizontal.set(0);
                                    level.getAndAdd(1);
                                }
                                createTableNode(v,horizontal.getAndAdd(1),level.get(),pane);
                            });
                        }
                        tab.over();

                        counter.clear();
                    }else{
                        counter.put(dbName,counter.get(dbName)+1);
                    }

                }else{
                    counter.clear();
                    counter.put(dbName,Constant.SHOW_TABLE_NUMBER);
                }

            }


        }
    }

    /**
     * 生成pdm对应的ER图
     * @param e
     * @param tableList
     */
    private void showEROfPDM(MouseEvent e, List<TableModel> tableList,String name) {
        //双击数据库名获取数据库中对应的表生成ER图
        this.tableModels = tableList;
        TreeItem i = (TreeItem) tree.getSelectionModel().getSelectedItem();
        if (!ObjectUtils.isEmpty(i)) {
            if(i.getParent().getValue().toString().contains(".pdm")){
                String dbName = i.getValue().toString();
                if (true) {
                    if (true) {
                        Pane pane = new Pane();
                        StackPane result = new StackPane(pane);
                        result.setAlignment(Pos.TOP_LEFT);
                        LoadingTab tab = null;
                        try {
                            tab = (LoadingTab) ShowTabScrollPane(result, name);
                            LoadingTab finalTab = tab;
                            Platform.runLater(() -> {
                                List<Tab> collect = jfxTabPane.getTabs().stream().filter(tab1 -> tab1.getText().equals(finalTab.getTab().getText())).collect(Collectors.toList());
                                if(CollectionUtil.isEmpty(collect)){
                                    jfxTabPane.getTabs().add(finalTab.getTab());
                                    System.out.println("数据库tab添加了,一共"+jfxTabPane.getTabs().size());
                                }
                            });


                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        //获取表结构信息,组装数据
                        Map<String, TableModel> tableMap = tableList.stream().collect(Collectors.toMap(TableModel::getTableName, a -> a, (a, b) -> a));
                        List<TreeTableModel> tableRelation = new ArrayList<>();
                        tableList.forEach(v -> {
                            if (CollectionUtil.isNotEmpty(v.getConstraint())) {
                                v.getConstraint().forEach(c -> {
                                    if (!StringUtils.isEmpty(c.getReferencedTableName())) {
                                        TreeTableModel treeTableModel;
                                        Optional<TreeTableModel> optional = tableRelation.stream().filter(t -> t.getName().equals(c.getReferencedTableName())).findAny();
                                        if (optional.isPresent()) {
                                            treeTableModel = optional.get();

                                        } else {
                                            treeTableModel = new TreeTableModel();
                                            treeTableModel.setName(c.getReferencedTableName());
                                            treeTableModel.setRelation(new ArrayList<>());
                                            tableRelation.add(treeTableModel);
                                        }
                                        treeTableModel.getRelation().add(v.getTableName());

                                    }

                                });
                            }
                        });

                        //开始绘制最外层框架和初始化数据


                        tableHeight.clear();
                        relationMap.clear();
                        AtomicInteger level = new AtomicInteger(0);

                        //绘制有关联关系的表
                        while (true) {
                            if (tableRelation.isEmpty()) {
                                break;
                            } else {
                                TreeTableModel treeTableModel = tableRelation.remove(0);
                                relationProcess(tableMap, level, pane, treeTableModel);

                            }
                        }
                        //绘制没有关联关系的表
                        if (!tableMap.isEmpty()) {
                            AtomicInteger horizontal = new AtomicInteger(0);
                            tableMap.forEach((k, v) -> {
                                //控制换行
                                if (horizontal.get() >= Constant.DEFAULT_TABLE_COLUMN_NUMBER) {
                                    horizontal.set(0);
                                    level.getAndAdd(1);
                                }
                                createTableNode(v, horizontal.getAndAdd(1), level.get(), pane);
                            });
                        }

                        tab.over();
                        counter.clear();
                    } else {
                        counter.put(dbName, counter.get(dbName) + 1);
                    }

                } else {
                    counter.clear();
                    counter.put(dbName, Constant.SHOW_TABLE_NUMBER);
                }


            }
        }
    }

    /**
     * 处理节点之间的逻辑关系
     * @param tableMap
     * @param level
     * @param pane
     * @param treeTableModel
     */
    private void relationProcess(Map<String,TableModel> tableMap,AtomicInteger level,Pane pane,TreeTableModel treeTableModel) {

        AtomicBoolean addLevel=new AtomicBoolean(false);
        if(relationMap.containsKey(treeTableModel.getName())){

        }else{
            int tempLevel=level.getAndAdd(1);
            addLevel.set(true);
            createTableNode(tableMap.remove(treeTableModel.getName()),0,tempLevel,pane);
        }
        int subLevel=level.get();
        AtomicInteger horizontal=new AtomicInteger(0);
        treeTableModel.getRelation().forEach(tableName->{

            if(relationMap.containsKey(tableName)){

            }else{
                if(! addLevel.get()){
                    addLevel.set(true);
                }
                createTableNode(tableMap.remove(tableName),horizontal.getAndAdd(1),subLevel,pane);

            }
            drawLine(pane,relationMap.get(treeTableModel.getName()),relationMap.get(tableName));
        });

        if(addLevel.get()){
            level.addAndGet(1);
        }



    }


    /**
     * 获取指定范围的随机数
     * @param start
     * @param end
     * @return
     */
    private  int getRandom(int start,int end) {
        int num=(int) (Math.random()*(end-start+1)+start);
        return num;
    }

    /**
     * 绘制两个节点之间的连接线
     * @param pane
     * @param source
     * @param target
     */
    private void drawLine(Pane pane,PositionModel source,PositionModel target){

        //保持表的映射关系
        if(!relationTableMap.containsKey(source.getTableName())){
            relationTableMap.put(source.getTableName(),new ArrayList<>());
        }
        relationTableMap.get(source.getTableName()).add(target.getTableName());

        Path path=new Path();
        path.setStrokeWidth(1);
        int randomX=getRandom(Constant.DRAW_MIN_GAP,source.getWidth());
        int randomY=getRandom(Constant.DRAW_MIN_GAP,Constant.DRAW_MAX_GAP);
        int randomXX=getRandom(Constant.DRAW_MIN_GAP,Constant.DRAW_MAX_GAP);
        int randomYY=getRandom(Constant.DRAW_MIN_GAP,Constant.DRAW_MAX_GAP);
        if(source.getX().equals(target.getX())){
            if(source.getY().equals(target.getY())){
                path.getElements().add(new MoveTo(source.getX(),source.getY()+randomY));
                path.getElements().add(new LineTo(source.getX()-randomXX,source.getY()+randomY));
                path.getElements().add(new LineTo(target.getX()-randomXX,target.getY()+randomY+randomYY));
                path.getElements().add(new LineTo(target.getX(),target.getY()+randomY+randomYY));
            }else{
                //处理同一列关系
                path.getElements().add(new MoveTo(source.getX(),source.getY()+randomY));
                path.getElements().add(new LineTo(source.getX()-randomXX,source.getY()+randomY));
                path.getElements().add(new LineTo(target.getX()-randomXX,target.getY()+randomY));
                path.getElements().add(new LineTo(target.getX(),target.getY()+randomY));
            }

        }else{
            if(source.getY().equals(target.getY())){
                //处理同一行关系
                path.getElements().add(new MoveTo(source.getX()+randomX,source.getY()));
                path.getElements().add(new LineTo(source.getX()+randomX,source.getY()-randomY));
                path.getElements().add(new LineTo(target.getX()+randomX,target.getY()-randomY));
                path.getElements().add(new LineTo(target.getX()+randomX,target.getY()));
            }else{
                if(target.getX().compareTo(source.getX())>0){
                    //处理源节点在前,目标节点在后的情况
                    path.getElements().add(new MoveTo(source.getX()+randomX,source.getY()));
                    path.getElements().add(new LineTo(source.getX()+randomX,source.getY()-randomY));
                    path.getElements().add(new LineTo(source.getX()+source.getWidth()+randomXX,source.getY()-randomY));
                    path.getElements().add(new LineTo(source.getX()+source.getWidth()+randomXX,target.getY()-randomY));
                    path.getElements().add(new LineTo(target.getX()+randomX,target.getY()-randomY));
                    path.getElements().add(new LineTo(target.getX()+randomX,target.getY()));
                }else{

                    //处理源节点在后,目标节点在前的情况
                    path.getElements().add(new MoveTo(source.getX(),source.getY()+randomY));
                    path.getElements().add(new LineTo(source.getX()-randomXX,source.getY()+randomY));
                    path.getElements().add(new LineTo(source.getX()-randomXX,target.getY()-randomY));
                    path.getElements().add(new LineTo(target.getX()+randomX,target.getY()-randomY));
                    path.getElements().add(new LineTo(target.getX()+randomX,target.getY()));


                }
            }


        }





        pane.getChildren().add(path);

    }
    /**
     * 提示信息方法
     * @param alertType
     * @param msg
     */
    private  void alert(Alert.AlertType alertType,String msg){
        Alert alert=new Alert(alertType,"提示信息");
        alert.setContentText(msg);
        alert.show();
    }


    /**
     * 创建表节点
     * @param table
     * @param horizontal
     * @param level
     * @param pane
     */
    private void createTableNode(TableModel table, int horizontal, int level, Pane pane) {
        //设置节点坐标位置
        int x=Constant.DEFAULT_GAP;
        int y=Constant.DEFAULT_GAP;
        x+=horizontal*Constant.DEFAULT_TABLE_GAP;
        int sum=0;
        for(int i=0;i<tableHeight.size();i++){
            if(level>i){
                sum+=tableHeight.get(i);
            }
        }
        y+=sum+(Constant.DEFAULT_GAP*level);
        PositionModel result=new PositionModel();
        result.setTableName(table.getTableName());
        result.setX(x);
        result.setY(y);
        result.setWidth(Constant.DEFAULT_TABLE_WIDTH);
        int height=(table.getFields().size()+1)*Constant.DEFAULT_TABLE_LINE_HEIGHT;

        result.setHeight(height>Constant.DEFAULT_TABLE_HEIGHT?Constant.DEFAULT_TABLE_HEIGHT:height);

        relationMap.put(table.getTableName(),result);
        if(tableHeight.size()>level){
            if(tableHeight.get(level)<result.getHeight()){

                tableHeight.remove(level);
                tableHeight.add(level,result.getHeight());
            }
        }else{
            tableHeight.add(result.getHeight());
        }

        //绘制外部面板
        String titleFormat="%s(%s)";
        TitledPane titledPane=new TitledPane();
        titledPane.setId("titledPane"+table.getTableName());
        titledPane.setCollapsible(false);// remove closing action
        titledPane.setAnimated(false);// stop animating
        titledPane.setText(String.format(titleFormat,table.getTableName(),table.getTableComment()));
        titledPane.setLayoutY(y);
        titledPane.setLayoutX(x);
        titledPane.setUserData(table);

        titledPane.setOnMouseClicked(e->{
            if(e.getClickCount()>=2){

                editTableInfo((TableModel)((TitledPane)e.getSource()).getUserData(),result);

            }

        });

        titledPane.setOnMouseDragged(e->{


            TitledPane source= (TitledPane) e.getSource();
            TableModel tableModel= (TableModel) source.getUserData();

            double targetX=source.getLayoutX()+e.getX();
            double targetY=source.getLayoutY()+e.getY();
            Optional<Node> node=pane.getChildren().stream().filter(v->"newLine".equals(v.getId())).findAny();
            if(node.isPresent()){
                Path path= (Path) node.get();
                path.getElements().clear();
                path.getElements().add(new MoveTo(source.getLayoutX(),source.getLayoutY()));
                path.getElements().add(new LineTo(targetX,targetY));


            }else{
                Path path=new Path();
                path.setId("newLine");
                path.setStrokeWidth(1);
                path.getElements().add(new MoveTo(source.getLayoutX(),source.getLayoutY()));
                path.getElements().add(new LineTo(targetX,targetY));
                pane.getChildren().add(path);
            }
        });

        titledPane.setOnMouseReleased(e->{


            TitledPane source= (TitledPane) e.getSource();
            TableModel tableModel= (TableModel) source.getUserData();
            Optional<Node> node=pane.getChildren().stream().filter(s->"newLine".equals(s.getId())).findAny();
            if(node.isPresent()){
                Path path= (Path) node.get();
                path.getElements().clear();
            }
            double targetX=source.getLayoutX()+e.getX();
            double targetY=source.getLayoutY()+e.getY();



            relationMap.forEach((k,v)->{
                double toX=v.getX()+v.getWidth();
                double toY=v.getY()+v.getHeight();
                if(targetX>v.getX()&&targetY>v.getY()&&targetX<toX&&targetY<toY){

                    if(!tableModel.getTableName().equals(k)){
//                        drawLine(pane,relationMap.get(tableModel.getTableName()),relationMap.get(k));
                        editRelation(tableModel,k);
                    }


                }
            });









        });

        //绘制列
        TableColumn columnName=new TableColumn("字段");
//        columnName.setCellFactory(TextFieldTableCell.forTableColumn());
        columnName.setCellValueFactory( new PropertyValueFactory<>("columnName"));
        TableColumn columnType=new TableColumn("类型");
        columnType.setCellFactory(TextFieldTableCell.forTableColumn());
        columnType.setCellValueFactory( new PropertyValueFactory<>("columnType"));
        TableColumn columnKey=new TableColumn("主键");
//        columnKey.setCellFactory(TextFieldTableCell.forTableColumn());
        columnKey.setCellValueFactory( new PropertyValueFactory<>("columnKey"));
        TableColumn columnComment=new TableColumn("注释");
//        columnComment.setCellFactory(TextFieldTableCell.forTableColumn());
        columnComment.setCellValueFactory( new PropertyValueFactory<>("columnComment"));

        //绘制表格
        TableView<FieldModel> tableView = new TableView<>();
        QueryModel queryModel=new QueryModel();
        queryModel.setConnectKey(table.getConnectKey());
        queryModel.setDbName(table.getDbName());
        queryModel.setTableName(table.getTableName());
        tableView.setId(table.getTableName());
        tableView.setUserData(queryModel);
        tableView.setEditable(true);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.getColumns().addAll(columnName,columnType,columnKey,columnComment);
        if(!CollectionUtil.isEmpty(table.getFields())){

            tableView.setMaxHeight(result.getHeight());
            ObservableList<FieldModel> observableList= FXCollections.observableArrayList(table.getFields());
            tableView.setItems(observableList);
        }
        titledPane.setContent(tableView);
        pane.getChildren().add(titledPane);






    }

    private void editRelation(TableModel source, String targetName) {



        try {
            String title="关系映射设置%s->%s";
            relationStage= new Stage();
            relationStage.setScene(new Scene(loadFxml("/fxml/editRelation.fxml").load()));
            relationStage.setTitle(String.format(title,source.getTableName(),targetName));
            relationStage.getIcons().add(new Image("/style/mysql.png"));
            relationStage.setWidth(500);
            relationStage.setHeight(330);
            // 禁止窗口缩放
            relationStage.setResizable(false);
            relationStage.show();


            sourceNameField.setText(source.getTableName());
            sourceNameField.setUserData(source);
            targetNameField.setText(targetName);

            source.getFields().forEach(v->{
                Label label=new Label(v.getColumnName());
                label.setUserData(v.getColumnName());
                sourceFieldComboBox.getItems().add(label);
            });

            List<FieldModel> fields =mysqlService.getTableColumnsList(source.getConnectKey(),source.getDbName(),targetName);
            if(CollectionUtil.isNotEmpty(fields)){
                fields.forEach(v->{
                    Label label=new Label(v.getColumnName());
                    label.setUserData(v.getColumnName());
                    targetFieldComboBox.getItems().add(label);
//                    sourceField.getSelectionModel().getSelectedItem()
                });
            }




        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    @FXML
    public  void saveEditRelation(){


        if(!sourceFieldComboBox.getSelectionModel().isEmpty()&&!targetFieldComboBox.getSelectionModel().isEmpty()){
            TableModel tableModel= (TableModel) sourceNameField.getUserData();
            Label sourceLable=sourceFieldComboBox.getSelectionModel().getSelectedItem();
            Label targetLable=targetFieldComboBox.getSelectionModel().getSelectedItem();

            String tableNameFormat="`%s`.`%S`";
            String keyNameFormat="%s_%s_%s_%s";
            String sourceTableName=String.format(tableNameFormat,tableModel.getDbName(),sourceNameField.getText());
            String targetTableName=String.format(tableNameFormat,tableModel.getDbName(),targetNameField.getText());
            String sourceKey=sourceLable.getText();
            String targetKey=targetLable.getText();
            String keyName=String.format(keyNameFormat,sourceNameField.getText(),sourceKey,targetNameField.getText(),targetKey);

            Optional<ConstraintModel> optional=tableModel.getConstraint().stream().filter(v->sourceNameField.getText().equals(v.getTableName())&&targetNameField.getText().equals(v.getReferencedTableName())&&sourceKey.equals(v.getColumnName())&&targetKey.equals(v.getReferencedColumnName())).findAny();
            if(optional.isPresent()){
                alert(Alert.AlertType.ERROR,"外键关系已存在");
            }else{
                ConstraintModel constraintModel=new ConstraintModel();
                constraintModel.setColumnName(sourceKey);
                constraintModel.setReferencedColumnName(targetKey);
                constraintModel.setReferencedTableName(targetNameField.getText());
                constraintModel.setTableName(sourceNameField.getText());
                constraintModel.setReferencedTableSchena(tableModel.getDbName());
                tableModel.getConstraint().add(constraintModel);
                sourceNameField.setUserData(tableModel);

                String sql=String.format(Constant.SQL_UPDATE_CONSTRAINT,sourceTableName,keyName,sourceKey,targetTableName,targetKey);
                QueryModel queryModel=new QueryModel();
                queryModel.setConnectKey(tableModel.getConnectKey());
                queryModel.setSql(sql);
                try {
                    mysqlService.updateTableColumns(queryModel);
                    alert(Alert.AlertType.INFORMATION,"设置成功");
                    relationStage.close();
                    if(contentPane.containsKey(tableModel.getDbName())){
                        Pane pane=contentPane.get(tableModel.getDbName());
                        drawLine(pane,relationMap.get(tableModel.getTableName()),relationMap.get(targetNameField.getText()));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    alert(Alert.AlertType.ERROR,e.getMessage());
                }
            }










        }else{

            alert(Alert.AlertType.ERROR,"请选择要关联的字段");

        }

    }

    public  void closeEditRelation(){
//        TableModel tableModel= (TableModel) sourceNameField.getUserData();
//        if(contentPane.containsKey(tableModel.getDbName())){
//            Pane pane=contentPane.get(tableModel.getDbName());
//            Optional<Node> node=pane.getChildren().stream().filter(s->"newLine".equals(s.getId())).findAny();
//            if(node.isPresent()){
//                Path path= (Path) node.get();
//                path.getElements().clear();
//            }
//        }

        relationStage.close();
    }
    private void editTableInfo(TableModel table,PositionModel result) {
        //读取连接connection.fxml

        try {
            String titleFormat="%s(%s)";
            editStage= new Stage();
            editStage.setScene(new Scene(loadFxml("/fxml/editTable.fxml").load()));
            editStage.setTitle(String.format(titleFormat,table.getTableName(),table.getTableComment()));
            editStage.getIcons().add(new Image("/style/mysql.png"));
            editStage.setWidth(500);
            editStage.setHeight(330);
            // 禁止窗口缩放
            editStage.setResizable(false);
            editStage.show();



            List<EditFieldModel> editFieldModels=table.getFields().stream().map(v->{

                EditFieldModel editFieldModel= new EditFieldModel();
                editFieldModel.setColumnComment(v.getColumnComment());
                editFieldModel.setColumnDefault(v.getColumnDefault());
                editFieldModel.setColumnName(v.getColumnName());
                editFieldModel.setColumnType(v.getColumnType());
                editFieldModel.setTableName(v.getTableName());
                editFieldModel.setOldColumnName(v.getColumnName());
                editFieldModel.setOldColumnComment(v.getColumnComment());
                editFieldModel.setOldColumnDefault(v.getColumnDefault());
                editFieldModel.setOldColumnKey(v.getColumnKey());
                editFieldModel.setOldColumnType(v.getColumnType());
                editFieldModel.setOldNull(v.getIsNullable());
                boolean nullStatus=false;
                if(!Constant.FIELD_COLUMNS_YES.equals(v.getIsNullable().toUpperCase())){
                    editFieldModel.getNullCheckBox().setSelected(true);
                    nullStatus=true;
                }
                editFieldModel.getNullCheckBox().setUserData(nullStatus);
                if(Constant.FIELD_COLUMNS_PRI_KEY.equals(v.getColumnKey().toUpperCase())){
                    editFieldModel.getKeyCheckBox().setSelected(true);
                }

                return editFieldModel;
            }).collect(Collectors.toList());




            //绘制列
            TableColumn<EditFieldModel,String> columnName=new TableColumn("字段");
            columnName.setSortable(false);
            columnName.setCellFactory(TextFieldTableCell.forTableColumn());
            columnName.setCellValueFactory( new PropertyValueFactory<>("columnName"));

            TableColumn<EditFieldModel,String>  columnType=new TableColumn("类型");
            columnType.setSortable(false);
            columnType.setCellFactory(TextFieldTableCell.forTableColumn());
            columnType.setCellValueFactory( new PropertyValueFactory<>("columnType"));

            TableColumn<EditFieldModel,CheckBox> columnNull=new TableColumn("不是null");
            columnNull.setSortable(false);
//            columnNull.setCellFactory(CheckBoxTableCell.forTableColumn((Callback<Integer, ObservableValue<Boolean>>) columnNull));
            columnNull.setCellValueFactory(data->data.getValue().getNullCheckBoxData());

            TableColumn<EditFieldModel,String>  columnDefault=new TableColumn("默认值");
            columnDefault.setSortable(false);
            columnDefault.setCellFactory(TextFieldTableCell.forTableColumn());
            columnDefault.setCellValueFactory( new PropertyValueFactory<>("columnDefault"));

            TableColumn<EditFieldModel,CheckBox>  columnKey=new TableColumn("主键");
            columnKey.setSortable(false);
            columnKey.setCellValueFactory( data->data.getValue().getKeyCheckBoxData());

            TableColumn<EditFieldModel,String>  columnComment=new TableColumn("注释");
            columnComment.setSortable(false);
            columnComment.setCellFactory(TextFieldTableCell.forTableColumn());
            columnComment.setCellValueFactory( new PropertyValueFactory<>("columnComment"));



            //给列添加编辑事件
            columnName.setOnEditCommit(( event)->{
                event.getRowValue().setColumnName(event.getNewValue());
            });
            columnType.setOnEditCommit(event->{
                event.getRowValue().setColumnType(event.getNewValue());
            });
            columnComment.setOnEditCommit(event->{
                event.getRowValue().setColumnComment(event.getNewValue());
            });
            columnDefault.setOnEditCommit(event->{
                event.getRowValue().setColumnDefault(event.getNewValue());
            });

            //绘制表格

            QueryModel queryModel=new QueryModel();
            queryModel.setConnectKey(table.getConnectKey());
            queryModel.setDbName(table.getDbName());
            queryModel.setTableName(table.getTableName());
            editTableView.setUserData(queryModel);
            editTableView.setEditable(true);
            editTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            editTableView.getColumns().addAll(columnName,columnType,columnNull,columnDefault,columnKey,columnComment);

            if(!CollectionUtil.isEmpty(editFieldModels)){

                editTableView.setMaxHeight(result.getHeight());
                ObservableList<EditFieldModel> observableList= FXCollections.observableArrayList(editFieldModels);
                editTableView.setItems(observableList);
            }


        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }


    @FXML
    public  void saveTable(){





        ObservableList<EditFieldModel> items =editTableView.getItems();
        QueryModel queryModel= (QueryModel) editTableView.getUserData();


        if(contentPane.containsKey(queryModel.getDbName())){

            Pane pane=contentPane.get(queryModel.getDbName());
            List<FieldModel> result=new ArrayList<>();




            StringBuffer priKey=new StringBuffer();
            StringBuffer oldPriKey=new StringBuffer();
            String tableName="`"+queryModel.getDbName()+"`.`"+queryModel.getTableName()+"`";
            items.forEach(tableColumn->{
                String nullCheck= tableColumn.getNullCheckBox().isSelected()?Constant.FIELD_COLUMNS_NO:Constant.FIELD_COLUMNS_YES;
                String priValue=tableColumn.getKeyCheckBox().isSelected()?Constant.FIELD_COLUMNS_PRI_KEY:"";
                if(Constant.FIELD_COLUMNS_PRI_KEY.equals(priValue)){
                    priKey.append(tableColumn.getColumnName());
                    priKey.append(",");
                }
                if(Constant.FIELD_COLUMNS_PRI_KEY.equals(tableColumn.getOldColumnKey())){
                    oldPriKey.append(tableColumn.getColumnName());
                    oldPriKey.append(",");
                }
                boolean nullStatus= (boolean) tableColumn.getNullCheckBox().getUserData();

                if(tableColumn.getColumnName().equals(tableColumn.getOldColumnName())&&((tableColumn.getColumnDefault()==null&&tableColumn.getOldColumnDefault()==null)||tableColumn.getColumnDefault().equals(tableColumn.getOldColumnDefault()))&&tableColumn.getColumnComment().equals(tableColumn.getOldColumnComment())&&tableColumn.getColumnType().equals(tableColumn.getOldColumnType())&&nullCheck.equals(tableColumn.getOldNull())){

                }else{
                    String newColumn=tableColumn.getColumnName();
                    String oldColumn=tableColumn.getOldColumnName();
                    String typeValue=tableColumn.getColumnType();
                    String commentValue=tableColumn.getColumnComment();

                    String defaultValue=tableColumn.getColumnDefault() ==null?Constant.FIELD_COLUMNS_DEFAULT_NULL:"'"+tableColumn.getColumnDefault()+"'";
                    String nullValue= tableColumn.getNullCheckBox().isSelected()?Constant.FIELD_COLUMNS_DEFAULT_NOT_NULL:Constant.FIELD_COLUMNS_DEFAULT_NULL;
                    String sql=String.format(Constant.SQL_UPDATE_COLUMNS,tableName,oldColumn,newColumn,typeValue,defaultValue,nullValue,commentValue);
                    System.out.println(sql);
                    queryModel.setSql(sql);
                    try {
                        //更新字段信息
                        mysqlService.updateTableColumns(queryModel);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }







                }

                FieldModel fieldModel=new FieldModel();
                fieldModel.setColumnComment(tableColumn.getColumnComment());
                fieldModel.setColumnType(tableColumn.getColumnType());
                fieldModel.setColumnKey(tableColumn.getKeyCheckBox().isSelected()?Constant.FIELD_COLUMNS_PRI_KEY:"");
                fieldModel.setColumnName(tableColumn.getColumnName());
                fieldModel.setColumnDefault(tableColumn.getColumnDefault());
                fieldModel.setIsNullable( tableColumn.getNullCheckBox().isSelected()?Constant.FIELD_COLUMNS_DEFAULT_NOT_NULL:Constant.FIELD_COLUMNS_DEFAULT_NULL);
                result.add(fieldModel);

            });


            if(CollectionUtil.isNotEmpty(result)){
                Optional<TableView> optionalTableView=pane.getChildren().stream().filter(v->{
                    TitledPane titledPane= (TitledPane) v;
                    TableView tableView = (TableView) titledPane.getContent();
                    return tableView.getId().equals(queryModel.getTableName());
                }).map(v->{
                    TitledPane titledPane= (TitledPane) v;
                    TableView tableView = (TableView) titledPane.getContent();
                    return tableView;
                }).findAny();
                optionalTableView.get().setItems(FXCollections.observableArrayList(result));

                Optional<TitledPane> optionalTitledPane=pane.getChildren().stream().filter(v->{
                    TitledPane titledPane= (TitledPane) v;

                    return  titledPane.getId().equals("titledPane"+queryModel.getTableName());
                }).map(v->(TitledPane) v).findAny();
                if(optionalTitledPane.isPresent()){
                    TableModel tableModel= (TableModel) optionalTitledPane.get().getUserData();
                    tableModel.setFields(result);
                    optionalTitledPane.get().setUserData(tableModel);
                }


            }


            if(!oldPriKey.toString().equals(priKey.toString())){
                String priKeyValue=priKey.delete(priKey.length()-1,priKey.length()).toString();
                String sql=String.format(Constant.SQL_UPDATE_PRIMARY,tableName,priKeyValue);
                queryModel.setSql(sql);
                System.out.println(sql);
                try {
                    //更新主键信息
                    mysqlService.updateTableColumns(queryModel);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Alert.AlertType alertType;
            String msg="信息修改成功";
            alertType=Alert.AlertType.INFORMATION;
            alert(alertType,msg);
            editStage.close();


        }







//        List<String> list =mysqlService.getDbList(queryModel.getConnectKey());
//        showDbEr(list);

    }
    @FXML
    public  void closeEdit(){
        editStage.close();
    }



    /**
     *  SplitPane ->  ScrollPane ->  StackPane ->  Group  ->  Region
     * @param region
     */
    public Tab ShowTabScrollPane( Region region,String name) {

        Region zoomTarget =region;
        zoomTarget.setPrefSize(1000, 1000);
        zoomTarget.setOnDragDetected(evt -> {
            Node target = (Node) evt.getTarget();
            while (target != zoomTarget && target != null) {
                target = target.getParent();
            }
            if (target != null) {
                target.startFullDrag();
            }
        });
        Group group = new Group(zoomTarget);
        // stackpane for centering the content, in case the ScrollPane viewport
        // is larget than zoomTarget
        StackPane content = new StackPane(group);
        group.layoutBoundsProperty().addListener((observable, oldBounds, newBounds) -> {
            // keep it at least as large as the content
            content.setMinWidth(newBounds.getWidth());
            content.setMinHeight(newBounds.getHeight());
        });
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setId("scrollPane");
        scrollPane.setPannable(true);
        scrollPane.viewportBoundsProperty().addListener((observable, oldBounds, newBounds) -> {
            // use vieport size, if not too small for zoomTarget
            content.setPrefSize(newBounds.getWidth(), newBounds.getHeight());
        });
        content.setOnScroll(evt -> {
            if (evt.isControlDown()) {
                evt.consume();

                final double zoomFactor = evt.getDeltaY() > 0 ? 1.2 : 1 / 1.2;

                Bounds groupBounds = group.getLayoutBounds();
                final Bounds viewportBounds = scrollPane.getViewportBounds();

                // calculate pixel offsets from [0, 1] range
                double valX = scrollPane.getHvalue() * (groupBounds.getWidth() - viewportBounds.getWidth());
                double valY = scrollPane.getVvalue() * (groupBounds.getHeight() - viewportBounds.getHeight());

                // convert content coordinates to zoomTarget coordinates
                Point2D posInZoomTarget = zoomTarget.parentToLocal(group.parentToLocal(new Point2D(evt.getX(), evt.getY())));

                // calculate adjustment of scroll position (pixels)
                Point2D adjustment = zoomTarget.getLocalToParentTransform().deltaTransform(posInZoomTarget.multiply(zoomFactor - 1));

                // do the resizing
                zoomTarget.setScaleX(zoomFactor * zoomTarget.getScaleX());
                zoomTarget.setScaleY(zoomFactor * zoomTarget.getScaleY());

                // refresh ScrollPane scroll positions & content bounds
                scrollPane.layout();

                // convert back to [0, 1] range
                // (too large/small values are automatically corrected by ScrollPane)
                groupBounds = group.getLayoutBounds();
                scrollPane.setHvalue((valX + adjustment.getX()) / (groupBounds.getWidth() - viewportBounds.getWidth()));
                scrollPane.setVvalue((valY + adjustment.getY()) / (groupBounds.getHeight() - viewportBounds.getHeight()));
            }
        });
        //scrollPane add to splitPane right
//        splitPane.getItems().remove(1);
//        splitPane.getItems().addAll(scrollPane);
        Tab tab1 = tabMap.get(name);
        if (tab1 == null) {
            tab1 = new LoadingTab(name, scrollPane);
            tabMap.put(name, tab1);
        }
        tab1.setOnClosed(event -> tabMap.remove(name));
        SingleSelectionModel<Tab> selectionModel = jfxTabPane.getSelectionModel();
        selectionModel.select(((LoadingTab) tab1).getTab());
        ((LoadingTab) tab1).sleep();
        return tab1;
    }

    /**
     * new 新建连接
     *
     * @param actionEvent
     * @throws IOException
     */
    public void newConnection(ActionEvent actionEvent) throws IOException {
        stage = new Stage();
        //读取连接connection.fxml
        stage.setScene(new Scene(loadFxml("/fxml/connection.fxml").load()));
        stage.setTitle("新建连接");
        stage.getIcons().add(new Image("/style/mysql.png"));
        stage.setWidth(400);
        stage.setHeight(330);
        // 禁止窗口缩放
        stage.setResizable(false);
        stage.show();
    }

    /**
     * open 打开文件
     *
     * @param event
     */
    private void initFileDb(String path){
        File file = new File(path);
        List<TableModel> tables = pdmHelper.getTableList(file);
        if (ObjectUtil.isNull(rootItem)) {
            rootItem = new TreeItem<>();
            tree.setRoot(rootItem);
            tree.setShowRoot(false);
        }
        TreeItem rootItem111 = new TreeItem<>(file.getName());
        this.rootItem.getChildren().add(rootItem111);
        if (CollectionUtil.isNotEmpty(tables)) {
            tables.forEach(v -> {
                ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/style/table.png")));
                imageView.setDisable(false);
                imageView.setFitHeight(20);
                imageView.setFitWidth(20);
                TreeItem dbItem = new TreeItem<>(v.getTableName(), imageView);
                rootItem111.getChildren().add(dbItem);
            });
            tree.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
                if (!isAdd) {
                    splitPane.getItems().add(jfxTabPane);
                    jfxTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
                    isAdd = true;
                }
                new Thread(() -> showEROfPDM(e, tables,file.getName().split("\\.")[0])).start();

            });
        }
    }
    /**
     * open 打开文件
     *
     * @param event
     */
    public void openFile(ActionEvent event) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Stage stage = new Stage();
        FileChooser fc = new FileChooser();
        File file = fc.showOpenDialog(stage);
        if (file == null) {
            return;
        }
        DbCheckModel dbCheckModel = new DbCheckModel();
        dbCheckModel.setConnectKey(file.getPath());
        dbCheckModel.setHost("file");
        dbCheckModelList.add(dbCheckModel);
        String dbCache = objectMapper.writeValueAsString(dbCheckModelList);
        LocalCacheUtil.set(dbCache);
        List<TableModel> tables = pdmHelper.getTableList(file);
        if (ObjectUtil.isNull(rootItem)) {
            rootItem = new TreeItem<>();
            tree.setRoot(rootItem);
            tree.setShowRoot(false);
        }
        TreeItem rootItem111 = new TreeItem<>(file.getName());
        this.rootItem.getChildren().add(rootItem111);
        if (CollectionUtil.isNotEmpty(tables)) {
            tables.forEach(v -> {
                ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/style/table.png")));
                imageView.setDisable(false);
                imageView.setFitHeight(20);
                imageView.setFitWidth(20);
                TreeItem dbItem = new TreeItem<>(v.getTableName(), imageView);
                rootItem111.getChildren().add(dbItem);
            });
            tree.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
                if (!isAdd) {
                    splitPane.getItems().add(jfxTabPane);
                    jfxTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
                    isAdd = true;
                }
                new Thread(() -> showEROfPDM(e, tables,file.getName().split("\\.")[0])).start();
            });
        }
    }

    /**
     * save 保存文件
     *
     * @param event
     */
    public void saveFile(ActionEvent event) {
        Stage stage = new Stage();
        FileChooser fc = new FileChooser();
        File file = fc.showSaveDialog(stage);
        if (file == null) {
            return;
        }
        //TODO : 保存的文件
    }

    /**
     * quit 退出
     */
    public void closeWindow() {
        Platform.exit();
    }

    /**
     * close 取消
     */
    public void closeConnection(){
        stage.close();
    }

    /**
     * about 关于
     */
    public void about(){
        JFXAlert alert = new JFXAlert();
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setOverlayClose(false);
        JFXDialogLayout layout = new JFXDialogLayout();
        layout.setHeading(new Label("PowerDesigner Client"));
        layout.setBody(new Label(" 中教云控股集团有限公司（简称“中教云”）成立于2016年，总部位于北京市海淀区中关村，" +
                "是一家以VR/AR/MR教育应用、5G、教育大数据、教育云计算、教育物联网、人工智能、知识图谱技术等高精尖技术为引擎，" +
                "助力中国教育现代化2035中长期战略规划，引领信息化时代教育变革，重塑智慧教育新业态的综合性教育科技集团。" +
                "集团下属中教云迪数字科技有限公司、中教云智数字科技有限公司、中教云教育科技集团有限公司三家教育业务子公司。" +
                "目前，集团旗下产品遍及全国20多个省市（自治区），覆盖700多所中小学、职业院校、高等院校，服务全国师生超过千万。"));
        JFXButton closeButton = new JFXButton();
        Label label = new Label("确定");
        label.setStyle(FX_TEXT_FILL_WHITE);
        closeButton.setGraphic(label);
        closeButton.setButtonType(JFXButton.ButtonType.RAISED);
        closeButton.getStyleClass().addAll(ANIMATED_OPTION_BUTTON,ANIMATED_OPTION_SUB_BUTTON2);
        closeButton.setOnAction(event -> alert.hideWithAnimation());
        layout.setActions(closeButton);
        alert.setContent(layout);
        alert.setAnimation(JFXAlertAnimation.SMOOTH);
        alert.show();
    }


    /**
     * 导出确认
     */
    public void exportFile() throws Exception{
        stage = new Stage();
        //读取连接connection.fxml
        stage.setScene(new Scene(loadFxml("/fxml/export.fxml").load()));
        stage.setTitle("导出文件");
        stage.getIcons().add(new Image("/style/export.png"));
        stage.setWidth(400);
        stage.setHeight(330);
        // 禁止窗口缩放
        stage.setResizable(false);
        stage.show();
    }

    /**
     * 导出确认
     */
    public void confirm() {
        if (CollectionUtil.isEmpty(tableModels)) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "错误提示");
            alert.setContentText("没有模型要导出");
            alert.show();
            return;
        }
        String content;
        String type;
        if (pdmStyle.isSelected()) {
            content = pdmHelper.coverModel(tableModels);
            type = "pdm";
        } else {
            content = htmlExportHelper.coverModel(tableModels);
            type = "html";
        }
        Stage stage = new Stage();
        FileChooser fc = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDM files (*." + type + ")", "*." + type + "");
        fc.getExtensionFilters().add(extFilter);
        File file = fc.showSaveDialog(stage);
        if (file == null) {
            return;
        }
        FileUtil.writeString(content, file, "utf-8");
        this.stage.close();
    }


}
