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
public enum DingTalkPickerNodeType implements BaseEnum<DingTalkPickerNodeType, String> {
    /***/
    USER("user","用户",""),
    DEPT("dept","部门","https://gw.alicdn.com/imgextra/i2/O1CN011xNH2e1iiJe4JOWik_!!6000000004446-2-tps-120-120.png"),
    ORG("org","企业","https://static.dingtalk.com/media/lADPDeC2twQdNI_NAyDNAyA_800_800.jpg_620x10000q90.jpg"),
    ;
    private final String code;
    private final String name;
    private final String avatar;

    public static Optional<DingTalkPickerNodeType> of(String code) {
        return Optional.ofNullable(BaseEnum.parseByCode(DingTalkPickerNodeType.class, code));
    }
}
