package com.yun.practice.midware.kafka.consumer.handler;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.listener.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class MyBatchErrorHandler extends SeekToCurrentBatchErrorHandler {

    private int maxFailures;
    private ThreadLocal<Map<String, Integer>> failureLocal = ThreadLocal.withInitial(HashMap::new);
//    private ConcurrentHashMap<String, Integer> failureMap = new ConcurrentHashMap<>();


    public MyBatchErrorHandler(int maxFailures) {
        this.maxFailures = maxFailures;
    }

    private int increase(String key) {
        final Map<String, Integer> localMap = failureLocal.get();
        Integer failureTimes = Optional.ofNullable(localMap.get(key)).orElse(0);
        failureTimes = failureTimes + 1;
        localMap.put(key, failureTimes);
        return failureTimes;
    }

    @Override
    public void handle(Exception thrownException, ConsumerRecords<?, ?> data, Consumer<?, ?> consumer,
                       MessageListenerContainer container) {
        final String keys = keys(data);
        log.info("caculated keys :: {}", keys);
        if (increase(keys) <= maxFailures) {
            log.info("Loop in i = {} :: times {}", keys, failureLocal.get().get(keys));
            data.partitions()
                    .stream()
                    .collect(
                            Collectors.toMap(tp -> tp,
                                    tp -> data.records(tp).get(0).offset(),
                                    (u, v) -> (long) v,
                                    LinkedHashMap::new))
                    .forEach(consumer::seek);
            throw new KafkaException("Seek to current after exception", thrownException);
        } else {
            log.info("Execute time :: {} - {}", keys, failureLocal.get().get(keys) - 1);
            failureLocal.get().remove(keys);
            log.info("Final Threadlocal Map :: {}", failureLocal.get());
        }
    }

    private String keys(ConsumerRecords<?, ?> data) {
        final Iterator<? extends ConsumerRecord<?, ?>> iterator = data.iterator();

        StringBuffer sb = new StringBuffer();
        while (iterator.hasNext()) {
            sb.append(iterator.next().key().toString()).append(",");
        }
        return sb + Thread.currentThread().getName();
    }

//    public static void main(String[] args) {
//
//        final MyBatchErrorHandler handler = new MyBatchErrorHandler(5);
//        String TOM = "tome";
//        handler.failureMap.put(TOM, 7);
//        handler.increaseMap(TOM);
//        System.out.println(handler.failureMap);
//        handler.increaseMap("Jack");
//        System.out.println(handler.failureMap);
//        handler.increaseMap(TOM);
//        handler.increaseMap("Jack");
//        System.out.println(handler.failureMap);
//
//    }
//
//    private int increaseMap(String key) {
//        Integer failureTimes =
//                Optional.ofNullable(failureMap.get(key)).orElse(0);
//        failureTimes = failureTimes + 1;
//        failureMap.put(key, failureTimes);
//        return failureTimes;
//    }
//
//    private List<String> keys(ConsumerRecords<?, ?> data) {
//        return data.partitions()
//                .stream()
//                .map(x -> {
//                    final List<? extends ConsumerRecord<?, ?>> records = data.records(x);
//                    StringBuffer sb = new StringBuffer();
//                    records.forEach(e -> sb.append(e.key().toString()).append(","));
//                    return sb.toString() + x;
//                })
//                .collect(Collectors.toList());
//    }
//
//
//    @Override
//    public void handle(Exception thrownException, ConsumerRecords<?, ?> data, Consumer<?, ?> consumer,
//                       MessageListenerContainer container) {
//        final List<String> keys = keys(data);
//        log.info("caculated keys :: {}", keys);
//
//        final List<String> guessRemoveKeys = new ArrayList<>();
//        keys.forEach(x -> {
//            if (increaseMap(x) <= maxFailures) {
//                log.info("Loop in i = {}", x);
//                data.partitions()
//                        .stream()
//                        .collect(
//                                Collectors.toMap(tp -> tp,
//                                        tp -> data.records(tp).get(0).offset(),
//                                        (u, v) -> (long) v,
//                                        LinkedHashMap::new))
//                        .forEach(consumer::seek);
//                throw new KafkaException("Seek to current after exception", thrownException);
//            } else {
//                guessRemoveKeys.add(x);
//            }
//        });
//
//        if (CollectionUtils.isNotEmpty(guessRemoveKeys) && guessRemoveKeys.size() == keys.size()){
//            guessRemoveKeys.forEach(failureMap::remove);
//            log.info("{} error handle seek finish keys {} :: map - {} ", this.getClass().getSimpleName(), guessRemoveKeys, failureMap);
//        }
//    }


}
