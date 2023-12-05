package com.luban.dingtalk.userpicker.application.service;

import com.luban.dingtalk.userpicker.api.request.ArchitectureByParentRequest;
import com.luban.dingtalk.userpicker.api.request.ArchitectureRequest;
import com.luban.dingtalk.userpicker.api.request.KeywordQueryRequest;
import com.luban.dingtalk.userpicker.domain.DingTalkPickerNode;

import java.util.List;

/**
 * @author hp
 */
public interface IDingTalkPickerQueryApplicationService {

    List<DingTalkPickerNode> search(KeywordQueryRequest request);

    List<DingTalkPickerNode> findAllUser();

    List<DingTalkPickerNode> findByParentId(ArchitectureByParentRequest request);

    List<DingTalkPickerNode> findAll(ArchitectureRequest request);
}
