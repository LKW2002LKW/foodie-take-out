import request from '../utils/request'

/**
 * 分页查询公告列表
 * @param {Object} params
 * @param {number} [params.type] - 公告类型(1-系统公告 2-活动公告)
 * @param {number} [params.page] - 页码
 * @param {number} [params.pageSize] - 每页大小
 */
export const getNoticePage = (params) => {
  return request({
    url: '/user/notice/page',
    method: 'get',
    params
  })
}

/**
 * 查询公告详情
 * @param {number|string} id - 公告ID
 */
export const getNoticeDetail = (id) => {
  return request({
    url: `/user/notice/${id}`,
    method: 'get'
  })
}
