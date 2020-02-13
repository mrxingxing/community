package com.neu.community;

import com.alibaba.fastjson.JSONObject;
import com.neu.community.entity.Event;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.HtmlUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class KafkaTests {

    @Autowired
    private KafkaProducer kafkaProducer;


    @Test
    public void testKafka(){
        kafkaProducer.sendMessage("test","zaima?");
        kafkaProducer.sendMessage("test","buzai,cmn");
        try{
            Thread.sleep(1000*10);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    @Test
    public void testJson(){
        Event event = JSONObject.parseObject(HtmlUtils.htmlUnescape("{&quot;entityType&quot;:1,&quot;entityId&quot;:285,&quot;postId&quot;:285,&quot;userId&quot;:151}"),Event.class);
        System.out.println(event.getEntityType());
    }
}

@Component
class KafkaProducer{
    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void sendMessage(String topic,String content){
        kafkaTemplate.send(topic,content);
    }
}

@Component
class kafkaConsumer{

    @KafkaListener(topics={"test"})
    public void handleMessage(ConsumerRecord record){
        System.out.println(record.value());
    }
}
