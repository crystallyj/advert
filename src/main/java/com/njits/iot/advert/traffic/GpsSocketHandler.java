package com.njits.iot.advert.traffic;

import com.njits.iot.advert.common.GpsHandler;
import com.njits.iot.advert.common.entity.Constant;
import com.njits.iot.advert.common.entity.GpsBean;
import com.njits.iot.advert.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

public class GpsSocketHandler implements GpsHandler
{
    
    private static final Logger logger = LoggerFactory.getLogger(GpsSocketHandler.class);
    
    public static String IP = "";
    public static int PORT = 0;
    public static String USER_NAME = "";
    public static String PASSWORD = "";
    
    public static Socket socket;
    public static OutputStream os;
    public static InputStream is;
    
    public static GpsSocketHandler instance;
    
    public static GpsSocketHandler getInstance()
    {
        if (instance == null)
        {
            instance = new GpsSocketHandler();
        }
        return instance;
    }
    
    public boolean sendMsg2(GpsBean bean)
    {
        return this.login() && this.sendDataKafka(bean);
    }
    
    public boolean sendMsg(List<GpsBean> list)
    {
        return this.login() && this.sendData(list);
    }
    
    private boolean sendDataKafka(GpsBean bean)
    {
        StringBuilder sb = new StringBuilder();
        String devName = bean.getDevName();
        sb.append("{TaxiGps,{Pack,1},{Position,").append(bean.getLng()).append(",");
        sb.append(bean.getLat()).append(",,").append(bean.getCreateDate()).append(",,,,t},{Vehicle,");
        sb.append("\"").append(bean.getDtuCode()).append("\",");
        sb.append("\"").append(devName).append("\"").append(",1,9},,{Warn,0,0}}");
        
        // (4)发送数据包
        if (this.sendResquest(sb.toString()))
        {
            logger.debug("Successful batch transmission of GPS data");
        }
        else
        {
            return false;
        }
        
        return true;
    }
    
    private boolean sendData(List<GpsBean> list)
    {
        int batchSize = Constant.BATCH_SIZE;//限制条数
        int totalSize = list.size();
        int part = totalSize / batchSize;//分批数
        int surplus = totalSize % batchSize;//余数
        if (surplus != 0)
        {
            part++;
        }
        
        for (int m = 0; m < part; m++)
        {
            StringBuilder sb = new StringBuilder();
            int batchMax = (m + 1) * batchSize > totalSize ? totalSize : (m + 1) * batchSize;
            for (int n = m * batchSize; n < batchMax; n++)
            {
                GpsBean bean = list.get(n);
                String devName = bean.getDevName();
//				if(devName.indexOf("鄂")==-1) {
//					devName = "鄂A" + devName;
//				}
                sb.append("{TaxiGps,{Pack,1},{Position,").append(bean.getLng()).append(",");
                sb.append(bean.getLat()).append(",,").append(bean.getCreateDate()).append(",,,,t},{Vehicle,");
                sb.append("\"").append(bean.getDtuCode()).append("\",");
                sb.append("\"").append(devName).append("\"").append(",1,9},,{Warn,0,0}}");
            }
            // (4)发送数据包
            if (this.sendResquest(sb.toString()))
            {
                logger.info("Successful batch transmission of GPS data, total:{}", batchMax);
            }
            else
            {
                return false;
            }
        }
        return true;
    }
    
    private boolean login()
    {
        int cn = this.getConnection(IP, PORT, 0);
        if (cn == -1)
        {
            logger.error("Retry connection failed");
            return false;
        }
        if (cn > 0)
        {
            // (1)登录
            String req = "{LoginRequest,0,\"" + USER_NAME + "\",\"" + PASSWORD + "\",0,}";
            this.sendLoginResquest(req);
            String str = this.getResponse("Login", 0);
            if (str != null && !"".equals(str))
            {
                logger.info("Successful data synchronization login, {}", str);
                // (2)发送请求接收心跳指令
                req = "{RequestReceive,0,1,0}";
                this.sendLoginResquest(req);
                str = this.getResponse("HeartBeat", 0);
                if (str != null && !"".equals(str))
                {
                    logger.info("Successful request to send heart beat, {}", str);
                }
                
                // (3)请求发送指令
                req = "{RequestSend,0,2}";
                this.sendLoginResquest(req);
                str = this.getResponse("RequestSend", 0);
                if (str != null && !"".equals(str))
                {
                    logger.info("Successful request to send instructions, {}", str);
                }
            }
        }
        return true;
    }
    
    private int getConnection(String ip, int port, int cn)
    {
        try
        {
            if (socket == null || !socket.isConnected() || socket.isClosed())
            {
                socket = new Socket(ip, port);
                cn++;
                if (cn == Constant.RETRY_COUNT)
                {
                    logger.warn("Retry {} times, connection failed", cn);
                    return -1;
                }
                Thread.sleep(Constant.PAUSE_TIME);
                return this.getConnection(ip, port, cn);
            }
            else
            {
                os = socket.getOutputStream();
                is = socket.getInputStream();
            }
        }
        catch (Exception e)
        {
            logger.error("Create connection exceptions", e.getMessage());
            return -1;
        }
        return cn;
    }
    
    private boolean sendResquest(String req)
    {
        try
        {
            byte[] bytes = req.getBytes("utf-8");
            int len = bytes.length;
            byte[] lengthBytes = ByteUtil.intTo4BytesBig(len);
            os.write(lengthBytes);
            os.write(bytes);
            os.flush();
        }
        catch (IOException e)
        {
            logger.warn("SendResquest inputStream processing error, {}", e.getMessage());
            //重新登录之前先关闭原连接对象
            this.close();
            //登录成功后才能重发
            if (this.login())
            {
                return this.sendResquest(req);
            }
            else
            {
                return false;
            }
        }
        return true;
    }
    
    private boolean sendLoginResquest(String req)
    {
        try
        {
            os.write(req.getBytes());
            os.flush();
        }
        catch (IOException e)
        {
            logger.warn("SendLoginResquest outputStream processing error, {}", e.getMessage());
            //重新登录之前先关闭原连接对象
            this.close();
            //登录成功后才能重发
            if (this.login())
            {
                return this.sendLoginResquest(req);
            }
            else
            {
                return false;
            }
        }
        return true;
    }
    
    private String getResponse(String flag, int cn)
    {
        String str = null;
        try
        {
            byte[] bytes = new byte[0];
            bytes = new byte[is.available()];
            is.read(bytes);
            str = new String(bytes);
            if (str == null || "".equals(str))
            {
                cn++;
                if (cn == Constant.RETRY_COUNT)
                {
                    logger.warn("Retry {} times, get response failed: {}", cn, flag);
                    return null;
                }
                Thread.sleep(Constant.PAUSE_TIME);
                return this.getResponse(flag, cn);
            }
        }
        catch (Exception e)
        {
            logger.warn("GetResponse outputStream processing error, {}", e.getMessage());
            //重新登录之前先关闭原连接对象
            this.close();
            this.login();
        }
        return str;
    }
    
    private void close()
    {
        try
        {
            if (is != null)
            {
                is.close();
            }
            if (os != null)
            {
                os.close();
            }
            if (socket != null)
            {
                socket.close();
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
}
