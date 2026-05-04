import request from '@/utils/request'

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
