import { ref } from 'vue'
import { showToast } from 'vant'
import { DEFAULT_PAGE_SIZE } from '@/constants/pagination'
import { getOrderPage } from '@/api/modules/order'

// 待评价列表数据层，只拉取已完成且尚未评价的订单。
export const usePendingReviewList = ({ refreshing }) => {
  const pendingOrderList = ref([])
  const pendingLoading = ref(false)
  const pendingFinished = ref(false)
  const pendingPage = ref(1)

  const resetPendingState = () => {
    pendingOrderList.value = []
    pendingPage.value = 1
    pendingFinished.value = false
  }

  const clearRefreshing = () => {
    if (refreshing.value) {
      refreshing.value = false
    }
  }

  const loadPendingReviewPage = async () => {
    if (pendingLoading.value || pendingFinished.value) return

    pendingLoading.value = true
    try {
      if (refreshing.value) {
        resetPendingState()
        clearRefreshing()
      }

      const res = await getOrderPage({
        page: pendingPage.value,
        pageSize: DEFAULT_PAGE_SIZE,
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
        : (pendingPage.value * DEFAULT_PAGE_SIZE < (data.total || 0))

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

  const reloadPendingReviewPage = async () => {
    pendingFinished.value = false
    pendingPage.value = 1
    await loadPendingReviewPage()
  }

  const refreshPendingTab = async (activeTab) => {
    resetPendingState()
    if (activeTab.value === 1) {
      await loadPendingReviewPage()
    }
  }

  const getOrderSummary = (order) => {
    const details = order.orderDetails || order.orderDetailList || []
    if (!details.length) return '订单商品信息'

    return details
      .slice(0, 2)
      .map((item) => item.name)
      .filter(Boolean)
      .join('、')
  }

  return {
    getOrderSummary,
    loadPendingReviewPage,
    pendingFinished,
    pendingLoading,
    pendingOrderList,
    refreshPendingTab,
    reloadPendingReviewPage,
  }
}
