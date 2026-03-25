import request from '@/utils/request'

export default {
  /**
   * 结算记录分页查询
   * @param {Object} params { page, pageSize, settlementCycle, status }
   */
  getSettlementPage(params) {
    return request({
      url: '/merchant/finance/settlement/page',
      method: 'get',
      params
    })
  },

  /**
   * 收入明细分页查询
   * @param {Object} params { page, pageSize, beginDate, endDate, orderNumber }
   */
  getIncomePage(params) {
    return request({
      url: '/merchant/finance/income/page',
      method: 'get',
      params
    })
  }
}
