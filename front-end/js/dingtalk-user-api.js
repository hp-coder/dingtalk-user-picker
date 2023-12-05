import request from "@/utils/request";

export function forceRefresh(){
  return request({
    url: `/dingtalk/picker/force-refresh`,
    method: "post"
  });
}

export function keyword(keyword, sourceUserIds) {
  return request({
    url: `/dingtalk/picker/keyword`,
    method: "post",
    data: {
      keyword:keyword,
      sourceUserIds: sourceUserIds
    }
  });
}

export function deptAll(sourceUserIds) {
  return request({
    url: `/dingtalk/picker/deptAll`,
    method: "post",
    data: {
      sourceUserIds: sourceUserIds
    }
  });
}

export function userAll() {
  return request({
    url: `/dingtalk/picker/userAll`,
    method: 'get',
  })
}

export function deptByParentId(deptId, source, sourceUserIds) {
  return request({
    url: `/dingtalk/picker/deptByParentId`,
    method: "post",
    data: {
      deptId: deptId,
      source: source,
      sourceUserIds: sourceUserIds
    }
  });
}
