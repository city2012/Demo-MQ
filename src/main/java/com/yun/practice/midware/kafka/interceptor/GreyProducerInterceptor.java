package com.yun.practice.midware.kafka.interceptor;

import com.yun.practice.midware.common.constant.Constants;
import com.yun.practice.midware.utils.FlowflagHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Component
public class GreyProducerInterceptor implements ProducerInterceptor {
    @Override
    public ProducerRecord onSend(ProducerRecord record) {
        log.info("{} ---- onSend!", this.getClass().getSimpleName());
        final String flowFlag = FlowflagHelper.getFlowFlag();
        if (StringUtils.isBlank(flowFlag)){
            return record;
        }
        Headers headers = record.headers();
        if (null != headers){
            headers.add(getHeader(flowFlag));
        }else {
            headers = new RecordHeaders(new Header[]{getHeader(flowFlag)});
        }
        return new ProducerRecord(record.topic(),record.partition(),record.timestamp(),
                record.key(), record.value(), headers);
    }


    private static Header getHeader(String flowFlag) {
        return new Header() {
            @Override
            public String key() {
                return Constants.MQ_FLOWFLAG;
            }

            @Override
            public byte[] value() {
                return flowFlag.getBytes(StandardCharsets.UTF_8);
            }
        };
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {

    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> configs) {

    }
}
