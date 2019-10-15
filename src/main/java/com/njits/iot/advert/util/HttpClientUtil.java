package com.njits.iot.advert.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class HttpClientUtil
{
    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
    
    public static void main(String[] args)
    {
        Map<String, Object> param = new HashMap<>();
//        param.put("startTime", "2019-06-21 17:30:00".trim().replaceAll(" ", "+"));
//        param.put("endTime", "2019-06-21+17:40:00".trim().replaceAll(" ", "+"));
//        param.put("startPos", 0);
//        param.put("endPos", 10);
        param.put("carNum", "");
        param.put("startTime", "");
        param.put("endTime", "");
        param.put("userId", "");
        param.put("url", "");
        param.put("taskKey", "");
        JSONArray jsonArray = sedGetBasicRequest("http://61.161.206.242:48081/getMdt", "streamax", "admin!@#", null);
        //  JSONArray jsonArray = sedGetBasicRequest("http://192.168.44.172:8082/taxi/notifyMsg/getRMSuccess", null,
        // null
        //       , param);
        System.out.println(jsonArray);
        jsonArray = sedPostBasicRequest("http://61.161.206.242:48081/getCar", 0, 10, "streamax", "admin!@#");
        System.out.println(jsonArray);
    }
    
    
    public static JSONArray sedGetBasicRequest(String url, String userId, String userKey, Map<String, Object> param)
    {
        
        StringBuilder urlWithParam = new StringBuilder(url);
        try
        {
            
            boolean first = true;
            if (null != param)
            {
                for (String pre : param.keySet())
                {
                    if (first)
                    {
                        urlWithParam.append("?").append(pre).append("=").append(param.get(pre).toString().trim());
                        first = false;
                    }
                    else
                    {
                        urlWithParam.append("&").append(pre).append("=").append(param.get(pre).toString().trim());
                    }
                }
            }
            int trytimes = 0;
            HttpClient client = HttpClients.createDefault();
            System.out.println("==========" + urlWithParam.toString());
            HttpGet get = new HttpGet(urlWithParam.toString());
            get.addHeader("Content-Type", "application/x-www-form-urlencoded");
            if (null != userId && !"".equals(userId))
            {
                get.addHeader("userId", userId);
                get.addHeader("userKey", userKey);
            }
            RequestConfig requestConfig =
                    RequestConfig.custom().setConnectTimeout(30000).setConnectionRequestTimeout(30000).setSocketTimeout(30000).build();
            get.setConfig(requestConfig);
            HttpResponse httpResponse = null;
            String reponseStr = null;
            JSONArray responArry = null;
            while (trytimes < 3)
            {
                trytimes++;
                try
                {
                    httpResponse = client.execute(get);
                    if (httpResponse.getStatusLine().getStatusCode() == 200)
                    {
                        reponseStr = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
                        responArry = JSONUtil.getARRAYFromString(reponseStr);
                        
                        break;
                    }
                    else
                    {
                        logger.error("Get response not 200={},,responseCode={} tryTimes={}...", urlWithParam,
                                httpResponse.getStatusLine().getStatusCode(), trytimes);
                        continue;
                    }
                }
                catch (ConnectionClosedException | HttpHostConnectException e)
                {
                    logger.error("Request Get ConnectionRefused={},tryTimes={}...", urlWithParam, trytimes);
                    continue;
                }
                catch (JSONException e)
                {
                    logger.error("Request Get JSONParse Error={},tryTimes={}...result={}", urlWithParam, trytimes,
                            reponseStr);
                    continue;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    logger.error("Request Get={} Error,, tryTimes={}...e={}", urlWithParam, trytimes, e.getMessage());
                    continue;
                }
            }
            
            return responArry;
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("[Get Error......],,,url={},, e={}", urlWithParam, e.getMessage());
        }
        return null;
    }
    
    public static JSONArray sedPostBasicRequest(String url, int startposi, int length, String userId, String userKey)
    {
        StringBuilder withParam = new StringBuilder(url);
        try
        {
            int trytimes = 0;
            HttpClient client = HttpClients.createDefault();
            
            withParam.append("?startPos=").append(startposi).append("&endPos=").append(length);
            HttpPost post = new HttpPost(withParam.toString());
            RequestConfig requestConfig =
                    RequestConfig.custom().setConnectTimeout(30000).setConnectionRequestTimeout(30000).setSocketTimeout(30000).build();
            post.addHeader("Content-Type", "application/x-www" + "-form-urlencoded");
            post.addHeader("userId", userId);
            post.addHeader("userKey", userKey);
            post.setConfig(requestConfig);
            HttpResponse httpResponse = null;
            String reponseStr = null;
            JSONArray responArry = null;
            while (trytimes < 3)
            {
                trytimes++;
                
                try
                {
                    httpResponse = client.execute(post);
                    if (httpResponse.getStatusLine().getStatusCode() == 200)
                    {
                        
                        reponseStr = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
                        responArry = JSONUtil.getARRAYFromString(reponseStr);
                        
                        break;
                    }
                    else
                    {
                        logger.error("Post Response not 200={} ,,responseCode={} tryTimes={}...", withParam,
                                httpResponse.getStatusLine().getStatusCode(), trytimes);
                        continue;
                    }
                }
                catch (ConnectionClosedException | HttpHostConnectException e)
                {
                    logger.error("Request Post ConnectionRefused={},tryTimes={},startPos={}..", withParam, trytimes,
                            startposi);
                    continue;
                }
                catch (JSONException e)
                {
                    logger.error("Request Post JSONParse Error={},tryTimes={},startPos={}..result={}", withParam,
                            trytimes, startposi, reponseStr);
                    continue;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    logger.error("Request Post={} Error,tryTimes={},startPos={}..e={}", withParam, trytimes,
                            startposi, e.getMessage());
                    continue;
                }
                
            }
            
            return responArry;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("[Post Error......],,,url={},startPos={},, e={}", withParam, startposi, e.getMessage());
        }
        return null;
    }
}

