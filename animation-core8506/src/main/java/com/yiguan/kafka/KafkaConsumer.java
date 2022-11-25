package com.yiguan.kafka;


import com.alibaba.fastjson.JSON;
import com.yiguan.bean.entity.AnimationBullet;
import com.yiguan.bean.entity.AnimationMessage;
import com.yiguan.controller.WebSocket;
import com.yiguan.dao.AnimationBulletMapper;
import com.yiguan.dao.AnimationMessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.listener.ConsumerAwareListenerErrorHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class KafkaConsumer {
    @Autowired
    AnimationBulletMapper animationBulletMapper;
    @Autowired
    AnimationMessageMapper animationMessageMapper;
    @Resource
    private WebSocket webSocket;

    // 新建一个异常处理器，用@Bean注入
    @Bean
    public ConsumerAwareListenerErrorHandler consumerAwareErrorHandler() {
        return (message, exception, consumer) -> {
            log.info("消费异常："+message.getPayload());
            log.error(exception.getMessage());
            return null;
        };
    }

    // 监听弹幕消息
    @KafkaListener(topics = {"bullet"},errorHandler = "consumerAwareErrorHandler")
    public void onBulletMessage(List<ConsumerRecord<?, ?>> records){
        List<AnimationBullet> bullets = records.stream().map(e -> JSON.parseObject(e.value() + "", AnimationBullet.class)).collect(Collectors.toList());
        animationBulletMapper.insertBatchSomeColumn(bullets);
        // 消费的哪个topic、partition的消息,打印出消息内容
//        System.out.println("简单消费："+record.topic()+"-"+record.partition()+"-"+record.value());
    }

    //TODO：目前只能在线的用户才能知道有新的系统消息，如果先增加系统消息，然后登陆就不能知道有新的消息，此处待优化，我这就懒得弄了
    // 监听全局消息
    @KafkaListener(id = "manager",groupId = "globalMessageGroup",topics = {"globalMessage"},errorHandler = "consumerAwareErrorHandler")
    public void onGlobalMessage(List<ConsumerRecord<?, ?>> records){
        List<AnimationMessage> message = records.stream().map(e -> JSON.parseObject(e.value() + "", AnimationMessage.class)).collect(Collectors.toList());
        //websocket广播消息
        webSocket.sendAllMessage(records.get(0).value()+"");
        //插入数据库
        animationMessageMapper.insertBatchSomeColumn(message);
    }



}
