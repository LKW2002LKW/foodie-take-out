<template>
  <div class="merchant-card" @click="$emit('click')">
    <!-- 商家卡片只展示格式化后的商家信息，不在组件内部处理数据请求。 -->
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
import { useMerchantCard } from '@/composables/business/useMerchantCard'

const props = defineProps({
  item: {
    type: Object,
    default: () => ({}),
  },
})
defineEmits(['click'])
const item = computed(() => props.item)

const {
  deliveryFeeText,
  distanceText,
  etaText,
  minDeliveryText,
  nameText,
  promoText,
  ratingText,
  salesText,
} = useMerchantCard(item)
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
