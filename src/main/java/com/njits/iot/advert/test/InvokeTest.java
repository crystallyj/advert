package com.njits.iot.advert.test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class InvokeTest
{
    public static void main(String[] args)
    {
        List<String> data = new ArrayList<String>();
        data.add("11111");
        data.add("22222");
        data.add("33333");
        data.add("44444");
        try
        {
            Class client1 = Class.forName("com.njits.iot.advert.test.ClientAnalysic1");
            Method analysicData = client1.getMethod("analysicData", Long.TYPE, String.class);
            
            for (String key : data)
            {
                analysicData.invoke(client1.newInstance(), Integer.valueOf(key), key);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
