package com.njits.iot.advert.baseinfo.advert;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.njits.iot.advert.baseinfo.BaseBeanDomain;
import com.njits.iot.advert.baseinfo.SyncDataClient;
import com.njits.iot.advert.common.entity.Constant;
import com.njits.iot.advert.util.DateUtil;
import com.njits.iot.advert.util.FileUtil;
import com.njits.iot.advert.util.HttpClientUtil;
import com.njits.iot.advert.util.KafkaProducerUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AdvertHttpClient implements Job

{
    private static final Logger logger = LoggerFactory.getLogger(AdvertHttpClient.class);
    
    public static void main(String[] args)
    {
        Constant.positionMap.put("420100_order_detail", "position/njits.txt");
        Constant.positionMap.put("420100_dev_detail", "position/njits.txt");
        Constant.positionMap.put("210100_order_detail", "position/njits.txt");
        Constant.positionMap.put("210100_dev_detail", "position/njits.txt");
        SyncDataClient.init();
        AdvertHttpClient client = new AdvertHttpClient();
        client.execute(null);
    }
    
    @Override
    public void execute(JobExecutionContext jobExecutionContext)
    {
        logger.info("begin 10min quartz....AdvertHttpClient");
        for (int i = 0; i < SyncDataClient.cityInfo.size(); i++)
        {
            //同步数据
            getAdvertTerms(SyncDataClient.cityInfo.getJSONObject(i));
            
        }
    }
    
    public static void getAdvertTerms(JSONObject jb)
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
        gernateAdvContent(beanDomain, jb);
    }
    
    public static void gernateAdvContent(BaseBeanDomain beanDomain, JSONObject jb)
    {
        boolean updatePosi = false;
        String frim = jb.getJSONObject("advertInfo").getString("frim");
        beanDomain.setFrim(frim);
        Map urlinfo = (Map) jb.getJSONObject("advertInfo");
        try
        {
            for (Object key : urlinfo.keySet())
            {
                beanDomain.setCommand(key.toString());
                long maxData = 500000l;
                Map<String, Object> param = new HashMap<>();
                String startDate = "";
                String endDate = "";
                File positionFile = null;
                //10min 带参数 order_detail\dev_list\dev_detail
                if (key.toString().equalsIgnoreCase("order_detail") || key.toString().equalsIgnoreCase("dev_detail") ||
                        key.toString().equalsIgnoreCase("dev_list"))
                {
                    
                    if (key.toString().equalsIgnoreCase("dev_list"))
                    {
                        updatePosi = true;
                    }
                    if (key.toString().equalsIgnoreCase("order_detail") ||
                            key.toString().equalsIgnoreCase("dev_detail"))
                    {
                        
                        //记录上次读取数据位置
                        positionFile = new File(Constant.positionMap.get(
                                beanDomain.getArea_code() + "_" + key.toString()));
                        startDate = FileUtil.readTxt(positionFile, "utf-8").trim();
                        long timeStamp = System.currentTimeMillis() - 1000 * 10;//延时10秒
                        endDate = DateUtil.milliSecond2Date(timeStamp).trim();
                        param.put("startTime", startDate.replaceAll(" ", "+"));
                        param.put("endTime", endDate.replaceAll(" ", "+"));
                        logger.debug("10min 带参数.............{}.........{},{}", key.toString(),
                                beanDomain.getArea_code(), param);
                    }
                    for (int startPos = 0; startPos < maxData; startPos = startPos + 1000)
                    {
                        param.put("startPos", startPos);
                        param.put("endPos", 1000);
                        JSONArray jsonArray = HttpClientUtil.sedGetBasicRequest(urlinfo.get(key).toString(), null,
                                null, param);
                        //如果抽取到数据
                        if (null != jsonArray)
                        {
                            if (jsonArray.size() > 0)
                            {
                                if (!updatePosi)
                                {
                                    updatePosi = true;
                                    //更新Position
                                    FileUtil.writeFile(endDate, positionFile, false);
                                    logger.info("city:{}  command: {} update  position={}", beanDomain.getCity(),
                                            key.toString(), endDate);
                                }
                                
                                KafkaProducerUtil.sedMsgToKafka(jsonArray, beanDomain, jb,
                                        SyncDataClient.ADVERTISE_TOPIC);
                                continue;
                            }
                            if (jsonArray.size() == 0)
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
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("ERROR.......generate advcContengt" + e.getMessage());
        }
    }
}
