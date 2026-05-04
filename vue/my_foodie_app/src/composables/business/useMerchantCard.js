import { computed } from 'vue'
import { getMerchantPromoText } from '@/utils/business/merchant'

export const useMerchantCard = (item) => {
  const nameText = computed(() => item.value?.merchantName || item.value?.name || '未命名商家')

  const ratingText = computed(() => {
    const value = item.value?.rating
    if (value === 0) return '0.0'
    if (!value) return '0.0'
    return String(value)
  })

  const salesText = computed(() => {
    const value = item.value?.salesVolume
    return value === 0 ? 0 : (value || 0)
  })

  const distanceText = computed(() => {
    const raw = item.value?.distance
    if (raw === 0) return '0m'
    if (!raw && raw !== 0) return ''

    const num = Number(raw)
    if (!Number.isNaN(num) && Number.isFinite(num)) {
      if (num < 1) return `${Math.max(1, Math.round(num * 1000))}m`
      return `${num.toFixed(1)}km`
    }

    const str = String(raw)
    if (/km|m/i.test(str)) return str
    return `${str}km`
  })

  const etaText = computed(() => {
    const raw = item.value?.estimatedDeliveryTime ?? item.value?.deliveryTime ?? item.value?.eta
    if (!raw && raw !== 0) return ''

    const num = Number(raw)
    if (!Number.isNaN(num) && Number.isFinite(num)) return `约${num}分钟`

    const str = String(raw)
    return /分钟|min/i.test(str) ? str : `约${str}分钟`
  })

  const minDeliveryText = computed(() => {
    const raw = item.value?.minDeliveryAmount
    if (raw === 0) return '0'
    return raw ? String(raw) : '0'
  })

  const deliveryFeeText = computed(() => {
    const raw = item.value?.deliveryFee
    if (raw === 0) return '0'
    return raw ? String(raw) : '0'
  })

  const promoText = computed(() => getMerchantPromoText(item.value))

  return {
    deliveryFeeText,
    distanceText,
    etaText,
    minDeliveryText,
    nameText,
    promoText,
    ratingText,
    salesText,
  }
}
