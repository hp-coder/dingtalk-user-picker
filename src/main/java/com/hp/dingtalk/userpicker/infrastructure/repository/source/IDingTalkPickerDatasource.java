package com.hp.dingtalk.userpicker.infrastructure.repository.source;

import com.hp.dingtalk.userpicker.domain.DingTalkPickerNode;
import com.hp.dingtalk.userpicker.domain.DingTalkPickerNodeSource;

/**
 * @author hp
 */
public interface IDingTalkPickerDatasource {

    boolean support(DingTalkPickerNodeSource source);

    DingTalkPickerNode tree();

}
