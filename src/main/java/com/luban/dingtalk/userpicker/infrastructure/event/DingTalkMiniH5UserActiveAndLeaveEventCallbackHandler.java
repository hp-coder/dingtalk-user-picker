package com.luban.dingtalk.userpicker.infrastructure.event;

import cn.hutool.core.collection.CollUtil;
import com.luban.dingtalk.component.configuration.DingMiniH5EventCallbackConfig;
import com.luban.dingtalk.constant.minih5event.DingMiniH5Event;
import com.luban.dingtalk.service.callback.minih5.AbstractDingMiniH5EventCallbackHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @author hp
 */
@Slf4j
@Component
public class DingTalkMiniH5UserActiveAndLeaveEventCallbackHandler extends
        AbstractDingMiniH5EventCallbackHandler<DingTalkMiniH5ContactEventBody> {
    private final DingMiniH5EventCallbackConfig eventCallbackConfig;
    private final ApplicationEventPublisher eventPublisher;

    public DingTalkMiniH5UserActiveAndLeaveEventCallbackHandler(
            DingMiniH5EventCallbackConfig eventCallbackConfig,
            ApplicationEventPublisher eventPublisher
    ) {
        super(new DingMiniH5ContactEventBodyGsonBuilderVisitor());
        this.eventCallbackConfig = eventCallbackConfig;
        this.eventPublisher = eventPublisher;
    }

    @Override
    protected boolean doSupport(DingTalkMiniH5ContactEventBody eventBody) {
        final DingMiniH5Event eventType = eventBody.getEventType();
        final boolean supportEvent = Objects.equals(eventType, DingMiniH5Event.ContactEvent.USER_ACTIVE_ORG) ||
                Objects.equals(eventType, DingMiniH5Event.ContactEvent.USER_LEAVE_ORG);
        return supportEvent && Objects.equals(eventBody.getCorpId(), eventCallbackConfig.getApp().getCorpId());
    }

    @Override
    protected boolean doProcess(DingTalkMiniH5ContactEventBody eventBody) {
        final List<String> userIds = eventBody.getUserId();
        if (CollUtil.isEmpty(userIds)) {
            log.error("{}:用户为空", eventBody.getEventType().getName());
            return false;
        }
        eventPublisher.publishEvent(new DingTalkContactEvents.contactUpdatedEvent());
        return true;
    }

}
