package com.luban.dingtalk.userpicker.infrastructure.search;

import com.luban.dingtalk.userpicker.domain.DingTalkPickerNode;
import com.luban.dingtalk.userpicker.domain.DingTalkPickerNodeSource;

import java.util.List;

/**
 * @author hp
 */
public interface SearchAlgorithm {

    default void load(List<DingTalkPickerNode> data) {
    }

    List<DingTalkPickerNode> search(DingTalkPickerNodeSource source, String input);
}
