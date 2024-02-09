package com.hp.dingtalk.userpicker.api.response;

import com.hp.common.base.model.Response;
import lombok.Data;

/**
 * @author hp
 */
@Data
public class DingTalkPickerRefreshResponse implements Response {

    private String message;
    private boolean result;

    private DingTalkPickerRefreshResponse(String message, boolean result) {
        this.message = message;
        this.result = result;
    }

    public static DingTalkPickerRefreshResponse failed(String message) {
        return new DingTalkPickerRefreshResponse(message, false);
    }

    public static DingTalkPickerRefreshResponse refreshed() {
        return new DingTalkPickerRefreshResponse("已刷新", true);
    }
}
