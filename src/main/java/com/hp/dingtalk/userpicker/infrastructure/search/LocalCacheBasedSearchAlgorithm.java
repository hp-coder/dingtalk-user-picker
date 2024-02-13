package com.hp.dingtalk.userpicker.infrastructure.search;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.Chars;
import com.hp.common.base.annotations.FieldDesc;
import com.hp.common.base.annotations.MethodDesc;
import com.hp.dingtalk.userpicker.context.TranslationHolder;
import com.hp.dingtalk.userpicker.domain.DingTalkPickerNode;
import com.hp.dingtalk.userpicker.domain.DingTalkPickerNodeSource;
import com.hp.dingtalk.userpicker.domain.mapper.DingTalkPickerMapper;
import com.hp.dingtalk.userpicker.infrastructure.pinyin.PinyinConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author hp
 */
@Slf4j
@RequiredArgsConstructor
public class LocalCacheBasedSearchAlgorithm implements SearchAlgorithm {

    @FieldDesc("构建一个带有解析过拼音名称的用户节点集合,该集合已修正id,用于查询")
    private final Map<DingTalkPickerNodeSource, List<DingTalkPickerNode>> modelsWithPinyin = Maps.newHashMap();
    @FieldDesc("首字母与用户节点解析后,可能对应的全拼集合,用于替换首字母")
    private final Map<DingTalkPickerNodeSource, Map<String, Set<String>>> shortPinyinKeyedMapping = Maps.newHashMap();

    private final PinyinConverter pinyinConverter;

    @Override
    public void load(List<DingTalkPickerNode> data) {
        modelsWithPinyin.clear();
        shortPinyinKeyedMapping.clear();
        buildLocalCache(data);
    }

    @Override
    public List<DingTalkPickerNode> search(DingTalkPickerNodeSource source, String keywords) {
        //pre-matching
        final List<String> allPossibilities = shortToFull(source, keywords);
        if (CollUtil.isEmpty(allPossibilities)) {
            return Collections.emptyList();
        }
        //matching
        List<DingTalkPickerNode> sourceNodes;
        if (Objects.isNull(source)) {
            sourceNodes = modelsWithPinyin.values()
                    .stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        } else {
            if (!modelsWithPinyin.containsKey(source)) {
                return Collections.emptyList();
            }
            sourceNodes = modelsWithPinyin.get(source);
        }
        final List<DingTalkPickerNode> data = sourceNodes
                .stream()
                .filter(DingTalkPickerNode::isSearchable)
                .filter(i -> match(allPossibilities, i))
                .toList();
        //removing duplications
        return data.stream()
                .collect(Collectors.groupingBy(DingTalkPickerNode::getName))
                .values()
                .stream()
                .map(list -> list.stream()
                        .filter(i -> i.isSourceOf(DingTalkPickerNodeSource.DING_TALK))
                        .findAny()
                        .orElse(list.get(0)
                        )
                )
                .toList();
    }

    @MethodDesc("是否匹配关键字")
    private boolean match(List<String> allPossibilities, DingTalkPickerNode model) {
        for (String lowerCasedKeywords : allPossibilities) {
            if (
                    model.getName().startsWith(lowerCasedKeywords) ||
                            model.getName().contains(lowerCasedKeywords) ||
                            String.join("", model.getShortPinyin()).startsWith(lowerCasedKeywords) ||
                            model.getNameToPinyin().startsWith(lowerCasedKeywords)
            ) {
                return true;
            }
        }
        return false;
    }


    private List<String> shortToFull(DingTalkPickerNodeSource source, String keyword) {
        final Map<String, Set<String>> sourceMap = Maps.newHashMap();
        if (Objects.isNull(source)) {
            shortPinyinKeyedMapping.values().forEach(sourceMap::putAll);
        } else {
            if (!shortPinyinKeyedMapping.containsKey(source)) {
                return Collections.emptyList();
            }
            sourceMap.putAll(shortPinyinKeyedMapping.get(source));
        }
        // input char array
        final List<Character> chars = Chars.asList(keyword.toCharArray());
        // translate
        AtomicInteger index = new AtomicInteger(0);
        final List<TranslationHolder> translationHolders = chars.stream()
                .map(c -> {
                    final String lowerCase = c.toString().toLowerCase();
                    final TranslationHolder translationHolder = new TranslationHolder(lowerCase, index.getAndIncrement());
                    if (pinyinConverter.isChineseLetter(c)) {
                        final List<PinyinConverter.PinyinModel> pinyin = pinyinConverter.chineseToPinyin(c.toString());
                        final Set<String> translations = pinyin.stream().map(PinyinConverter.PinyinModel::getName).collect(Collectors.toCollection(Sets::newLinkedHashSet));
                        translationHolder.setTranslations(translations);
                        return translationHolder;
                    } else if (Character.isAlphabetic(c)) {
                        final Set<String> translations = sourceMap.getOrDefault(lowerCase, null);
                        translationHolder.setTranslations(translations);
                        return translationHolder;
                    } else {
                        return translationHolder;
                    }
                })
                .toList();
        final List<TranslationHolder> optimized = TranslationHolder.Optimizer.optimize(translationHolders);
        log.info("prints out translation starts \n");
        optimized.forEach(System.out::println);
        log.info("prints out translation ends \n");

        Set<String> _1st = optimized.get(0).getTranslations();

        for (int i = 1; i < optimized.size(); i++) {
            final Set<String> translations = optimized.get(i).getTranslations();
            _1st = _1st.stream()
                    .flatMap(s1 -> translations.stream().map(s2 -> s1 + s2))
                    .collect(Collectors.toSet());
        }
        _1st.add(keyword);
        log.info("expanded translations: \n");
        _1st.forEach(System.out::println);
        log.info("expanded translations: \n");
        return Lists.newArrayList(_1st);
    }


    private void buildLocalCache(List<DingTalkPickerNode> nodes) {
        if (CollUtil.isEmpty(nodes)) {
            return;
        }
        nodes.forEach(node -> {
            if (!node.isUserNode()) {
                buildLocalCache(node.getChildren());
            } else {
                final DingTalkPickerNodeSource source = node.getSource();
                final List<DingTalkPickerNode> pinyinNodes = buildPinyinNodes(node);
                modelsWithPinyin.compute(source, (k, v) -> {
                    if (Objects.isNull(v)) {
                        return Lists.newArrayList(pinyinNodes);
                    } else {
                        v.addAll(pinyinNodes);
                        return v;
                    }
                });
                buildPinyinMapping(source, pinyinNodes);
            }
        });
    }

    private List<DingTalkPickerNode> buildPinyinNodes(DingTalkPickerNode node) {
        return pinyinConverter.chineseToPinyin(node.getName())
                .stream()
                .map(pinyin -> {
                    final DingTalkPickerNode copy = DingTalkPickerMapper.INSTANCE.copy(node);
                    // Set pinyin.
                    copy.setNameToPinyin(pinyin.getName());
                    copy.setPinyin(pinyin.getPinyin());
                    copy.setShortPinyin(pinyin.getShortPinyin());
                    return copy;
                })
                .toList();
    }

    private void buildPinyinMapping(DingTalkPickerNodeSource source, List<DingTalkPickerNode> pinyinNodes) {
        pinyinNodes.forEach(pinyinNode -> {
            final List<String> pinyin = Lists.newArrayList(pinyinNode.getPinyin().iterator());
            final List<String> shortPinyin = Lists.newArrayList(pinyinNode.getShortPinyin().iterator());
            for (int i = 0; i < shortPinyin.size(); i++) {
                int finalI2 = i;
                shortPinyinKeyedMapping.compute(source, (k, v) -> {
                    Map<String, Set<String>> mapping;
                    if (Objects.nonNull(v)) {
                        mapping = v;
                    } else {
                        mapping = Maps.newHashMap();
                    }
                    if (mapping.containsKey(shortPinyin.get(finalI2))) {
                        mapping.computeIfPresent(shortPinyin.get(finalI2), (s, arr) -> {
                            arr.add(pinyin.get(finalI2));
                            return arr;
                        });
                    } else {
                        mapping.computeIfAbsent(shortPinyin.get(finalI2), s -> {
                            final Set<String> arr = Sets.newLinkedHashSet();
                            arr.add(pinyin.get(finalI2));
                            return arr;
                        });
                    }
                    return mapping;
                });

            }
        });
    }
}
