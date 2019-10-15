package com.njits.iot.advert.njits;

import com.njits.iot.advert.common.GpsHandler;
import com.njits.iot.advert.common.entity.Constant;
import com.njits.iot.advert.common.entity.GpsBean;
import com.njits.iot.advert.util.ByteUtil;
import com.njits.iot.advert.util.CRC16Util;
import com.njits.iot.advert.util.DateUtil;
import com.njits.iot.advert.util.MD5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Gps905Handler implements GpsHandler
{
    
    private static final Logger logger = LoggerFactory.getLogger(Gps905Handler.class);
    
    public static String ip;
    public static int port;
    public static int userId;
    public static String password;
    public static String accessCodeHex;
    public int sn = 0;
    
    public Socket socket;
    public OutputStream os;
    public InputStream is;
    
    private static final byte F_0x5a = 90;
    private static final byte F_0x5b = 91;
    private static final byte F_0x5d = 93;
    private static final byte F_0x5e = 94;
    private static final byte F_0x01 = 1;
    private static final byte F_0x02 = 2;
    private static final byte[] MSG_ID_LOGIN_REQ = {32, 1};// 0x2001
    private static final byte[] MSG_ID_LOCATION_REQ = {33, 0};// 0x2100
    private static final byte[] plateColorBytes = {9};//车牌颜色
    private static final int maxTryTime = 3;//失败尝试次数
    private static final byte[] SUB_MSG_ID_LOCATION_REQ = {33, 1};// 0x2101
    private static final int unit = 60 * 10000;
    
    public static void main(String[] arg)
    {
        Gps905Handler client = new Gps905Handler();
        List<GpsBean> list = new ArrayList<>();
        GpsBean bean1 = new GpsBean();
        bean1.setDevName("XP995");
        bean1.setDtuCode("1809100054");
        bean1.setCreateDate(20190320104430L);
        bean1.setLat(30.55964);
        bean1.setLng(114.21313);
        list.add(bean1);
		
		
		/*GpsBean bean2 = new GpsBean();
		bean2.setDevName("XU758");
		bean2.setDtuCode("1808100077");
		bean2.setCreateDate(20190320104430L);
		bean2.setLat(30.63637);
		bean2.setLng(114.28099);
		list.add(bean2);*/
        client.sendMsg(list);
    }
    
    public static Gps905Handler instance;
    
    public static Gps905Handler getInstance()
    {
        if (instance == null)
        {
            instance = new Gps905Handler();
        }
        return instance;
    }
    
    public Gps905Handler()
    {
        try
        {
            Properties prop = new Properties();
            InputStream is = Gps905Handler.class.getResourceAsStream("/base.properties");
            prop.load(is);
            ip = prop.getProperty("server905.ip");
            port = Integer.parseInt(prop.getProperty("server905.port"));
            userId = Integer.parseInt(prop.getProperty("server905.userId"));
            password = prop.getProperty("server905.password");
            accessCodeHex = prop.getProperty("server905.accessCode");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public boolean sendMsgKafka(GpsBean bean)
    {
        if (!checkConnection(this.ip, this.port))
        {
            logger.error("Connect 905 server is failure, ip:{}, port:{}", ip, port);
            return false;
        }
        
        byte[] gpsBytes = this.getGpsMsg(bean);
        
        byte[] lengthBytes = ByteUtil.intTo4BytesBig(gpsBytes.length);
        byte[] gpsMsgBody = ByteUtil.mergerBytes(SUB_MSG_ID_LOCATION_REQ, lengthBytes, gpsBytes);
        if (this.sendDataReq(getMsgData(gpsMsgBody, MSG_ID_LOCATION_REQ)))
        {
            //重试3次失败后丢掉
            for (int i = 0; i < maxTryTime; i++)
            {
                if (getGpsRsp())
                {
                    break;
                }
                this.sendDataReq(getMsgData(gpsMsgBody, MSG_ID_LOCATION_REQ));
            }
        }
        else
        {
            logger.error("=========== Try 3 times, failed ============");
        }
        
        return true;
    }
    
    public boolean sendMsg(List<GpsBean> list)
    {
        if (!checkConnection(this.ip, this.port))
        {
            logger.error("Connect 905 server is failure, ip:{}, port:{}", ip, port);
            return false;
        }
        int batchSize = Constant.BATCH_905;// 限制条数
        int totalSize = list.size();
        int part = totalSize / batchSize;// 分批数
        int surplus = totalSize % batchSize;// 余数
        if (surplus != 0)
        {
            part++;
        }
        for (int m = 0; m < part; m++)
        {
            int batchMax = (m + 1) * batchSize > totalSize ? totalSize : (m + 1) * batchSize;
            byte[] gpsBytes = {};
            for (int n = m * batchSize; n < batchMax; n++)
            {
                GpsBean bean = list.get(n);
                gpsBytes = ByteUtil.mergerBytes(gpsBytes, this.getGpsMsg(bean));
            }
            byte[] lengthBytes = ByteUtil.intTo4BytesBig(gpsBytes.length);
            byte[] gpsMsgBody = ByteUtil.mergerBytes(SUB_MSG_ID_LOCATION_REQ, lengthBytes, gpsBytes);
            if (this.sendDataReq(getMsgData(gpsMsgBody, MSG_ID_LOCATION_REQ)))
            {
                //重试3次失败后丢掉
                for (int i = 0; i < maxTryTime; i++)
                {
                    if (getGpsRsp())
                    {
                        break;
                    }
                    this.sendDataReq(getMsgData(gpsMsgBody, MSG_ID_LOCATION_REQ));
                }
            }
            else
            {
                logger.error("=========== Try 3 times, failed ============");
            }
        }
        return true;
    }
    
    // gps消息体
    public byte[] getGpsMsg(GpsBean gps)
    {
        byte[] gpsBytes = null;
        try
        {
            String devName = gps.getDevName();
//			if (devName.indexOf("鄂") == -1) {
//				devName = "鄂A" + devName;
//			}
            byte[] carNumBytes = new byte[12];
            carNumBytes = ByteUtil.strToBytes(devName, 12);
            String deviceCode = ByteUtil.addZeroForNum(gps.getDtuCode(), 20);
            byte[] deviceCodeBytes = ByteUtil.stringToBcd(deviceCode);
            byte[] encryptBytes = {0};
            byte[] alarmBytes = {0, 0, 0, 0};// 4个字节
            byte[] stateBytes = {0, 0, 0, 0};// 4个字节
            Double lat = gps.getLat() * unit;
            Double lng = gps.getLng() * unit;
            byte[] latBytes = ByteUtil.intTo4BytesBig(lat.intValue());// 4个字节
            byte[] lngBytes = ByteUtil.intTo4BytesBig(lng.intValue());// 4个字节
            byte[] vecBytes = {0, 0};// 2个字节
            byte[] directionBytes = {0};// 1个字节
            String datetime = String.valueOf(gps.getCreateDate()).substring(2);
            byte[] datetimeBytes = ByteUtil.stringToBcd(datetime);// 6BCD
            gpsBytes = ByteUtil.mergerBytes(carNumBytes, deviceCodeBytes, plateColorBytes, encryptBytes, alarmBytes,
                    stateBytes, latBytes, lngBytes, vecBytes, directionBytes, datetimeBytes);
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return gpsBytes;
    }
    
    private boolean checkConnection(String ip, int port)
    {
        try
        {
            if (socket == null || !socket.isConnected() || socket.isClosed())
            {
                socket = new Socket(ip, port);
                os = socket.getOutputStream();
                is = socket.getInputStream();
                if (this.sendDataReq(getMsgData(getLoginMsgBody(), MSG_ID_LOGIN_REQ)))
                {
                    Thread.sleep(2000);
                    if (this.getLoginRsp())
                    {
                        return true;
                    }
                }
                else
                {
                    logger.error("Login failed");
                    return false;
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Create connection exceptions", e.getMessage());
            return false;
        }
        return true;
    }
    
    private boolean sendDataReq(byte[] dataBytes)
    {
        try
        {
            os.write(dataBytes);
            os.flush();
        }
        catch (IOException e)
        {
            logger.warn("SendResquest inputStream processing error, {}", e.getMessage());
            // 重新登录之前先关闭原连接对象
            this.close();
            return false;
        }
        return true;
    }
    
    
    // 整体消息
    private byte[] getMsgData(byte[] msgBody, byte[] msgId)
    {
        byte[] msgHead = getMsgHead(msgBody.length, msgId);
        byte[] crcBytes = getCRC16(ByteUtil.mergerBytes(msgHead, msgBody));
        byte[] tmpBytes = ByteUtil.mergerBytes(msgHead, msgBody, crcBytes);
        //处理头尾标识特殊字符问题
        byte[] retBytes = ByteUtil.mergerBytes(new byte[]{F_0x5b}, this.specialHandle(tmpBytes), new byte[]{F_0x5d});
//		System.out.println("msgHead:"+ByteUtil.bytesToHexString(msgHead));
//		System.out.println("msgBody:"+ByteUtil.bytesToHexString(msgBody));
//		System.out.println("crc:"+ByteUtil.bytesToHexString(crcBytes));
//		System.out.println("tmpBytes:"+ByteUtil.bytesToHexString(tmpBytes));
        return retBytes;
    }
    
    // 消息头
    private byte[] getMsgHead(int msgBodyLength, byte[] msgIdBytes)
    {
        int msgHead = 1 + 22 + msgBodyLength + 2 + 1;
        byte[] msgHeadBytes = ByteUtil.intTo4BytesBig(msgHead);
        byte[] snBytes = ByteUtil.intTo4BytesBig(sn++);
        byte[] accessCodeBytes = ByteUtil.hexStringToByte(accessCodeHex);
        byte[] versionBytes = {00, 00, 01};
        byte[] encryptFlagBytes = {00};
        byte[] encryptKeyBytes = {00, 00, 00, 00};
        
        return ByteUtil.mergerBytes(msgHeadBytes, snBytes, msgIdBytes, accessCodeBytes, versionBytes,
                encryptFlagBytes, encryptKeyBytes);
    }
    
    // 登录消息体
    private byte[] getLoginMsgBody()
    {
        byte[] userIdBytes = ByteUtil.intTo4BytesBig(this.userId);
        String connectTime = DateUtil.nowDateStr(DateUtil.YYYYMMDD_HHMMSS);
        byte[] connectTimeBytes = ByteUtil.stringToBcd(connectTime);
        String mac = MD5Util.md5(this.password + connectTime);
        byte[] macBytes = mac.getBytes();
        return ByteUtil.mergerBytes(userIdBytes, connectTimeBytes, macBytes);
    }
    
    // CRC校验
    private byte[] getCRC16(byte[] bytes)
    {
        return CRC16Util.setParamCRC(bytes);
    }
    
    private byte[] getMsgRsp()
    {
        try
        {
            byte[] bytes = new byte[1024];
            while (is.read(bytes) != -1)
            {
//			System.err.println(ByteUtil.bytesToHexString(bytes));
                byte[] msgLengthBytes = ByteUtil.getSubBytes(bytes, 1, 4);
                int msgLength = ByteUtil.bytesToIntBig(msgLengthBytes, 0);
                int bodyLength = msgLength - 26;
                return ByteUtil.getSubBytes(bytes, 23, bodyLength);
            }
        }
        catch (Exception e)
        {
            logger.warn("GetResponse outputStream processing error, {}", e.getMessage());
            e.printStackTrace();
            // 重新登录之前先关闭原连接对象
            this.close();
        }
        return null;
    }
    
    private boolean getLoginRsp()
    {
        byte[] bytes = this.getMsgRsp();
        int ret = ByteUtil.byteToIntBig(bytes[0]);
        if (ret == 0)
        {
            return true;
        }
        else
        {
            logger.error("########## login response, ret:{} ###########", ret);
            return false;
        }
    }
    
    private boolean getGpsRsp()
    {
        byte[] bytes = this.getMsgRsp();
        int ret = ByteUtil.byteToIntBig(bytes[0]);
        if (ret == 0)
        {
            return true;
        }
        else
        {
            logger.warn("########## gps failure, ret:{} ###########", ret);
            return false;
        }
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
    
    //处理头尾标识特殊字符问题
    private byte[] specialHandle(byte[] srcBytes)
    {
        byte[] tmp = new byte[srcBytes.length * 2];
        int j = 0;
        for (int i = 0; i < srcBytes.length; i++)
        {
            switch (srcBytes[i])
            {
                case F_0x5b:
                    tmp[j] = F_0x5a;
                    j++;
                    tmp[j] = F_0x01;
                    break;
                case F_0x5a:
                    tmp[j] = F_0x5a;
                    j++;
                    tmp[j] = F_0x02;
                    break;
                case F_0x5d:
                    tmp[j] = F_0x5e;
                    j++;
                    tmp[j] = F_0x01;
                    break;
                case F_0x5e:
                    tmp[j] = F_0x5e;
                    j++;
                    tmp[j] = F_0x02;
                    break;
                default:
                    tmp[j] = srcBytes[i];
                    break;
            }
            j++;
        }
        byte[] ret = new byte[j];
        System.arraycopy(tmp, 0, ret, 0, j);
        return ret;
    }
}
