package com.njits.iot.advert.baseinfo;

import com.alibaba.fastjson.JSONArray;
import com.njits.iot.advert.baseinfo.advert.AdvertHttpClient;
import com.njits.iot.advert.baseinfo.basebean.SyncBaseInfoClient;
import com.njits.iot.advert.common.db.DataSource;
import com.njits.iot.advert.common.entity.Constant;
import com.njits.iot.advert.common.quartz.QuartzManager;
import com.njits.iot.advert.server.DataSynServer;
import com.njits.iot.advert.util.JSONUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * 基础信息和沈阳武汉三杰8类广告同步数据开始类
 */
public class SyncDataClient
{
    
    private static final Logger logger = LoggerFactory.getLogger(DataSynServer.class);
    public static final String BASICINFO_TOPIC = "com.itsp.taxi.basicinfo.sourcedata";
    public static final String ADVERTISE_TOPIC = "com.itsp.taxi.adv.sourcedata";
    public static JSONArray cityInfo = null;
    
    public static void init()
    {
        try
        {
            //-------------
            Properties prop = new Properties();
            
            InputStream is = DataSource.class.getResourceAsStream("/base.properties");
            prop.load(is);
            SyncBaseInfoClient.baseinfo_id = prop.getProperty("baseinfo.user_id");
            SyncBaseInfoClient.baseinfo_pswd = prop.getProperty("baseinfo.pswd");
            //order_detail\dev_detail
            Constant.positionMap.put("420100_order_detail",
                    prop.getProperty("positionAdvPath") + "420100_order_detail.txt");
            Constant.positionMap.put("420100_dev_detail",
                    prop.getProperty("positionAdvPath") + "420100_dev_detail.txt");
            Constant.positionMap.put("210100_order_detail",
                    prop.getProperty("positionAdvPath") + "210100_order_detail.txt");
            Constant.positionMap.put("210100_dev_detail",
                    prop.getProperty("positionAdvPath") + "210100_dev_detail.txt");
            
            //--------------------
            InputStream inputStream = SyncDataClient.class.getResourceAsStream("/baseinfo.json");
            StringBuilder sb = new StringBuilder();
            String line;
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = br.readLine()) != null)
            {
                sb.append(line);
            }
            cityInfo = JSONUtil.getARRAYFromString(sb.toString());
            logger.info("init baseinfo json,totalCity=[{}]", cityInfo.size());
            is.close();
            inputStream.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        
    }
    
    public static void main(String[] args)
    {
        
        
        init();
        
        // 每天1点执行一次，0 0 1 * * ?
        QuartzManager.addJob("同步瑞明基础数据", "瑞明基础数据", "baseInfo", "group", SyncBaseInfoClient.class, "0 0 1 * * ?", null
                , null);
        
        // 每10分钟执行一次 0 0/10 * * * ?
        QuartzManager.addJob("同步广告数据", "同步广告数据", "advertInfo", "group", AdvertHttpClient.class, "0 0/10 * * * ?",
                null, null);
        Properties properties = new Properties();
        properties.put("bootstrap.servers", "192.168.44.212:9092,192.168.44.213:9092,192.168.44.214:9092");
        properties.put("group.id", "njits");
        properties.put("enable.auto.commit", "false");
        properties.put("auto.commit.interval.ms", "1000");
        properties.put("auto.offset.reset", "latest");
        properties.put("session.timeout.ms", "30000");
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        
        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(properties);
        kafkaConsumer.subscribe(Arrays.asList("com.itsp.taxi.advys.sourcedata"));
        
        
    }
    
    
    public static void getInfo()
    {
        try
        {
            //    URIBuilder uriBuilder = new URIBuilder("http://120.202.28.253:48081/getMdt");
            
            HttpClient client = HttpClients.createDefault();
            
            HttpGet get = new HttpGet("http://220.164.162.34:48081/getCompany");
            get.addHeader("Content-Type", "application/x-www-form-urlencoded");
            get.addHeader("userId", "admin");
            get.addHeader("userKey", "admin111");
            HttpResponse getReson = client.execute(get);
            HttpEntity ent = getReson.getEntity();
            String getResu = EntityUtils.toString(ent);
            System.out.println("get resu===" + getResu);
            //----
            System.out.println("begin post====");
            HttpPost post = new HttpPost("http://220.164.162.34:48081/getCar?startPos=0&endPos=2");
            post.addHeader("Content-Type", "application/x-www-form-urlencoded");
            post.addHeader("userId", "admin");
            post.addHeader("userKey", "admin111");
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
//            nvps.add(new BasicNameValuePair("startPos", "3"));
//            nvps.add(new BasicNameValuePair("endPos", "3"));
            HttpResponse httpResponse = client.execute(post);
            HttpEntity postenty = httpResponse.getEntity();
            String postresu = EntityUtils.toString(postenty);
            System.out.println("post resu===" + postresu);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
