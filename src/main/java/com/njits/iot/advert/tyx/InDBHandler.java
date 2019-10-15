package com.njits.iot.advert.tyx;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.njits.iot.advert.common.GpsHandler;
import com.njits.iot.advert.common.db.SqlHandler;
import com.njits.iot.advert.common.entity.GpsBean;

public class InDBHandler implements GpsHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(InDBHandler.class);
	
	public static InDBHandler instance;
	
	public static InDBHandler getInstance() {
		if (instance == null) {
			instance = new InDBHandler();
		}
		return instance;
	}
	
	public boolean sendMsg(List<GpsBean> list) {
		SqlHandler handler = new SqlHandler();
		//防止插入重复数据
		String sql = "insert into XJCZC_SSWZXX_TAB_HISTORY(deciveid,plate,stamp,longitude,latitude) \n"
				+ "values(?,?,?,?,?)";
		try {
			handler.execInsertTYXList(sql, list);
		} catch (SQLException e) {
			logger.error("sql={}, msg={}", sql, e.getMessage());
			return false;
		}
		
		try {
			//防止插入重复数据，最新位置数据
			sql = "replace into XJCZC_SSWZXX_TAB(deciveid,plate,stamp,longitude,latitude) \n"
					+ "values(?,?,?,?,?)";
			Map<String, GpsBean> map = new HashMap<>();
			//获取这批数据中每辆车时间最新的一条记录
			for(GpsBean gspBean : list) {
				GpsBean tmp = map.get(gspBean.getDtuCode());
				if(tmp==null) {
					map.put(gspBean.getDtuCode(), gspBean);
				} else {
					if(gspBean.getStamp().getTime() > tmp.getStamp().getTime()) {
						map.put(gspBean.getDtuCode(), gspBean);
					}
				}
			}
			//将map中的value值转list
			Collection<GpsBean> valueCollection = map.values();
			List<GpsBean> valueList = new ArrayList<>(valueCollection);
			handler.execInsertTYXList(sql, valueList);
			
			//防止插入重复数据，最新位置数据
			sql = "insert ignore into CAR_TAB(plate,company) \n"
					+ "values(?,'njits')";	
			handler.execInsertCarTYXList(sql, valueList);
		} catch (SQLException e) {
			logger.error("sql={}, msg={}", sql, e.getMessage());
			return false;
		}
		return true;
	}
}
