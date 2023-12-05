package com.luban.dingtalk.userpicker.infrastructure.repository.cache;

import com.luban.dingtalk.userpicker.domain.DingTalkPickerNode;
import com.luban.dingtalk.userpicker.domain.DingTalkPickerNodeSource;

import java.util.Objects;
import java.util.Optional;

/**
 * @author hp
 */
public interface IDingTalkUserPickerCache {
    int priority();

    boolean exist(DingTalkPickerNodeSource source);

    boolean save(DingTalkPickerNodeSource source, DingTalkPickerNode root);

    Optional<DingTalkPickerNode> findAll(DingTalkPickerNodeSource source);

    default boolean support(DingTalkPickerNodeSource source) {
        return Objects.equals(DingTalkPickerNodeSource.DING_TALK, source);
    }
}
