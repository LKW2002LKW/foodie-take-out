<template>
  <div class="merchant-card" @click="$emit('click')">
    <div class="card-left">
      <van-image :src="item.logo || item.image" width="8.8rem" height="8.8rem" radius="1.2rem" fit="cover">
        <template #error><van-icon name="photo-o" size="32" color="var(--primary-color-dark)" /></template>
      </van-image>
      <div v-if="item.status !== 1" class="status-overlay">{{ item.statusDesc }}</div>
    </div>
    <div class="card-right">
      <div class="m-title-row">
        <h3 class="m-name van-ellipsis">{{ nameText }}</h3>
        <span v-if="distanceText" class="m-distance">{{ distanceText }}</span>
      </div>

      <div class="m-stats">
        <span class="m-rating">
          <van-icon name="star" class="m-star" />
          {{ ratingText }}
        </span>
        <span class="m-sales">月售{{ salesText }}</span>
        <span v-if="etaText" class="m-eta">{{ etaText }}</span>
      </div>

      <div class="m-fee">
        <span>起送￥{{ minDeliveryText }}</span>
        <span class="dot">·</span>
        <span>配送￥{{ deliveryFeeText }}</span>
      </div>

      <div class="m-tags">
        <van-tag v-if="item.status === 1" plain class="mt-tag mt-tag--yellow">准时达</van-tag>
        <van-tag plain class="mt-tag mt-tag--gray">支持自取</van-tag>
        <van-tag v-if="promoText" plain class="mt-tag mt-tag--outline">优惠</van-tag>
      </div>

      <div v-if="promoText" class="m-promo van-ellipsis">{{ promoText }}</div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  item: {
    type: Object,
    default: () => ({}),
  },
})
defineEmits(['click'])

const nameText = computed(() => props.item.merchantName || props.item.name || '未命名商家')

const ratingText = computed(() => {
  const value = props.item.rating
  if (value === 0) return '0.0'
  if (!value) return '0.0'
  return String(value)
})

const salesText = computed(() => {
  const value = props.item.salesVolume
  return value === 0 ? 0 : (value || 0)
})

const distanceText = computed(() => {
  const raw = props.item.distance
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
  const raw = props.item.estimatedDeliveryTime ?? props.item.deliveryTime ?? props.item.eta
  if (!raw && raw !== 0) return ''
  const num = Number(raw)
  if (!Number.isNaN(num) && Number.isFinite(num)) return `约${num}分钟`
  const str = String(raw)
  return /分钟|min/i.test(str) ? str : `约${str}分钟`
})

const minDeliveryText = computed(() => {
  const raw = props.item.minDeliveryAmount
  if (raw === 0) return '0'
  return raw ? String(raw) : '0'
})

const deliveryFeeText = computed(() => {
  const raw = props.item.deliveryFee
  if (raw === 0) return '0'
  return raw ? String(raw) : '0'
})

const promoText = computed(() => {
  const candidateKeys = [
    'promotion',
    'promotionDesc',
    'activity',
    'activityDesc',
    'discountDesc',
    'discount',
    'remark',
  ]

  for (const key of candidateKeys) {
    const value = props.item?.[key]
    if (typeof value === 'string' && value.trim()) return value.trim()
  }
  return ''
})
</script>

<style scoped>
.merchant-card {
  display: flex;
  background: var(--mt-card-bg);
  padding: 1.2rem;
  border-radius: var(--mt-card-radius);
  margin-bottom: 1.2rem;
  gap: 1.2rem;
  transition: transform 0.1s;
  box-shadow: var(--shadow-sm);
}
.merchant-card:active { transform: scale(0.98); }
.card-left { position: relative; flex-shrink: 0; }
.status-overlay {
  position: absolute; bottom: 0; left: 0; right: 0;
  background: rgba(245, 194, 0, 0.82); color: var(--secondary-color);
  font-size: 1.1rem; text-align: center; border-radius: 0 0 1.2rem 1.2rem;
  padding: 0.2rem 0;
}
.card-right { flex: 1; display: flex; flex-direction: column; justify-content: space-between; overflow: hidden; }

.m-title-row {
  display: flex;
  align-items: baseline;
  gap: 0.8rem;
}

.m-name {
  flex: 1;
  font-size: 1.6rem;
  font-weight: 800;
  margin: 0;
  color: var(--mt-strong);
}

.m-distance {
  flex-shrink: 0;
  font-size: 1.2rem;
  color: var(--mt-muted);
}

.m-stats {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  margin-top: 0.4rem;
  font-size: 1.2rem;
  color: var(--text-color-secondary);
}

.m-rating {
  color: #d39a00;
  font-weight: 800;
  display: inline-flex;
  align-items: center;
  gap: 0.2rem;
}

.m-star {
  color: #f0b400;
}

.m-eta {
  color: var(--text-color-secondary);
}

.m-fee {
  margin-top: 0.4rem;
  font-size: 1.2rem;
  color: var(--text-color-secondary);
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.dot {
  color: rgba(245, 194, 0, 0.32);
}

.m-tags {
  display: flex;
  gap: 0.6rem;
  margin-top: 0.6rem;
  flex-wrap: wrap;
}

.m-promo {
  margin-top: 0.4rem;
  font-size: 1.2rem;
  color: #D96B00;
}

:deep(.mt-tag.van-tag--plain) {
  border-radius: 1rem;
  padding: 0.2rem 0.6rem;
  font-size: 1.1rem;
  line-height: 1.4rem;
  border-width: 1px;
}

:deep(.mt-tag--yellow.van-tag--plain) {
  color: var(--mt-strong);
  border-color: rgba(232, 178, 22, 0.72);
  border-width: 1.5px;
  background: var(--mt-soft-yellow);
}

:deep(.mt-tag--gray.van-tag--plain) {
  color: var(--mt-strong);
  border-color: rgba(232, 178, 22, 0.72);
  border-width: 1.5px;
  background: var(--mt-soft-yellow);
}

:deep(.mt-tag--outline.van-tag--plain) {
  color: #E34D59;
  border-color: rgba(227, 77, 89, 0.35);
  background: rgba(227, 77, 89, 0.08);
}
</style>
