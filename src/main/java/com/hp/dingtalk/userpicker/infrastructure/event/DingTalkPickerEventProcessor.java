package com.hp.dingtalk.userpicker.infrastructure.event;

import com.hp.dingtalk.userpicker.infrastructure.repository.IDingTalkPickerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author hp
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DingTalkPickerEventProcessor {

    private final IDingTalkPickerRepository dingTalkPickerRepository;

    @Async
    @EventListener
    public void handleDingTalkContactChanged(DingTalkContactEvents.contactUpdatedEvent event) {
        dingTalkPickerRepository.refresh(true);
    }
}
