package com.hp.dingtalk.userpicker.infrastructure.repository.source;

import cn.hutool.core.collection.CollUtil;
import com.google.common.base.Preconditions;
import com.hp.dingtalk.userpicker.domain.DingTalkPickerNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * @author hp
 */
public abstract class AbstractDingTalkPickerDatasource implements IDingTalkPickerDatasource {

    @Override
    public DingTalkPickerNode tree() {
        return buildTree();
    }

    protected abstract DingTalkPickerNode getOrgNode();

    protected abstract List<DingTalkPickerNode> findDeptByParentId(@Nullable String id);

    protected abstract List<DingTalkPickerNode> findUserByDeptId(@Nonnull String id);

    private DingTalkPickerNode buildTree() {
        final DingTalkPickerNode orgNode = getOrgNode();
        Preconditions.checkArgument(Objects.nonNull(orgNode));
        orgNode.setChildren(recursivelyBuildTree(null));
        return orgNode;
    }

    private List<DingTalkPickerNode> recursivelyBuildTree(String deptId) {
        final List<DingTalkPickerNode> departments = findDeptByParentId(deptId);
        if (CollUtil.isNotEmpty(departments)) {
            departments.forEach(department -> {
                final List<DingTalkPickerNode> subNodes = recursivelyBuildTree(department.getId());
                if (CollUtil.isNotEmpty(subNodes)) {
                    department.setChildren(subNodes);
                }
                final List<DingTalkPickerNode> users = findUserByDeptId(department.getId());
                if (CollUtil.isEmpty(users)) {
                    return;
                }
                department.setChildren(users);
            });
        }
        return departments;
    }
}
