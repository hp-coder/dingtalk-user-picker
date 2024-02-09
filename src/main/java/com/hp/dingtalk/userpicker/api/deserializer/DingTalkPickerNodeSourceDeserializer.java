package com.hp.dingtalk.userpicker.api.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.hp.dingtalk.userpicker.domain.DingTalkPickerNodeSource;

import java.io.IOException;
import java.util.Optional;

/**
 * @author hp
 */
public class DingTalkPickerNodeSourceDeserializer extends JsonDeserializer<DingTalkPickerNodeSource> {

    @Override
    public DingTalkPickerNodeSource deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return Optional.ofNullable(p.readValueAs(String.class))
                .map(DingTalkPickerNodeSource::of)
                .flatMap(opt -> opt)
                .orElse(null);
    }
}
