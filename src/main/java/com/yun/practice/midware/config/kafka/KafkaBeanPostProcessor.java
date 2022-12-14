package com.yun.practice.midware.config.kafka;

import com.yun.practice.midware.kafka.interceptor.GreyConsumerInterceptor;
import com.yun.practice.midware.kafka.interceptor.GreyProducerInterceptor;
import com.yun.practice.midware.utils.FlowflagHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Configuration
public class KafkaBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        log.info("{} instantceof KafkaListenerContainerFactory :: {}",beanName,bean instanceof KafkaListenerContainerFactory);
        if (bean instanceof KafkaListenerContainerFactory){
            final KafkaListenerContainerFactory containerFactory = (KafkaListenerContainerFactory) bean;
            return processListnerFactory(containerFactory);
        }
//        else if (bean instanceof ProducerFactory){
//            final ProducerFactory producerFactory = (ProducerFactory) bean;
//            return processProducerFactory(producerFactory);
//        }
        else if(bean instanceof KafkaTemplate){
            return processKafkaTemplate((KafkaTemplate) bean);
        }
        return bean;
    }

    private ProducerFactory processKafkaTemplate(KafkaTemplate kafkaTemplate) {

        final ProducerFactory producerFactory = kafkaTemplate.getProducerFactory();
        final Field configsField = getDeclaredField(producerFactory, "configs");
        try {
            configsField.setAccessible(true);
            final Map<String, Object> configs = (Map<String, Object>) configsField.get(producerFactory);
            final List<String> interceptors = new ArrayList<>();
            interceptors.add(GreyProducerInterceptor.class.getName());
            configs.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, interceptors);

        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
        return producerFactory;
    }

    private ProducerFactory processProducerFactory(ProducerFactory producerFactory) {

        final Field configsField = getDeclaredField(producerFactory, "configs");
        try {
            configsField.setAccessible(true);
            final Map<String, Object> configs = (Map<String, Object>) configsField.get(producerFactory);
            final List<String> interceptors = new ArrayList<>();
            interceptors.add(GreyProducerInterceptor.class.getName());
            configs.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, interceptors);

        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
        return producerFactory;
    }

    private static KafkaListenerContainerFactory processListnerFactory(KafkaListenerContainerFactory containerFactory) {

        final Field consumerFactoryField = getDeclaredField(containerFactory, "consumerFactory");
        try {
            consumerFactoryField.setAccessible(true);
            final ConsumerFactory consumerFactory = (ConsumerFactory) consumerFactoryField.get(containerFactory);
            final Field configsField = getDeclaredField(consumerFactory, "configs");
            configsField.setAccessible(true);
            final Map<String, Object> configs = (Map<String, Object>) configsField.get(consumerFactory);

            final List<String> interceptors = new ArrayList<>();
            interceptors.add(GreyConsumerInterceptor.class.getName());
            configs.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, interceptors);

//                BrokerSetUp.setUpBrokers(configs);

            if (!FlowflagHelper.hasFlowtag()) {
                return containerFactory;
            }

            if (!Objects.isNull(configs.get(ConsumerConfig.GROUP_ID_CONFIG))) {
                configs.put(ConsumerConfig.GROUP_ID_CONFIG, configs.get(ConsumerConfig.GROUP_ID_CONFIG) + "_" + FlowflagHelper.getFlowFlag());
                log.warn("The new groupId is :: {}", configs.get(ConsumerConfig.GROUP_ID_CONFIG));
            }

            configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return containerFactory;
    }

    public static Field getDeclaredField(Object object, String fieldName){
        Field field = null ;
        Class<?> clazz = object.getClass() ;
        for(; clazz != Object.class ; clazz = clazz.getSuperclass()) {
            try {
                field = clazz.getDeclaredField(fieldName) ;
                return field ;
            } catch (Exception e) {
                //????????????????????????????????????????????????????????????????????????????????????
                //???????????????????????????????????????????????????????????????clazz = clazz.getSuperclass(),????????????????????????????????????
            }
        }
        return null;
    }

}
