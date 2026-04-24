<template>
  <div class="mt-merchant-header" :style="headerStyle">
    <div class="mt-header-content">
      <div class="mt-header-top">
        <van-image
          :src="merchantInfo?.logo || merchantInfo?.image"
          width="6.4rem"
          height="6.4rem"
          radius="1.2rem"
          class="mt-logo-img"
        />
        <div class="mt-header-text">
          <h1 class="mt-shop-name">{{ merchantInfo?.name || merchantInfo?.merchantName }}</h1>
          <div class="mt-shop-meta">
            <span class="mt-rating">
              <van-icon name="star" class="mt-star" />
              {{ ratingText }}
            </span>
            <span v-if="salesText" class="mt-sales">月售{{ salesText }}</span>
            <span v-if="etaText" class="mt-delivery-time">{{ etaText }}</span>
          </div>
          <div v-if="feeText" class="mt-shop-fee">{{ feeText }}</div>
        </div>
      </div>
      <div class="mt-shop-notice van-ellipsis">公告：{{ noticeText }}</div>
      <div class="mt-shop-tags">
        <van-tag plain class="mt-tag mt-tag--yellow">品质好店</van-tag>
        <van-tag plain class="mt-tag mt-tag--yellow">满减优惠</van-tag>
        <van-tag v-if="promoText" plain class="mt-tag mt-tag--outline">{{ promoText }}</van-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ merchantInfo: Object })

const bgUrl = computed(() => props.merchantInfo?.image || props.merchantInfo?.logo || '')
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
  const value = props.merchantInfo?.rating
  if (value === 0) return '0.0'
  return value ? String(value) : '0.0'
})

const salesText = computed(() => {
  const value = props.merchantInfo?.salesVolume
  if (value === 0) return '0'
  return value ? String(value) : ''
})

const etaText = computed(() => {
  const raw = props.merchantInfo?.estimatedDeliveryTime ?? props.merchantInfo?.deliveryTime
  if (!raw && raw !== 0) return ''
  const num = Number(raw)
  if (!Number.isNaN(num) && Number.isFinite(num)) return `约${num}分钟`
  const str = String(raw)
  return /分钟|min/i.test(str) ? str : `约${str}分钟`
})

const feeText = computed(() => {
  const min = props.merchantInfo?.minDeliveryAmount
  const fee = props.merchantInfo?.deliveryFee
  if (min == null && fee == null) return ''
  const minText = min == null ? '' : `起送￥${min}`
  const feeValue = fee == null ? '' : `配送￥${fee}`
  return [minText, feeValue].filter(Boolean).join(' · ')
})

const noticeText = computed(() => props.merchantInfo?.description || '欢迎光临！')

const promoText = computed(() => {
  const candidateKeys = ['promotionDesc', 'promotion', 'activityDesc', 'activity', 'discountDesc', 'discount']
  for (const key of candidateKeys) {
    const value = props.merchantInfo?.[key]
    if (typeof value === 'string' && value.trim()) return value.trim()
  }
  return ''
})
</script>

<style scoped>
.mt-merchant-header {
  height: 16rem;
  padding: 6rem 1.6rem 1.6rem;
  box-sizing: border-box;
  color: var(--mt-card-bg);
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  position: relative;
}

.mt-merchant-header::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.06) 0%, rgba(255, 208, 0, 0.08) 100%);
  pointer-events: none;
}

.mt-header-top {
  display: flex;
  align-items: center;
  margin-bottom: 1rem;
}

.mt-logo-img {
  border: 0.2rem solid rgba(255, 255, 255, 0.9);
  box-shadow: var(--shadow-sm);
}

.mt-header-text {
  margin-left: 1.2rem;
  min-width: 0;
  flex: 1;
}

.mt-shop-name {
  font-size: 2rem;
  font-weight: 900;
  margin: 0 0 0.4rem;
  text-shadow: 0 0.2rem 0.5rem rgba(0, 0, 0, 0.38);
}

.mt-shop-meta {
  display: flex;
  align-items: center;
  gap: 1.2rem;
  font-size: 1.2rem;
  opacity: 0.95;
}

.mt-rating {
  display: inline-flex;
  align-items: center;
  gap: 0.2rem;
  font-weight: 800;
}

.mt-star {
  color: var(--primary-color);
}

.mt-sales,
.mt-delivery-time {
  color: rgba(255, 255, 255, 0.92);
}

.mt-shop-fee {
  margin-top: 0.4rem;
  font-size: 1.1rem;
  color: rgba(255, 255, 255, 0.9);
}

.mt-shop-notice {
  font-size: 1.1rem;
  opacity: 0.92;
  margin-bottom: 0.8rem;
  position: relative;
  z-index: 1;
}

.mt-shop-tags {
  display: flex;
  gap: 0.8rem;
  flex-wrap: wrap;
  position: relative;
  z-index: 1;
}

:deep(.mt-tag.van-tag--plain) {
  border-radius: 999px;
  padding: 0.2rem 0.7rem;
  font-size: 1.1rem;
  line-height: 1.5rem;
  border-width: 1px;
}

:deep(.mt-tag--yellow.van-tag--plain) {
  color: var(--mt-strong);
  border-color: rgba(255, 208, 0, 0.5);
  background: rgba(255, 208, 0, 0.2);
  font-weight: 800;
}

:deep(.mt-tag--outline.van-tag--plain) {
  color: rgba(255, 255, 255, 0.92);
  border-color: rgba(255, 255, 255, 0.45);
  background: rgba(255, 255, 255, 0.12);
  font-weight: 700;
}
</style>
