package com.yun.practice.midware.kafka.consumer.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.kafka.listener.ConsumerAwareListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Slf4j
@Service("alarmErrorHandler")
public class AlarmErrorHandler implements ConsumerAwareListenerErrorHandler {
    @Override
    public Object handleError(Message<?> message, ListenerExecutionFailedException e, Consumer<?, ?> consumer) {

        message.getPayload();
        log.error("{} kafka consume handleError for {} handleError :: ", this.getClass().getSimpleName(), message, e);

        return null;
    }
}

