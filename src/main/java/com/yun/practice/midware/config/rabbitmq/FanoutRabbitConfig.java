package com.yun.practice.midware.config.rabbitmq;

import com.yun.practice.midware.common.constant.MessageQueueConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FanoutRabbitConfig {

    //创建FanoutExchange
    @Bean
    FanoutExchange fanoutExchange(){
        return new FanoutExchange(MessageQueueConstant.TEST_FANOUT_EXCHANGE,true,false);
    }

    //创建队列A
    @Bean
    Queue queueA(){
        return new Queue(MessageQueueConstant.FANOUT_FIRST_QUEUE,true,false,false);
    }

    //创建队列B
    @Bean
    Queue queueB(){
        return new Queue(MessageQueueConstant.FANOUT_SECOND_QUEUE,true,false,false);
    }

    //创建队列C
    @Bean
    Queue queueC(){
        return new Queue(MessageQueueConstant.FANOUT_THIRD_QUEUE,true,false,false);
    }

    //将创建的队列绑定到创建的交换机上
    @Bean
    Binding bindingA(){
        return BindingBuilder.bind(queueA()).to(fanoutExchange());
    }
    @Bean
    Binding bindingB(){
        return BindingBuilder.bind(queueB()).to(fanoutExchange());
    }
    @Bean
    Binding bindingC(){
        return BindingBuilder.bind(queueC()).to(fanoutExchange());
    }

}
