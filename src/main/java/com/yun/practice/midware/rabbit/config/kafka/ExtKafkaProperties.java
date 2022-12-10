package com.yun.practice.midware.rabbit.config.kafka;

import lombok.Data;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;

import java.util.List;

@Data
public class ExtKafkaProperties {


    private List<String> bootstrapServers;

    private Producer producer;
    private Consumer consumer;
    @Data
    public static class Producer{
        private String producerBeanName;
        private String producerFactory;
    }

    @Data
    public static class Consumer{

        private String listenerContainerFactory;

        private Integer listenerConcurrency;

        private String listenerGroupId;

        private Boolean enableAutoCommit;

        private Boolean listenerBatch;

    }


    private Deserializer<?> defaultDeserializer = new StringDeserializer();

    private Class<?> producerKeySerializer = StringSerializer.class;

    private Class<?> producerValueSerializer = StringSerializer.class;

    private String producerBeanName;

}
