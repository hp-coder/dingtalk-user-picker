package com.luban.dingtalk.userpicker.domain.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.luban.dingtalk.userpicker.domain.DingTalkPickerNodeType;

import java.io.IOException;

/**
 * @author hp
 */
public class DingTalkPickerNodeTypeSerializer extends JsonSerializer<DingTalkPickerNodeType> {
    @Override
    public void serialize(DingTalkPickerNodeType value, JsonGenerator gen, SerializerProvider serializers) {
        try {
            gen.writeString(value.getCode());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
