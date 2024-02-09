package com.hp.dingtalk.userpicker.domain;

import com.hp.common.base.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

/**
 * @author hp
 */
@Getter
@AllArgsConstructor
public enum DingTalkPickerNodeSource implements BaseEnum<DingTalkPickerNodeSource, String> {
    /***/
    DING_TALK("dingtalk", "钉钉"),
    SYSTEM("system", "系统"),
    ;
    private final String code;
    private final String name;

    public static Optional<DingTalkPickerNodeSource> of(String code) {
        return Optional.ofNullable(BaseEnum.parseByCode(DingTalkPickerNodeSource.class, code));
    }
}
