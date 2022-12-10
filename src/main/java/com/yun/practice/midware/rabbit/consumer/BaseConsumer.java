package com.yun.practice.midware.rabbit.consumer;

import com.rabbitmq.client.Channel;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;

@Slf4j
public abstract class BaseConsumer {

    public void handle(Message message, Channel channel, Long deliveryTag,
                          Boolean redelivered){
        log.info("{} received msg :: {}", this.getClass(), message);
        String body = null;

        try{
            body = new String(message.getBody(), "UTF-8");
            if (StringUtils.isBlank(body)) {
                log.warn("{} msg body is blank", this.getClass());
                return;
            }

            doHandle(body);
        }catch (Exception ex){
            log.warn("{} message error body: ,{}", this.getClass(), body, ex);
            if (redelivered) {
                log.error(String.format("[{}]Consume mq fail.The message can't be handled.content:%s",this.getClass(), body), ex);
                nAck(channel, deliveryTag, false, false);
                return;
            }
        }finally{
            ack(channel, deliveryTag);
        }
    }

    protected abstract void doHandle(String message);

    protected void ack(Channel channel, long deliveryTag) {
        try {
            // 手动ack确认
            channel.basicAck(deliveryTag, false);
        } catch (Exception ex) {
            log.error("Channel basic ask error :", ex);
        }
    }

    protected void nAck(Channel channel, long deliveryTag, boolean multiple, boolean requeue) {
        try {
            // 手动nAck
            channel.basicNack(deliveryTag, multiple, requeue);
        } catch (Exception ex) {
            log.error("Channel basic ask error :", ex);
        }
    }

}
