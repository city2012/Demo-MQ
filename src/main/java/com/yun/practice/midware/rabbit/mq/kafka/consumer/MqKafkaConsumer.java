package com.yun.practice.midware.rabbit.mq.kafka.consumer;

import com.yun.practice.midware.rabbit.common.constant.MessageQueueConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MqKafkaConsumer extends AbsKafkaConsumer {
    private Map<String, List<String>> consumeMap = new HashMap<>();

//    @KafkaListener(
//            topics = {MessageQueueConstant.KAFKA_TOPIC_TEST},
//            containerFactory = "defaultKafkaListenerContainerFactory",
//            groupId = "kafka_group_test")
    public void onMessage(ConsumerRecord<String, String> record){
        super.consume(record);
    }

    public void onBatchMessage(List<ConsumerRecord<String, String>> consumerRecords, Acknowledgment ack){
        super.consumeBatch(consumerRecords);
    }





    @Override
    public void doBatchConsume(List<ConsumerRecord<String, String>> consumerRecords, Acknowledgment ack){
//        log.info("{} - Message record :: {}",this.getClass().getName(), consumerRecords.size());
        for (ConsumerRecord<String, String> record : consumerRecords) {
            final List<String> value = consumeMap.get(record.key());
            if (CollectionUtils.isEmpty(value)){
                final List<String> rec = new ArrayList<>();
                rec.add(record.value());
                consumeMap.put(record.key(), rec);
            }else {
                if (!value.contains(record.value())){
                    value.add(record.value());
                }
            }

            log.info("MqKafkaConsumer --消费消息:{} - {}", record.key(), record.value());
            onMessage(record.value());
        }
        //submit manually
        ack.acknowledge();
        log.info("{} consumeMap,{}", this.getClass().getSimpleName(), consumeMap);
    }
    public void onMessage(String message){
//        log.info("MqKafkaConsumer--消费消息:" + message);

    }


    @Override
    protected boolean doConsume(String key, String message) {
        return true;
    }
}
