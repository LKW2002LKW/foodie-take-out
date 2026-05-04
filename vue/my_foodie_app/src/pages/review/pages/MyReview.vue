<template>
  <div class="my-review-page mobile-page">
    <van-nav-bar title="我的评价" left-arrow fixed placeholder @click-left="goBack" />

    <van-tabs :active="activeTab" @change="onTabChange" class="review-tabs">
      <van-tab title="已评价" :name="0" />
      <van-tab title="待评价" :name="1" />
    </van-tabs>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        :loading="currentLoading"
        :finished="currentFinished"
        finished-text="没有更多了"
        @load="onLoad"
        class="list-wrap"
      >
        <div class="review-list" v-if="activeTab === 0 && reviewedList.length">
          <div v-for="(item, index) in reviewedList" :key="item.id" class="review-item-wrap">
            <ReviewCard :review="item">
              <template #actions>
                <van-button
                  size="small"
                  round
                  plain
                  type="danger"
                  class="delete-review-btn"
                  :loading="isDeleting(item.id)"
                  :disabled="isDeleting(item.id)"
                  @click="onDeleteReview(item, index)"
                >
                  删除评价
                </van-button>
              </template>
            </ReviewCard>
          </div>
        </div>

        <div class="pending-list" v-else-if="activeTab === 1 && pendingOrderList.length">
          <div v-for="item in pendingOrderList" :key="item.id" class="pending-card">
            <div class="pending-header">
              <div class="merchant-name">{{ item.merchantName || '商户' }}</div>
              <div class="order-time">{{ item.orderTime || '' }}</div>
            </div>
            <div class="pending-body">
              <div class="order-summary">{{ getOrderSummary(item) }}</div>
              <div class="order-amount">实付 ￥{{ item.totalAmount }}</div>
            </div>
            <div class="pending-actions">
              <van-button size="small" round type="primary" class="review-action-btn" @click="goToReview(item)">去评价</van-button>
            </div>
          </div>
        </div>

        <van-empty v-else-if="currentFinished" :description="activeTab === 0 ? '暂无评价' : '暂无待评价订单'" />
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import ReviewCard from '@/components/business/ReviewCard.vue'
import { useMyReviewPage } from '@/composables/business/useMyReviewPage'

const {
  activeTab,
  currentFinished,
  currentLoading,
  getOrderSummary,
  goBack,
  goToReview,
  isDeleting,
  onDeleteReview,
  onLoad,
  onRefresh,
  onTabChange,
  pendingOrderList,
  refreshing,
  reviewedList,
} = useMyReviewPage()
</script>

<style scoped>
.my-review-page {
  min-height: 100vh;
  min-height: 100dvh;
  background: var(--mt-page-bg);
}

.review-tabs {
  background: var(--mt-card-bg);
  position: sticky;
  top: calc(4.6rem + env(safe-area-inset-top));
  z-index: 20;
  box-shadow: 0 0.2rem 0.8rem rgba(0, 0, 0, 0.04);
}

.list-wrap {
  padding: 1.2rem;
  padding-top: 1.6rem;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
}

.review-item-wrap {
  display: flex;
  flex-direction: column;
}

.pending-list {
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
}

.pending-card {
  background: var(--mt-card-bg);
  border-radius: var(--mt-card-radius);
  padding: 1.4rem;
  box-shadow: var(--shadow-sm);
}

.pending-header {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: center;
}

.merchant-name {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--mt-strong);
}

.order-time {
  font-size: 1.2rem;
  color: var(--mt-muted);
}

.pending-body {
  margin-top: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
}

.order-summary {
  font-size: 1.3rem;
  color: var(--text-color-secondary);
}

.order-amount {
  font-size: 1.3rem;
  color: var(--mt-strong);
  font-weight: 700;
}

.pending-actions {
  margin-top: 1.2rem;
  display: flex;
  justify-content: flex-end;
}

:deep(.van-button--small) {
  min-height: 4.4rem;
  font-size: 1.2rem;
}

:deep(.van-tabs__wrap) {
  min-height: 4.8rem;
}

:deep(.van-tabs__nav) {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(255, 248, 235, 0.98) 100%);
}

:deep(.delete-review-btn.van-button--small) {
  min-height: 3.2rem;
  padding: 0 1rem;
  font-size: 1.15rem;
}

:deep(.review-action-btn.van-button--small) {
  min-height: 3.2rem;
  padding: 0 1rem;
  font-size: 1.15rem;
}
</style>
