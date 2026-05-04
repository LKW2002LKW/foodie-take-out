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
