package com.hp.dingtalk.userpicker.application.service;

import com.hp.dingtalk.userpicker.api.response.DingTalkPickerRefreshResponse;

/**
 * @author hp
 */
public interface IDingTalkPickerCommandApplicationService {

    DingTalkPickerRefreshResponse refresh();
}
