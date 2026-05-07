import { computed, onActivated, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { usePendingReviewList } from '@/composables/business/usePendingReviewList'
import { useReviewedReviewList } from '@/composables/business/useReviewedReviewList'

// 我的评价页总控组合式，负责已评价与待评价两个标签的数据切换。
export const useMyReviewPage = () => {
  const router = useRouter()

  const activeTab = ref(0)
  const refreshing = ref(false)
  const reviewedModel = useReviewedReviewList({ refreshing })
  const pendingModel = usePendingReviewList({ refreshing })

  const currentLoading = computed(() => (
    activeTab.value === 0 ? reviewedModel.reviewedLoading.value : pendingModel.pendingLoading.value
  ))
  const currentFinished = computed(() => (
    activeTab.value === 0 ? reviewedModel.reviewedFinished.value : pendingModel.pendingFinished.value
  ))

  const goBack = () => {
    if (window.history.length > 1) {
      router.back()
      return
    }
    router.push('/profile')
  }

  const onRefresh = () => {
    refreshing.value = true

    if (activeTab.value === 0) {
      reviewedModel.reloadReviewedPage()
      return
    }

    pendingModel.reloadPendingReviewPage()
  }

  const onLoad = () => {
    if (activeTab.value === 0) {
      reviewedModel.loadReviewedPage()
      return
    }
    pendingModel.loadPendingReviewPage()
  }

  const onTabChange = (name) => {
    activeTab.value = Number(name)

    if (activeTab.value === 0 && reviewedModel.reviewedList.value.length === 0 && !reviewedModel.reviewedFinished.value) {
      reviewedModel.loadReviewedPage()
      return
    }

    if (activeTab.value === 1 && pendingModel.pendingOrderList.value.length === 0 && !pendingModel.pendingFinished.value) {
      pendingModel.loadPendingReviewPage()
    }
  }

  const goToReview = (item) => {
    router.push({ path: '/review/create', query: { orderId: item.id } })
  }

  onMounted(() => {
    reviewedModel.loadReviewedPage()
  })

  onActivated(() => {
    pendingModel.refreshPendingTab(activeTab)
  })

  return {
    activeTab,
    currentFinished,
    currentLoading,
    getOrderSummary: pendingModel.getOrderSummary,
    goBack,
    goToReview,
    isDeleting: reviewedModel.isDeleting,
    onDeleteReview: reviewedModel.onDeleteReview,
    onLoad,
    onRefresh,
    onTabChange,
    pendingOrderList: pendingModel.pendingOrderList,
    refreshing,
    reviewedList: reviewedModel.reviewedList,
  }
}
