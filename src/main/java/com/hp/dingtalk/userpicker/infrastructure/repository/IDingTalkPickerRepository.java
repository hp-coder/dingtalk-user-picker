package com.hp.dingtalk.userpicker.infrastructure.repository;

import com.hp.dingtalk.userpicker.domain.DingTalkPickerNode;
import com.hp.dingtalk.userpicker.domain.DingTalkPickerNodeType;
import com.hp.dingtalk.userpicker.domain.DingTalkPickerNodeSource;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * @author hp
 */
public interface IDingTalkPickerRepository {

    void refresh(boolean forceRefresh);

    List<DingTalkPickerNode> findAll(@Nullable DingTalkPickerNodeSource source);

    Optional<DingTalkPickerNode> findById(@Nullable DingTalkPickerNodeSource source, String id);

    Optional<DingTalkPickerNode> findByName(@Nullable DingTalkPickerNodeSource source, String name);

    List<DingTalkPickerNode> findByNameLike(@Nullable DingTalkPickerNodeSource source, String name);

    List<DingTalkPickerNode> findByType(@Nullable DingTalkPickerNodeSource source, DingTalkPickerNodeType type);

    List<DingTalkPickerNode> findByParentId(@Nullable DingTalkPickerNodeSource source, String parenId);

}
