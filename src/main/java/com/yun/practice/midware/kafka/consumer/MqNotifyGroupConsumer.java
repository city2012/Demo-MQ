package com.yun.practice.midware.kafka.consumer;

import com.yun.practice.midware.common.constant.MessageQueueConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.DependsOn;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@DependsOn("defaultKafkaListenerContainerFactory")
public class MqNotifyGroupConsumer extends AbsKafkaConsumer {
    private Map<String, List<String>> consumeMap = new HashMap<>();

    @KafkaListener(
            topics = {MessageQueueConstant.KAFKA_NOTIFY_TOPIC},
            containerFactory ="${kafka.ext.multiple.notify.consumer.listener-container-factory}")
    public void onMessage(List<ConsumerRecord<String, String>> record){
        super.consumeBatch(record);
    }
//    public void onMessage(ConsumerRecord<String, String> record){
//        super.consume(record);
//    }

    @Override
    public void doBatchConsume(List<ConsumerRecord<String, String>> consumerRecords, Acknowledgment ack){
    }

    @Override
    protected boolean doConsume(String key, String message) {
        return true;
    }

}
