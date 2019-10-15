package com.njits.iot.advert.privatecar;

import com.alibaba.fastjson.JSONObject;
import com.njits.iot.advert.common.db.DataSource;
import com.njits.iot.advert.common.entity.GpsBean;
import com.njits.iot.advert.njits.Gps905Handler;
import com.njits.iot.advert.traffic.GpsSocketHandler;
import com.njits.iot.advert.util.DateUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 武汉个体出租车GPS同步类开始类
 */
public class SendGPSClient
{
    public static ConcurrentHashMap<String, String> imei_plate = new ConcurrentHashMap<String, String>();
    private static final Logger log = LoggerFactory.getLogger(SendGPSClient.class);
    public static AtomicLong send_count_JTJ = new AtomicLong(0);
    public static AtomicLong send_count_905 = new AtomicLong(0);
    
    public static void init()
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
            Gps905Handler.ip = prop.getProperty("server905.ip");
            Gps905Handler.port = Integer.parseInt(prop.getProperty("server905.port"));
            Gps905Handler.userId = Integer.parseInt(prop.getProperty("server905.userId"));
            Gps905Handler.password = prop.getProperty("server905.password");
            Gps905Handler.accessCodeHex = prop.getProperty("server905.accessCode");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args)
    {
        init();
        SendGpsTestThread test = new SendGpsTestThread();
        test.start();
        //更新设备的线程
        UpdateDeviceThread updateDeviceThread = new UpdateDeviceThread();
        updateDeviceThread.start();
        //打印日志的线程
        PrintLogThread printLogThread = new PrintLogThread();
        printLogThread.start();
        Properties properties = new Properties();
        properties.put("bootstrap.servers", "192.168.44.161:9092,192.168.44.162:9092,192.168.44.163:9092");
        properties.put("group.id", "njits");
        properties.put("enable.auto.commit", "false");
        properties.put("auto.commit.interval.ms", "1000");
        properties.put("auto.offset.reset", "latest");
        properties.put("session.timeout.ms", "30000");
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        
        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(properties);
        kafkaConsumer.subscribe(Arrays.asList("adv_gps"));
        
        consumerData(kafkaConsumer);
    }
    
    public static void consumerData(KafkaConsumer<String, String> kafkaConsumer)
    {
        ConsumerRecords<String, String> records = null;
        while (true)
        {
            records = kafkaConsumer.poll(5000);
            for (ConsumerRecord<String, String> record : records)
            {
                try
                {
//                System.out.printf("offset = %d, value = %s", record.offset(), record.value());
//                System.out.println();
                    JSONObject jsb = JSONObject.parseObject(record.value());
                    GpsBean gps = new GpsBean();
                    gps.setDtuCode(jsb.getString("deviceId"));
                    gps.setLat(Double.valueOf(jsb.getString("lat")));
                    gps.setLng(Double.valueOf(jsb.getString("lng")));
                    gps.setDevName(imei_plate.get(jsb.getString("deviceId")));
                    gps.setCreateDate(DateUtil.millinToMillon(jsb.getLong("timestamp")));
                    //交通局
                    if (GpsSocketHandler.getInstance().sendMsg2(gps))
                    // if (true)
                    {
//                        log.info("send to jiaotongju success.....");
//                        System.out.println("send to jiaotongju success.....");
                        send_count_JTJ.addAndGet(1);
                    }
                    //905
//                    List<GpsBean> test = new ArrayList<>();
//                    test.add(gps);
                    if (Gps905Handler.getInstance().sendMsgKafka(gps))
                    {
                        //  System.out.println("send to 905 success.....");
                        send_count_905.addAndGet(1);
                        
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            kafkaConsumer.commitAsync();//异步提交
        }
    }
}
