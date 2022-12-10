package com.yun.practice.midware.rabbit.config.kafka;

import com.yun.practice.midware.rabbit.mq.interceptor.GreyConsumerInterceptor;
import com.yun.practice.midware.rabbit.mq.interceptor.GreyProducerInterceptor;
import com.yun.practice.midware.rabbit.utils.FlowflagHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.ProducerFactory;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.*;

@Slf4j
@Configuration
public class KafkaBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        log.info("{} instantceof KafkaListenerContainerFactory :: {}",beanName,bean instanceof KafkaListenerContainerFactory);
        if (bean instanceof KafkaListenerContainerFactory){
            final KafkaListenerContainerFactory containerFactory = (KafkaListenerContainerFactory) bean;
            return processListnerFactory(containerFactory);
        }else if (bean instanceof ProducerFactory){
            final ProducerFactory producerFactory = (ProducerFactory) bean;
            return processProducerFactory(producerFactory);
        }
        return bean;
    }

    private Object processProducerFactory(ProducerFactory producerFactory) {

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

    private Field getField(Object obj, String fieldName) {

        Class clazz = obj.getClass();
        // 获取类中声明的字段
        Field[] fields = clazz.getDeclaredFields();
        Field result = null;
        for (Field field : fields) {
            // 避免 can not access a member of class com.java.test.Person with modifiers "private"
            field.setAccessible(true);
            if (fieldName.equals(field.getName())) {
                result = field;
                break;
            }
        }
        return result;
    }


    public static Field getDeclaredField(Object object, String fieldName){
        Field field = null ;
        Class<?> clazz = object.getClass() ;
        for(; clazz != Object.class ; clazz = clazz.getSuperclass()) {
            try {
                field = clazz.getDeclaredField(fieldName) ;
                return field ;
            } catch (Exception e) {
                //这里甚么都不要做！并且这里的异常必须这样写，不能抛出去。
                //如果这里的异常打印或者往外抛，则就不会执行clazz = clazz.getSuperclass(),最后就不会进入到父类中了
            }
        }
        return null;
    }

    /**     * 实例化并调用已经注入的 BeanPostProcessor     * 必须在应用中 bean 实例化之前调用     */
//    protected void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory) {
//        PostProcessorRegistrationDelegate.registerBeanPostProcessors(beanFactory, this);
//    }
//    public static void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {
//        // 获取所有的 BeanPostProcessor 的 beanName        // 这些 beanName 都已经全部加载到容器中去，但是没有实例化
//        String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);
//        // 记录所有的beanProcessor数量
//        int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
//        // 注册 BeanPostProcessorChecker，它主要是用于在 BeanPostProcessor 实例化期间记录日志
//        // 当 Spring 中高配置的后置处理器还没有注册就已经开始了 bean 的实例化过程，这个时候便会打印 BeanPostProcessorChecker 中的内容
//        beanFactory.addBeanPostProcessor(new PostProcessorRegistrationDelegate.BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));
//        // PriorityOrdered 保证顺序
//        List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
//        // MergedBeanDefinitionPostProcessor
//        List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
//        // 使用 Ordered 保证顺序
//        List<String> orderedPostProcessorNames = new ArrayList<>();
//        // 没有顺序
//        List<String> nonOrderedPostProcessorNames = new ArrayList<>();
//        for (String ppName : postProcessorNames) {
//            // PriorityOrdered
//            if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
//                // 调用 getBean 获取 bean 实例对象
//                BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
//                priorityOrderedPostProcessors.add(pp);
//                if (pp instanceof MergedBeanDefinitionPostProcessor) {
//                    internalPostProcessors.add(pp);
//                }
//            }
//            // Ordered
//            else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
//                orderedPostProcessorNames.add(ppName);
//            }
//            else {
//                // 无序
//                nonOrderedPostProcessorNames.add(ppName);
//            }
//        }
//        // 第一步注册所有实现了 PriorityOrdered 的BeanPostProcessor
//        // 先排序
//        sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
//        // 后注册
//        registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);
//        // 第二步注册所有实现了 Ordered 的 BeanPostProcessor
//        List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>();
//        for (String ppName : orderedPostProcessorNames) {
//            BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
//            orderedPostProcessors.add(pp);
//            if (pp instanceof MergedBeanDefinitionPostProcessor) {
//                internalPostProcessors.add(pp);
//            }
//        }
//        sortPostProcessors(orderedPostProcessors, beanFactory);
//        registerBeanPostProcessors(beanFactory, orderedPostProcessors);
//        // 第三步注册所有无序的 BeanPostProcessor
//        List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>();
//        for (String ppName : nonOrderedPostProcessorNames) {
//            BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
//            nonOrderedPostProcessors.add(pp);
//            if (pp instanceof MergedBeanDefinitionPostProcessor) {
//                internalPostProcessors.add(pp);
//            }
//        }
//        registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);
//        // 最后，注册所有的 MergedBeanDefinitionPostProcessor 类型的 BeanPostProcessor
//        sortPostProcessors(internalPostProcessors, beanFactory);
//        registerBeanPostProcessors(beanFactory, internalPostProcessors);
//        // 加入ApplicationListenerDetector（探测器）        // 重新注册 BeanPostProcessor 以检测内部 bean，因为 ApplicationListeners 将其移动到处理器链的末尾
//        beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
//    }

}
