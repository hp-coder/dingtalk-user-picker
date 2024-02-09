package com.hp.dingtalk.userpicker.infrastructure.pinyin;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hp.common.base.annotations.MethodDesc;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hp
 */
@Slf4j
public class DefaultPinyinConverter implements PinyinConverter {

    @Override
    public List<PinyinModel> chineseToPinyin(String chinese) {
        try {
            return pinyin(chinese);
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            log.error("pinyin4j Exception", e);
            throw new RuntimeException(e);
        }
    }

    @MethodDesc("是否中文字符")
    @Override
    public boolean isChineseLetter(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D
                || block == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || block == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT;
    }

    @MethodDesc("pinyin4j只能处理纯中文; 解析名称转拼音后的绝大部分可能结果")
    private List<PinyinModel> pinyin(String chinese) throws BadHanyuPinyinOutputFormatCombination {
        List<PinyinModel> allPossibilities = Lists.newArrayList(new PinyinModel(chinese, Sets.newLinkedHashSet(), Sets.newLinkedHashSet()));
        final HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (char c : chinese.toCharArray()) {
            Set<String> pinyinSet;
            if (isChineseLetter(c)) {
                final String[] pinyin = PinyinHelper.toHanyuPinyinStringArray(c, format);
                pinyinSet = pinyin == null ? null : Sets.newLinkedHashSet(Arrays.asList(pinyin));
            } else {
                pinyinSet = Sets.newLinkedHashSet();
                pinyinSet.add(String.valueOf(c));
            }
            allPossibilities = possibilities(String.valueOf(c), allPossibilities, pinyinSet);
        }
        return allPossibilities;
    }

    @MethodDesc("多音字的所有可能,但对于多音字连字问题没做处理,这种类库都依赖外部解析库,外部库存在多音字不准确的问题,暂时不处理")
    private List<PinyinModel> possibilities(String letter, List<PinyinModel> list, Set<String> pinyin) {
        return list.stream().map(i ->
                        {
                            if (CollUtil.isEmpty(pinyin)) {
                                return null;
                            }
                            return pinyin.stream()
                                    .map(j -> {
                                        //这里只是简单替换所有出现的字符, 没有真的做到 x^2 种可能性
                                        //例如 的 = (de/di);
                                        //的的 = dedi/dede/dide/didi, 这里无脑换,只会换出 dede一种情况
                                        i.setName(i.getName().replace(letter, j));
                                        i.getPinyin().add(j);
                                        i.getShortPinyin().add(String.valueOf(j.charAt(0)));
                                        return i;
                                    })
                                    .collect(Collectors.toList());
                        }
                )
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
