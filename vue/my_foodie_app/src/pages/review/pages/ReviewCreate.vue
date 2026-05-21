<template>
  <div class="review-create-page mobile-page">
    <!-- 发布评价页只负责表单渲染，订单校验与提交逻辑由 useReviewCreatePage 接管。 -->
    <van-nav-bar title="提交评价" left-arrow fixed placeholder @click-left="goBack" />

    <div class="page-body" v-if="orderInfo.id">
      <div class="summary-card">
        <div class="summary-head">
          <div>
            <div class="summary-title">{{ orderInfo.merchantName || '商户' }}</div>
            <div class="summary-line">订单号：{{ orderInfo.orderNumber || orderInfo.id }}</div>
          </div>
          <div class="summary-amount">￥{{ orderInfo.totalAmount || 0 }}</div>
        </div>
        <div class="summary-dishes">
          <div v-for="item in orderInfo.orderDetailList || []" :key="item.id" class="summary-dish">
            <span class="dish-name">{{ item.name }}</span>
            <span class="dish-count">x{{ item.number }}</span>
          </div>
        </div>
      </div>

      <div class="review-card">
        <div class="section-title">评分</div>
        <div class="rate-row">
          <van-rate v-model="rating" :size="26" color="#f5c200" void-icon="star" void-color="rgba(245, 194, 0, 0.2)" />
          <span class="rate-text">{{ rating }} 星</span>
        </div>

        <div class="section-title">评价内容（选填）</div>
        <div class="field-shell">
          <van-field
            v-model="content"
            rows="4"
            autosize
            type="textarea"
            maxlength="300"
            show-word-limit
            placeholder="说说这次用餐体验吧"
          />
        </div>

        <div class="section-title">上传图片（选填）</div>
        <div class="uploader-shell">
          <van-uploader
            v-model="reviewFileList"
            multiple
            :max-count="6"
            :after-read="afterRead"
            :before-delete="beforeDelete"
            preview-size="8rem"
          />
        </div>

        <van-button
          type="primary"
          block
          round
          class="submit-btn"
          :loading="submitting"
          @click="submitReview"
        >
          提交评价
        </van-button>
      </div>
    </div>

    <van-empty v-else description="订单加载中" />
  </div>
</template>

<script setup>
import { useReviewCreatePage } from '@/composables/business/useReviewCreatePage'

const {
  afterRead,
  beforeDelete,
  content,
  goBack,
  orderInfo,
  rating,
  reviewFileList,
  submitReview,
  submitting,
} = useReviewCreatePage()
</script>

<style scoped>
.review-create-page {
  min-height: 100vh;
  background: var(--mt-page-bg);
}

.page-body {
  padding: 1.2rem;
  padding-bottom: calc(2.4rem + env(safe-area-inset-bottom));
}

.summary-card,
.review-card {
  background: linear-gradient(180deg, #ffffff 0%, #fffdf7 100%);
  border-radius: 1.8rem;
  padding: 1.6rem;
  box-shadow: 0 0.8rem 1.8rem rgba(245, 194, 0, 0.08);
  border: 1px solid rgba(245, 194, 0, 0.12);
  margin-bottom: 1.2rem;
}

.summary-head {
  display: flex;
  justify-content: space-between;
  gap: 1.2rem;
  align-items: flex-start;
}

.summary-title {
  font-size: 1.7rem;
  font-weight: 800;
  color: var(--mt-strong);
  margin-bottom: 0.6rem;
}

.summary-line {
  font-size: 1.3rem;
  color: var(--mt-muted);
}

.summary-amount {
  flex-shrink: 0;
  font-size: 1.8rem;
  font-weight: 800;
  color: var(--mt-strong);
  background: linear-gradient(180deg, #fff5c9 0%, #ffe79a 100%);
  padding: 0.7rem 1rem;
  border-radius: 999px;
}

.summary-dishes {
  margin-top: 1.2rem;
  padding-top: 1.2rem;
  border-top: 1px solid rgba(245, 194, 0, 0.12);
}

.summary-dish {
  display: flex;
  justify-content: space-between;
  gap: 1.2rem;
  padding: 0.7rem 0;
  font-size: 1.3rem;
  color: var(--text-color-secondary);
}

.dish-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.section-title {
  margin: 1.6rem 0 1rem;
  font-size: 1.5rem;
  font-weight: 800;
  color: var(--mt-strong);
  position: relative;
  padding-left: 1rem;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  width: 0.4rem;
  height: 1.4rem;
  transform: translateY(-50%);
  border-radius: 999px;
  background: linear-gradient(180deg, var(--primary-color) 0%, var(--primary-color-dark) 100%);
}

.rate-row {
  display: flex;
  align-items: center;
  gap: 1.2rem;
  min-height: 4.4rem;
  padding: 0.4rem 0 0.8rem;
}

.rate-text {
  font-size: 1.4rem;
  color: var(--mt-warning);
  font-weight: 700;
}

.field-shell,
.uploader-shell {
  border-radius: 1.6rem;
  border: 1px solid rgba(245, 194, 0, 0.12);
  background: linear-gradient(180deg, #fffefb 0%, #fff9e9 100%);
  box-shadow: inset 0 0 0 0.1rem rgba(255, 255, 255, 0.7);
}

.submit-btn {
  margin-top: 1.8rem;
  min-height: 4.4rem;
  font-size: 1.4rem;
  color: var(--mt-strong);
  border: none;
  background: linear-gradient(180deg, #ffe27f 0%, #f5c200 100%);
  box-shadow: 0 0.8rem 1.6rem rgba(245, 194, 0, 0.2);
}

:deep(.van-uploader__preview) {
  margin-bottom: 1rem;
}

:deep(.van-field) {
  background: transparent;
}

:deep(.field-shell .van-field) {
  padding: 1.2rem 1.4rem;
}

:deep(.uploader-shell .van-uploader) {
  padding: 1.2rem 1.2rem 0.2rem;
}

:deep(.van-field__control),
:deep(.van-field__word-limit) {
  font-size: 1.4rem;
}

:deep(.van-field__control) {
  line-height: 1.7;
}

:deep(.van-field__word-limit) {
  color: var(--mt-muted);
}
</style>
