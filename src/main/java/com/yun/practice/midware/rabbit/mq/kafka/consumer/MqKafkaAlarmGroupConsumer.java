package com.yun.practice.midware.rabbit.mq.kafka.consumer;

import com.yun.practice.midware.rabbit.common.constant.MessageQueueConstant;
import com.yun.practice.midware.rabbit.data.entity.KafkaConsumeRecord;
import com.yun.practice.midware.rabbit.repo.KafkaConsumeRecordRepo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.DependsOn;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@DependsOn("initOtherKafkaConsumer")
public class MqKafkaAlarmGroupConsumer {

    private Map<String, List<String>> consumeMap = new HashMap<>();
    @Resource
    private KafkaConsumeRecordRepo kafkaConsumeRecordRepo;

//    @KafkaListener(
//            topics = {MessageQueueConstant.ALARM_DEMO_TOPIC},
//            containerFactory ="${kafka.ext.multiple.alarm.consumer.listener-container-factory}"
//    )
    @KafkaListener(
            topics = {MessageQueueConstant.KAFKA_ALARM_TOPIC},
            containerFactory ="${kafka.ext.multiple.alarm.consumer.listener-container-factory}"
    )
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment ack){
        try {
            final String key = record.key();
            final String message = record.value();
            log.info("{} --消费消息:{} - {}", this.getClass().getSimpleName(), key, message);

//            if (Integer.parseInt(key) % 10 == 0){
//                throw new RuntimeException("DO NOT ACK!!!" + key +" :: "+ message);
//            }
            ack.acknowledge();
        } catch (Exception ex) {
//            log.error("MqKafkaAlarmGroupConsumer consume :: ", ex);
            throw ex;
        }
    }



    public static void main(String[] args) {
        for (int i = 0; i < 60; i++) {
            final int hashCode = String.valueOf(i).hashCode();
            if (hashCode % 10 == 0){
                System.out.println("Hash 0 :: " + i);
            }
//            if (i % 10 == 0){
//                System.out.println("Number 0 :: " + i);
//            }
        }
    }
}
