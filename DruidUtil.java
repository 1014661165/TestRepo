package cn.edu.fudan.util;


import cn.edu.fudan.config.Config;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;

import java.sql.SQLException;

/**
 * @author wujun
 * @date 2020-3-26
 */
public class DruidUtil {

    private static DruidDataSource dataSource;

    /**
     * 初始化druid数据源连接池
     * @throws SQLException
     */
    public static void initDataSource() throws SQLException {
        if (dataSource != null){
            return;
        }
        dataSource = new DruidDataSource();
        dataSource.setDriverClassName(Config.DRIVER);
        dataSource.setUrl(Config.URL);
        dataSource.setUsername(Config.USERNAME);
        dataSource.setPassword(Config.PASSWORD);
        dataSource.setInitialSize(4);
        dataSource.setMaxActive(16);
        dataSource.setMaxWait(5000);
        dataSource.setTimeBetweenEvictionRunsMillis(5000);
        dataSource.setMinEvictableIdleTimeMillis(3000);
        dataSource.setValidationQuery("SELECT 1");
        dataSource.setTestOnBorrow(true);
        dataSource.init();
    }

    /**
     * 从连接池中获取连接
     * @return
     * @throws SQLException
     */
    public static DruidPooledConnection getConnection()throws SQLException {
        if (dataSource == null){
            return null;
        }
        return dataSource.getConnection();
    }

    /**
     * 关闭数据源
     */
    public static void  close(){
        if (dataSource != null && !dataSource.isClosed()){
            dataSource.close();
        }
    }

    public int add(int a,int b){
        return a + b;
    }

}
