package com.luban.dingtalk.userpicker.domain.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.luban.dingtalk.userpicker.domain.DingTalkPickerNodeSource;

import java.io.IOException;

/**
 * @author hp
 */
public class DingTalkPickerNodeSourceSerializer extends JsonSerializer<DingTalkPickerNodeSource> {
    @Override
    public void serialize(DingTalkPickerNodeSource value, JsonGenerator gen, SerializerProvider serializers) {
        try {
            gen.writeString(value.getCode());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
