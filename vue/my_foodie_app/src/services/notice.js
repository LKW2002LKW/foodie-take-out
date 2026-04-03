import request from '../utils/request'

export const getNoticePage = (params) => {
  return request({
    url: '/user/notice/page',
    method: 'get',
    params,
  })
}

export const getNoticeDetail = (id) => {
  return request({
    url: `/user/notice/${id}`,
    method: 'get',
  })
}
