package com.njits.iot.advert.shenyang;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  同步给沈阳交警的应用
 * @author 43797
 * 启动命令：java -classpath "advert-1.0-jar-with-dependencies.jar" com.njits.iot.advert.shenyang.UDPClient "127.0.0.1" 8999 test107
 */
public class UDPClient {
	
	private static final Logger logger = LoggerFactory.getLogger(UDPClient.class);
	
	public static void main(String[] args) {
		
		
//		args = new String[]{"127.0.0.1", "8999", "test110"};
		
		if(args.length != 3) {
			logger.error("Parameter error, number of parameters should be 3");
			return;
		}
		
		String serverIp = args[0];
		int serverPort = Integer.parseInt(args[1]);
		String groupId = args[2];
		
		logger.info("serverIp:{},serverPort:{},groupId:{}",serverIp,serverPort,groupId);
		
		for(int i=0;i<3;i++) {
			UDPThread thread = new UDPThread(serverIp, serverPort, i, groupId);
			thread.start();
		}
	}
}
