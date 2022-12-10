package com.yun.practice.midware.web;

import com.yun.practice.midware.common.constant.MessageQueueConstant;
import com.yun.practice.midware.dto.CommonRequest;
import com.yun.practice.midware.rabbit.publisher.MessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RabbitController {

    private final MessageSender messageSender;

    @PostMapping("/msg/simple/publish/first")
    public String publishTest01(@RequestBody CommonRequest payload){

      log.info("publishTest01 :: {}-{}",this.getClass(),payload);

        for (int i = 0; i < 5; i++) {
            messageSender.send(MessageQueueConstant.TEST_EXCHANGE, MessageQueueConstant.FIRST_ROUTINGKEY, payload.getPayload());
        }

      return payload.getPayload();
    }

    @GetMapping("/msg/simple/look")
    public String publishTest02(@RequestParam String payload){

        log.info("{}-{}",this.getClass(),payload);
        return payload;
    }

    @PostMapping("/msg/simple/publish/second")
    public String publishTest03(@RequestBody CommonRequest payload){

        log.info("publishTest03 :: {}-{}",this.getClass(),payload);

        for (int i = 0; i < 3; i++) {
            messageSender.send(MessageQueueConstant.TEST_EXCHANGE, MessageQueueConstant.SECOND_ROUTINGKEY, payload.getPayload());
        }
        return payload.getPayload();
    }

}
