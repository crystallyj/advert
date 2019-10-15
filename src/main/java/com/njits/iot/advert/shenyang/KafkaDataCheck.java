package com.njits.iot.advert.shenyang;

import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Properties;

public class KafkaDataCheck
{
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaDataCheck.class);
    
    public static void main(String[] args)
    {
        // 1、定义服务器地址、端口号、数据
        Properties properties = new Properties();
        properties.put("bootstrap.servers", "prd212:9092,prd213:9092,prd214:9092");
        properties.put("group.id", "test113");
        properties.put("enable.auto.commit", "false");
        properties.put("auto.commit.interval.ms", "1000");
        properties.put("auto.offset.reset", "latest");
        properties.put("session.timeout.ms", "30000");
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        
        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(properties);
        kafkaConsumer.subscribe(Arrays.asList("com.itsp.taxi.adv.sourcedata"));
        
        KafkaDataCheck.getData(kafkaConsumer);
    }
    
    public static void getData(KafkaConsumer<String, String> kafkaConsumer)
    {
        ConsumerRecords<String, String> records = null;
        int i = 0, j = 0;
        while (true)
        {
            records = kafkaConsumer.poll(5000);
            for (ConsumerRecord<String, String> record : records)
            {
//                System.out.printf("offset = %d, value = %s", record.offset(), record.value());
//                System.out.println();
                
                JSONObject jsb = JSONObject.parseObject(record.value());
                String command = jsb.getString("command");//trackrecord.value()
//    			String content = jsb.getString("content");//rm
//    			JSONObject jsb2 = JSONObject.parseObject(content);
//    			String online = jsb2.getString("area_code");//210100
                
                if (!"dev_list".equals(command))
                {
                    continue;
                }

//    			if("1".equals(online)) {
//    				i++;
//    			} else {
//    				j++;
//    			}
                logger.info(record.value());

//    			System.out.println("i="+i+" j="+j);

//    			if(!"track".equals(command) || !"rm".equals(frim) || !"cz".equals(carType) || !"210100".equals
// (areaCode)) {
//    				continue;
//    			}
            }
            kafkaConsumer.commitAsync();//异步提交
        }
    }
}
