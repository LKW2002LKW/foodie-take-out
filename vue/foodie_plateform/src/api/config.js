import request from '@/utils/request'

// 系统配置原始请求实现，供配置模块统一导出复用。
export default {
  getConfigList() {
    return request.get('/config/list')
  },
  updateConfig(data) {
    return request.put('/config', data)
  },
  updateConfigBatch(data) {
    return request.put('/config/batch', data)
  },
}
