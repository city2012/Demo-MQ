package com.yun.practice.midware.rabbit.mq.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.support.Acknowledgment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbsKafkaConsumer {

    protected ThreadLocal<Map<String, List<String>>> consumeMapLocal = ThreadLocal.withInitial(HashMap::new);


    public void consume(ConsumerRecord<String, String> record) {
        Map<String, List<String>> consumeMap = consumeMapLocal.get();
        boolean commitFlag = false;
        try {
            final List<String> value = consumeMap.get(record.key());
            if (CollectionUtils.isEmpty(value)) {
                final List<String> rec = new ArrayList<>();
                rec.add(record.value());
                consumeMap.put(record.key(), rec);
            } else {
                if (!value.contains(record.value())) {
                    value.add(record.value());
                }
            }
            log.info("{} --消费消息:{} - {}", this.getClass().getSimpleName(), record.key(), record.value());
            commitFlag = doConsume(record.key(), record.value());
//            if (!commitFlag){
//                throw new RuntimeException("Not Commit :: " + record.key());
//            }
        } catch (Exception ex) {
            log.error("AbsKafkaConsumer consume :: ", ex);
            commitFlag = false;
        }
    }

    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        Map<String, List<String>> consumeMap = consumeMapLocal.get();
        boolean commitFlag = false;
        try {
            final List<String> value = consumeMap.get(record.key());
            if (CollectionUtils.isEmpty(value)) {
                final List<String> rec = new ArrayList<>();
                rec.add(record.value());
                consumeMap.put(record.key(), rec);
            } else {
                if (!value.contains(record.value())) {
                    value.add(record.value());
                }
            }
            log.info("{} --消费消息:{} - {}", this.getClass().getSimpleName(), record.key(), record.value());
            commitFlag = doConsume(record.key(), record.value());
//            if (!commitFlag){
//                throw new RuntimeException("Not Commit :: " + record.key());
//            }
        } catch (Exception ex) {
            log.error("AbsKafkaConsumer consume :: ", ex);
            commitFlag = false;
            throw new RuntimeException("consume error",ex);
        } finally {
            consumeMapLocal.remove();
            //submit manually
            if (commitFlag){
                ack.acknowledge();
            }else {
                log.info("Not Commit :: {}", record.key());
            }
            log.info("{} consumeMap,{}", this.getClass().getSimpleName(), consumeMap);
        }
    }


    public void consumeBatch(List<ConsumerRecord<String, String>> consumerRecords) {
        log.info("{} - Message record :: {}", this.getClass().getSimpleName(), consumerRecords.stream().map(ConsumerRecord::key).collect(Collectors.toList()));
        Map<String, List<String>> consumeMap = consumeMapLocal.get();
        for (ConsumerRecord<String, String> record : consumerRecords) {
            final String key = record.key();
            final List<String> value = consumeMap.get(key);
            if (CollectionUtils.isEmpty(value)) {
                final List<String> rec = new ArrayList<>();
                rec.add(record.value());
                consumeMap.put(key, rec);
            } else {
                if (!value.contains(record.value())) {
                    value.add(record.value());
                }
            }

//            if (Integer.parseInt(key) % 10 == 0){
//                throw new RuntimeException(this.getClass().getSimpleName() + " DO NOT ACK!!!" + key +" :: "+ record.value());
//            }
            log.info("{} --消费消息:{} - {}", this.getClass().getSimpleName(), key, record.value());

        }
        log.info("{} consumeMap,{}", this.getClass().getSimpleName(), consumeMap);
    }

    protected abstract boolean doConsume(String key, String message);

    protected abstract void doBatchConsume(List<ConsumerRecord<String, String>> consumerRecords, Acknowledgment ack);

}
