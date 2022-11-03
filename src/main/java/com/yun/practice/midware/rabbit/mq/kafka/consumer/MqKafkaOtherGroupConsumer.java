package com.yun.practice.midware.rabbit.mq.kafka.consumer;

import com.yun.practice.midware.rabbit.common.constant.MessageQueueConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class MqKafkaOtherGroupConsumer extends AbsKafkaConsumer{

//    @KafkaListener(
//            topics = {MessageQueueConstant.KAFKA_TOPIC_TEST},
//            containerFactory = "defaultKafkaListenerContainerFactory",
//            groupId = "kafka_group_other_test")
    public void onMessage(ConsumerRecord<String, String> record) {
        super.consume(record);
    }

    @Override
    protected boolean doConsume(String key, String message) {
        return true;
    }

    @Override
    protected void doBatchConsume(List<ConsumerRecord<String, String>> consumerRecords, Acknowledgment ack) {

    }
}
