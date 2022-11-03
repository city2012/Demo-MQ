package com.yun.practice.midware.rabbit;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@MapperScan(basePackages = {"com.yun.practice.midware.rabbit.data.mapper"})
@SpringBootApplication
public class DemoRabbitApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoRabbitApplication.class, args);
    }

}
