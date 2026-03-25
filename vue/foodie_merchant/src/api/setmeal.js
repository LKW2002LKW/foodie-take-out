import request from '@/utils/request'

export default {
  /**
   * 分页查询套餐
   * @param {Object} params - { page, pageSize, name, categoryId, status }
   */
  getSetmealPage(params) {
    return request({
      url: '/merchant/setmeal/page',
      method: 'get',
      params
    })
  },

  /**
   * 根据 ID 查询套餐详情
   * @param {Number} id 
   */
  getSetmealById(id) {
    return request({
      url: `/merchant/setmeal/${id}`,
      method: 'get'
    })
  },

  /**
   * 新增套餐
   * @param {Object} data 
   */
  addSetmeal(data) {
    return request({
      url: '/merchant/setmeal',
      method: 'post',
      data
    })
  },

  /**
   * 修改套餐
   * @param {Object} data 
   */
  updateSetmeal(data) {
    return request({
      url: '/merchant/setmeal',
      method: 'put',
      data
    })
  },

  /**
   * 删除单个套餐
   * @param {Number} id 
   */
  deleteSetmeal(id) {
    return request({
      url: `/merchant/setmeal/${id}`,
      method: 'delete'
    })
  },

  /**
   * 批量删除套餐
   * @param {String} ids - 逗号分隔的ID字符串
   */
  deleteSetmealBatch(ids) {
    return request({
      url: '/merchant/setmeal/batch',
      method: 'delete',
      params: { ids }
    })
  },

  /**
   * 修改套餐状态信息 (起售/停售)
   * @param {Number} id 
   * @param {Number} status 
   */
  updateSetmealStatus(id, status) {
    return request({
      url: `/merchant/setmeal/status/${id}/${status}`,
      method: 'post'
    })
  },

  /**
   * 根据分类查询套餐
   * @param {Number} categoryId 
   */
  getSetmealList(categoryId) {
    return request({
      url: `/merchant/setmeal/list/${categoryId}`,
      method: 'get'
    })
  }

}
