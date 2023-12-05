<template>
  <div>
    <!--placeholder在单选选中时没有被隐藏-->
    <el-select
      style="width: 100%"
      :class="multiple? 'userPickerSelector multiple':'userPickerSelector single'"
      ref="userSelectorVisibleRef"
      multiple
      clearable
      v-model="archIds"
      :placeholder="(archIds !== undefined && archIds.length > 0) ? '' : mutablePlaceholder"
      @remove-tag="userSelectorRemoved"
      @clear="userSelectorCleared"
      @visible-change="userSelectorVisibleChanged"
    >
      <el-button
        slot="prefix"
        size="small"
        style="padding:5px;"
        @click="selectorOpener"
      >
        {{ multiple ? `批量添加` : `请选择` }}
      </el-button>
      <el-option
        v-for="acrh in options"
        :key="acrh.id"
        :label="acrh.name"
        :value="acrh.id"
      />
    </el-select>

    <el-dialog
      v-if="dingTalkUserSelectorOpener"
      :visible.sync="dingTalkUserSelectorOpener"
      :fullscreen="false"
      :append-to-body="true"
      :close-on-click-modal="false"
      width="40%"
      @close="close"
    >
      <div slot="title">
        <span class="title-span">{{ title }}</span>
        <span class="refresh-wrapper">
          <el-button
            class="refresh-button"
            round
            size="mini"
            icon="el-icon-refresh"
            title="强制刷新,重新加载数据"
            @click="forceRefresh"
          >
          </el-button>
        </span>
      </div>
      <el-row :gutter="10">
        <el-col :span="12" class="col-wrapper" style="padding-left: 0;padding-right: 0">
          <div class="left-selector-wrapper">
            <div class="search-box-wrapper">
              <el-input
                class="search-box"
                placeholder="请输入用户名称"
                auto-complete="on"
                autofocus
                clearable
                @input="search"
                @clear="searchInputCleared"
                v-model="searchText"
              >
                <i slot="prefix" class="el-input__icon el-icon-search"></i>
              </el-input>
            </div>
            <div class="entry-list-wrapper" v-if="!resourcePickerOpener">
              <div class="entry-list-item" :key="key" v-for="(arch,key) in architecture">
                <div v-if="arch.type==='corp' || arch.type==='org'" class="entry-list-item-wrapper"
                     @click="displayChildren(arch)"
                >
                  <div class="entry-list-item-left-wrapper">
                    <img class="entry-list-org-icon"
                         :src="arch.avatar"
                         alt="avatar"
                    >
                    <span class="entry-list-org-name">
                    <span class="entry-list-text-ellipse">
                      {{ arch.name }}
                    </span>
                  </span>
                  </div>
                  <span class="entry-list-icon">
                  <i class="el-icon-arrow-down"></i>
                </span>
                </div>
                <div class="entry-list-child-item-wrapper" v-if="arch.show"
                     @click="resourcePicker(arch)"
                >
                  <div class="entry-list-child-item-left-wrapper">
                    <span class="dash-icon"></span>
                    <span class="entry-list-child-item-name">
                      <span class="entry-list-text-ellipse">组织架构</span>
                    </span>
                  </div>
                  <i class="el-icon-arrow-right"></i>
                </div>
              </div>
            </div>
            <div class="empty-result-wrapper" v-show="emptyResultOpener">
              <div class="empty-result">
                <span>未查询到对应数据</span>
              </div>
            </div>
            <div class="resource-picker" v-if="resourcePickerOpener && !emptyResultOpener">
              <div class="resource-picker-wrapper">
                  <span class="back-to-entry-list-wrapper" @click="resourcePickerOpener = false">
                    <i class="el-icon-arrow-left"></i>
                  </span>
                <div class="resource-picker-title-wrapper">
                  <span :title="currentName" class="resource-picker-title">{{ currentName }}</span>
                </div>
              </div>
              <div class="breadcrumb-wrapper">
                <div class="dtd-breadcrumb res-picker-bread-crumb">
                    <span v-for="(bd,key) in breadcrumbs" :key="key">
                      <span class="dtd-breadcrumb-link dtd-breadcrumb-link-clickable"
                            @click="breadcrumbsClicked(bd.id, bd.source, key)"
                      >{{ bd.name }}</span>
                      <span class="dtd-breadcrumb-separator">
                        <i class="el-icon-arrow-right"></i>
                      </span>
                    </span>
                </div>
              </div>
              <div class="select-all-wrapper" v-if="multiple">
                <el-checkbox v-model="selectAll"
                             v-show="this.architecture.length>0"
                             :disabled="!multiSelectable"
                             @change="selectAllChanged"
                >
                  选择全部
                </el-checkbox>
              </div>
              <div id="resource-picker-list-wrapper" class="resource-picker-list-wrapper">
                <div id="virtual-list" class="virtual-list">
                  <div class="virtual-list-scroll"
                       style="overflow: auto; will-change: transform; height: 100%; width: 100%;"
                  >
                    <div style="position: inherit; width: 100%; min-height: 100%; height: 280px;">
                      <div class="virtual-list-item-wrapper" v-for="(arch, key) in architecture"
                           :key="key"
                           style="position: relative; top: 0px; left: 0px; width: 100%; height: 55px;"
                      >
                        <div class="virtual-list-item">
                          <div class="virtual-list-item-checkbox-wrapper">
                            <el-checkbox v-model="arch.selected"
                                         v-if="multiple? true: arch.type === 'user'"
                                         @change="selectItemChanged(arch)"
                                         :disabled="!arch.selectable || (!multiple && (selectedItems === undefined ||(selectedItems.length >0 && selectedItems.at(0).id !== arch.id))) "
                                         class="virtual-list-item-checkbox"
                            ></el-checkbox>
                          </div>
                          <div class="virtual-list-item"
                               @click="selectItemClicked(arch, true)"
                          >
                            <div class="virtual-list-item-avatar-wrapper">
                              <img v-if="arch.avatarStyle" :style="arch.avatarStyle" :src="arch.avatar"
                                   class="virtual-list-item-avatar"
                                   style="width: 36px; height: 36px; font-size: 16.2px;"
                                   alt="头像"
                              >
                              <img v-if="!arch.avatarStyle" :src="arch.avatar" class="virtual-list-item-avatar"
                                   style="width: 36px; height: 36px; font-size: 16.2px;"
                                   alt="头像"
                              >
                            </div>
                            <div class="virtual-list-item-name-wrapper">
                              <div class="virtual-list-item-name-dept-item"
                                   v-if="arch.type!=='user'"
                              >
                                <div class="virtual-list-item-dept-name"
                                     :title="arch.name"
                                >{{ arch.name }}
                                </div>
                                <span class="virtual-list-item-dept-count">({{
                                    arch.children.length
                                  }})</span>
                              </div>
                              <div class="virtual-list-item-name-user-item"
                                   v-if="arch.type==='user'"
                              >
                                <div class="virtual-list-item-user-name"
                                     :title="arch.name"
                                >{{ arch.name }}
                                </div>
                                <span class="virtual-list-item-user-title">{{
                                    arch.title
                                  }}</span>
                              </div>
                            </div>
                          </div>
                          <div class="virtual-list-item-sub" v-if="arch.type !== 'user'">
                            <el-link type="primary" :underline="false"
                                     style="font-size: 13px;"
                                     :disabled="isSubDisabled(arch)"
                                     @click="resourcePicker(arch)"
                            >下级
                            </el-link>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="12" class="col-wrapper">
          <div class="right-selector-wrapper">
            <div class="selected-user-count-wrapper">
              已选择({{ selectedItems.length }}/5000)
            </div>
            <div id="selected-scroll-wrapper" class="selected-scroll-wrapper">
              <div id="selected-virtual-wrapper" class="selected-virtual-wrapper"
                   data-spm-anchor-id="0.0.0.i5.465a3p5x3p5xZd"
              >
                <div class="selected-virtual-user-list">
                  <div v-for="(item,key) in selectedItems" :key="key"
                       class="selected-virtual-user-list-item"
                  >
                    <div class="selected-virtual-user-avatar">
                      <div style="width: 24px; height: 24px; font-size: 10.8px;">
                        <div class="dd-avatar-content" style="opacity: 1;">
                          <img :src="item.avatar"
                               class="ding-avatar-web ding-avatar-web-square"
                               style="width: 24px; height: 24px; font-size: 10.8px; border-radius: 5px"
                          >
                        </div>
                      </div>
                    </div>
                    <span class="selected-virtual-user-remove">{{ item.name }}</span>
                    <div style="margin-left: 8px; display: flex; align-items: center;"
                         @click="removeSelectedItem(item)"
                    >
                      <i class="el-icon-error" title="删除"></i>
                    </div>
                  </div>

                </div>
              </div>
            </div>
            <div class="submit-button-wrapper">
              <el-button @click="close">取消</el-button>
              <el-button @click="submit" type="primary">确认</el-button>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-dialog>
  </div>
</template>
<script>
import 'css/dingtalk-user-picker.css'
import 'css/dingtalk-resource-picker.css'
import { deptAll, deptByParentId, keyword, forceRefresh } from 'js/dingtalk-user-api'

export default {
  name: 'DingTalkUserSelector',
  props: {
    //是否多选
    multiple: { type: Boolean, default: true },
    //多选的值
    value: { type: Array, default: () => [] },
    //单选的值
    userId: { type: String, default: null },
    //select框的占位提示
    placeholder: { type: String, default: '请选择用户' },
    //弹窗标题
    title: { type: String, default: '选择部门与人员' },
    //源用户id集合
    sourceUserIds: { type: Array, default: () => [] },
    //是否在sourceUserId有数据后查询
    loadOnSource: { type: Boolean, default: false },
    //当sourceUserIds为空时,点击按钮提示
    loadOnSourceMessage: { type: String, default: '请先选择上一级人员' }
  },
  data() {
    return {
      mutablePlaceholder: this.placeholder,
      //全部用户(未去重)
      allUsers: [],
      //当前组织名称
      currentName: null,
      //搜索内容
      searchText: null,
      //是否存在内容
      searchTextLength: false,
      //防抖的一个定时
      searchTimer: null,
      //选中的用户, 这个为选人组件右侧的数据容器
      selectedItems: [],
      //注入到select中的options,给外部展示用,以及删除tag
      options: [],
      //最初的架构, 从根开始
      originalArchitecture: [],
      //组织架构数据
      architecture: [],
      //父级节点进入后, 能否多选
      multiSelectable: true,
      //组织架构选择开关
      resourcePickerOpener: false,
      //加载ui, 转圈圈
      resourcePickerLoader: false,
      //全选开关
      selectAll: false,
      //面包屑
      breadcrumbs: [],
      //是否打开选人的组件
      dingTalkUserSelectorOpener: false,
      //空结果提示
      emptyResultOpener: false,
      //离职用户名称后缀
      resignedSuffix: '[已离职]'

    }
  },
  watch: {
    //直接从选择列表重新加载数据, 服务端是缓存到内存的, 随便调
    resourcePickerOpener(newValue, oldValue) {
      if (!newValue && oldValue) {
        this.reset()
      }
    },
    sourceUserIds(val) {
      if (val && this.loadOnSource) {
        this.userSelectorRemovedBySourceUserId(val)
        this.loadArchitecture(true)
      }
    }
  },
  computed: {
    archIds: {
      get() {
        if (this.multiple) {
          return this.value
        } else {
          if (this.userId) {
            return [this.userId]
          }
          return []
        }
      },
      set(val) {
        this.notifyParent(val)
      }
    }
  },
  created() {
    this.loadArchitecture(true)
  },
  mounted() {
  },
  methods: {
    forceRefresh() {
      forceRefresh()
        .then(resp => {
          if (resp.data.result) {
            this.$notify({
              type: 'success',
              title: '提示',
              message: '数据已刷新',
              duration: 2000
            })
            this.loadArchitecture(true)
          } else {
            this.$notify({
              type: 'error',
              title: '提示',
              message: resp.data.message,
              duration: 2000
            })
          }
        })
    },
    //点击批量按钮打开dialog
    selectorOpener() {
      if (this.loadOnSource && this.sourceUserIds.length === 0) {
        this.$notify({
          type: 'warning',
          title: '提示',
          message: this.loadOnSourceMessage,
          duration: 1000
        })
        return
      }
      if (this.loadOnSource) {
        this.loadArchitecture(true)
      } else {
        this.loadSelected()
      }
      this.dingTalkUserSelectorOpener = true
    },
    //查询input被清空
    searchInputCleared() {
      this.resourcePickerOpener = false
    },
    //关键字查询(拼音首字母, 拼音, 中英文)
    search(words) {
      if (this.searchTimer) {
        clearTimeout(this.searchTimer)
      }
      if (words && words.length > 0) {
        this.searchTextLength = true
        this.searchTimer = setTimeout(() => {
          this.searchKeywords(words)
        }, 500)
      } else {
        this.loadArchitecture(false)
      }
    },
    // 搜不到离职员工
    searchKeywords(words) {
      this.resourcePickerLoader = true
      keyword(words, this.sourceUserIds)
        .then(resp => {
          this.resourcePickerOpener = true
          resp.data.forEach(i => {
            this.defaultAvatar(i)
          })
          this.architecture = resp.data
          this.resourcePickerLoader = false
          this.emptyResultOpener = !this.architecture || this.architecture.length === 0
        })
    },
    //清空数组的通用方法, 直接赋值[]不更新
    clearArray(arr) {
      if (arr && arr.length > 0) {
        arr.splice(0, arr.length)
      }
    },
    //在select框中删除所有选中的tag
    userSelectorCleared() {
      this.reset()
      //元素是对象, 直接给空数组不会更新;
      this.clearArray(this.options)
      this.clearArray(this.selectedItems)
      this.clearArray(this.archIds)
      this.$emit('clear')
    },
    //当select框中的tag删除之后,同步删除选项
    userSelectorRemoved(id) {
      let index = this.options.findIndex(a => a.id === id)
      this.options.splice(index, 1)
      let index2 = this.archIds.findIndex(a => a.id === id)
      this.archIds.splice(index2, 1)
      this.$emit('remove-tag', id)
    },
    //存在指定了数据源id的情况, 当数据源删除元素时, 要将此实例中的对应元素删除
    userSelectorRemovedBySourceUserId(val) {
      if (!val || val.length === 0) {
        return
      }
      this.options.forEach(a => {
        let index = val.findIndex(v => v === a.id)
        if (index === -1) {
          this.options.splice(this.options.findIndex(b => a === b), 1)
          this.archIds.splice(this.archIds.findIndex(c => a.id === c), 1)
        }
      })
    },
    //阻止默认的select下拉框显示
    userSelectorVisibleChanged(e) {
      this.$refs.userSelectorVisibleRef.blur()
    },
    //下级按钮是否禁用
    isSubDisabled(arch) {
      return !arch.children || arch.children.length === 0 || arch.selected
    },
    //修正头像,无头像使用默认生成的
    defaultAvatar(arch) {
      if (!arch.avatar) {
        arch.avatar = this.nameBasedAvatarGenerator(arch.name, arch.resigned)
      }
    },
    //基于名称生成头像
    nameBasedAvatarGenerator(username, resigned) {
      const canvas = document.createElement('canvas')
      canvas.width = 50
      canvas.height = 50
      canvas.style.borderRadius = '5px'
      //配置
      const ctx = canvas.getContext('2d')
      ctx.fillStyle = resigned ? 'rgba(185,185,185,0.51)' : '#3d8bf3'
      ctx.fillRect(0, 0, canvas.width, canvas.height)
      ctx.fillStyle = '#FFFFFF'
      //取后两位

      if (username.length >= 3) {
        username = username.replace(this.resignedSuffix, '').substr(-2, 2)
      }
      ctx.font = '20px Arial'
      ctx.textAlign = 'center'
      ctx.textBaseline = 'middle'
      // canvas.height/2.0 中文稍微有点偏上 , /1.9 看着正中间
      ctx.fillText(username, canvas.width / 2, canvas.height / 1.9)
      return canvas.toDataURL()
    },
    //重置数据项
    reset() {
      //重置到根
      this.searchText = null
      this.searchTextLength = false
      this.emptyResultOpener = false
      if (this.searchTimer) {
        clearTimeout(this.searchTimer)
      }
      this.architecture = this.originalArchitecture
      this.breadcrumbs = []
      this.selectAll = false
    },
    //删除选中项
    removeSelectedItem(item) {
      item.selected = false
      this.updateSelectedUsers(item)
    },
    //选中单独一项(部门/人员)
    selectItemChanged(arch) {
      this.updateSelectedUsers(arch)
    },
    //点击单独一项(部门/人员)
    selectItemClicked(arch, manuallyChange) {
      if (!arch.selectable) {
        return
      }
      // 多选时, 点部门也可以选中
      // 单选时, 点部门直接下一级,
      //        如果点的时用户节点, 且已选中的集合为空, 或者已选中的就是这个节点本身那么可能交互选中逻辑
      //        否则无效
      if (this.multiple || (arch.type === 'user' && (this.selectedItems.length === 0 || this.selectedItems.at(0).id === arch.id))) {
        if (manuallyChange) {
          arch.selected = !arch.selected
        }
        this.updateSelectedUsers(arch)
      } else {
        if (arch.type !== 'user') {
          this.resourcePicker(arch)
        }
      }
    },
    //是否全选
    selectAllChanged() {
      this.architecture.forEach(arch => {
        arch.selected = arch.selectable && this.selectAll
        this.updateSelectedUsers(arch)
      })
    },
    //更新选中的列表
    updateSelectedUsers(arch) {
      let index = this.selectedItems.findIndex(tmp => tmp.id === arch.id)
      if (arch.selected) {
        if (index === -1) {
          this.selectedItems.push(arch)
        }
      } else {
        if (index !== -1) {
          this.selectedItems.splice(index, 1)
        }
      }
    },
    //打开并加载组织架构选择的div
    resourcePicker(arch) {
      if (!this.resourcePickerOpener) {
        this.resourcePickerOpener = true
        this.breadcrumbs.push({ name: '通讯录', id: '0', source: arch.source, multiSelectable: true })
        this.currentName = arch.name
      }
      this.defaultAvatar(arch)
      arch.children.forEach(child => {
        this.defaultAvatar(child)
        let index = this.selectedItems.findIndex(a => a.id === child.id)
        if (index > -1) {
          child.selected = true
        }
      })
      // 构建面包屑
      this.breadcrumbs.push({
        name: arch.name,
        id: arch.id,
        source: arch.source,
        multiSelectable: arch.multiSelectable
      })
      this.architecture = arch.children
      this.multiSelectable = arch.multiSelectable
    },
    //点面包屑的跳转
    breadcrumbsClicked(deptId, source, index) {
      if (index === this.breadcrumbs.length - 1) {
        return
      }
      let number = this.breadcrumbs.length - index
      this.breadcrumbs.splice(index + 1, number)
      deptByParentId(deptId, source, this.sourceUserIds)
        .then(resp => {
          let arches = resp.data
          arches.forEach(child => {
            this.defaultAvatar(child)
            let index = this.selectedItems.findIndex(a => a.id === child.id)
            if (index > -1) {
              child.selected = true
            }
          })
          // 面包屑中包含节点能否使用多选的值
          this.multiSelectable = this.breadcrumbs[this.breadcrumbs.length - 1].multiSelectable
          this.architecture = arches
        })
    },
    //请求组织架构接口
    loadArchitecture(loadSelected) {
      deptAll(this.sourceUserIds)
        .then(resp => {
          this.architecture = resp.data
          //效率问题, 前端直接持有一个从根开始的结构, 做重置用
          this.originalArchitecture = resp.data
          this.recursivelyGetUsers(this.architecture, this.allUsers)
          if (loadSelected) {
            this.loadSelected()
          }
          this.emptyResultOpener = !this.architecture || this.architecture.length === 0
        })
    },
    //在dialog挂载后, 加载已经选择的人员
    loadSelected() {
      //重置组件右侧已选择
      this.clearArray(this.selectedItems)
      //重置select组件的option
      this.clearArray(this.options)
      //如果父组件已经选中过部分了, 这里直接渲染选中的上去
      if (this.archIds && this.archIds.length > 0) {
        //过滤掉不可选或离职人员的集合
        let legitArr = []
        this.archIds.forEach(userId => {
          let index = this.allUsers.findIndex(i => i.id === userId)
          if (index > -1) {
            let arch = this.allUsers[index]
            // 可选,或人没离职才可以被用于构造容器组件
            if (arch.selectable && !arch.resigned) {
              legitArr.push(userId)
              this.options.push(arch)
              this.selectedItems.push(arch)
            }
          }
        })
        // 去除源数据中不可选的值
        this.archIds = legitArr
      }
    },
    //展示子选项
    displayChildren(org) {
      org.show = !org.show
    },
    //关闭
    close() {
      this.reset()
      this.dingTalkUserSelectorOpener = false
    },
    //提交选中的部门下的所有人员
    submit() {
      //重置一下
      let submitArr = []
      this.clearArray(this.options)
      this.clearArray(this.archIds)
      //选中的选项中的所有用户
      this.recursivelyGetUsers(this.selectedItems, submitArr)
      //去重, filter那种不好使, 不想管为啥了
      submitArr.forEach(i => {
        if (this.options.findIndex(a => a.id === i.id) < 0) {
          //select的选项赋值
          this.options.push(i)
        }
        if (this.archIds.findIndex(a => a === i.id) < 0) {
          //给父组件的userId集合赋值
          this.archIds.push(i.id)
        }
      })
      this.notifyParent(this.archIds)
      //重置部分值(全选, 关闭, 选人的组件也关掉)
      this.reset()
      this.selectAll = false
      this.resourcePickerOpener = false
      this.dingTalkUserSelectorOpener = false
    },
    notifyParent(archIds) {
      if (archIds) {
        if (this.multiple) {
          this.$emit('change', archIds)
        } else {
          this.$emit('change', archIds.at(0))
        }
      } else {
        if (this.multiple) {
          this.$emit('change', [])
        } else {
          this.$emit('change', null)
        }
      }
    },
    //递归的将所有选中的用户添加到给定的一个集合中
    recursivelyGetUsers(children, arr) {
      children.forEach(item => {
        item.selected = false
        if (item.type !== 'user') {
          const children = item.children
          if (!children || children.length === 0) {
            return
          }
          this.recursivelyGetUsers(children, arr)
        } else {
          this.defaultAvatar(item)
          arr.push(item)
        }
      })
    }
  },
  beforeDestroy() {
    this.reset()
    this.clearArray(this.architecture)
  },
  destroyed() {
    this.clearArray(this.architecture)
  }
}
</script>
<style scoped>
/*按钮和tag重合问题*/
/deep/ .userPickerSelector.multiple > .el-select__tags {
  width: 90% !important;
  margin-left: 70px;
}

/deep/ .userPickerSelector.single > .el-select__tags {
  width: 90% !important;
  margin-left: 60px;
}

/*placeholder和按钮重合问题*/
/deep/ .userPickerSelector.multiple > .el-input > input::-webkit-input-placeholder {
  padding-left: 40px;
}

/deep/ .userPickerSelector.single > .el-input > input::-webkit-input-placeholder {
  padding-left: 30px;
}

/deep/ .el-dialog {
  border-radius: 12px;
}

/deep/ .el-dialog__body {
  padding: 10px 20px;
}
</style>

