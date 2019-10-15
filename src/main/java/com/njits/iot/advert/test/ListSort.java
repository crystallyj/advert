package com.njits.iot.advert.test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ListSort
{
    public static void main(String[] args)
    {
        String test1 = new String();
        SimpleDateFormat sf = new SimpleDateFormat("dd-MM月-yy");
        SimpleDateFormat sf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try
        {
            System.out.println(sf1.format(sf.parse("19-12月-18").getTime()));
        }
        catch (Exception E)
        {
            E.printStackTrace();
        }
        List<String> result = new ArrayList<String>();
        for (int i = 0; i < 53; i++)
        {
            result.add("item-" + i);
        }
        int totalNum = result.size() % 10 > 0 ? result.size() / 10 + 1 : result.size() / 10;
        System.out.println("共：" + totalNum + " 页");
        System.out.println("第1页数据：=" + getPage(result, 1, totalNum));
        System.out.println("第6页数据：=" + getPage(result, 6, totalNum));
        // System.out.println("第10页数据：=" + getPage(result, 10));
        
    }
    
    public static List<String> getPage(List<String> data, int pageNum, int totalNum)
    
    {
        List<String> getPageData = new ArrayList<>();
        //假设每页10条数据
//        if(pageNum>totalNum)
//        {
//            return getPageData;
//        }
        int startIndex = (pageNum - 1) * 10;
        //如果是最后一页,截取条数为余数值
        if (pageNum == totalNum)
        {
            getPageData = data.subList(startIndex, startIndex + (data.size() % 10));
        }
        else
        {
            getPageData = data.subList(startIndex, startIndex + 10);
        }
        return getPageData;
    }
}
