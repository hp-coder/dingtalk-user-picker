package com.luban.dingtalk.userpicker.infrastructure.event;

import com.google.gson.GsonBuilder;
import com.luban.dingtalk.constant.minih5event.DingMiniH5Event;
import com.luban.dingtalk.pojo.GsonBuilderVisitor;
import com.luban.dingtalk.pojo.callback.eventbody.DingMiniH5EventDeserializer;

/**
 * @author hp
 */
public class DingMiniH5ContactEventBodyGsonBuilderVisitor implements GsonBuilderVisitor {

    @Override
    public void customize(GsonBuilder gsonBuilder) {
        gsonBuilder.registerTypeAdapter(DingMiniH5Event.class, new DingMiniH5EventDeserializer());
    }
}
