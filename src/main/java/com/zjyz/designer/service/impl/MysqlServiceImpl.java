package com.zjyz.designer.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zjyz.designer.conf.MysqlConf;
import com.zjyz.designer.constant.Constant;
import com.zjyz.designer.model.*;
import com.zjyz.designer.service.MysqlService;

import com.zjyz.designer.utils.ThreadPoolsUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * mysql业务处理逻辑
 *
 */
@Service
public class MysqlServiceImpl implements MysqlService {

    /**
     * 临时存储表信息结果的字段
     */
    private  static  Map<String,List<TableModel>> tableListMap=new ConcurrentHashMap<>();

    @Override
    public List<String> getDbList(String connectKey) {
        List<String> result=null;
        try {

            //查询所有数据库列表
            String sql= Constant.SQL_SHOW_DATABASES;
            List<Map<String,Object>>  list= MysqlConf.get(connectKey).queryForList(sql);
            if(!CollectionUtils.isEmpty(list)){
                result=list.stream().map(v->v.get(Constant.FIELD_DATABASE).toString()).collect(Collectors.toList());
            }
        } catch (Exception e) {
            e.printStackTrace();
            //数据库连接不存在或者数据库连接失败


        }
        if(CollectionUtils.isEmpty(result)){
            result=new ArrayList<>();
        }
        return  result;
    }

    @Override
    public List<TableModel> getTableList(String connectKey,String dbName) {
        List<TableModel> tableList=null;
        try {
            String sql=String.format(Constant.SQL_SHOW_TABLES_ALL,dbName);
            System.out.println(sql);
            List<Map<String,Object>>  list= MysqlConf.get(connectKey).queryForList(sql);
            if(!CollectionUtils.isEmpty(list)){
                ObjectMapper objectMapper=new ObjectMapper();
                List<TableInfoModel> tableInfoModels =list.stream().map(v->{
                    TableInfoModel tableInfoModel=objectMapper.convertValue(v,TableInfoModel.class);
                    return  tableInfoModel;

                }).collect(Collectors.toList());
                String fieldName=String.format(Constant.FIELD_TABKE,dbName);
                tableList=getTableInfoByDb(tableInfoModels,fieldName,dbName,connectKey,objectMapper);

                System.out.println(tableList);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        if(CollectionUtils.isEmpty(tableList)){
            tableList=new ArrayList<>();
        }
        return tableList;

    }

    @Override
    public List<TableModel> getTableListAsync(String connectKey, String dbName) {

        String key =UUID.randomUUID().toString();
        tableListMap.put(key,new Vector<>());
        try {
            String sql=String.format(Constant.SQL_SHOW_TABLES,dbName);
            System.out.println(sql);
            List<Map<String,Object>>  list= MysqlConf.get(connectKey).queryForList(sql);

            if(!CollectionUtils.isEmpty(list)){

                String fieldName=String.format(Constant.FIELD_TABKE,dbName);
                ObjectMapper objectMapper=new ObjectMapper();
                CountDownLatch countDownLatch=new CountDownLatch(list.size());
                list.forEach(v->{
                    ThreadPoolsUtil.execute(()->{
                        TableModel tableModel=getTableInfo(v,fieldName,dbName,connectKey,objectMapper);
                        tableListMap.get(key).add(tableModel);
                        countDownLatch.countDown();

                    });
                });
                countDownLatch.await();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return tableListMap.remove(key);
    }

    @Override
    public Integer updateTableColumns(QueryModel queryModel) throws Exception {


        return MysqlConf.get(queryModel.getConnectKey()).update(queryModel.getSql());
    }

    @Override
    public List<String> getTableNameList(String connectKey, String dbName) {
        List<String> result=null;
        try {
            String sql=String.format(Constant.SQL_SHOW_TABLES,dbName);
            System.out.println(sql);
            List<Map<String,Object>>  list= MysqlConf.get(connectKey).queryForList(sql);
            String fieldName=String.format(Constant.FIELD_TABKE,dbName);
            result = list.stream().map(v->v.get(fieldName).toString()).collect(Collectors.toList());


            System.out.println(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(CollectionUtil.isEmpty(result)){
            result=new ArrayList<>();
        }
        return result;
    }

    @Override
    public List<FieldModel> getTableColumnsList(String connectKey, String dbName, String targetName) {
        //获取表字段说明列表

        List<FieldModel> fieldModels=null;
        try {
            String tableSql=String.format(Constant.SQL_SHOW_COLUMNS,dbName,targetName);
            List<Map<String,Object>>  columnsList= MysqlConf.get(connectKey).queryForList(tableSql);
            if(!CollectionUtils.isEmpty(columnsList)){
                ObjectMapper objectMapper=new ObjectMapper();
                fieldModels =columnsList.stream().map(column->{
                    FieldModel fieldModel=objectMapper.convertValue(column, FieldModel.class);
                    return fieldModel;
                }).collect(Collectors.toList());


            }else{
                //这里后续补充
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if(CollectionUtil.isEmpty(fieldModels)){
            fieldModels=new ArrayList<>();
        }
        return fieldModels;
    }

    /**
     * 获取表的信息按表查询
     * @param v
     * @param fieldName
     * @param dbName
     * @param connectKey
     * @param objectMapper
     * @return
     */
    private TableModel  getTableInfo(Map<String,Object> v,String fieldName,String dbName ,String connectKey,ObjectMapper objectMapper){
        //获取表名称
        TableModel tableModel=new TableModel();
        String tableName=v.get(fieldName).toString();
        tableModel.setTableName(tableName);
        tableModel.setConnectKey(connectKey);
        tableModel.setDbName(dbName);
        //获取表字段说明列表
        String tableSql=String.format(Constant.SQL_SHOW_COLUMNS,dbName,tableName);
        try {
            List<Map<String,Object>>  columnsList= MysqlConf.get(connectKey).queryForList(tableSql);
            if(!CollectionUtils.isEmpty(columnsList)){
                List<FieldModel> fieldModelList=columnsList.stream().map(column->{
                    FieldModel fieldModel=objectMapper.convertValue(column, FieldModel.class);
                    return fieldModel;
                }).collect(Collectors.toList());
                tableModel.setFields(fieldModelList);
            }else{
                //这里后续补充
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //获取外键关联关系列表
        String constraintSql=String.format(Constant.SQL_SHOW_CONSTRAINT,dbName,tableName);
        try {
            List<Map<String,Object>>  constraintList= MysqlConf.get(connectKey).queryForList(constraintSql);
            if(!CollectionUtils.isEmpty(constraintList)){
                List<ConstraintModel> constraintModelList =constraintList.stream().map(constraint->{
                    ConstraintModel constraintModel=objectMapper.convertValue(constraint,ConstraintModel.class);
                    return constraintModel;
                }).collect(Collectors.toList());
                tableModel.setConstraint(constraintModelList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tableModel;
    }
    /**
     * 获取表的信息按库查询
     * @param list
     * @param fieldName
     * @param dbName
     * @param connectKey
     * @param objectMapper
     * @return
     */
    private List<TableModel>  getTableInfoByDb( List<TableInfoModel>  list,String fieldName,String dbName ,String connectKey,ObjectMapper objectMapper){

        //初始化列表
        List<TableModel> tableModelList=list.stream().map(v->{

            TableModel tableModel=new TableModel();
            tableModel.setTableName(v.getTableName());
            tableModel.setTableComment(v.getTableComment());
            tableModel.setConnectKey(connectKey);
            tableModel.setDbName(dbName);
            return  tableModel;
        }).collect(Collectors.toList());


        //获取表字段说明列表
        String tableSql=String.format(Constant.SQL_SHOW_COLUMNS_ALL,dbName);
        try {
            List<Map<String,Object>>  columnsList= MysqlConf.get(connectKey).queryForList(tableSql);
            if(!CollectionUtils.isEmpty(columnsList)){

                Map<String,List<FieldModel>> fieldModelMap =columnsList.stream().map(column->{
                    FieldModel fieldModel=objectMapper.convertValue(column, FieldModel.class);
                    return fieldModel;
                }).collect(Collectors.groupingBy(FieldModel::getTableName));
                tableModelList.forEach(v->{
                    if(fieldModelMap.containsKey(v.getTableName())){
                        v.setFields(fieldModelMap.get(v.getTableName()));
                    }

                });

            }else{
                //这里后续补充
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //获取外键关联关系列表
        String constraintSql=String.format(Constant.SQL_SHOW_CONSTRAINT_ALL,dbName);
        try {
            List<Map<String,Object>>  constraintList= MysqlConf.get(connectKey).queryForList(constraintSql);
            if(!CollectionUtils.isEmpty(constraintList)){

                Map<String,List<ConstraintModel>> constraintModelMap =constraintList.stream().map(constraint->{
                    ConstraintModel constraintModel=objectMapper.convertValue(constraint,ConstraintModel.class);
                    return constraintModel;
                }).collect(Collectors.groupingBy(ConstraintModel::getTableName));
                tableModelList.forEach(v->{
                    if(constraintModelMap.containsKey(v.getTableName())){
                        v.setConstraint(constraintModelMap.get(v.getTableName()));
                    }

                });

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(11);
        return tableModelList;
    }



}
