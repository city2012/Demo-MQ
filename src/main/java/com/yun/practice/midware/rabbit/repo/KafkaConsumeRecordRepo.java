package com.yun.practice.midware.rabbit.repo;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.yun.practice.midware.rabbit.data.entity.KafkaConsumeRecord;
import com.yun.practice.midware.rabbit.data.mapper.KafkaConsumeRecordMapper;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaConsumeRecordRepo {

    private final KafkaConsumeRecordMapper kafkaConsumeRecordMapper;
    public void save(KafkaConsumeRecord failure) {
        kafkaConsumeRecordMapper.insert(failure);
    }

    public KafkaConsumeRecord queryByBusinessId(String key) {

        if (StringUtils.isBlank(key)){
            return null;
        }

        return new LambdaQueryChainWrapper<>(kafkaConsumeRecordMapper)
                .eq(KafkaConsumeRecord::getBusinessId, key)
                .last("limit 1")
                .one();
    }
}
