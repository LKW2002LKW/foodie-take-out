import request from '@/utils/request'

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
