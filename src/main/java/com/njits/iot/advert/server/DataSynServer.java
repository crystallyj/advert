package com.njits.iot.advert.server;

import com.njits.iot.advert.common.GpsSendClient;
import com.njits.iot.advert.common.db.DataSource;
import com.njits.iot.advert.common.entity.Constant;
import com.njits.iot.advert.common.quartz.QuartzManager;
import com.njits.iot.advert.http.AdvertHttpServer;
import com.njits.iot.advert.njits.Gps905Handler;
import com.njits.iot.advert.traffic.GpsSocketHandler;
import com.njits.iot.advert.tyx.CarInfoHandler;
import com.njits.iot.advert.tyx.InDBHandler;
import com.njits.iot.advert.tyx.RemoveDBClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 服务入口，启动
 *
 * @author 43797
 * @date 2018年12月20日 下午9:56:31
 */
public class DataSynServer
{
    
    private static final Logger logger = LoggerFactory.getLogger(DataSynServer.class);
    
    public static void main(String[] arg) throws IOException
    {
        String[] jobs = new String[]{"http", "traffic", "tyx", "njits", "delete", "carInfo"};
//		String[] jobs = new String[] { "http", "tyx", "njits", "delete" };
        for (int i = 0; i < jobs.length; i++)
        {
            startup(jobs[i]);
        }
    }
    
    public static void startup(String job) throws IOException
    {
        Properties prop = new Properties();
        try
        {
            InputStream is = DataSource.class.getResourceAsStream("/base.properties");
            prop.load(is);
            GpsSocketHandler.IP = prop.getProperty("ip");
            GpsSocketHandler.PORT = Integer.parseInt(prop.getProperty("port"));
            GpsSocketHandler.USER_NAME = prop.getProperty("username");
            GpsSocketHandler.PASSWORD = prop.getProperty("password");
            Constant.positionMap.put("traffic", prop.getProperty("positionPath") + "traffic.txt");
            Constant.positionMap.put("tyx", prop.getProperty("positionPath") + "tyx.txt");
            Constant.positionMap.put("njits", prop.getProperty("positionPath") + "njits.txt");
            
            CarInfoHandler.execQueryCarInfo();
            
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        if ("http".equalsIgnoreCase(job))
        {
            AdvertHttpServer.startup();
            logger.info("=============== Http Server startup success ===============");
        }
        else if (Constant.KEY_TRAFFIC.equalsIgnoreCase(job))
        {
            try
            {
                // 0 0/4 * * * ? 每4分钟执行一次
                QuartzManager.addJob("同步位置数据", "出租车位置信息->交通局", "traffic", "group", GpsSendClient.class,
                        "0 0/" + Constant.INTERVAL_TRAFFIC +
                                " * * * ?", Constant.KEY_TRAFFIC, GpsSocketHandler.getInstance());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            logger.info("=============== Data synchronization to traffic task start-up completion ===============");
        }
        else if (Constant.KEY_TYX.equalsIgnoreCase(job))
        {
            try
            {
                // 0 0/3 * * * ? 每3分钟执行一次
                QuartzManager.addJob("同步位置数据", "出租车位置信息->天宇星", "tyx", "group", GpsSendClient.class,
                        "0 0/" + Constant.INTERVAL_TYX + " * * * ?", Constant.KEY_TYX, InDBHandler.getInstance());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            logger.info("=============== Data synchronization to TYX task start-up completion ===============");
        }
        else if (Constant.KEY_NJITS.equalsIgnoreCase(job))
        {
            try
            {
                // 0 0/3 * * * ? 每3分钟执行一次
                QuartzManager.addJob("同步位置数据", "出租车位置信息->njits", "njits", "group", GpsSendClient.class,
                        "0 0/" + Constant.INTERVAL_NJITS + " * * * ?", Constant.KEY_NJITS, Gps905Handler.getInstance());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            logger.info("=============== Data synchronization to ITS task start-up completion ===============");
        }
        else if ("delete".equalsIgnoreCase(job))
        {
            try
            {
                // 每天23点执行一次， 清理30天之前的GPS数据
                QuartzManager.addJob("武汉数据库表分区", "出租车位置信息表分区", "delete", "group", RemoveDBClient.class,
                        "0 0 23 * * " + "?", null, null);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            logger.info("=============== Delete GPS Data task start-up completion ===============");
        }
        else if ("carInfo".equalsIgnoreCase(job))
        {
            try
            {
                // 每天01点执行一次
                QuartzManager.addJob("初始化车辆基本信息", "基本信息", "carInfo", "group", CarInfoHandler.class, "0 0 1 * * ?",
                        null, null);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            logger.info("=============== Init car info completion ===============");
        }
    }
    
}
