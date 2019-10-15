package com.njits.iot.advert.privatecar;

import com.njits.iot.advert.common.db.DataSource;
import com.njits.iot.advert.util.KafkaProducerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UpdateDeviceThread extends Thread
{
    private static Logger logger = LoggerFactory.getLogger(UpdateDeviceThread.class);
    
    @Override
    public void run()
    {
        
        Connection conn = null;
        Statement ps = null;
        ResultSet rs = null;
        String sql = "select t.number,t2.plate_no from equipment_info t inner join car_info t2 where t.cj_id = t2.id " +
                "and t.si_id = 'sj' and t2.is_delete=1 and t.is_delete=1 and t2.city = '420100'";
        while (true)
        {
            try
            {
                logger.info("TYX Execute insert sql ==> {}", sql);
                conn = DataSource.getConnection(DataSource.MYSQL_DEVICE);
                
                ps = conn.createStatement();
                rs = ps.executeQuery(sql);
                // 遍历ResultSet中的每条数据
                while (rs.next())
                {
                    String deviceid = rs.getString(1);
                    String plateno = rs.getString(2);
                    SendGPSClient.imei_plate.put(deviceid, plateno);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                try
                {
                    if (rs != null)
                    {
                        rs.close();
                    }
                    if (ps != null)
                    {
                        ps.close();
                    }
                    if (conn != null)
                    {
                        conn.close();
                    }
                    //一天更新一次设备信息
                    Thread.sleep(1000 * 60 * 60 * 24);
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                    logger.error("close connection failed, {}", e.getMessage());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public static void main(String[] args)
    {
        UpdateDeviceThread tt = new UpdateDeviceThread();
        tt.start();
        System.out.println(SendGPSClient.imei_plate.size());
    }
}

class PrintLogThread extends Thread
{
    private Logger log = LoggerFactory.getLogger(PrintLogThread.class);
    
    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                log.info("sent to JiaoTongJu count={},,sent to 905 count={}",
                        SendGPSClient.send_count_JTJ.getAndSet(0), SendGPSClient.send_count_905.getAndSet(0));
                
                //一小时打印一次日志
                Thread.sleep(1000 * 60 * 60);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}

class SendGpsTestThread extends Thread
{
    @Override
    public void run()
    {
        while (true)
        {
            String mst = "{\"lng\":118.8924," + "\"deviceId\":\"1808100098\"," + "        \"lat\":32.06834," +
                    "        \"timestamp\":1562565049" + "}";
            // System.out.println(mst);
            KafkaProducerUtil.sedMsg("adv_gps", mst);
            try
            {
                System.out.println("send sucess kafka.....");
                Thread.sleep(100);
                
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}