package com.hp.dingtalk.userpicker.api.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hp.common.base.annotations.FieldDesc;
import com.hp.common.base.model.Request;
import com.hp.dingtalk.userpicker.domain.DingTalkPickerNodeSource;
import com.hp.dingtalk.userpicker.api.deserializer.DingTalkPickerNodeSourceDeserializer;
import lombok.Data;

import java.util.List;

/**
 * @author hp 2023/5/4
 */
@Data
public class ArchitectureRequest implements Request {

    @FieldDesc("前端通过父组件提供一个id集")
    private List<String> sourceUserIds;

    @FieldDesc("数据来源")
    @JsonDeserialize(using = DingTalkPickerNodeSourceDeserializer.class)
    private DingTalkPickerNodeSource source;
}
