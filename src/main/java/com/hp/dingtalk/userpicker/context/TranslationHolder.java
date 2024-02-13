package com.hp.dingtalk.userpicker.context;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author hp
 */
@Getter
public class TranslationHolder {

    private String originalInput;

    @Setter
    private Set<String> translations;

    private Integer index;

    public TranslationHolder(String originalInput, Integer index) {
        Preconditions.checkArgument(StrUtil.isNotEmpty(originalInput), "input can't be empty");
        Preconditions.checkArgument(Objects.nonNull(index) && index > -1, "index can't be null and has to be greater than -1");
        this.originalInput = originalInput;
        this.index = index;
    }

    @Override
    public String toString() {
        return "TranslationHolder{" + "originalInput='" + originalInput + '\'' +
                ", translations=" + String.join("||", CollUtil.emptyIfNull(translations)) +
                ", index=" + index +
                '}';
    }

    public static class Optimizer {
        public static List<TranslationHolder> optimize(List<TranslationHolder> translationHolders) {
            if (CollUtil.isEmpty(translationHolders)) {
                return Collections.emptyList();
            }
            final List<TranslationHolder> optimized = Lists.newArrayList(translationHolders.get(0));
            StringBuilder tempAppender = new StringBuilder(optimized.getLast().getOriginalInput());
            for (int i = 1; i < translationHolders.size(); i++) {
                final TranslationHolder currHolder = translationHolders.get(i);
                final String originalInput = currHolder.getOriginalInput();
                final TranslationHolder last = optimized.getLast();
                final Set<String> translations = last.getTranslations();
                final String string = tempAppender.append(originalInput).toString();
                final boolean optimizable = optimizable(translations, string);
                if (!optimizable) {
                    optimized.add(currHolder);
                    tempAppender = new StringBuilder(currHolder.getOriginalInput());
                }
            }
            return optimized;
        }

        private static final List<String> specialLetters = Lists.newArrayList("u", "v", "i");

        public static boolean neverAppearAtTheBeginning(char c) {
            return specialLetters.contains(String.valueOf(c));
        }

        public static boolean optimizable(Set<String> translations, String target) {
            if (CollUtil.isEmpty(translations)) {
                return false;
            }
            AtomicBoolean flag = new AtomicBoolean(false);
            for (String translation : translations) {
                if (flag.get()) {
                    break;
                }
                flag.set(StrUtil.startWithIgnoreCase(translation, target));
            }
            return flag.get();
        }
    }
}
