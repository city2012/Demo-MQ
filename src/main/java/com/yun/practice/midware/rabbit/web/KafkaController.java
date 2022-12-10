package com.yun.practice.midware.rabbit.web;

import com.yun.practice.midware.rabbit.common.KafkaBrokerEnum;
import com.yun.practice.midware.rabbit.common.constant.MessageQueueConstant;
import com.yun.practice.midware.rabbit.dto.CommonRequest;
import com.yun.practice.midware.rabbit.mq.kafka.producer.MqKafkaNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class KafkaController {

    private final MqKafkaNotifier mqKafkaNotifier;


    @PostMapping("/kafka/msg/simple/publish/test")
    public String publishTest01(@RequestBody CommonRequest payload){

        log.info("publishTest01 :: {}-{}",this.getClass(),payload);

        for (int i = 0; i < 60; i++) {
//            mqKafkaNotifier.sendMessage(KafkaBrokerEnum.ONE.getKafkaSiteName(), MessageQueueConstant.KAFKA_TOPIC_TEST, String.valueOf(i), payload.getPayload()+i);
            mqKafkaNotifier.sendMessage(KafkaBrokerEnum.ALARM.getKafkaSiteName(), MessageQueueConstant.KAFKA_ALARM_TOPIC, String.valueOf(i), "Alarm :: "+payload.getPayload()+i*2);
//            mqKafkaNotifier.sendMessage(KafkaBrokerEnum.NOTIFY.getKafkaSiteName(), MessageQueueConstant.KAFKA_NOTIFY_TOPIC, String.valueOf(i), "Notify :: "+payload.getPayload()+i*3);
        }

//        for (int i = 0; i < 10; i++) {
//            mqKafkaNotifier.sendMessage(KafkaBrokerEnum.ONE.getKafkaSiteName(), MessageQueueConstant.KAFKA_TOPIC_TEST, String.valueOf(i), "Second "+payload.getPayload()+i);
//            mqKafkaNotifier.sendMessage(KafkaBrokerEnum.ALARM.getKafkaSiteName(), MessageQueueConstant.KAFKA_ALARM_TOPIC, String.valueOf(i), "Second Alarm :: "+payload.getPayload()+i*2);
//            mqKafkaNotifier.sendMessage(KafkaBrokerEnum.NOTIFY.getKafkaSiteName(), MessageQueueConstant.KAFKA_NOTIFY_TOPIC, String.valueOf(i), "Second Notify :: "+payload.getPayload()+i*3);
//        }

        return payload.getPayload();
    }

}
