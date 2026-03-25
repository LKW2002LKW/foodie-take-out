import request from '@/utils/request'

/**
 * 分类管理 API
 */
export default {
  /**
   * 新增分类
   * @param {Object} data - { type, name, sort }
   */
  addCategory(data) {
    return request({
      url: '/merchant/category',
      method: 'post',
      data
    })
  },

  /**
   * 修改分类
   * @param {Object} data - { id, type, name, sort }
   */
  updateCategory(data) {
    return request({
      url: '/merchant/category',
      method: 'put',
      data
    })
  },

  /**
   * 删除分类
   * @param {Number|String} id 
   */
  deleteCategory(id) {
    return request({
      url: `/merchant/category/${id}`,
      method: 'delete'
    })
  },

  /**
   * 根据 ID 查询分类
   * @param {Number|String} id 
   */
  getCategoryById(id) {
    return request({
      url: `/merchant/category/${id}`,
      method: 'get'
    })
  },

  /**
   * 分页查询分类
   * @param {Object} params - { page, pageSize, name, type }
   */
  getCategoryPage(params) {
    return request({
      url: '/merchant/category/page',
      method: 'get',
      params
    })
  },

  /**
   * 根据类型查询分类列表 (不分页，用于下拉选择等)
   * @param {Number} type - 1:菜品分类, 2:套餐分类
   */
  getCategoryList(type) {
    return request({
      url: '/merchant/category/list',
      method: 'get',
      params: { type }
    })
  },

  /**
   * 启用/禁用分类
   * @param {Number|String} id 
   * @param {Number} status - 0:禁用, 1:启用
   */
  updateCategoryStatus(id, status) {
    return request({
      url: `/merchant/category/status/${id}/${status}`,
      method: 'put'
    })
  }
}
