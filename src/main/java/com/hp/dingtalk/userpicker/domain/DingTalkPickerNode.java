package com.hp.dingtalk.userpicker.domain;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.hp.common.base.annotations.FieldDesc;
import com.hp.dingtalk.userpicker.domain.serializer.DingTalkPickerNodeSourceSerializer;
import com.hp.dingtalk.userpicker.domain.serializer.DingTalkPickerNodeTypeSerializer;
import lombok.Data;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author hp
 */
@Data
public class DingTalkPickerNode {

    @Expose(deserialize = false, serialize = false)
    private static final String RESIGNED_SUFFIX = "[已离职]";

    @Expose(deserialize = false, serialize = false)
    private static final Gson JSON;

    static {
        JSON = new GsonBuilder()
                .disableHtmlEscaping()
                .create();
    }

    @JsonSerialize(using = DingTalkPickerNodeSourceSerializer.class)
    @FieldDesc("数据源")
    private DingTalkPickerNodeSource source;

    @FieldDesc("数据id")
    private String id;

    @FieldDesc("父级id")
    private String parentId;

    @FieldDesc("系统用户id")
    private String systemUserId;

    @FieldDesc("名称")
    private String name;

    @FieldDesc("头像")
    private String avatar;

    @FieldDesc("头像样式,最高级别,直接用在标签里")
    private String avatarStyle;

    @FieldDesc("是否展示")
    private boolean show = true;

    @JsonSerialize(using = DingTalkPickerNodeTypeSerializer.class)
    @FieldDesc("节点类型")
    private DingTalkPickerNodeType type;

    @FieldDesc("职称等")
    private String title;

    @FieldDesc("是否选中")
    private boolean selected = false;

    @FieldDesc("是否可选")
    private boolean selectable = true;

    @FieldDesc("该节点的子节点是否可多选")
    private boolean multiSelectable = true;

    @FieldDesc("是否可以被检索到")
    private boolean searchable = true;

    @FieldDesc("用户节点是否已离职")
    private boolean resigned = false;

    @FieldDesc("子节点")
    private List<DingTalkPickerNode> children;

    @JsonIgnore
    @FieldDesc("name中文转拼音")
    private String nameToPinyin;

    @JsonIgnore
    @FieldDesc("全拼")
    private Set<String> pinyin;

    @JsonIgnore
    @FieldDesc("拼音首字母")
    private Set<String> shortPinyin;

    public static DingTalkPickerNode defaultRoot(DingTalkPickerNodeSource source){
        final DingTalkPickerNode root = new DingTalkPickerNode();
        root.setSource(source);
        return root;
    }

    public DingTalkPickerNode() {
        this.children = Lists.newArrayList();
    }

    private DingTalkPickerNode(
            DingTalkPickerNodeSource source,
            String id,
            String name,
            String avatar,
            DingTalkPickerNodeType type,
            String title
    ) {
        this.source = source;
        this.id = id;
        this.name = name;
        if (StrUtil.isEmpty(avatar)) {
            this.avatar = type.getAvatar();
        } else {
            this.avatar = avatar;
        }
        this.type = type;
        this.title = title;
        this.children = Lists.newArrayList();
    }

    public boolean isTypeOf(DingTalkPickerNodeType type) {
        return Objects.equals(type, getType());
    }

    public boolean isSourceOf(DingTalkPickerNodeSource source){
        return Objects.equals(source, getSource());
    }

    public void setChildren(List<DingTalkPickerNode> children) {
        if (CollUtil.isNotEmpty(children)) {
            this.children.addAll(children);
        }
    }

    public DingTalkPickerNode clearChildren() {
        this.children = Lists.newArrayList();
        return this;
    }

    public boolean hasChildren() {
        return CollUtil.isNotEmpty(children);
    }

    @JsonIgnore
    public boolean hasUsers() {
        return hasChildren() && children.stream().anyMatch(i -> i.getType().equals(DingTalkPickerNodeType.USER));
    }

    @JsonIgnore
    public boolean hasDepartments() {
        return hasChildren() && children.stream().anyMatch(i -> i.getType().equals(DingTalkPickerNodeType.DEPT));
    }

    @JsonIgnore
    public boolean isOrgNode() {
        return Objects.equals(getType(), DingTalkPickerNodeType.ORG);
    }

    @JsonIgnore
    public boolean isDeptNode() {
        return Objects.equals(getType(), DingTalkPickerNodeType.DEPT);
    }

    @JsonIgnore
    public boolean isUserNode() {
        return Objects.equals(getType(), DingTalkPickerNodeType.USER);
    }

    public void resigned() {
        Preconditions.checkArgument(isUserNode());
        setResigned(true);
        setSelectable(false);
        setSearchable(false);
        setName(
                getName().replaceAll("[\\[已离职\\]]", StrUtil.EMPTY)
                        .replaceAll("已离职", StrUtil.EMPTY)
                        + RESIGNED_SUFFIX
        );
    }

    public void employed() {
        Preconditions.checkArgument(isUserNode());
        setResigned(false);
        setSelectable(true);
        setSearchable(true);
        if (StrUtil.isNotEmpty(getName())) {
            setName(
                    getName().replaceAll("[\\[已离职\\]]", StrUtil.EMPTY)
                            .replaceAll("已离职", StrUtil.EMPTY)
            );
        }
    }

    public static String serialize(DingTalkPickerNode root) {
        Preconditions.checkArgument(Objects.nonNull(root));
        return JSON.toJson(root);
    }

    public static DingTalkPickerNode deserialize(String jsonString) {
        Preconditions.checkArgument(StrUtil.isNotEmpty(jsonString));
        Preconditions.checkArgument(JSONUtil.isTypeJSON(jsonString));
        return JSON.fromJson(jsonString, DingTalkPickerNode.class);
    }

    public static class UserBuilder {
        private String id;
        private String parentId;
        private String systemUserId;
        private String name;
        private String avatar;
        private String title;
        private boolean selected = false;
        private boolean selectable = true;
        private boolean searchable = true;
        private boolean resigned = false;
        private String nameToPinyin;
        private Set<String> pinyin;
        private Set<String> shortPinyin;

        private DingTalkPickerNodeSource source;

        public UserBuilder id(String id) {
            this.id = id;
            return this;
        }
        public UserBuilder parentId(String parentId) {
            this.parentId = parentId;
            return this;
        }

        public UserBuilder systemUserId(String systemUserId) {
            this.systemUserId = systemUserId;
            return this;
        }

        public UserBuilder name(String name) {
            this.name = name;
            return this;
        }

        public UserBuilder avatar(String avatar) {
            this.avatar = avatar;
            return this;
        }

        public UserBuilder title(String title) {
            this.title = title;
            return this;
        }

        public UserBuilder selected(boolean selected) {
            this.selected = selected;
            return this;
        }

        public UserBuilder selectable(boolean selectable) {
            this.selectable = selectable;
            return this;
        }

        public UserBuilder searchable(boolean searchable) {
            this.searchable = searchable;
            return this;
        }

        public UserBuilder onTheJob() {
            this.resigned = false;
            return this;
        }

        public UserBuilder resigned() {
            this.resigned = true;
            return this;
        }

        public UserBuilder source(DingTalkPickerNodeSource source) {
            this.source = source;
            return this;
        }

        public UserBuilder nameToPinyin(String nameToPinyin) {
            this.nameToPinyin = nameToPinyin;
            return this;
        }

        public UserBuilder pinyin(Set<String> pinyin) {
            this.pinyin = pinyin;
            return this;
        }

        public UserBuilder shortPinyin(Set<String> shortPinyin) {
            this.shortPinyin = shortPinyin;
            return this;
        }

        public DingTalkPickerNode build() {
            final DingTalkPickerNode node = new DingTalkPickerNode(source, id, name, avatar, DingTalkPickerNodeType.USER, title);
            node.setParentId(parentId);
            node.setResigned(resigned);
            node.setNameToPinyin(nameToPinyin);
            node.setPinyin(pinyin);
            node.setShortPinyin(shortPinyin);
            node.setSystemUserId(systemUserId);
            node.setSelected(selected);
            node.setSelectable(selectable);
            node.setSearchable(searchable);
            return node;
        }
    }

    public static class DeptBuilder {
        private String id;
        private String parentId;
        private String name;
        private boolean selected = false;
        private boolean selectable = true;
        private boolean multiSelectable = true;
        private DingTalkPickerNodeSource source;

        public DeptBuilder() {
        }

        public static DingTalkPickerNode createResignedNode(DingTalkPickerNodeSource source) {
            final DingTalkPickerNode node = new DeptBuilder()
                    .source(source)
                    .id(source.getCode() + "_" + "resigned")
                    .name("已离职员工")
                    .selectable(false)
                    .multiSelectable(false)
                    .build();
            node.setAvatarStyle("-webkit-filter: grayscale(1);filter: gray;filter: grayscale(1);");
            return node;
        }

        public DeptBuilder id(String id) {
            this.id = id;
            return this;
        }

        public DeptBuilder parentId(String parentId) {
            this.parentId = parentId;
            return this;
        }

        public DeptBuilder name(String name) {
            this.name = name;
            return this;
        }

        public DeptBuilder source(DingTalkPickerNodeSource source) {
            this.source = source;
            return this;
        }

        public DeptBuilder selected(boolean selected) {
            this.selected = selected;
            return this;
        }

        public DeptBuilder selectable(boolean selectable) {
            this.selectable = selectable;
            return this;
        }

        public DeptBuilder multiSelectable(boolean multiSelectable) {
            this.multiSelectable = multiSelectable;
            return this;
        }

        public DingTalkPickerNode build() {
            final DingTalkPickerNode node = new DingTalkPickerNode(source, id, name, null, DingTalkPickerNodeType.DEPT, null);
            node.setParentId(parentId);
            node.setSelected(selected);
            node.setSelectable(selectable);
            node.setMultiSelectable(multiSelectable);
            return node;
        }
    }

    public static class OrgBuilder {
        private String id;
        private String name;
        private String avatar;
        private boolean selected = false;
        private boolean selectable = true;
        private boolean multiSelectable = true;
        private DingTalkPickerNodeSource source;

        public OrgBuilder id(String id) {
            this.id = id;
            return this;
        }

        public OrgBuilder name(String name) {
            this.name = name;
            return this;
        }

        public OrgBuilder avatar(String avatar) {
            this.avatar = avatar;
            return this;
        }

        public OrgBuilder selected(boolean selected) {
            this.selected = selected;
            return this;
        }

        public OrgBuilder selectable(boolean selectable) {
            this.selectable = selectable;
            return this;
        }

        public OrgBuilder multiSelectable(boolean multiSelectable) {
            this.multiSelectable = multiSelectable;
            return this;
        }

        public OrgBuilder source(DingTalkPickerNodeSource source) {
            this.source = source;
            return this;
        }

        public DingTalkPickerNode build() {
            final DingTalkPickerNode node = new DingTalkPickerNode(source, id, name, avatar, DingTalkPickerNodeType.ORG, null);
            node.setSelected(selected);
            node.setSelectable(selectable);
            node.setMultiSelectable(multiSelectable);
            return node;
        }
    }
}
