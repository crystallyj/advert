package com.njits.iot.advert.shenyang.flume;

import com.alibaba.fastjson.JSONObject;
import com.njits.iot.advert.util.DateUtil;

import org.apache.commons.compress.utils.Charsets;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SynData implements Interceptor {
	
	private static final Logger logger = LoggerFactory.getLogger(SynData.class);
	private static final String head = "ASYN GPS ITSVSTTP2T2019022502";
	private static final String delimiter = "|";
	private static final String connector = "-";
	private static final String lineSplit = "\r\n";
	private DecimalFormat df = new DecimalFormat("0"); 

	@Override
	public void initialize() {

	}

	public static String getTime() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
		return df.format(new Date());
	}

	@Override
	public Event intercept(Event event) {
		String line = new String(event.getBody(), Charsets.UTF_8);
		event.setBody("".getBytes());
		if("".equals(line)) {
			return null;
		}
		try {
			JSONObject jsb = JSONObject.parseObject(line);
			String command = jsb.getString("command");//track
			String frim = jsb.getString("frim");//rm
			String carType = jsb.getString("car_type");//cz
			String areaCode = jsb.getString("area_code");//210100
			
			if(!"track".equals(command) || !"rm".equals(frim) || !"cz".equals(carType) || !"210100".equals(areaCode)) {
				return null;
			}
			
			// 样例： ASYN GPS MMC8000GPSANDASYN051113-38491-00000000|1552349434|0|123457585|41807490|0.1295|11.27|00000000|00040000|
			String imei = jsb.getString("imei");//设备号
			long stamp = DateUtil.string2Date(jsb.getString("stamp")).getTime()/1000;//时间戳
			String mileage = "0";//里程
			String longitude = df.format(jsb.getDouble("longitude")*1000000);//经度
			String latitude = df.format(jsb.getDouble("latitude")*1000000);//纬度
			
			double d = jsb.getDouble("speed")/3.6;
			BigDecimal bd = new BigDecimal(d);
			String speed = String.valueOf(bd.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue());//速度
			String directionAngle = jsb.getString("direction_angle");//方向
			String alarmState = "00000000";//报警字,跟交通局沟通过，不需要报警字段，全部设置为0
			String travelState = getTravelState(jsb.getString("veh_state"));//状态字,跟交通局沟通过，只需要车辆状态字段
			String information = "";//信息字段
			
			//ASYN <类型> <设备ID号>|<时间>|<里程>|<经度>|<纬度>|<速度>|<方向>|<报警字>|<状态字>|<信息字段>
			StringBuilder sb = new StringBuilder();
			sb.append(head).append(connector).append(imei).append(connector).append("00000000").append(delimiter);
			sb.append(stamp).append(delimiter);
			sb.append(mileage).append(delimiter);
			sb.append(longitude).append(delimiter);
			sb.append(latitude).append(delimiter);
			sb.append(speed).append(delimiter);
			sb.append(directionAngle).append(delimiter);
			sb.append(alarmState).append(delimiter);
			sb.append("00").append(travelState).append("0000").append(delimiter);
			sb.append(information);
			sb.append(lineSplit);
			
			logger.info("========= Source ======== {}", stamp);
//			logger.info("=========ASYN GPS======== {}", sb.toString());
			
			event.setBody(sb.toString().getBytes());
			
			return event;
		} catch (Exception e) {
			logger.error("Data transmission error, data:{}, e:{}", line, e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public String getTravelState(String jsonString) {
		JSONObject jsb = JSONObject.parseObject(jsonString);
		String b0 = jsb.getString("b11");
		String b1 = jsb.getString("b13");
		String b2 = jsb.getString("b9");
		String b3 = jsb.getString("b8");
		String ret = Integer.toHexString(Integer.valueOf("0000"+b3+b2+b1+b0,2));
		return "0"+ret;
	}

	@Override
	public List<Event> intercept(List<Event> events) {
		List<Event> intercepted = new ArrayList<>();
		for (Event event : events) {
			Event interceptedEvent = intercept(event);
			if (interceptedEvent != null && interceptedEvent.getBody().length > 0) {
				intercepted.add(interceptedEvent);
			}
		}
		return intercepted;
	}

	@Override
	public void close() {
		
	}
	
	public static class Builder implements Interceptor.Builder {
        //使用Builder初始化Interceptor
        @Override
        public Interceptor build() {
            return new SynData();
        }

        @Override
        public void configure(Context context) {

        }
    }
}
