package com.njits.iot.advert.common.entity;

import java.util.HashMap;
import java.util.Map;

public class Constant {

	public static final String ENCODE_RULE = "sanbao";
	public static final int PAUSE_TIME = 3*1000;
	public static final int RETRY_COUNT = 3;
	
	public static final int BIG_BATCH_SIZE = 200000;
	//同步到交通厅的批次大小（不超过1M）
	public static final int BATCH_SIZE = 8000;
	public static final int BATCH_905 = 10;
	
	//毫秒
	public static final long TIME_LIMIT = 15*24*60*60*1000;
	//分钟，每多少分钟执行一次同步，位置数据包，因武汉交通厅系统统计在线频率为5分钟，所以，同步频率小于5分钟
	public static final int INTERVAL_TRAFFIC = 4;
	//分钟，每多少分钟执行一次同步，位置数据包，天宇星
	public static final int INTERVAL_TYX = 3;
	//分钟，每多少分钟执行一次同步，位置数据包，南京
	public static final int INTERVAL_NJITS = 2;
	
	public static final int appType_Traffic = 1;
	public static final int appType_TYX = 2;
	public static final int appType_ITS = 3;
	
	//上次读取文件的位置记录
	public static Map<String, String> positionMap = new HashMap<>();
	
	public static String KEY_TRAFFIC = "traffic";
	public static String KEY_TYX = "tyx";
	public static String KEY_NJITS = "njits";
}
