package com.luban.dingtalk.userpicker.infrastructure.config;

import com.google.common.collect.Lists;
import com.luban.dingtalk.component.application.IDingMiniH5;
import com.luban.dingtalk.userpicker.infrastructure.pinyin.DefaultPinyinConverter;
import com.luban.dingtalk.userpicker.infrastructure.pinyin.PinyinConverter;
import com.luban.dingtalk.userpicker.infrastructure.repository.IDingTalkPickerRepository;
import com.luban.dingtalk.userpicker.infrastructure.repository.LocalCacheBasedDingTalkPickerRepositoryImpl;
import com.luban.dingtalk.userpicker.infrastructure.repository.cache.IDingTalkUserPickerCache;
import com.luban.dingtalk.userpicker.infrastructure.repository.cache.LocalFileBasedDingTalkPickerCache;
import com.luban.dingtalk.userpicker.infrastructure.repository.cache.RedisBasedDingTalkOrgPickerCache;
import com.luban.dingtalk.userpicker.infrastructure.repository.source.ApiBasedDingTalkPickerDatasource;
import com.luban.dingtalk.userpicker.infrastructure.repository.source.IDingTalkPickerDatasource;
import com.luban.dingtalk.userpicker.infrastructure.search.LocalCacheBasedSearchAlgorithm;
import com.luban.dingtalk.userpicker.infrastructure.search.SearchAlgorithm;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

/**
 * @author hp
 */
@Configuration
public class DingTalkPickerConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PinyinConverter pinyinConverter() {
        return new DefaultPinyinConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    public SearchAlgorithm searchAlgorithm(PinyinConverter pinyinConverter) {
        return new LocalCacheBasedSearchAlgorithm(pinyinConverter);
    }

    @Bean
    @ConditionalOnBean(IDingMiniH5.class)
    public IDingTalkPickerDatasource dingTalkPickerDatasource(IDingMiniH5 app) {
        return new ApiBasedDingTalkPickerDatasource(app);
    }

    @Bean
    @ConditionalOnMissingBean
    public IDingTalkPickerRepository dingTalkPickerRepository(
            List<IDingTalkUserPickerCache> caches,
            List<IDingTalkPickerDatasource> datasources,
            SearchAlgorithm searchAlgorithm
    ) {
        return new LocalCacheBasedDingTalkPickerRepositoryImpl(caches, datasources, searchAlgorithm);
    }

    @Bean
    @ConditionalOnMissingBean
    public List<IDingTalkUserPickerCache> caches(StringRedisTemplate stringRedisTemplate) {
        return Lists.newArrayList(new RedisBasedDingTalkOrgPickerCache(stringRedisTemplate),
                new LocalFileBasedDingTalkPickerCache());
    }

}
