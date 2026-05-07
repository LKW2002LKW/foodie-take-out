import request from '@/utils/request'

// 财务中心原始请求实现，覆盖结算、抽成配置和统计概览查询。
export default {
  getSettlementPage(params) {
    return request.get('/finance/settlement/page', { params })
  },
  getCommissionPage(params) {
    return request.get('/finance/commission/page', { params })
  },
  getReport() {
    return request.get('/finance/report')
  },
  createCommission(data) {
    return request.post('/finance/commission', data)
  },
}
