package com.njits.iot.advert.baseinfo.basebean;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.njits.iot.advert.baseinfo.BaseBeanDomain;
import com.njits.iot.advert.baseinfo.SyncDataClient;
import com.njits.iot.advert.util.DateUtil;
import com.njits.iot.advert.util.HttpClientUtil;
import com.njits.iot.advert.util.KafkaProducerUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class SyncBaseInfoClient implements Job
{
    private static final Logger logger = LoggerFactory.getLogger(SyncBaseInfoClient.class);
    public static String baseinfo_id;
    public static String baseinfo_pswd;
    
    @Override
    public void execute(JobExecutionContext jobExecutionContext)
    {
        logger.info("begin everyday quartz....... SyncBaseInfoClient");
        for (int i = 0; i < SyncDataClient.cityInfo.size(); i++)
        {
            //同步数据
            queryInfoTerms(SyncDataClient.cityInfo.getJSONObject(i));
            
        }
    }
    
    
    public static void queryInfoTerms(JSONObject jb)
    {
        String areacode = jb.getString("areacode");
        
        String prov = jb.getString("prov");
        String city = jb.getString("city");
        String carType = jb.getString("carType");
        BaseBeanDomain beanDomain = new BaseBeanDomain();
        
        beanDomain.setProv(prov);
        beanDomain.setCity(city);
        beanDomain.setArea_code(areacode);
        beanDomain.setCar_type(carType);
        beanDomain.setTimestamp(DateUtil.getCurrentDateTime("yyyy-MM-dd HH:mm:ss"));
        //请求基础数据
        generrateBaseinfo(beanDomain, jb);
        //请求广告数据
        generrateAdvert(beanDomain, jb);
        
    }
    
    public static void generrateAdvert(BaseBeanDomain beanDomain, JSONObject jb)
    {
        logger.debug("");
        String frim = jb.getJSONObject("advertInfo").getString("frim");
        beanDomain.setFrim(frim);
        Map urlinfo = (Map) jb.getJSONObject("advertInfo");
        long maxData = 200000l;
        for (Object key : urlinfo.keySet())
        {
            //日全量 01:00 不带参数 adv_list\order_list\adv_fen
            
            if (key.toString().equalsIgnoreCase("adv_list") || key.toString().equalsIgnoreCase("order_list") ||
                    key.toString().equalsIgnoreCase("adv_fen"))
            {
                JSONArray jsonArray = HttpClientUtil.sedGetBasicRequest(urlinfo.get(key).toString(), null, null, null);
                beanDomain.setCommand(key.toString());
                KafkaProducerUtil.sedMsgToKafka(jsonArray, beanDomain, jb, SyncDataClient.ADVERTISE_TOPIC);
            }
            //日全量 01:00 带参数 adv_playtime\adv_time_date
            
            if (key.toString().equalsIgnoreCase("adv_playtime") || key.toString().equalsIgnoreCase("adv_timedate"))
            {
                
                for (int starpos = 0; starpos < maxData; starpos = starpos + 1000)
                {
                    Map<String, Object> param = new HashMap<>();
                    param.put("startPos", starpos);
                    param.put("endPos", 1000);
                    JSONArray jsonArray = HttpClientUtil.sedGetBasicRequest(urlinfo.get(key).toString(), null, null,
                            param);
                    if (null != jsonArray)
                    {
                        if (jsonArray.size() > 0)
                        {
                            beanDomain.setCommand(key.toString());
                            KafkaProducerUtil.sedMsgToKafka(jsonArray, beanDomain, jb, SyncDataClient.ADVERTISE_TOPIC);
                            continue;
                        }
                        else
                        {
                            logger.info("city:{}  command: {} ,param={} has no data...", beanDomain.getCity(),
                                    key.toString(), param);
                            break;
                        }
                    }
                    
                    break;
                }
            }
        }
    }
    
    public static void generrateBaseinfo(BaseBeanDomain beanDomain, JSONObject jb)
    {
//        String userId = jb.getString("userId");
//        String userKey = jb.getString("userKey");
//        int maxCar = jb.getInteger("maxCar");
//        int maxDriver = jb.getInteger("maxDriver");
        String frim = jb.getJSONObject("baseInfo").getString("frim");
        beanDomain.setFrim(frim);
        Map urlinfo = (Map) jb.getJSONObject("baseInfo");
        long maxData = 200000l;
        for (Object key : urlinfo.keySet())
        {
            if (key.toString().equalsIgnoreCase("mdtl") || key.toString().equalsIgnoreCase("company"))
            {
                JSONArray jsonArray = HttpClientUtil.sedGetBasicRequest(urlinfo.get(key).toString(), baseinfo_id,
                        baseinfo_pswd, null);
                beanDomain.setCommand(key.toString());
                KafkaProducerUtil.sedMsgToKafka(jsonArray, beanDomain, jb, SyncDataClient.BASICINFO_TOPIC);
            }
            
            
            else if (key.toString().equalsIgnoreCase("car") || key.toString().equalsIgnoreCase("driver"))
            {
//                maxData = key.toString().equalsIgnoreCase("car") ? jb.getInteger("maxCar") : jb.getInteger
// ("maxDriver");
                for (int starpos = 0; starpos < maxData; starpos = starpos + 1000)
                {
                    JSONArray jsonArray = HttpClientUtil.sedPostBasicRequest(urlinfo.get(key).toString(), starpos,
                            1000, baseinfo_id, baseinfo_pswd);
                    if (null != jsonArray && jsonArray.size() > 0)
                    {
                        beanDomain.setCommand(key.toString());
                        KafkaProducerUtil.sedMsgToKafka(jsonArray, beanDomain, jb, SyncDataClient.BASICINFO_TOPIC);
                        continue;
                    }
                    
                    break;
                }
            }
//            else if (key.toString().equalsIgnoreCase("driver"))
//            {
//                for (int starpos = 0; starpos < maxDriver; starpos = starpos + 1000)
//                {
//                    JSONArray jsonArray = HttpClientUtil.sedPostBasicRequest(urlinfo.get(key).toString(), starpos,
//                            1000, userId, userKey);
//                    beanDomain.setCommand("driver");
//                    sedMsgToKafka(jsonArray, beanDomain, jb, SyncDataClient.BASICINFO_TOPIC);
//                }
//            }
        }
    }
    
    
}
