package com.njits.iot.advert.shenyang.flume;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.flume.Channel;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.Transaction;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.njits.iot.advert.util.ByteUtil;

public class UDPSinker extends AbstractSink implements Configurable {

	private static final Logger logger = LoggerFactory.getLogger(UDPSinker.class);

	// 3、创建DatagramSocket对象
	private static DatagramSocket datagramSocket;
	private static InetAddress inetAddress;
	private static int port;
	private static int batchSize;

	@Override
	public Status process() throws EventDeliveryException {
		Transaction transaction = null;
		Event event = null;
		Status status = null;
		DatagramPacket datagramPacket = null;
		byte[] bytes = new byte[] {};
		try {
			Channel ch = getChannel();
			transaction = ch.getTransaction();
			transaction.begin();
			for (int i = 1; i <= batchSize; i++) {
				event = ch.take();
				if (event != null) {
					bytes = ByteUtil.concatAll(bytes, event.getBody());
				}
				if(i%50==0) {
					// 创建数据报，包含发送的信息
					datagramPacket = new DatagramPacket(bytes, bytes.length, inetAddress, port);
					// 向服务器端发送数据报
					datagramSocket.send(datagramPacket);
					bytes = new byte[] {};
					Thread.sleep(1);
//					logger.debug("send by upd success, data:{}", new String(bytes));
				}
			}
			status = Status.READY;
			transaction.commit();
			logger.info("========{} batch send success ==========", batchSize);
		} catch (Exception e) {
			if (transaction != null) {
				transaction.rollback();
				transaction.close();
				status = Status.BACKOFF;
			}
			e.printStackTrace();
		} finally {
			if (transaction != null) {
				transaction.close();
			}
		}
		return status;
	}

	// 从配置文件中读取各种属性，并进行一些非空验证
	@Override
	public void configure(Context context) {
		// 1、定义服务器地址、端口号、数据
		try {
			inetAddress = InetAddress.getByName(context.getString("updServer"));
			port = context.getInteger("updPort");
			batchSize = context.getInteger("batchSize");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	// 在整个sink开始时执行一遍，用来初始化数据库连接
	@Override
	public synchronized void start() {
		try {
			datagramSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	// 在整个sink结束时执行一遍
	@Override
	public synchronized void stop() {
		datagramSocket.close();
		super.stop();
	}
}
