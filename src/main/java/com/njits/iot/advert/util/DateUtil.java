package com.njits.iot.advert.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期工具类
 *
 * @author 43797
 * @date 2019年1月16日 下午12:16:55
 */
public class DateUtil
{
    
    public static final String YYYYMMDD_HHMMSS = "yyyyMMddHHmmss";
    public static final String YYMMDD_HHMMSS = "yyMMddHHmmss";
    public static final String SEG_YYYYMMDD_HHMMSS = "yyyy-MM-dd HH:mm:ss";
    
    public static void main(String[] args)
    {
        System.out.println(DateUtil.millinToMillon(1562565049));
    }
    
    /**
     * 获取当前日期时间
     *
     * @param format
     * @return
     */
    public static String getCurrentDateTime(String format)
    {
        return getCurrentDateTime(new Date(), format);
    }
    
    /**
     * 根据样式获取当前日期时间
     *
     * @param date
     * @param format
     * @return
     */
    public static String getCurrentDateTime(Date date, String format)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }
    
    /**
     * 毫秒差
     *
     * @param endDate
     * @param beginDate
     * @return
     */
    public static long diffMilliSeconds(Date endDate, Date beginDate)
    {
        return (endDate.getTime() - beginDate.getTime());
    }
    
    /**
     * 当前绝对毫秒数
     *
     * @return
     */
    public static long nowAbsMilliSeconds()
    {
        return System.currentTimeMillis();
    }
    
    /**
     * 根据样式获取当前时间字符串
     *
     * @param format
     * @return
     */
    public static String nowDateStr(String format)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date());
    }
    
    /**
     * 获取当前时间字符串
     *
     * @return
     */
    public static String nowDateStr()
    {
        return nowDateStr(SEG_YYYYMMDD_HHMMSS);
    }
    
    /**
     * 毫秒根据样式转日期
     *
     * @param timeStamp
     * @param format
     * @return
     */
    public static String milliSecond2Date(long timeStamp, String format)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(timeStamp);
    }
    
    /**
     * 毫秒转日期
     *
     * @param timeStamp
     * @return
     */
    public static String milliSecond2Date(long timeStamp)
    {
        return milliSecond2Date(timeStamp, SEG_YYYYMMDD_HHMMSS);
    }
    
    public static long millinToMillon(long timestamp)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(YYYYMMDD_HHMMSS);
        return Long.valueOf(sdf.format(timestamp));
    }
    
    /**
     * 字符串转日期
     *
     * @param str
     * @return
     */
    public static Date string2Date(String str)
    {
        return string2Date(str, DateUtil.SEG_YYYYMMDD_HHMMSS);
    }
    
    /**
     * 字符串根据样式转日期
     *
     * @param str
     * @param format
     * @return
     */
    public static Date string2Date(String str, String format)
    {
        Date date = null;
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        if (str == null || "".equals(str))
        {
            date = new Date();
            try
            {
                date = formatter.parse(str);
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
            return date;
        }
        
        try
        {
            date = formatter.parse(str);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        return date;
    }
}
