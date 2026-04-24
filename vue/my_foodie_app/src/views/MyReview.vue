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
              <van-button size="small" round type="primary" @click="goToReview(item)">去评价</van-button>
            </div>
          </div>
        </div>

        <van-empty v-else-if="currentFinished" :description="activeTab === 0 ? '暂无评价' : '暂无待评价订单'" />
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { computed, onActivated, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showSuccessToast, showToast } from 'vant'
import ReviewCard from '../components/ReviewCard.vue'
import { deleteMyReview, getMyReviewPage } from '../services/review'
import { getOrderPage } from '../api/order'

const router = useRouter()

const activeTab = ref(0)
const refreshing = ref(false)
const deletingMap = ref({})

const reviewedList = ref([])
const reviewedLoading = ref(false)
const reviewedFinished = ref(false)
const reviewedPage = ref(1)
const reviewedTotal = ref(0)

const pendingOrderList = ref([])
const pendingLoading = ref(false)
const pendingFinished = ref(false)
const pendingPage = ref(1)

const pageSize = 10

const currentLoading = computed(() => activeTab.value === 0 ? reviewedLoading.value : pendingLoading.value)
const currentFinished = computed(() => activeTab.value === 0 ? reviewedFinished.value : pendingFinished.value)

const isDeleting = (reviewId) => Boolean(deletingMap.value[reviewId])

const setDeleting = (reviewId, value) => {
  deletingMap.value = {
    ...deletingMap.value,
    [reviewId]: value,
  }
}

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push('/profile')
}

const loadReviewedPage = async () => {
  if (reviewedLoading.value || reviewedFinished.value) return

  reviewedLoading.value = true
  try {
    if (refreshing.value) {
      reviewedList.value = []
      reviewedPage.value = 1
      reviewedFinished.value = false
      refreshing.value = false
    }

    const res = await getMyReviewPage({
      page: reviewedPage.value,
      pageSize,
    })

    if (res.code === 1) {
      const data = res.data || {}
      const list = data.list || []
      reviewedTotal.value = Number(data.total || 0)
      reviewedList.value.push(...list)

      if (data.hasNext === false || list.length < pageSize || reviewedList.value.length >= (data.total || 0)) {
        reviewedFinished.value = true
      } else {
        reviewedPage.value += 1
      }
      return
    }

    reviewedFinished.value = true
    showToast(res.msg || '加载失败')
  } catch (error) {
    console.error(error)
    reviewedFinished.value = true
    showToast('加载失败')
  } finally {
    reviewedLoading.value = false
  }
}

const loadPendingReviewPage = async () => {
  if (pendingLoading.value || pendingFinished.value) return

  pendingLoading.value = true
  try {
    if (refreshing.value) {
      pendingOrderList.value = []
      pendingPage.value = 1
      pendingFinished.value = false
      refreshing.value = false
    }

    const res = await getOrderPage({
      page: pendingPage.value,
      pageSize,
      status: 5,
      pendingReviewOnly: true,
    })

    if (res.code !== 1) {
      pendingFinished.value = true
      showToast(res.msg || '加载失败')
      return
    }

    const data = res.data || {}
    const records = data.records || []
    pendingOrderList.value.push(...records)

    const hasNext = typeof data.hasNext === 'boolean'
      ? data.hasNext
      : (pendingPage.value * pageSize < (data.total || 0))

    if (hasNext) {
      pendingPage.value += 1
    } else {
      pendingFinished.value = true
    }
  } catch (error) {
    console.error(error)
    pendingFinished.value = true
    showToast('加载失败')
  } finally {
    pendingLoading.value = false
  }
}

const onRefresh = () => {
  refreshing.value = true
  if (activeTab.value === 0) {
    reviewedFinished.value = false
    reviewedPage.value = 1
    loadReviewedPage()
    return
  }

  pendingFinished.value = false
  pendingPage.value = 1
  loadPendingReviewPage()
}

const onLoad = () => {
  if (activeTab.value === 0) {
    loadReviewedPage()
    return
  }
  loadPendingReviewPage()
}

const onTabChange = (name) => {
  activeTab.value = Number(name)
  if (activeTab.value === 0 && reviewedList.value.length === 0 && !reviewedFinished.value) {
    loadReviewedPage()
    return
  }
  if (activeTab.value === 1 && pendingOrderList.value.length === 0 && !pendingFinished.value) {
    loadPendingReviewPage()
  }
}

const refreshPendingTab = async () => {
  pendingOrderList.value = []
  pendingPage.value = 1
  pendingFinished.value = false
  if (activeTab.value === 1) {
    await loadPendingReviewPage()
  }
}

const goToReview = (item) => {
  router.push({ path: '/review/create', query: { orderId: item.id } })
}

const getOrderSummary = (order) => {
  const details = order.orderDetails || order.orderDetailList || []
  if (!details.length) return '订单商品信息'
  return details
    .slice(0, 2)
    .map(item => item.name)
    .filter(Boolean)
    .join('、')
}

const onDeleteReview = async (item, index) => {
  if (isDeleting(item.id)) return

  try {
    await showConfirmDialog({
      title: '确认删除',
      message: '删除后不可恢复，是否继续？',
    })
  } catch (error) {
    return
  }

  setDeleting(item.id, true)
  try {
    const res = await deleteMyReview(item.id)
    if (res.code !== 1) {
      showToast(res.msg || '删除失败')
      return
    }

    reviewedList.value.splice(index, 1)
    reviewedTotal.value = Math.max(0, reviewedTotal.value - 1)

    if (reviewedPage.value > 1 && reviewedList.value.length === (reviewedPage.value - 1) * pageSize) {
      const previousPage = Math.max(1, reviewedPage.value - 1)
      reviewedList.value = reviewedList.value.slice(0, Math.max(0, (previousPage - 1) * pageSize))
      reviewedPage.value = previousPage
      reviewedFinished.value = false
      await loadReviewedPage()
      showSuccessToast('删除成功')
      return
    }

    reviewedFinished.value = reviewedList.value.length >= reviewedTotal.value
    showSuccessToast('删除成功')
  } catch (error) {
    const msg = error?.response?.data?.msg || error?.message || '删除失败'
    showToast(msg)
  } finally {
    setDeleting(item.id, false)
  }
}

onMounted(() => {
  loadReviewedPage()
})

onActivated(() => {
  refreshPendingTab()
})
</script>

<style scoped>
.my-review-page {
  min-height: 100vh;
  background: var(--mt-page-bg);
}

.review-tabs {
  background: var(--mt-card-bg);
}

.list-wrap {
  padding: 1.2rem;
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
</style>
