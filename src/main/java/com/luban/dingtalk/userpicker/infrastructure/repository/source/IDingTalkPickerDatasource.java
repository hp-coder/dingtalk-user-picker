package com.luban.dingtalk.userpicker.infrastructure.repository.source;

import com.luban.dingtalk.userpicker.domain.DingTalkPickerNode;
import com.luban.dingtalk.userpicker.domain.DingTalkPickerNodeSource;

/**
 * @author hp
 */
public interface IDingTalkPickerDatasource {

    boolean support(DingTalkPickerNodeSource source);

    DingTalkPickerNode tree();

}
