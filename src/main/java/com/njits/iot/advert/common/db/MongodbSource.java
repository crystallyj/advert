package com.njits.iot.advert.common.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.njits.iot.advert.common.entity.GpsBean;
import com.njits.iot.advert.tyx.CarInfoHandler;
import com.njits.iot.advert.util.DateUtil;
import com.njits.iot.advert.util.StringUtil;

public class MongodbSource {
	
	private MongoDatabase db;
	private MongoClient mongoClient;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public void init() {
		InputStream is = DataSource.class.getResourceAsStream("/base.properties");
		Properties prop = new Properties();
		try {
			prop.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String ip = prop.getProperty("mongodb.ip");
		int port = Integer.parseInt(prop.getProperty("mongodb.port"));
		String database = prop.getProperty("mongodb.database");
		mongoClient = new MongoClient(ip, port);
		//获取指定数据库对象
		db = mongoClient.getDatabase(database);
	}

	public static void main(String[] args) {
		CarInfoHandler.execQueryCarInfo();
		String startDate = "2019-04-18 13:30:46";
		String endDate = "2019-04-28 08:20:50";
		MongodbSource mongodbSource = new MongodbSource();
		List<GpsBean> list = mongodbSource.queryGpsList(startDate, endDate);
		System.out.println(list.size());
	}
	
	public List<GpsBean> queryGpsList(String startDate, String endDate){
		init();
		//获取指定集合对象
		MongoCollection<Document> dataGPS = db.getCollection("dataGPS");
		//加入查询条件
        BasicDBObject query = new BasicDBObject();
        DBObject dbObject = new BasicDBObject();
        try {
        	dbObject.put("$gte", sdf.parse(startDate));
        	dbObject.put("$lte",  sdf.parse(endDate));
		} catch (ParseException e) {
			e.printStackTrace();
		}
        
        query.put("createDate", dbObject);
        query.put("activeFlag", 1);
        //skip 是分页查询，从第0条开始查10条数据。 Sorts是排序用的。有descending 和ascending
		MongoCursor<Document> cursor = dataGPS.find(query).iterator();
		List<GpsBean> retList = new ArrayList<>();
		while(cursor.hasNext()) {
			Document doc = cursor.next();
			GpsBean bean = new GpsBean();
			bean.setDtuCode(doc.getString("dtuCode"));
			
			String devName = CarInfoHandler.carInfoMap.get(doc.getString("dtuCode"));
			if(StringUtil.isNullEmpty(devName)) {
				continue;
			} 
			if(devName.indexOf("鄂")==-1) {
				devName = "鄂A" + devName;
			}
			bean.setDevName(devName);
			bean.setLat(Double.parseDouble(doc.getString("lat")));
			bean.setLng(Double.parseDouble(doc.getString("lng")));
			Date date = doc.getDate("createDate");
			bean.setCreateDate(Long.parseLong(DateUtil.getCurrentDateTime(date, DateUtil.YYYYMMDD_HHMMSS)));
			bean.setStamp(new Timestamp(date.getTime()));
			
			retList.add(bean);
		}
		
		mongoClient.close();
		return retList;
	}
}
