package com.njits.iot.advert.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Map.Entry;

import com.njits.iot.advert.server.DataSynServer;
import com.njits.iot.advert.util.JSONUtil;
import com.sun.net.httpserver.HttpServer;

public class AdvertHttpServer {
	
	private static final int port = 8001;

	public static void startup() {
		try {
			HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
			Map<String, Object> map = getSqlMap();
			for (Entry<String, Object> entry : map.entrySet()) {
				String url = entry.getKey();
				String sql = (String) entry.getValue();
				AdvertHttpHandler handler = new AdvertHttpHandler(sql);
				server.createContext(url, handler);
			}
			server.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * 解析JSON文件中的请求与sql的对应关系
	 * 
	 * @return
	 * @throws IOException
	 */
	private static Map<String, Object> getSqlMap() throws IOException {
		InputStream inputStream = DataSynServer.class.getResourceAsStream("/sql.json");
		StringBuilder sb = new StringBuilder();
		String line;
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		return JSONUtil.toMap(sb.toString());
	}
}
