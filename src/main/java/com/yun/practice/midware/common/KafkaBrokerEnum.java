package com.yun.practice.midware.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum KafkaBrokerEnum {

    /**
     * CARD broker bootstrap servers
     */
    ONE("kafkaTemplate"),
    /**
     * GROWTH broker bootstrap servers
     */
    ALARM("alarmKafkaTemplate"),

    /**
     * GROWTH broker bootstrap servers
     */
    NOTIFY("notifyKafkaTemplate"),
    ;

    private String kafkaSiteName;

    public static KafkaBrokerEnum getByKafkaSiteName(String kafkaSiteName){
        return Arrays.stream(values()).filter(x->x.getKafkaSiteName().equals(kafkaSiteName)).findAny().orElse(null);
    }

}
