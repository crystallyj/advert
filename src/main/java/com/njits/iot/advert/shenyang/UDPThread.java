package com.njits.iot.advert.shenyang;

import com.alibaba.fastjson.JSONObject;
import com.njits.iot.advert.util.DateUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Properties;

public class UDPThread extends Thread
{
    
    private static final String head = "ASYN GPS ITSVSTTP2T2019022502";
    private static final String delimiter = "|";
    private static final String connector = "-";
    private static final String lineSplit = "\r\n";
    private DecimalFormat df = new DecimalFormat("0");
    private DatagramSocket datagramSocket = null;
    private InetAddress inetAddress = null;
    private int port;
    private KafkaConsumer<String, String> kafkaConsumer;
    
    public UDPThread(String serverIp, int serverPort, int partition, String groupId)
    {
        // 1、定义服务器地址、端口号、数据
        try
        {
            inetAddress = InetAddress.getByName(serverIp);
            this.port = serverPort;
            datagramSocket = new DatagramSocket();
            
            Properties properties = new Properties();
            properties.put("bootstrap.servers", "prd212:9092,prd213:9092,prd214:9092");
            properties.put("group.id", groupId);
            properties.put("enable.auto.commit", "false");
            properties.put("auto.commit.interval.ms", "1000");
            properties.put("auto.offset.reset", "latest");
            properties.put("session.timeout.ms", "30000");
            properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            
            kafkaConsumer = new KafkaConsumer<>(properties);
            
            TopicPartition part = new TopicPartition("com.itsp.taxi.gps.sourcedata", partition);
            kafkaConsumer.assign(Arrays.asList(part));
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        catch (SocketException e)
        {
            e.printStackTrace();
        }
    }
    
    @Override
    public void run()
    {
        ConsumerRecords<String, String> records = null;
        while (true)
        {
            records = kafkaConsumer.poll(5000);
            for (ConsumerRecord<String, String> record : records)
            {
//                System.out.printf("offset = %d, value = %s", record.offset(), record.value());
//                System.out.println();
                
                JSONObject jsb = JSONObject.parseObject(record.value());
                String command = jsb.getString("command");//track
                String frim = jsb.getString("frim");//rm
                String carType = jsb.getString("car_type");//cz
                String areaCode = jsb.getString("area_code");//210100
                
                if (!"track".equals(command) || !"rm".equals(frim) || !"cz".equals(carType) ||
                        !"210100".equals(areaCode))
                {
                    continue;
                }
                
                // 样例： ASYN GPS MMC8000GPSANDASYN051113-38491-00000000|1552349434|0|123457585|41807490|0.1295|11
                // .27|00000000|00040000|
                String imei = jsb.getString("imei");//设备号
                long stamp = DateUtil.string2Date(jsb.getString("stamp")).getTime() / 1000;//时间戳
                String mileage = "0";//里程
                String longitude = df.format(jsb.getDouble("longitude") * 1000000);//经度
                String latitude = df.format(jsb.getDouble("latitude") * 1000000);//纬度
                
                double d = jsb.getDouble("speed") / 3.6;
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
                
                byte[] data = sb.toString().getBytes();
                // 2、创建数据报，包含发送的信息
                DatagramPacket datagramPacket = new DatagramPacket(data, data.length, inetAddress, port);
                // 4、向服务器端发送数据报
                try
                {
                    datagramSocket.send(datagramPacket);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            kafkaConsumer.commitAsync();//异步提交
        }
    }
    
    public String getTravelState(String jsonString)
    {
        JSONObject jsb = JSONObject.parseObject(jsonString);
        String b0 = jsb.getString("b11");
        String b1 = jsb.getString("b13");
        String b2 = jsb.getString("b9");
        String b3 = jsb.getString("b8");
        String ret = Integer.toHexString(Integer.valueOf("0000" + b3 + b2 + b1 + b0, 2));
        return "0" + ret;
    }
}
