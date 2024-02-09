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
public class ArchitectureByParentRequest implements Request {

   @FieldDesc("部门id")
   private String deptId;

   @FieldDesc("架构源")
   @JsonDeserialize(using = DingTalkPickerNodeSourceDeserializer.class)
   private DingTalkPickerNodeSource source;

   @FieldDesc("前端可以通过父选择器提供一id集")
   private List<String> sourceUserIds;
}
