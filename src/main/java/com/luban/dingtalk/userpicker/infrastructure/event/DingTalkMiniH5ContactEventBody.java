package com.luban.dingtalk.userpicker.infrastructure.event;

import com.google.gson.annotations.SerializedName;
import com.luban.dingtalk.constant.minih5event.DingMiniH5Event;
import com.luban.dingtalk.pojo.callback.eventbody.IDingMiniH5EventBody;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author hp
 * @see <a href="https://open.dingtalk.com/document/orgapp/address-book-events">通讯录事件</a>
 */
@Data
@Slf4j
public class DingTalkMiniH5ContactEventBody implements IDingMiniH5EventBody {

    @SerializedName(
            value = "corpId",
            alternate = {"CorpId"}
    )
    private String corpId;

    @SerializedName(
            value = "eventType",
            alternate = {"EventType"}
    )
    private DingMiniH5Event eventType;

    @SerializedName(
            value = "userId",
            alternate = {"UserId"}
    )
    private List<String> userId;

    @SerializedName(
            value = "optStaffId",
            alternate = {"OptStaffId"}
    )
    private String optStaffId;

    @SerializedName(
            value = "timeStamp",
            alternate = {"TimeStamp"}
    )
    private String timeStamp;

}
