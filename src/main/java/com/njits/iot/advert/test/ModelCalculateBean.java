package com.njits.iot.advert.test;

public class ModelCalculateBean
{
    public ModelBean generateBean(String f1, String f2, String f3)
    {
        ModelBean bean = new ModelBean();
        bean.setName(f1);
        bean.setTimes(3);
        bean.setFilter("passt-" + f3);
        return bean;
    }
}
