package com.yun.practice.midware.rabbit;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import springfox.documentation.oas.annotations.EnableOpenApi;

@MapperScan(basePackages = {"com.yun.practice.midware.rabbit.data.mapper"})
@SpringBootApplication
@EnableEurekaClient
public class DemoRabbitApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoRabbitApplication.class, args);
    }

}
