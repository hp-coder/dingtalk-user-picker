package com.hp.dingtalk.userpicker.application.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.hp.dingtalk.userpicker.application.service.IDingTalkPickerQueryApplicationService;
import com.hp.common.base.annotations.MethodDesc;
import com.hp.dingtalk.userpicker.api.request.ArchitectureByParentRequest;
import com.hp.dingtalk.userpicker.api.request.ArchitectureRequest;
import com.hp.dingtalk.userpicker.api.request.KeywordQueryRequest;
import com.hp.dingtalk.userpicker.domain.DingTalkPickerNode;
import com.hp.dingtalk.userpicker.domain.DingTalkPickerNodeType;
import com.hp.dingtalk.userpicker.infrastructure.repository.IDingTalkPickerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author hp 2023/4/28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DingTalkPickerQueryApplicationServiceImpl implements IDingTalkPickerQueryApplicationService {

    private final IDingTalkPickerRepository dingTalkPickerRepository;

    @Override
    public List<DingTalkPickerNode> search(KeywordQueryRequest request) {
        final List<DingTalkPickerNode> nodes = dingTalkPickerRepository.findByNameLike(request.getSource(), request.getKeyword());
        return filterNodes(nodes, request.getSourceUserIds());
    }

    @Override
    public List<DingTalkPickerNode> findAllUser() {
        return dingTalkPickerRepository.findByType(null, DingTalkPickerNodeType.USER);
    }

    @Override
    public List<DingTalkPickerNode> findByParentId(ArchitectureByParentRequest request) {
        final List<DingTalkPickerNode> nodes = dingTalkPickerRepository.findByParentId(request.getSource(), request.getDeptId());
        return filterNodes(nodes, request.getSourceUserIds());
    }

    @Override
    public List<DingTalkPickerNode> findAll(ArchitectureRequest request) {
        final List<DingTalkPickerNode> nodes = dingTalkPickerRepository.findAll(request.getSource());
        return filterNodes(nodes, request.getSourceUserIds());
    }

    private List<DingTalkPickerNode> filterNodes(List<DingTalkPickerNode> nodes, List<String> sourceUserIds){
        if (CollUtil.isEmpty(sourceUserIds)){
            return nodes;
        }
        final DingTalkPickerNode holder = new DingTalkPickerNode();
        recursivelyFilterUsers(holder, nodes, sourceUserIds);
        return holder.getChildren();
    }

    @MethodDesc("根据提交的指定源Id集合,从全量中筛选对应节点,并保留原始结构层级")
    private void recursivelyFilterUsers(DingTalkPickerNode holder, List<DingTalkPickerNode> nodes, List<String> sourceUserIds) {
        if (CollUtil.isEmpty(sourceUserIds)) {
            return;
        }
        List<DingTalkPickerNode> tmp = Lists.newArrayList();
        for (DingTalkPickerNode node : nodes) {
            final boolean hasChildren = node.hasChildren();
            final List<DingTalkPickerNode> children = node.getChildren();
            //节点为用户且在指定的数据源中再添加,其他情况清空下级
            if (node.isUserNode() && CollUtil.isNotEmpty(sourceUserIds)) {
                if (sourceUserIds.contains(node.getId())) {
                    tmp.add(node);
                }
            } else {
                //清空下级
                node.clearChildren();
            }
            //原来是否存在下级
            if (hasChildren) {
                //递归
                recursivelyFilterUsers(node, children, sourceUserIds);
                //已本节点清空子节点后作为父节点递归后如果添加了子节点则保留, 否则丢弃
                if (node.hasChildren()) {
                    tmp.add(node);
                }
            }
        }
        //子节点处理完后添加到父节点上, 可能为空集合
        Optional.ofNullable(holder).ifPresent(p -> p.clearChildren().setChildren(tmp));
    }
}
