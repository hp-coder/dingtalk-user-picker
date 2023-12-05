package com.luban.dingtalk.userpicker.infrastructure.repository.cache;

import com.luban.common.base.annotations.FieldDesc;
import com.luban.dingtalk.userpicker.domain.DingTalkPickerNode;
import com.luban.dingtalk.userpicker.domain.DingTalkPickerNodeSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Optional;

/**
 * @author hp
 */
@Slf4j
@RequiredArgsConstructor
public class RedisBasedDingTalkOrgPickerCache implements IDingTalkUserPickerCache {

    @FieldDesc("缓存key前缀")
    private static final String PREFIX = "dingtalk:";
    @FieldDesc("实际缓存key")
    private static final String CACHE_KEY_FORMAT = PREFIX + "user-picker:%s";

    private final StringRedisTemplate redisTemplate;

    @Override
    public int priority() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public boolean save(DingTalkPickerNodeSource source, DingTalkPickerNode root) {
        try {
            writeToRedis(source, root);
            return true;
        } catch (Exception e) {
            log.error("Write DingTalk organization architecture to Redis using the key {} failed", getCacheKey(source), e);
        }
        return false;
    }

    @Override
    public Optional<DingTalkPickerNode> findAll(DingTalkPickerNodeSource source) {
        if (exist(source)) {
            return Optional.ofNullable(readFromRedis(source));
        }
        return Optional.empty();
    }

    @Override
    public boolean exist(DingTalkPickerNodeSource source) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(getCacheKey(source)));
    }

    private String getCacheKey(DingTalkPickerNodeSource source) {
        return String.format(CACHE_KEY_FORMAT, source.getCode());
    }

    private void writeToRedis(DingTalkPickerNodeSource source, DingTalkPickerNode root) {
        redisTemplate.opsForValue().set(getCacheKey(source), DingTalkPickerNode.serialize(root));
    }

    private DingTalkPickerNode readFromRedis(DingTalkPickerNodeSource source) {
        final String jsonString = redisTemplate.opsForValue().get(getCacheKey(source));
        return DingTalkPickerNode.deserialize(jsonString);
    }
}
