// 按约定字段顺序提取商家活动文案，供列表卡片和店铺头部复用。
const merchantPromoTextKeys = [
  'promotionDesc',
  'promotion',
  'activityDesc',
  'activity',
  'discountDesc',
  'discount',
  'remark',
]

export const getMerchantPromoText = (merchantInfo) => {
  for (const key of merchantPromoTextKeys) {
    const value = merchantInfo?.[key]
    if (typeof value === 'string' && value.trim()) {
      return value.trim()
    }
  }
  return ''
}
