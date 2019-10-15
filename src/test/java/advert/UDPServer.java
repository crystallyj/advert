package advert;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务器端，实现基于UDP的用户登录 Created by Jim Calark on 2017/3/19.
 */
public class UDPServer {
	
	private static final Logger logger = LoggerFactory.getLogger(UDPServer.class);
	
	public static void main(String[] args) throws SocketException, IOException {
		// 1、创建服务器端DatagramSocket,指定端口
		DatagramSocket datagramSocket = new DatagramSocket(8999);
//		datagramSocket.setReceiveBufferSize(32*1024);
		// 2、创建数据报，用于接受客户端发送的数据
		byte[] data = new byte[8192];// 创建字节数组，指定接受的数据报的大小
		DatagramPacket datagramPacket = new DatagramPacket(data, data.length);
		// 3、接收客户端发送的数据
		System.out.println("服务器已经开启，等待客户端的连接");
		int i = 0;
		while(true) {
			// 此方法在接收到数据之前会一直阻塞
			datagramSocket.receive(datagramPacket);
			// 4、读取客户端发送的数据
			// 参数： data 要转换的数组 0 从数组的下标0 开始 datagramPacket.getLength() 长度为接收到的长度
			String info = new String(data, 0, datagramPacket.getLength());
			i++;
			logger.info(i+" ========" + info);
		}

		/**
		 * 向客户端进行响应
		 *//*
		// 1、定义客户端的地址、端口号、数据
		// 获取客户端 ip地址
		InetAddress inetAddress = datagramPacket.getAddress();
		// 获取客户端端口号
		int port = datagramPacket.getPort();
		// 将要响应的内容保存到byte数组中
		byte[] data2 = "欢迎您！".getBytes();
		// 2创建数据报，包含响应的数据信息
		DatagramPacket datagramPacket12 = new DatagramPacket(data2, data2.length, inetAddress, port);
		// 3、响应客户端
		datagramSocket.send(datagramPacket12);
		// 4、关闭资源
		datagramSocket.close();*/
	}
}