package com.hp.dingtalk.userpicker.infrastructure.repository;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hp.dingtalk.userpicker.infrastructure.repository.cache.IDingTalkUserPickerCache;
import com.hp.dingtalk.userpicker.domain.DingTalkPickerNode;
import com.hp.dingtalk.userpicker.domain.DingTalkPickerNodeSource;
import com.hp.dingtalk.userpicker.domain.DingTalkPickerNodeType;
import com.hp.dingtalk.userpicker.infrastructure.repository.source.IDingTalkPickerDatasource;
import com.hp.dingtalk.userpicker.infrastructure.search.SearchAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author hp
 */
@Slf4j
@ConditionalOnBean(value = {IDingTalkPickerDatasource.class, SearchAlgorithm.class})
public class LocalCacheBasedDingTalkPickerRepositoryImpl implements IDingTalkPickerRepository {

    private final Map<DingTalkPickerNodeSource, Map<String, DingTalkPickerNode>> idMapping = Maps.newHashMap();
    private final Map<DingTalkPickerNodeSource, Map<String, DingTalkPickerNode>> nameMapping = Maps.newHashMap();
    private final List<IDingTalkUserPickerCache> caches;
    private final List<IDingTalkPickerDatasource> datasourceSet;
    private final SearchAlgorithm searchAlgorithm;


    public LocalCacheBasedDingTalkPickerRepositoryImpl(
            List<IDingTalkUserPickerCache> caches,
            List<IDingTalkPickerDatasource> datasourceSet,
            SearchAlgorithm searchAlgorithm
    ) {
        Preconditions.checkArgument(CollUtil.isNotEmpty(datasourceSet));
        if (CollUtil.isNotEmpty(caches)) {
            caches.sort(Comparator.comparing(IDingTalkUserPickerCache::priority));
        }
        this.caches = caches;
        this.datasourceSet = datasourceSet;
        this.searchAlgorithm = searchAlgorithm;
    }

    @Async
    @EventListener(ApplicationReadyEvent.class)
    @Override
    public void refresh(boolean forceRefresh) {
        final List<DingTalkPickerNode> nodes = Stream.of(DingTalkPickerNodeSource.values())
                .map(source -> {
                    Optional<DingTalkPickerNode> cachedNode = Optional.empty();
                    if (!forceRefresh){
                        cachedNode = caches.stream()
                                .filter(cache -> cache.support(source))
                                .filter(cache -> cache.exist(source))
                                .findFirst()
                                .flatMap(cache -> cache.findAll(source));
                    }
                    final DingTalkPickerNode root = cachedNode.orElseGet(() -> datasourceSet.stream()
                            .filter(datasource -> datasource.support(source))
                            .findFirst()
                            .map(IDingTalkPickerDatasource::tree)
                            .orElse(null));
                    if (Objects.isNull(root)) {
                        return null;
                    }
                    buildResignedNode(root);
                    caches.stream()
                            .filter(cache -> cache.support(source))
                            .forEach(cache -> cache.save(source, root));
                    return root;
                })
                .filter(Objects::nonNull)
                .toList();
        searchAlgorithm.load(nodes);
        loadLocalCache(nodes);
    }

    private void buildResignedNode(DingTalkPickerNode root) {
        final DingTalkPickerNodeSource source = root.getSource();
        log.info("building resigned node. source is {}", source);
        final Map<String, DingTalkPickerNode> idMapping = this.idMapping.getOrDefault(source, Maps.newHashMap());
        final Map<String, DingTalkPickerNode> userNodes = idMapping.values().stream()
                .filter(DingTalkPickerNode::isUserNode)
                .peek(DingTalkPickerNode::resigned)
                .collect(Collectors.toMap(DingTalkPickerNode::getId, Function.identity()));
        recursivelyBuildResignedNode(root, userNodes);

        final List<DingTalkPickerNode> resignedUsers = userNodes.values()
                .stream()
                .filter(DingTalkPickerNode::isResigned)
                .collect(Collectors.toList());

        if (CollUtil.isEmpty(resignedUsers)){
            return;
        }
        final DingTalkPickerNode resignedNode = DingTalkPickerNode.DeptBuilder.createResignedNode(source);
        resignedNode.setChildren(resignedUsers);
        root.setChildren(Collections.singletonList(resignedNode));
    }

    private void recursivelyBuildResignedNode(DingTalkPickerNode node, Map<String, DingTalkPickerNode> userNodes) {
        if (node.isUserNode() && userNodes.containsKey(node.getId())) {
            userNodes.get(node.getId()).employed();
        } else {
            if (node.hasChildren()) {
                node.getChildren().forEach(n -> recursivelyBuildResignedNode(n, userNodes));
            }
        }
    }

    private void loadLocalCache(List<DingTalkPickerNode> nodes) {
        nodes.forEach(node -> {
            buildMapping(idMapping, node, DingTalkPickerNode::getId);
            buildMapping(nameMapping, node, DingTalkPickerNode::getName);
            if (node.hasChildren()) {
                loadLocalCache(node.getChildren());
            }
        });
    }

    private void buildMapping(
            Map<DingTalkPickerNodeSource, Map<String, DingTalkPickerNode>> mapping,
            DingTalkPickerNode node,
            Function<DingTalkPickerNode, String> key
    ) {
        mapping.compute(
                node.getSource(),
                (k, v) -> {
                    if (Objects.isNull(v)) {
                        final Map<String, DingTalkPickerNode> map = Maps.newHashMap();
                        map.put(key.apply(node), node);
                        return map;
                    } else {
                        v.put(key.apply(node), node);
                        return v;
                    }
                }
        );
    }

    @Override
    public List<DingTalkPickerNode> findAll(@Nullable DingTalkPickerNodeSource source) {
        final List<DingTalkPickerNodeSource> container = Lists.newArrayList();
        if (source != null) {
            container.add(source);
        } else {
            container.addAll(List.of(DingTalkPickerNodeSource.values()));
        }
        return container
                .stream()
                .map(s -> {
                    final Optional<DingTalkPickerNode> cachedNode = caches.stream()
                            .filter(cache -> cache.support(s))
                            .filter(cache -> cache.exist(s))
                            .findFirst()
                            .flatMap(cache -> cache.findAll(s));
                    return cachedNode.orElseGet(() -> {
                        final DingTalkPickerNode root = datasourceSet.stream()
                                .filter(datasource -> datasource.support(s))
                                .findFirst()
                                .map(IDingTalkPickerDatasource::tree)
                                .orElse(null);
                        caches.stream()
                                .filter(cache -> cache.support(s))
                                .forEach(cache -> cache.save(s, root));
                        return root;
                    });
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public Optional<DingTalkPickerNode> findById(@Nullable DingTalkPickerNodeSource source, String id) {
        Preconditions.checkArgument(Objects.nonNull(id));
        final Collection<Map<String, DingTalkPickerNode>> values = idMapping.values();
        return values.stream()
                .filter(i -> i.containsKey(id))
                .map(i -> i.get(id))
                .findFirst();
    }

    @Override
    public Optional<DingTalkPickerNode> findByName(@Nullable DingTalkPickerNodeSource source, String name) {
        return Optional.empty();
    }

    @Override
    public List<DingTalkPickerNode> findByNameLike(@Nullable DingTalkPickerNodeSource source, String name) {
        Preconditions.checkArgument(StrUtil.isNotEmpty(name));
        return searchAlgorithm.search(source, name);
    }

    @Override
    public List<DingTalkPickerNode> findByType(@Nullable DingTalkPickerNodeSource source, DingTalkPickerNodeType type) {
        Preconditions.checkArgument(Objects.nonNull(type));
        if (Objects.isNull(source)) {
            return idMapping.values().stream()
                    .flatMap(node -> node.values().stream())
                    .filter(node -> node.isTypeOf(type))
                    .collect(Collectors.toMap(DingTalkPickerNode::getName, Function.identity(), (a, b) -> a))
                    .values()
                    .stream().toList();
        } else {
            if (!idMapping.containsKey(source)) {
                return Collections.emptyList();
            }
            return idMapping.get(source)
                    .values()
                    .stream()
                    .filter(node -> node.isTypeOf(type))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<DingTalkPickerNode> findByParentId(@Nullable DingTalkPickerNodeSource source, String parenId) {
        Preconditions.checkArgument(StrUtil.isNotEmpty(parenId));
        final String virtualNodeId = "0";
        if (Objects.equals(virtualNodeId, parenId)) {
            return findAll(source);
        }
        return findById(source, parenId)
                .map(DingTalkPickerNode::getChildren)
                .orElse(Collections.emptyList());
    }
}
