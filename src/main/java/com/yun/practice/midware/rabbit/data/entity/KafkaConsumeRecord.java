package com.yun.practice.midware.rabbit.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("kafka_consume_record")
public class KafkaConsumeRecord extends AuditEntity{

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String topic;
    private String message;
    private String handleStatus;
    private Integer retryTimes;
    private String businessId;

}
