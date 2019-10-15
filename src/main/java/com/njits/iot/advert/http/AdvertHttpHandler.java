package com.njits.iot.advert.http;

import com.njits.iot.advert.common.db.SqlHandler;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdvertHttpHandler implements HttpHandler
{
    
    private static final Logger logger = LoggerFactory.getLogger(AdvertHttpHandler.class);
    
    private String sql;
    
    public AdvertHttpHandler(String sql)
    {
        this.sql = sql;
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException
    {
        String response = null;
        //不开启登录校验
        Map<String, String> paramMap = null;
        String path = "";
        Headers headers = exchange.getRequestHeaders();
        List<String> list = headers.get("Content-type");
        if (list == null || list.isEmpty() || !"application/x-www-form-urlencoded".equalsIgnoreCase(list.get(0)))
        {
            response = "Content-type must be application/x-www-form-urlencoded.";
        }
        else
        {
            if ("GET".equals(exchange.getRequestMethod()))
            {
                path = exchange.getRequestURI().getQuery();
            }
            else
            {
                InputStream is = exchange.getRequestBody();
                BufferedReader in = new BufferedReader(new InputStreamReader(is));
                String line = null;
                StringBuilder sb = new StringBuilder();
                while ((line = in.readLine()) != null)
                {
                    line = URLDecoder.decode(line, "UTF-8");
                    sb.append(line);
                }
                path = sb.toString();
            }
            
            //如果是带参数则获取参数值 普通风格
            if (path != null && !"".equals(path))
            {
                paramMap = new HashMap<>();
                String[] paramArr = path.split("&");
                for (int i = 0; i < paramArr.length; i++)
                {
                    String[] params = paramArr[i].split("=");
                    paramMap.put(params[0], params[1]);
                }
            }
            
            SqlHandler queryHandler = new SqlHandler();
            
            try
            {
                response = queryHandler.execQuery(sql, paramMap);
            }
            catch (SQLException e)
            {
                e.printStackTrace();
                response = e.getMessage() + ", SQL is not consistent with the number of parameters.";
                logger.error("SQL execution error ==> {}", response);
            }
        }
        
        exchange.sendResponseHeaders(200, 0);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes("utf-8"));
        os.close();
    }
}
