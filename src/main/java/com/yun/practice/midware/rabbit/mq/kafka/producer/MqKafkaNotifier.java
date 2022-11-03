package com.yun.practice.midware.rabbit.mq.kafka.producer;

import com.yun.practice.midware.rabbit.common.KafkaBrokerEnum;
import com.yun.practice.midware.rabbit.common.constant.MessageQueueConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class MqKafkaNotifier {

    private final ApplicationContext applicationContext;

    public void sendMessage(String kafkaSiteName, String topic, String key, String message) {
        log.info("==={} :: Producing message: {}", kafkaSiteName, message);
        this.getBean(kafkaSiteName).send(topic, key, message).addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onFailure(Throwable ex) {
                log.error("Fail to publish", ex);
            }

            @Override
            public void onSuccess(SendResult<String, String> result) {
                log.info("Success published! :: {}", result);
            }
        });
    }


    private KafkaTemplate getBean(String beanName) {
        Object bean = null;
        try {
            bean = applicationContext.getBean(beanName);
        } catch (Exception ex) {
            log.warn("SpringBean is not existed {}", beanName);
        }
        return Objects.isNull(bean) ? null : (KafkaTemplate) bean;
    }

}
