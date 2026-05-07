import { computed } from 'vue'
import { getMerchantPromoText } from '@/utils/business/merchant'

// 商家头部展示模型，负责封面、配送信息和活动文案等展示数据计算。
export const useMerchantHeader = (merchantInfo) => {
  const bgUrl = computed(() => merchantInfo.value?.image || merchantInfo.value?.logo || '')

  const headerStyle = computed(() => {
    if (!bgUrl.value) {
      return {
        backgroundImage: 'linear-gradient(135deg, rgba(255, 208, 0, 0.92), rgba(245, 194, 0, 0.78), rgba(44, 33, 18, 0.9))',
        backgroundSize: 'cover',
        backgroundPosition: 'center',
      }
    }

    return {
      backgroundImage: `linear-gradient(135deg, rgba(255, 208, 0, 0.35), rgba(245, 194, 0, 0.15), rgba(0, 0, 0, 0.58)), url(${bgUrl.value})`,
      backgroundSize: 'cover',
      backgroundPosition: 'center',
    }
  })

  const ratingText = computed(() => {
    const value = merchantInfo.value?.rating
    if (value === 0) return '0.0'
    return value ? String(value) : '0.0'
  })

  const salesText = computed(() => {
    const value = merchantInfo.value?.salesVolume
    if (value === 0) return '0'
    return value ? String(value) : ''
  })

  const etaText = computed(() => {
    const raw = merchantInfo.value?.estimatedDeliveryTime ?? merchantInfo.value?.deliveryTime
    if (!raw && raw !== 0) return ''

    const num = Number(raw)
    if (!Number.isNaN(num) && Number.isFinite(num)) {
      return `约${num}分钟`
    }

    const str = String(raw)
    return /分钟|min/i.test(str) ? str : `约${str}分钟`
  })

  const feeText = computed(() => {
    const min = merchantInfo.value?.minDeliveryAmount
    const fee = merchantInfo.value?.deliveryFee
    if (min == null && fee == null) return ''

    const minText = min == null ? '' : `起送￥${min}`
    const feeValue = fee == null ? '' : `配送￥${fee}`
    return [minText, feeValue].filter(Boolean).join(' · ')
  })

  const noticeText = computed(() => merchantInfo.value?.description || '欢迎光临！')
  const promoText = computed(() => getMerchantPromoText(merchantInfo.value))

  return {
    etaText,
    feeText,
    headerStyle,
    noticeText,
    promoText,
    ratingText,
    salesText,
  }
}
