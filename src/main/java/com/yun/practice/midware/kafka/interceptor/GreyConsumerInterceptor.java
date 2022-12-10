package com.yun.practice.midware.kafka.interceptor;

import com.yun.practice.midware.utils.FlowflagHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class GreyConsumerInterceptor implements ConsumerInterceptor {
    @Override
    public ConsumerRecords onConsume(ConsumerRecords records) {

        log.info("{} ---- onConsume!", this.getClass().getSimpleName());
        if (records.isEmpty()){
            return records;
        }
        final Map<TopicPartition, List<ConsumerRecord>> recordsMap = new HashMap<>();
        List<ConsumerRecord> recordList = new ArrayList<>();
        for(Object record : records){
            final ConsumerRecord consumerRecord = (ConsumerRecord) record;
            if(FlowflagHelper.needConsumer(consumerRecord)){
                final TopicPartition topicPartition = new TopicPartition(consumerRecord.topic(), consumerRecord.partition());
                recordList.add(consumerRecord);
                recordsMap.put(topicPartition, recordList);
            }
        }
        return new ConsumerRecords(recordsMap);
    }

    @Override
    public void close() {
        log.info("{} ---- close!", this.getClass().getSimpleName());
    }

    @Override
    public void onCommit(Map offsets) {
        log.info("{} ---- onCommit!", this.getClass().getSimpleName());
    }

    @Override
    public void configure(Map<String, ?> configs) {

    }
}
