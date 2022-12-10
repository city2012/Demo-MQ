package com.yun.practice.midware.data.entity;

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
@TableName("kafka_publish_record")
public class KafkaPublishRecord extends AuditEntity{

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String topic;
    private String message;
    private String sendStatus;
    private Integer retryTimes;
    private String businessId;
    private String kafkaSiteName;

}
