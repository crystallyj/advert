package com.njits.iot.advert.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.mysql.jdbc.StringUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * json操作工具类
 *
 * @author 43797
 * @date 2018年12月20日 下午10:00:05
 */
public class JSONUtil
{
    
    static
    {
        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
    }
    
    /**
     * 默认json格式化方式
     */
    protected static final SerializerFeature[] DEFAULT_FORMAT = {SerializerFeature.WriteDateUseDateFormat,
            SerializerFeature.WriteEnumUsingToString, SerializerFeature.WriteNonStringKeyAsString,
            SerializerFeature.QuoteFieldNames, SerializerFeature.SkipTransientField, SerializerFeature.SortField,
            SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue};
    
    /**
     * 从json获取指定key的字符串
     *
     * @param json
     * @param key
     * @return
     */
    public static Object getStringFromJSONObject(final String json, final String key)
    {
        return JSON.parseObject(json).getString(key);
    }
    
    /**
     * 通过Map生成一个json字符串
     *
     * @param map
     * @return
     */
    public static <T> String toJson(final Map<String, T> map)
    {
        return JSON.toJSONString(map, DEFAULT_FORMAT);
    }
    
    /**
     * 通过List生成一个json字符串
     *
     * @param list
     * @return
     */
    public static <T> String toJson(List<T> list)
    {
        return JSON.toJSONString(list, DEFAULT_FORMAT);
    }
    
    /**
     * 将传入的json字符串转换成List
     *
     * @param jsonString
     * @param beanClass
     * @return
     */
    public static <T> List<T> toList(final String jsonString, final Class<T> beanClass)
    {
        return JSON.parseArray(jsonString, beanClass);
    }
    
    /**
     * 将传入的json字符串转换成Map
     *
     * @param jsonString
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> toMap(final String jsonString)
    {
        return (Map<String, T>) getJSONFromString(jsonString);
    }
    
    /**
     * 将字符串转换成JSON字符串
     *
     * @param jsonString
     * @return
     */
    public static JSONObject getJSONFromString(final String jsonString)
    {
        if (StringUtils.isNullOrEmpty(jsonString))
        {
            return new JSONObject();
        }
        return JSON.parseObject(jsonString);
    }
    
    /**
     * 将字符串转换成JSON
     *
     * @param jsonString
     * @return
     */
    public static JSONArray getARRAYFromString(final String jsonString) throws Exception
    {
        if (StringUtils.isNullOrEmpty(jsonString))
        {
            return new JSONArray();
        }
        return JSON.parseArray(jsonString);
    }
    
    /**
     * 将resultSet转化为JSON列表
     *
     * @param rs
     * @return
     * @throws SQLException
     * @throws JSONException
     */
    public static List<JSONObject> resultSetToJsonList(ResultSet rs) throws SQLException, JSONException
    {
        // 获取列数
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        List<JSONObject> list = new ArrayList<>();
        // 遍历ResultSet中的每条数据
        while (rs.next())
        {
            JSONObject jsonObj = new JSONObject();
            // 遍历每一列
            for (int i = 1; i <= columnCount; i++)
            {
                String columnName = metaData.getColumnLabel(i);
                String value = rs.getString(columnName);
                jsonObj.put(underline2Camel(columnName), value);
            }
            list.add(jsonObj);
        }
        return list;
    }
    
    /**
     * 下划线转驼峰法
     *
     * @param line
     * @return
     */
    public static String underline2Camel(String line)
    {
        if (line == null || "".equals(line))
        {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        Pattern pattern = Pattern.compile("([A-Za-z\\d]+)(_)?");
        Matcher matcher = pattern.matcher(line);
        // 匹配正则表达式
        while (matcher.find())
        {
            String word = matcher.group();
            // 当是true 或则是空的情况
            if (matcher.start() == 0)
            {
                sb.append(Character.toLowerCase(word.charAt(0)));
            }
            else
            {
                sb.append(Character.toUpperCase(word.charAt(0)));
            }
            int index = word.lastIndexOf('_');
            if (index > 0)
            {
                sb.append(word.substring(1, index).toLowerCase());
            }
            else
            {
                sb.append(word.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }
}
