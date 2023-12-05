package com.luban.dingtalk.userpicker.infrastructure.pinyin;

import com.luban.common.base.annotations.FieldDesc;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * @author hp 2023/4/28
 */
public interface PinyinConverter {

    List<PinyinModel> chineseToPinyin(String chinese);

    boolean isChineseLetter(char c);

    @Data
    @AllArgsConstructor
    class PinyinModel {

        @FieldDesc("名称替换中文后的结果")
        private String name;

        @FieldDesc("替换字的拼音: LinkedHashSet")
        private Set<String> pinyin;

        @FieldDesc("替换字的拼音首字母")
        private Set<String> shortPinyin;
    }
}
