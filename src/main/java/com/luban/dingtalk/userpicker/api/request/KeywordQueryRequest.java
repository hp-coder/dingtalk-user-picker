package com.luban.dingtalk.userpicker.api.request;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.luban.common.base.annotations.FieldDesc;
import com.luban.common.base.model.Request;
import com.luban.dingtalk.userpicker.api.deserializer.DingTalkPickerNodeSourceDeserializer;
import com.luban.dingtalk.userpicker.domain.DingTalkPickerNodeSource;
import lombok.Data;

import java.util.List;

/**
 * @author hp 2023/5/4
 */
public @Data class KeywordQueryRequest implements Request {

    @FieldDesc("关键字,需要接口自行调用validate校验")
    private String keyword;

    @FieldDesc("前端提供的源id, 一般由另一个(父)选择器产生")
    private List<String> sourceUserIds;

    @FieldDesc("架构源")
    @JsonDeserialize(using = DingTalkPickerNodeSourceDeserializer.class)
    private DingTalkPickerNodeSource source;

    public void setKeyword() {
        if (StrUtil.isNotEmpty(keyword)) {
            this.keyword = keyword.trim();
        }
    }

    public boolean isEmptyKeyword() {
        return StrUtil.isEmpty(keyword);
    }
}
