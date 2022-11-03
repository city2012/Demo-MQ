package com.yun.practice.midware.rabbit.config.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yun.practice.midware.rabbit.config.kafka.partitioner.NumberRoundRobinPartitioner;
import com.yun.practice.midware.rabbit.mq.kafka.consumer.handler.AlarmErrorHandler;
import com.yun.practice.midware.rabbit.mq.kafka.consumer.handler.MyBatchErrorHandler;
import com.yun.practice.midware.rabbit.utils.JsonUtils;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.*;
import org.springframework.kafka.support.LoggingProducerListener;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.AlwaysRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.FixedBackOff;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "kafka.ext")
public class KafkaConfiguration {

    @Resource
    private KafkaProperties kafkaProperties;
    @Resource
    private ApplicationContext applicationContext;
    @Getter
    @Setter
    @Deprecated
    private List<ExtKafkaProperties> others;

    @Getter
    @Setter
    private Map<String, ExtKafkaProperties> multiple;

    /*****************Producer********************/

    /**
     * Producer config setup
     *
     * @param kafkaProducerFactory
     * @param kafkaProducerListener
     * @param messageConverter
     * @return
     */
    @Bean("kafkaTemplate")
    @DependsOn(value = "defaultKafkaProducerFactory")
    public KafkaTemplate<?, ?> kafkaTemplate(@Qualifier("defaultKafkaProducerFactory") ProducerFactory<Object, Object> kafkaProducerFactory,
                                             ProducerListener<Object, Object> kafkaProducerListener,
                                             ObjectProvider<RecordMessageConverter> messageConverter) {
        log.info("Read First kafka Factory :: {}", kafkaProducerFactory);
        KafkaTemplate<Object, Object> kafkaTemplate = new KafkaTemplate(kafkaProducerFactory);
        messageConverter.ifUnique(kafkaTemplate::setMessageConverter);
        kafkaTemplate.setProducerListener(kafkaProducerListener);
        kafkaTemplate.setDefaultTopic(this.kafkaProperties.getTemplate().getDefaultTopic());
        return kafkaTemplate;
    }

    @Bean
    public ProducerListener<Object, Object> kafkaProducerListener() {
        return new LoggingProducerListener();
    }

    @Bean("defaultKafkaProducerFactory")
    public ProducerFactory<Object, Object> kafkaProducerFactory() {
        final Map<String, Object> properties = this.kafkaProperties.buildProducerProperties();
        properties.put("partitioner.class", NumberRoundRobinPartitioner.class);
        DefaultKafkaProducerFactory<Object, Object> factory = new DefaultKafkaProducerFactory(properties);
        String transactionIdPrefix = this.kafkaProperties.getProducer().getTransactionIdPrefix();
        if (transactionIdPrefix != null) {
            factory.setTransactionIdPrefix(transactionIdPrefix);
        }

        return factory;
    }

    @Bean
    public String initOtherKafkaProducer(ProducerListener<Object, Object> kafkaProducerListener, ObjectProvider<RecordMessageConverter> messageConverter) {
        if (CollectionUtils.isEmpty(this.others) && MapUtils.isEmpty(this.multiple)) {
            log.info("initOtherKafkaProducer end! properties is null...");
        } else {
            if (CollectionUtils.isNotEmpty(this.others)) {
                this.others.stream().filter(x -> StringUtils.isNotEmpty(x.getProducerBeanName()))
                        .forEach((e) -> configOtherProducer(kafkaProducerListener, messageConverter, e, e.getProducerBeanName()));
            }

            if (MapUtils.isNotEmpty(this.multiple)) {
                this.multiple.forEach((k, v) -> {
                    final ExtKafkaProperties.Producer extProducer = v.getProducer();
                    if (Objects.isNull(extProducer)
                            || StringUtils.isBlank(extProducer.getProducerBeanName())
                            || !extProducer.getProducerBeanName().contains("KafkaTemplate")) {
                        log.info("{} no kafka producer to config.", k);
                        return;
                    }
                    log.info("init with map, k-v,{}-{}", k, v);
                    configOtherProducer(kafkaProducerListener, messageConverter, v, extProducer.getProducerBeanName());
                });
            }
        }
        return "";
    }

    private void configOtherProducer(ProducerListener<Object, Object> kafkaProducerListener,
                                     ObjectProvider<RecordMessageConverter> messageConverter,
                                     ExtKafkaProperties extKafkaProperties, String producerBeanName) {

        ConfigurableApplicationContext context = (ConfigurableApplicationContext) this.applicationContext;
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getBeanFactory();
        if (beanFactory.containsBean(producerBeanName)) {
            log.info("{} Other producer has been inited!", producerBeanName);
            return;
        }

        KafkaTemplate<Object, Object> kafkaTemplate = new KafkaTemplate(this.getProducerFactory(extKafkaProperties));
        messageConverter.ifUnique(kafkaTemplate::setMessageConverter);
        kafkaTemplate.setProducerListener(kafkaProducerListener);
        kafkaTemplate.setDefaultTopic(this.kafkaProperties.getTemplate().getDefaultTopic());
        beanFactory.registerSingleton(producerBeanName, kafkaTemplate);
        log.info("initOtherKafkaProducer success! beanName:{},bootstrap.servers:{}", producerBeanName, extKafkaProperties.getBootstrapServers());
    }

    private ProducerFactory<Object, Object> getProducerFactory(ExtKafkaProperties extKafkaProperties) {
        Map<String, Object> producerProperties = this.kafkaProperties.buildProducerProperties();
        producerProperties.put("bootstrap.servers", extKafkaProperties.getBootstrapServers());
        producerProperties.put("key.serializer", extKafkaProperties.getProducerKeySerializer());
        producerProperties.put("value.serializer", extKafkaProperties.getProducerValueSerializer());
        producerProperties.put("partitioner.class", NumberRoundRobinPartitioner.class);
        DefaultKafkaProducerFactory<Object, Object> factory = new DefaultKafkaProducerFactory(producerProperties);
        String transactionIdPrefix = this.kafkaProperties.getProducer().getTransactionIdPrefix();
        if (transactionIdPrefix != null) {
            factory.setTransactionIdPrefix(transactionIdPrefix);
        }
        return factory;
    }


    /*****************Consumer********************/

    /**
     * Consumer config setup
     *
     * @return
     */
    @SneakyThrows
    @Bean("defaultKafkaListenerContainerFactory")
    public KafkaListenerContainerFactory<?> kafkaListenerContainerFactory() {
        log.info("kafka consumer setup");
        ConcurrentKafkaListenerContainerFactory<Integer, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(getConsumerFactory(null));
        factory.setConcurrency(1);
        factory.setAutoStartup(true);
        factory.getContainerProperties().setPollTimeout(3000);
//        SeekToCurrentErrorHandler single = new SeekToCurrentErrorHandler((consumerRecord, e) -> {
//            log.error("异常.抛弃这个消息============,{}", consumerRecord.toString(), e);
//        }, 3);
//        factory.setErrorHandler(single);
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(100,3)));
        if (!factory.getConsumerFactory().isAutoCommit()){
            factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        }
        log.info("consumer default factory :: {}", JsonUtils.toJsonHasNullKey(factory));
        return factory;
    }


    @Bean
    public String initOtherKafkaConsumer() {
        if (MapUtils.isEmpty(this.multiple)) {
            log.info("initOtherKafkaConsumer end! properties is null...");
        } else {

            Integer concurrency = Optional.ofNullable(kafkaProperties.getListener()).map(KafkaProperties.Listener::getConcurrency).orElse(1);
            this.multiple.forEach((k, v) -> {
                final ExtKafkaProperties.Consumer extConsumer = v.getConsumer();
                if (Objects.isNull(extConsumer)
                        || StringUtils.isBlank(extConsumer.getListenerContainerFactory())) {
                    log.info("{} no kafka consumer to config.", k);
                    return;
                }
                configOtherConsumer(concurrency, v, extConsumer.getListenerContainerFactory());
            });
        }
        return "";
    }

    @SneakyThrows
    private void configOtherConsumer(Integer concurrency, ExtKafkaProperties extKafkaProperties, String listenerBeanName) {
        ConfigurableApplicationContext context = (ConfigurableApplicationContext) this.applicationContext;
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getBeanFactory();
        if (beanFactory.containsBean(listenerBeanName)) {
            log.info("{} Other consumer has been inited!", listenerBeanName);
            return;
        }

        final ExtKafkaProperties.Consumer extConsumer = extKafkaProperties.getConsumer();
        ConcurrentKafkaListenerContainerFactory<Integer, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(getConsumerFactory(extKafkaProperties));
        factory.setConcurrency(Optional.ofNullable(extConsumer.getListenerConcurrency()).orElse(concurrency));
        factory.setAutoStartup(true);

        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(100,3)));
//        if (Objects.nonNull(extConsumer.getListenerBatch())&&extConsumer.getListenerBatch()){
//            factory.setBatchErrorHandler(new MyBatchErrorHandler(3));
//        }else {
//            SeekToCurrentErrorHandler single = new SeekToCurrentErrorHandler((consumerRecord, e) -> {
//                log.error("异常.抛弃这个消息============,{}", consumerRecord.toString(), e);
//            }, 3);
//            factory.setErrorHandler(single);
//        }

        Optional.ofNullable(extConsumer.getListenerBatch()).ifPresent(factory::setBatchListener);
        factory.getContainerProperties().setPollTimeout(3000);
        if (!factory.getConsumerFactory().isAutoCommit()){
            factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        }
        beanFactory.registerSingleton(extConsumer.getListenerContainerFactory(), factory);
        log.info("initOtherKafkaConsumer consumerconfig succ :: {}", JsonUtils.toJsonHasNullKey(factory));
        log.info("initOtherKafkaConsumer success! beanName:{},bootstrap.servers:{}", extConsumer.getListenerContainerFactory(), extKafkaProperties.getBootstrapServers());
    }

    private ConsumerFactory<Object, Object> getConsumerFactory(ExtKafkaProperties extKafkaProperties) {
        Map<String, Object> consumerProperties = this.kafkaProperties.buildConsumerProperties();
        if (Objects.isNull(extKafkaProperties)) {
            final StringDeserializer stringDeserializer = new StringDeserializer();
            return new DefaultKafkaConsumerFactory(consumerProperties, stringDeserializer, stringDeserializer);
        }
        consumerProperties.put("bootstrap.servers", extKafkaProperties.getBootstrapServers());
        consumerProperties.put("group.id", extKafkaProperties.getConsumer().getListenerGroupId());
        Optional.ofNullable(extKafkaProperties.getConsumer().getEnableAutoCommit()).ifPresent(x -> consumerProperties.put("enable.auto.commit", x));

        DefaultKafkaConsumerFactory<Object, Object> factory = new DefaultKafkaConsumerFactory(consumerProperties, extKafkaProperties.getDefaultDeserializer(), extKafkaProperties.getDefaultDeserializer());
        return factory;
    }


}
