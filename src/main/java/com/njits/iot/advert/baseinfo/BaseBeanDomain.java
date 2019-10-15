package com.njits.iot.advert.baseinfo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.njits.iot.advert.util.JSONUtil;

public class BaseBeanDomain
{
    public String timestamp;
    public String frim;
    public String prov;
    public String city;
    public String area_code;
    public String car_type;
    public String command;
    public Object content;
    
    
    public String getTimestamp()
    {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp)
    {
        this.timestamp = timestamp;
    }
    
    public String getFrim()
    {
        return frim;
    }
    
    public void setFrim(String frim)
    {
        this.frim = frim;
    }
    
    public String getProv()
    {
        return prov;
    }
    
    public void setProv(String prov)
    {
        this.prov = prov;
    }
    
    public String getCity()
    {
        return city;
    }
    
    public void setCity(String city)
    {
        this.city = city;
    }
    
    public String getArea_code()
    {
        return area_code;
    }
    
    public void setArea_code(String area_code)
    {
        this.area_code = area_code;
    }
    
    public String getCar_type()
    {
        return car_type;
    }
    
    public void setCar_type(String car_type)
    {
        this.car_type = car_type;
    }
    
    public String getCommand()
    {
        return command;
    }
    
    public void setCommand(String command)
    {
        this.command = command;
    }
    
    public Object getContent()
    {
        return content;
    }
    
    public void setContent(Object content)
    {
        this.content = content;
    }
    
    public static void main(String[] args)
    {
        
        BaseBeanDomain driver = new BaseBeanDomain();
        //DriverItem item = new DriverItem();
        driver.setArea_code("210100");
        driver.setCar_type("cz");
        driver.setCity("rm");
        // driver.setCommand("car");
        driver.setFrim("ff");
        driver.setProv("liaoning");
        driver.setTimestamp("2019");
        //  item.setAddress("127.0.0.0");
        // item.setAlipayVersion("cccc");
        //  item.setAssessmentCompany("192.0.0");
        // List<DriverItem> dr = new ArrayList<DriverItem>();
        // dr.add(item);
        String jbStr = "{\n" + "        \"channelNo\":1,\n" + "        \"esn\":0,\n" + "        \"gatewayId\":0,\n" +
                "        \"groupId\":1,\n" + "        \"iccid\":\"\",\n" + "        \"id\":1,\n" +
                "        \"inUse\":false,\n" + "        \"mac\":null,\n" + "        \"mdtid\":1004277807,\n" +
                "        \"module\":null,\n" + "        \"phoneNo\":\"\",\n" + "        \"protocalversion\":0,\n" +
                "        \"remark\":\"\",\n" + "        \"type\":\"TPJ-Elysee\",\n" + "        \"version\":null\n" +
                "    }";
        JSONObject jbObj = JSONUtil.getJSONFromString(jbStr);
        JSONArray jaRaay = new JSONArray();
        jaRaay.add(jbObj);
        driver.setContent(jaRaay);
        String jb = JSONObject.toJSONString(driver, SerializerFeature.WriteMapNullValue);
        System.out.println("jb===" + jb);
    }
}
