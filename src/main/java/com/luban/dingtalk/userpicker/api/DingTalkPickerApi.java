package com.luban.dingtalk.userpicker.api;

import com.luban.common.base.model.Returns;
import com.luban.dingtalk.userpicker.api.request.ArchitectureByParentRequest;
import com.luban.dingtalk.userpicker.api.request.ArchitectureRequest;
import com.luban.dingtalk.userpicker.api.request.KeywordQueryRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author hp
 */
public interface DingTalkPickerApi {

    String PATH = "dingtalk/picker";

    @PostMapping("refresh")
    Returns refresh();

    @PostMapping("keyword")
    Returns keyword(@RequestBody KeywordQueryRequest request);

    @GetMapping({"user-all","userAll"})
    Returns userAll();

    @PostMapping({"dept-all","deptAll"})
    Returns deptAll(@RequestBody ArchitectureRequest request);

    @PostMapping({"deptByParentId","dept-by-parentId"})
    Returns deptByParentId(@RequestBody ArchitectureByParentRequest request);

}
