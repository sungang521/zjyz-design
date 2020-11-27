package com.zjyz.designer.conf;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.zjyz.designer.constant.Constant;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class MysqlConf {

    private static  Map<String,JdbcTemplate> jdbcTemplateMap=new ConcurrentHashMap();

    public static void init(String name ,String host,String port,String user,String pwd) {

        if(!jdbcTemplateMap.containsKey(name)){

            Properties properties=new Properties();
            // 获取druid.properties配置文件资源输入流
//            InputStream resourceAsStream = getClass().getResourceAsStream("/druid.properties");

            try{
                // 加载配置文件
//                properties.load(resourceAsStream);
                properties.setProperty("driverClassName","com.mysql.cj.jdbc.Driver");
                String url="jdbc:mysql://%s:%s/?characterEncoding=utf8&useUnicode=true&useSSL=false&allowMultiQueries=true&allowPublicKeyRetrieval=true&serverTimezone=%s&zeroDateTimeBehavior=convertToNull";
                properties.setProperty("url",String.format(url,host.trim(),port.trim(),"GMT%2B8"));
                properties.setProperty("username",user.trim());
                properties.setProperty("password",pwd.trim());
                properties.setProperty("validationQuery ","select 1");
                properties.setProperty("testWhileIdle","true");
                // 获取连接池对象
                DruidDataSource druidDataSource = (DruidDataSource) DruidDataSourceFactory.createDataSource(properties);
                druidDataSource.setRemoveAbandoned(true);
                druidDataSource.setRemoveAbandonedTimeout(600);
                druidDataSource.setLogAbandoned(true);
			    druidDataSource.setBreakAfterAcquireFailure(true);
                druidDataSource.setTimeBetweenConnectErrorMillis(3000);
			    druidDataSource.setConnectionErrorRetryAttempts(0);
                druidDataSource.setTestOnBorrow(true);
                druidDataSource.setFailFast(true);
                // 创建JdbcTemplate对象
                JdbcTemplate jdbcTemplate = new JdbcTemplate(druidDataSource);
                jdbcTemplateMap.put(name,jdbcTemplate);
            }catch (Exception e){
                e.printStackTrace();
            }

        }

    }



    public static JdbcTemplate get(String key) throws Exception {
        if(!jdbcTemplateMap.containsKey(key)){
            throw  new Exception("数据库连接不存在");
        }
        return jdbcTemplateMap.get(key);
    }

    public static boolean test(String key)   {
        boolean status=true;
        if(!jdbcTemplateMap.containsKey(key)){
            status=false;
        }
        try {
            jdbcTemplateMap.get(key).queryForMap(Constant.SQL_TEST);
        }catch (Exception e){
            e.printStackTrace();
            status=false;
        }
        return status;
    }

    public  static void remove(String key){
        if(jdbcTemplateMap.containsKey(key)){
            jdbcTemplateMap.remove(key);
        }

    }

}
