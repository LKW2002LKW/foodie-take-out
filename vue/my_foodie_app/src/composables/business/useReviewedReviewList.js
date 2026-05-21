import { ref } from 'vue'
import { showConfirmDialog, showSuccessToast, showToast } from 'vant'
import { DEFAULT_PAGE_SIZE } from '@/constants/pagination'
import { deleteMyReview, getMyReviewPage } from '@/api/modules/review'

// 已评价列表数据层，负责我的评价分页拉取与删除操作。
export const useReviewedReviewList = ({ refreshing }) => {
  const deletingMap = ref({})
  const reviewedList = ref([])
  const reviewedLoading = ref(false)
  const reviewedFinished = ref(false)
  const reviewedPage = ref(1)
  const reviewedTotal = ref(0)

  const isDeleting = (reviewId) => Boolean(deletingMap.value[reviewId])

  const setDeleting = (reviewId, value) => {
    deletingMap.value = {
      ...deletingMap.value,
      [reviewId]: value,
    }
  }

  const resetReviewedState = () => {
    reviewedList.value = []
    reviewedPage.value = 1
    reviewedFinished.value = false
  }

  const clearRefreshing = () => {
    if (refreshing.value) {
      refreshing.value = false
    }
  }

  const loadReviewedPage = async () => {
    if (reviewedLoading.value || reviewedFinished.value) return

    reviewedLoading.value = true
    try {
      if (refreshing.value) {
        resetReviewedState()
        clearRefreshing()
      }

      const res = await getMyReviewPage({
        page: reviewedPage.value,
        pageSize: DEFAULT_PAGE_SIZE,
      })

      if (res.code !== 1) {
        reviewedFinished.value = true
        showToast(res.msg || '加载失败')
        return
      }

      const data = res.data || {}
      const list = data.list || []
      reviewedTotal.value = Number(data.total || 0)
      reviewedList.value.push(...list)

      if (
        data.hasNext === false
        || list.length < DEFAULT_PAGE_SIZE
        || reviewedList.value.length >= (data.total || 0)
      ) {
        reviewedFinished.value = true
      } else {
        reviewedPage.value += 1
      }
    } catch (error) {
      console.error(error)
      reviewedFinished.value = true
      showToast('加载失败')
    } finally {
      reviewedLoading.value = false
    }
  }

  const reloadReviewedPage = async () => {
    reviewedFinished.value = false
    reviewedPage.value = 1
    await loadReviewedPage()
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

      if (reviewedPage.value > 1 && reviewedList.value.length === (reviewedPage.value - 1) * DEFAULT_PAGE_SIZE) {
        const previousPage = Math.max(1, reviewedPage.value - 1)
        reviewedList.value = reviewedList.value.slice(0, Math.max(0, (previousPage - 1) * DEFAULT_PAGE_SIZE))
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

  return {
    isDeleting,
    loadReviewedPage,
    onDeleteReview,
    reloadReviewedPage,
    reviewedFinished,
    reviewedList,
    reviewedLoading,
  }
}
