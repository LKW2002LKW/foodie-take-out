<template>
  <div class="review-card">
    <div class="review-head">
      <div class="user-block">
        <van-image
          round
          width="44"
          height="44"
          fit="cover"
          :src="review.userAvatar || defaultAvatar"
        />
        <div class="user-meta">
          <div class="user-name">{{ review.userNickname || '匿名用户' }}</div>
          <div class="meta-row">
            <span v-if="review.merchantName" class="merchant-name">{{ review.merchantName }}</span>
            <span class="time">{{ review.createTime || '' }}</span>
          </div>
        </div>
      </div>
      <van-rate
        :model-value="Number(review.rating || 0)"
        readonly
        allow-half
        color="#ffb400"
        void-icon="star"
        void-color="#e5e5e5"
      />
    </div>

    <div v-if="review.content" class="review-content">{{ review.content }}</div>

    <div v-if="images.length" class="image-grid">
      <van-image
        v-for="(image, index) in images"
        :key="`${image}-${index}`"
        class="review-image"
        fit="cover"
        width="92"
        height="92"
        radius="8"
        :src="image"
      />
    </div>

    <div v-if="orderDetailText" class="order-detail">
      <span class="order-detail-label">订单摘要</span>
      <span class="order-detail-value">{{ orderDetailText }}</span>
    </div>

    <div v-if="review.hasReply || review.merchantReply" class="reply-box">
      <div class="reply-title">商户回复</div>
      <div class="reply-content">{{ review.merchantReply || '暂无回复' }}</div>
      <div v-if="review.replyTime" class="reply-time">{{ review.replyTime }}</div>
    </div>

    <div v-if="$slots.actions" class="card-actions">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  review: {
    type: Object,
    required: true,
  },
})

const defaultAvatar = 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'

const images = computed(() => {
  const imageList = props.review?.imageList
  if (!imageList) return []
  if (Array.isArray(imageList)) return imageList.filter(Boolean)
  return String(imageList)
    .split(',')
    .map(item => item.trim())
    .filter(Boolean)
})

const orderDetailText = computed(() => {
  const detail = props.review?.orderDetail
  if (!detail) return ''
  if (typeof detail === 'string') return detail
  if (Array.isArray(detail)) return detail.map(item => item?.name || '').filter(Boolean).join('、')
  if (typeof detail === 'object') {
    return detail.summary || detail.name || detail.orderNumber || JSON.stringify(detail)
  }
  return String(detail)
})
</script>

<style scoped>
.review-card {
  background: var(--mt-card-bg);
  border-radius: var(--mt-card-radius);
  padding: 1.4rem;
  box-shadow: var(--shadow-sm);
}

.review-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.user-block {
  display: flex;
  gap: 10px;
  min-width: 0;
}

.user-meta {
  min-width: 0;
}

.user-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--mt-strong);
}

.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 4px;
  font-size: 12px;
  color: var(--mt-muted);
}

.merchant-name {
  color: var(--mt-warning);
}

.review-content {
  margin-top: 12px;
  font-size: 14px;
  line-height: 1.7;
  color: var(--text-color);
  white-space: pre-wrap;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(92px, 1fr));
  gap: 10px;
  margin-top: 12px;
}

.review-image {
  overflow: hidden;
  border-radius: 8px;
}

.order-detail {
  margin-top: 12px;
  padding: 10px 12px;
  border-radius: 10px;
  background: #f8fafc;
  color: var(--text-color-secondary);
  font-size: 13px;
  line-height: 1.6;
}

.order-detail-label {
  display: block;
  margin-bottom: 4px;
  color: var(--mt-strong);
  font-weight: 700;
}

.reply-box {
  margin-top: 12px;
  padding: 12px;
  border-radius: 12px;
  background: rgba(255, 208, 0, 0.14);
  border: 1px solid rgba(255, 208, 0, 0.3);
}

.reply-title {
  font-size: 13px;
  font-weight: 700;
  color: var(--mt-strong);
  margin-bottom: 6px;
}

.reply-content {
  font-size: 13px;
  line-height: 1.7;
  color: var(--text-color);
  white-space: pre-wrap;
}

.reply-time {
  margin-top: 6px;
  font-size: 12px;
  color: var(--text-color-secondary);
}

.card-actions {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--mt-divider);
  display: flex;
  justify-content: flex-end;
}
</style>