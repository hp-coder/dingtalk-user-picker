package com.luban.dingtalk.userpicker.feginservice;

import com.luban.common.base.annotations.MethodDesc;
import com.luban.common.base.model.Returns;
import com.luban.dingtalk.userpicker.api.DingTalkPickerApi;
import com.luban.dingtalk.userpicker.api.request.ArchitectureByParentRequest;
import com.luban.dingtalk.userpicker.api.request.ArchitectureRequest;
import com.luban.dingtalk.userpicker.api.request.KeywordQueryRequest;
import com.luban.dingtalk.userpicker.api.response.DingTalkPickerRefreshResponse;
import com.luban.dingtalk.userpicker.application.service.IDingTalkPickerCommandApplicationService;
import com.luban.dingtalk.userpicker.application.service.IDingTalkPickerQueryApplicationService;
import com.luban.dingtalk.userpicker.domain.DingTalkPickerNode;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author hp 2023/4/23
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(DingTalkPickerApi.PATH)
public class DingTalkPickerFeignService implements DingTalkPickerApi {

    private final IDingTalkPickerQueryApplicationService dingTalkPickerQueryApplicationService;
    private final IDingTalkPickerCommandApplicationService dingTalkPickerCommandApplicationService;

    @MethodDesc("人工手动刷新钉钉缓存")
    @PostMapping("refresh")
    @Override
    public Returns refresh() {
        final DingTalkPickerRefreshResponse response = dingTalkPickerCommandApplicationService.refresh();
        return Returns.success().data(response);
    }

    @MethodDesc("关键字查询")
    @PostMapping("keyword")
    @Override
    public Returns keyword(@RequestBody KeywordQueryRequest request) {
        List<DingTalkPickerNode> users = dingTalkPickerQueryApplicationService.search(request);
        return Returns.success().data(users);
    }

    @MethodDesc("全量去重用户")
    @GetMapping({"user-all","userAll"})
    @Override
    public Returns userAll() {
        final List<DingTalkPickerNode> users = dingTalkPickerQueryApplicationService.findAllUser();
        return Returns.success().data(users);
    }

    @MethodDesc("全量组织架构")
    @PostMapping({"dept-all","deptAll"})
    @Override
    public Returns deptAll(@RequestBody ArchitectureRequest request) {
        final List<DingTalkPickerNode> nodes = dingTalkPickerQueryApplicationService.findAll(request);
        return Returns.success().data(nodes);
    }

    @MethodDesc("根据节点查询子节点架构")
    @PostMapping({"deptByParentId","dept-by-parentId"})
    @Override
    public Returns deptByParentId(@RequestBody ArchitectureByParentRequest request) {
        List<DingTalkPickerNode> nodes = dingTalkPickerQueryApplicationService.findByParentId(request);
        return Returns.success().data(nodes);
    }
}
