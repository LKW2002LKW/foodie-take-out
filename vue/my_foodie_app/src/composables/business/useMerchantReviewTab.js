import { computed, ref, watch } from 'vue'
import { MERCHANT_REVIEW_PAGE_SIZE } from '@/constants/pagination'
import { reviewStatLabelMap } from '@/constants/review'
import { getReviewPage, getReviewStats } from '@/api/modules/review'

const normalizeReviewImages = (imageList) => {
  if (!imageList) return []
  if (Array.isArray(imageList)) return imageList.filter(Boolean)

  return String(imageList)
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

// 店铺详情评价标签页逻辑，负责统计、筛选与分页加载。
export const useMerchantReviewTab = (merchantId, activeTab) => {
  const reviewStats = ref({})
  const reviewList = ref([])
  const reviewLoading = ref(false)
  const reviewFinished = ref(false)
  const reviewRefreshing = ref(false)
  const reviewPage = ref(1)
  const activeRatingFilter = ref(0)
  const hasImageFilter = ref(false)
  const reviewInitialized = ref(false)

  const reviewStatItems = computed(() => {
    const entries = Object.entries(reviewStats.value || {})

    return entries
      .filter(([, value]) => ['string', 'number', 'boolean'].includes(typeof value))
      .map(([key, value]) => ({
        key,
        label: reviewStatLabelMap[key] || key,
        value,
      }))
  })

  const loadReviewStatsData = async () => {
    try {
      const res = await getReviewStats(Number(merchantId))
      if (res.code === 1) {
        reviewStats.value = res.data || {}
      }
    } catch (error) {
      console.error(error)
    }
  }

  const loadReviewList = async () => {
    if (reviewLoading.value) return

    reviewLoading.value = true
    try {
      if (reviewRefreshing.value) {
        reviewList.value = []
        reviewPage.value = 1
        reviewFinished.value = false
        reviewRefreshing.value = false
      }

      if (reviewFinished.value) return

      const params = {
        merchantId: Number(merchantId),
        page: reviewPage.value,
        pageSize: MERCHANT_REVIEW_PAGE_SIZE,
      }

      if (activeRatingFilter.value !== 0) {
        params.ratingFilter = activeRatingFilter.value
      }

      if (hasImageFilter.value) {
        params.hasImage = true
      }

      const res = await getReviewPage(params)
      if (res.code === 1) {
        const data = res.data || {}
        const list = (data.list || []).map((item) => ({
          ...item,
          imageList: normalizeReviewImages(item.imageList),
        }))

        reviewList.value.push(...list)

        if (
          data.hasNext === false
          || list.length < MERCHANT_REVIEW_PAGE_SIZE
          || reviewList.value.length >= (data.total || 0)
        ) {
          reviewFinished.value = true
        } else {
          reviewPage.value += 1
        }

        reviewInitialized.value = true
        return
      }

      reviewFinished.value = true
    } catch (error) {
      console.error(error)
      reviewFinished.value = true
    } finally {
      reviewLoading.value = false
      reviewRefreshing.value = false
    }
  }

  const ensureInitialized = async () => {
    if (reviewInitialized.value) return
    await loadReviewStatsData()
    await loadReviewList()
  }

  const setRatingFilter = async (value) => {
    activeRatingFilter.value = value
    reviewRefreshing.value = true
    await loadReviewList()
  }

  const toggleHasImageFilter = async () => {
    hasImageFilter.value = !hasImageFilter.value
    reviewRefreshing.value = true
    await loadReviewList()
  }

  const onReviewRefresh = async () => {
    reviewRefreshing.value = true
    await loadReviewList()
  }

  watch(activeTab, async (tab) => {
    if (tab === 1 && !reviewInitialized.value) {
      await ensureInitialized()
    }
  })

  return {
    activeRatingFilter,
    hasImageFilter,
    loadReviewList,
    onReviewRefresh,
    reviewFinished,
    reviewList,
    reviewLoading,
    reviewRefreshing,
    reviewStatItems,
    setRatingFilter,
    toggleHasImageFilter,
  }
}
