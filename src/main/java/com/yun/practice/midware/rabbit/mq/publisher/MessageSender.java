package com.yun.practice.midware.rabbit.mq.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageSender {

    private final RabbitTemplate rabbitTemplate;

    public void send(String exchange, String routingKey,String messageData) {

        String messageId = UUID.randomUUID().toString();
        String current = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        Map<String,Object> map = new HashMap<>();
        map.put("messageId",messageId);
        map.put("data",messageData);
        map.put("current",current);
        rabbitTemplate.convertAndSend(exchange, routingKey, map, new CorrelationData(UUID.randomUUID().toString()));

    }

}
