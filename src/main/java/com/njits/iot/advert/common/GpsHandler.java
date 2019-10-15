package com.njits.iot.advert.common;

import java.util.List;

import com.njits.iot.advert.common.entity.GpsBean;

public interface GpsHandler {

	public boolean sendMsg(List<GpsBean> list);
}
