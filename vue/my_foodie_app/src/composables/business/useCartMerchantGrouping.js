import { computed } from 'vue'

const createMerchantGroup = (merchantId, item) => ({
  id: merchantId,
  name: item.merchantName || '店铺',
  logo: item.merchantLogo || '',
  items: [],
  totalNum: 0,
  totalPrice: 0,
})

export const useCartMerchantGrouping = ({ cartStore, currentMerchantId }) => {
  const groupedCart = computed(() => {
    const groups = {}

    cartStore.list.forEach((item) => {
      const merchantId = item.merchantId
      if (!groups[merchantId]) {
        groups[merchantId] = createMerchantGroup(merchantId, item)
      }

      if (item.selected === undefined) {
        item.selected = false
      }

      groups[merchantId].items.push(item)
      groups[merchantId].totalNum += item.number
      groups[merchantId].totalPrice += item.number * item.amount
    })

    return groups
  })

  const merchantGroupList = computed(() => Object.values(groupedCart.value))
  const currentMerchant = computed(() => groupedCart.value[currentMerchantId.value] || {})

  return {
    currentMerchant,
    groupedCart,
    merchantGroupList,
  }
}
