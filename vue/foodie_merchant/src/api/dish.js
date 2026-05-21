import request from '@/utils/request'

export default {
  /**
   * 分页查询菜品列表
   * @param {Object} params - { page, pageSize, name, categoryId, status }
   */
  getDishPage(params) {
    return request({
      url: '/merchant/dish/page',
      method: 'get',
      params
    })
  },

  /**
   * 获取菜品详情
   * @param {Number} id 
   */
  getDishById(id) {
    return request({
      url: `/merchant/dish/${id}`,
      method: 'get'
    })
  },

  /**
   * 新增菜品
   * @param {Object} data 
   */
  addDish(data) {
    return request({
      url: '/merchant/dish',
      method: 'post',
      data
    })
  },

  /**
   * 修改菜品
   * @param {Object} data 
   */
  updateDish(data) {
    return request({
      url: '/merchant/dish',
      method: 'put',
      data
    })
  },

  /**
   * 删除单个菜品
   * @param {Number} id 
   */
  deleteDish(id) {
    return request({
      url: `/merchant/dish/${id}`,
      method: 'delete'
    })
  },

  /**
   * 批量删除菜品
   * @param {String} ids - 逗号分隔的ID字符串
   */
  deleteDishBatch(ids) {
    return request({
      url: '/merchant/dish/batch',
      method: 'delete',
      params: { ids }
    })
  },

  /**
   * 修改菜品状态 (起售/停售)
   * @param {Number} id 
   * @param {Number} status 
   */
  updateDishStatus(id, status) {
    return request({
      url: `/merchant/dish/status/${id}/${status}`,
      method: 'put'
    })
  },

  /**
   * 获取所有口味选项
   */
  getFlavors() {
    return request({
      url: '/merchant/dish/flavors',
      method: 'get'
    })
  }
}
