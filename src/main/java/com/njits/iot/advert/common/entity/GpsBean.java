package com.njits.iot.advert.common.entity;

import java.sql.Timestamp;

public class GpsBean
{
    
    private String devName;
    private String dtuCode;
    private double lng;
    private double lat;
    private long createDate; //年月日时分秒  交通局
    private Timestamp stamp; //天宇星，xinjiang
    
    public String getDevName()
    {
        return devName;
    }
    
    public void setDevName(String devName)
    {
        this.devName = devName;
    }
    
    public String getDtuCode()
    {
        return dtuCode;
    }
    
    public void setDtuCode(String dtuCode)
    {
        this.dtuCode = dtuCode;
    }
    
    public double getLng()
    {
        return lng;
    }
    
    public void setLng(double lng)
    {
        this.lng = lng;
    }
    
    public double getLat()
    {
        return lat;
    }
    
    public void setLat(double lat)
    {
        this.lat = lat;
    }
    
    public long getCreateDate()
    {
        return createDate;
    }
    
    public void setCreateDate(long createDate)
    {
        this.createDate = createDate;
    }
    
    public Timestamp getStamp()
    {
        return stamp;
    }
    
    public void setStamp(Timestamp stamp)
    {
        this.stamp = stamp;
    }
    
    @Override
    public String toString()
    {
        return this.getDtuCode() + "-" + this.getDevName() + "-" + this.getLat() + ",," + this.getLng() + "-" +
                this.getCreateDate();
    }
}
