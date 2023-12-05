package com.luban.dingtalk.userpicker.infrastructure.repository;

import com.luban.dingtalk.userpicker.domain.DingTalkPickerNode;
import com.luban.dingtalk.userpicker.domain.DingTalkPickerNodeSource;
import com.luban.dingtalk.userpicker.domain.DingTalkPickerNodeType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * @author hp
 */
public interface IDingTalkPickerRepository {

    void refresh();

    List<DingTalkPickerNode> findAll(@Nullable DingTalkPickerNodeSource source);

    Optional<DingTalkPickerNode> findById(@Nullable DingTalkPickerNodeSource source, String id);

    Optional<DingTalkPickerNode> findByName(@Nullable DingTalkPickerNodeSource source, String name);

    List<DingTalkPickerNode> findByNameLike(@Nullable DingTalkPickerNodeSource source, String name);

    List<DingTalkPickerNode> findByType(@Nullable DingTalkPickerNodeSource source, DingTalkPickerNodeType type);

    List<DingTalkPickerNode> findByParentId(@Nullable DingTalkPickerNodeSource source, String parenId);

}
