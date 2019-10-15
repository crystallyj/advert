package com.njits.iot.advert.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.njits.iot.advert.baseinfo.BaseBeanDomain;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class KafkaProducerUtil
{
    public static String brokerid;
    static KafkaProducer<String, String> producer;
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerUtil.class);
    
    static
    {
        Properties props = new Properties();
        props.put("bootstrap.servers", "prd212:9092,prd213:9092,prd214:9092");
        //测试Kafka
        // props.put("bootstrap.servers", "192.168.44.161:9092,192.168.44.162:9092,192.168.44.163:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<>(props);
    }
    
    public static void sedMsg(String topic, String msg)
    {
        producer.send(new ProducerRecord<String, String>(topic, msg));
    }
    
    public static void sedMsgToKafka(JSONArray data, BaseBeanDomain beanDomain, JSONObject jb, String topic)
    {
        
        
        if (null != data)
        {
            logger.info("begin sedMsgToKafka,city={},command={},,dataSize=[{}]...", beanDomain.getCity(),
                    beanDomain.getCommand(), data.size());
            JSONArray array = null;
            for (int i = 0; i < data.size(); i++)
            {
                array = new JSONArray();
                array.add(data.getJSONObject(i));
                beanDomain.setContent(array);
                sedMsg(topic, JSONObject.toJSONString(beanDomain, SerializerFeature.WriteMapNullValue));
                
            }
        }
        
    }
}
