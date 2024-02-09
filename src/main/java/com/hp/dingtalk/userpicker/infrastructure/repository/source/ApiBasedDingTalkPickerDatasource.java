package com.hp.dingtalk.userpicker.infrastructure.repository.source;

import cn.hutool.core.collection.CollUtil;
import com.aliyun.dingtalkcontact_1_0.models.GetOrgAuthInfoResponseBody;
import com.dingtalk.api.response.OapiUserListsimpleResponse;
import com.dingtalk.api.response.OapiV2DepartmentListsubResponse;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.hp.dingtalk.component.application.IDingMiniH5;
import com.hp.dingtalk.constant.Language;
import com.hp.dingtalk.service.IDingDeptHandler;
import com.hp.dingtalk.service.IDingOrganizationHandler;
import com.hp.dingtalk.service.IDingUserHandler;
import com.hp.dingtalk.service.dept.DingDeptHandler;
import com.hp.dingtalk.service.organization.DingOrganizationHandler;
import com.hp.dingtalk.service.user.DingUserHandler;
import com.hp.dingtalk.userpicker.domain.DingTalkPickerNode;
import com.hp.dingtalk.userpicker.domain.DingTalkPickerNodeSource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author hp
 */
@Slf4j
public class ApiBasedDingTalkPickerDatasource extends AbstractDingTalkPickerDatasource {

    private final IDingDeptHandler deptHandler;
    private final IDingUserHandler userHandler;
    private final IDingOrganizationHandler organizationHandler;

    public ApiBasedDingTalkPickerDatasource(IDingMiniH5 app) {
        this.deptHandler = new DingDeptHandler(app);
        this.userHandler = new DingUserHandler(app);
        this.organizationHandler = new DingOrganizationHandler(app);
    }

    @Override
    protected DingTalkPickerNode getOrgNode() {
        final GetOrgAuthInfoResponseBody authInfo = organizationHandler.getAuthInfo();
        return new DingTalkPickerNode.OrgBuilder()
                .id("1")
                .source(DingTalkPickerNodeSource.DING_TALK)
                .name(authInfo.getOrgName())
                .build();
    }

    @Override
    protected List<DingTalkPickerNode> findDeptByParentId(@Nullable String id) {
        final Long longId = Optional.ofNullable(id).map(Long::valueOf).orElse(null);
        final List<OapiV2DepartmentListsubResponse.DeptBaseResponse> deptList = deptHandler.getDeptList(longId, Language.zh_CN);
        return deptList.stream()
                .map(dept ->
                        new DingTalkPickerNode.DeptBuilder()
                                .source(DingTalkPickerNodeSource.DING_TALK)
                                .id(String.valueOf(dept.getDeptId()))
                                .name(dept.getName())
                                .build()
                )
                .collect(Collectors.toList());
    }

    @Override
    protected List<DingTalkPickerNode> findUserByDeptId(@NotNull String id) {
        final List<OapiUserListsimpleResponse.ListUserSimpleResponse> container = Lists.newArrayList();
        recursivelyFindAllUserByDeptId(Long.valueOf(id), 0L, 100L, container);

        return container.stream()
                .map(user ->
                        new DingTalkPickerNode.UserBuilder()
                                .source(DingTalkPickerNodeSource.DING_TALK)
                                .id(user.getUserid())
                                .systemUserId(user.getUserid())
                                .onTheJob()
                                .name(user.getName())
                                .build()
                )
                .collect(Collectors.toList());
    }

    private void recursivelyFindAllUserByDeptId(
            Long id,
            Long nextCursor,
            Long pageSize,
            List<OapiUserListsimpleResponse.ListUserSimpleResponse> container
    ) {
        Preconditions.checkArgument(Objects.nonNull(id));
        Preconditions.checkArgument(Objects.nonNull(container));
        final OapiUserListsimpleResponse.PageResult userPage = userHandler.findNameAndUserIdByDept(id, nextCursor, pageSize);
        final List<OapiUserListsimpleResponse.ListUserSimpleResponse> users = userPage.getList();
        if (CollUtil.isNotEmpty(users)) {
            container.addAll(users);
        }
        if (userPage.getHasMore()) {
            recursivelyFindAllUserByDeptId(id, userPage.getNextCursor(), pageSize, container);
        }
    }

    @Override
    public boolean support(DingTalkPickerNodeSource source) {
        return Objects.equals(source, DingTalkPickerNodeSource.DING_TALK);
    }
}
