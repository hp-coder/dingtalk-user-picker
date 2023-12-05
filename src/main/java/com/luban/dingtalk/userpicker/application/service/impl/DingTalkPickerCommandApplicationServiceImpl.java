package com.luban.dingtalk.userpicker.application.service.impl;

import com.luban.dingtalk.userpicker.api.response.DingTalkPickerRefreshResponse;
import com.luban.dingtalk.userpicker.application.service.IDingTalkPickerCommandApplicationService;
import com.luban.dingtalk.userpicker.infrastructure.repository.IDingTalkPickerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author hp
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DingTalkPickerCommandApplicationServiceImpl implements IDingTalkPickerCommandApplicationService {

    private final IDingTalkPickerRepository dingTalkPickerRepository;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String REFRESH_LOCK = "dingtalk:user-picker:refresh-lock";

    @Override
    public DingTalkPickerRefreshResponse refresh() {
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(REFRESH_LOCK))) {
            final Long expire = stringRedisTemplate.getExpire(REFRESH_LOCK, TimeUnit.SECONDS);
            return DingTalkPickerRefreshResponse.failed(String.format("%s秒后可再次刷新", expire));
        }
        dingTalkPickerRepository.refresh();
        stringRedisTemplate.opsForValue().set(REFRESH_LOCK, "LOCK", 1, TimeUnit.HOURS);
        return DingTalkPickerRefreshResponse.refreshed();
    }
}
