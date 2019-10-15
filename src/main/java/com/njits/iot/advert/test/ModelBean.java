package com.njits.iot.advert.test;

public class ModelBean
{
    public String name;
    public int times;
    public String filter;
    
    public void setFilter(String filter)
    {
        this.filter = filter;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public void setTimes(int times)
    {
        this.times = times;
    }
    
    public int getTimes()
    {
        return times;
    }
    
    public String getName()
    {
        return name;
    }
    
    public String getFilter()
    {
        return filter;
    }
}
