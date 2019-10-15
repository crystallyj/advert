package com.njits.iot.advert.util;

import com.njits.iot.advert.common.entity.GpsBean;
import com.njits.iot.advert.njits.Gps905Handler;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class ByteUtil
{
    
    public static void main(String[] args) throws Exception
    {
        // byte[] bytes = ByteUtil.hexStringToByte("118.8924");
        //  System.out.println(bytes);
        GpsBean gpsBean = new GpsBean();
        gpsBean.setDevName("鄂A88888");
        gpsBean.setDtuCode("1808100098");
        gpsBean.setLng(118.8924);
        gpsBean.setLat(32.06834);
        gpsBean.setCreateDate(20190708234430L);
        List<GpsBean> test = new ArrayList<>();
        test.add(gpsBean);
        // System.out.println("====" + ByteUtil.bytesToHexString(han.getGpsMsg(gpsBean)));
        Gps905Handler.getInstance().sendMsg(test);
    }
    
    /**
     * 以大端模式将int转成byte[]
     */
    public static byte[] intToBytesBig(int value)
    {
        byte[] src = new byte[1];
        src[0] = (byte) (value & 0xFF);
        return src;
    }
    
    /**
     * 以大端模式将int转成byte[]
     */
    public static byte[] intTo4BytesBig(int value)
    {
        byte[] src = new byte[4];
        src[0] = (byte) ((value >> 24) & 0xFF);
        src[1] = (byte) ((value >> 16) & 0xFF);
        src[2] = (byte) ((value >> 8) & 0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }
    
    /**
     * 以大端模式将int转成byte[]
     */
    public static byte[] intTo2BytesBig(int value)
    {
        byte[] src = new byte[2];
        src[0] = (byte) ((value >> 8) & 0xFF);
        src[1] = (byte) (value & 0xFF);
        return src;
    }
    
    /**
     * 以小端模式将int转成byte[]
     *
     * @param value
     * @return
     */
    public static byte[] intToBytesLittle(int value)
    {
        byte[] src = new byte[4];
        src[3] = (byte) ((value >> 24) & 0xFF);
        src[2] = (byte) ((value >> 16) & 0xFF);
        src[1] = (byte) ((value >> 8) & 0xFF);
        src[0] = (byte) (value & 0xFF);
        return src;
    }
    
    /**
     * 以大端模式将byte[]转成int
     */
    public static int bytesToIntBig(byte[] src, int offset)
    {
        int value;
        value = (int) (((src[offset] & 0xFF) << 24) | ((src[offset + 1] & 0xFF) << 16) |
                ((src[offset + 2] & 0xFF) << 8) | (src[offset + 3] & 0xFF));
        return value;
    }
    
    /**
     * 以大端模式将byte转成int
     */
    public static int byteToIntBig(byte src)
    {
        return (int) (src & 0xFF);
    }
    
    /**
     * 以小端模式将byte[]转成int
     */
    public static int bytesToIntLittle(byte[] src, int offset)
    {
        int value;
        value = (int) ((src[offset] & 0xFF) | ((src[offset + 1] & 0xFF) << 8) | ((src[offset + 2] & 0xFF) << 16) |
                ((src[offset + 3] & 0xFF) << 24));
        return value;
    }
    
    /**
     * 把16进制字符串转换成字节数组
     *
     * @param hexString
     * @return byte[]
     */
    public static byte[] hexStringToByte(String hexString)
    {
        if (hexString == null || hexString.trim().equals(""))
        {
            return new byte[0];
        }
        
        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length() / 2; i++)
        {
            String subStr = hexString.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }
        
        return bytes;
    }
    
    /**
     * 合并两个byte数组
     *
     * @param bt1
     * @param bt2
     * @return
     */
    public static byte[] mergerBytes(byte[]... bts)
    {
        int length = 0;
        for (byte[] bt : bts)
        {
            length += bt.length;
        }
        
        byte[] ret = new byte[length];
        int i = 0;
        for (byte[] bt : bts)
        {
            for (byte b : bt)
            {
                ret[i] = b;
                i++;
            }
        }
        return ret;
    }
    
    /**
     * 数值字符串转BCD数组
     *
     * @param asc
     * @return
     */
    public static byte[] stringToBcd(String asc)
    {
        int len = asc.length();
        int mod = len % 2;
        if (mod != 0)
        {
            asc = "0" + asc;
            len = asc.length();
        }
        byte abt[] = new byte[len];
        if (len >= 2)
        {
            len = len / 2;
        }
        byte bbt[] = new byte[len];
        abt = asc.getBytes();
        int j, k;
        for (int p = 0; p < asc.length() / 2; p++)
        {
            if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9'))
            {
                j = abt[2 * p] - '0';
            }
            else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z'))
            {
                j = abt[2 * p] - 'a' + 0x0a;
            }
            else
            {
                j = abt[2 * p] - 'A' + 0x0a;
            }
            if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9'))
            {
                k = abt[2 * p + 1] - '0';
            }
            else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z'))
            {
                k = abt[2 * p + 1] - 'a' + 0x0a;
            }
            else
            {
                k = abt[2 * p + 1] - 'A' + 0x0a;
            }
            int a = (j << 4) + k;
            byte b = (byte) a;
            bbt[p] = b;
        }
        return bbt;
    }
    
    /**
     * 截取子数组
     *
     * @param bytes
     * @param start
     * @param length
     * @return
     */
    public static byte[] getSubBytes(byte[] bytes, int start, int length)
    {
        byte[] ret = new byte[length];
        for (int i = start, j = 0; j < length; i++, j++)
        {
            ret[j] = bytes[i];
        }
        return ret;
    }
    
    /**
     * 获取字节的位置
     *
     * @param bytes
     * @param bt
     * @return
     */
    public static int indexOf(byte[] bytes, byte bt)
    {
        for (int i = 0; i < bytes.length; i++)
        {
            if (bytes[i] == bt)
            {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * 字节数组转16进制字符串
     *
     * @param bArray
     * @return
     */
    public static String bytesToHexString(byte[] bArray)
    {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++)
        {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2) sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }
    
    public static String addZeroForNum(String str, int length)
    {
        int strLen = str.length();
        StringBuffer sb = null;
        while (strLen < length)
        {
            sb = new StringBuffer();
            sb.append("0").append(str);// 左补0
//            sb.append(str).append("0");//右补0
            str = sb.toString();
            strLen = str.length();
        }
        return str;
    }
    
    public static byte[] strToBytes(String str, int length) throws UnsupportedEncodingException
    {
        byte[] strBytes = str.getBytes("GBK");
        int strLength = strBytes.length;
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++)
        {
            if (i < strLength)
            {
                bytes[i] = strBytes[i];
            }
            else
            {
                bytes[i] = 0;
            }
        }
        return bytes;
    }
    
    /**
     * 多个数组合并
     *
     * @param first
     * @param rest
     * @return
     */
    public static byte[] concatAll(byte[]... bytes)
    {
        int length = 0;
        for (byte[] bts : bytes)
        {
            length += bts.length;
        }
        byte[] result = new byte[length];
        int offset = 0;
        for (byte[] array : bytes)
        {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }
}
