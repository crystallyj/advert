package com.njits.iot.advert.common.db;

import org.apache.commons.dbcp.BasicDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DataSource
{
    
    private static Map<String, BasicDataSource> bsMap;
    
    public static final String MYSQL_ADV = "mysql_adv";
    
    //武汉天宇星轨迹查询数据库
    public static final String MYSQL_TYX = "mysql_tyx";
    
    public static final String MYSQL_DEVICE = "mysql_device";
    
    static
    {
        try
        {
            String[] dbTypes = {MYSQL_ADV, MYSQL_DEVICE};
            bsMap = new HashMap<>();
            // 一、实例化BasicDataSource
            InputStream is = DataSource.class.getResourceAsStream("/db.properties");
            Properties prop = new Properties();
            prop.load(is);
            
            // 二、设置BasicDataSource属性
            // 1、设置四个属性
            for (int i = 0; i < dbTypes.length; i++)
            {
                String dbType = dbTypes[i] + ".";
                BasicDataSource bs = new BasicDataSource();
                bs.setDriverClassName(prop.getProperty(dbType + "driverClassName"));
                bs.setUrl(prop.getProperty(dbType + "url"));
                bs.setUsername(prop.getProperty(dbType + "username"));
                bs.setPassword(prop.getProperty(dbType + "password"));
                // 2、设置连接是否默认自动提交
                bs.setDefaultAutoCommit(false);
                // 3、设置初始后连接数
                bs.setInitialSize(Integer.parseInt(prop.getProperty(dbType + "initialSize")));
                // 4、设置最大的连接数
                bs.setMaxActive(Integer.parseInt(prop.getProperty(dbType + "maxActive")));
                // 5、设置空闲等待时间，获取连接后没有操作开始计时，到达时间后没有操作回收链接
                bs.setMaxIdle(Integer.parseInt(prop.getProperty(dbType + "maxWait")));
                
                bsMap.put(dbTypes[i], bs);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 获取连接
     *
     * @return
     */
    public static Connection getConnection(String dbType)
    {
        try
        {
            return bsMap.get(dbType).getConnection();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
