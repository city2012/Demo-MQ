package com.yun.practice.midware.rabbit.mq.consumer;

import com.rabbitmq.client.Channel;
import com.yun.practice.midware.rabbit.common.constant.MessageQueueConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FistQConsumer extends BaseConsumer{

    @RabbitListener(queues = MessageQueueConstant.FIRST_QUEUE)
    public void onFirstMessage(Message message, Channel channel,
                                @Header(AmqpHeaders.DELIVERY_TAG) Long deliveryTag,
                                @Header(AmqpHeaders.REDELIVERED) Boolean redelivered) {

        super.handle(message, channel, deliveryTag, redelivered);
    }

    @Override
    protected void doHandle(String message) {

        log.info("{} - consumer receive - {}", this.getClass(), message);

    }
}
