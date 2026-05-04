import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getDishList } from '@/api/modules/dish'
import { getMerchantDetail, getMerchantCategories } from '@/api/modules/merchant'
import { getSetmealList } from '@/api/modules/setmeal'
import { useCartActions } from '@/composables/business/useCartActions'
import { useMerchantCartPanel } from '@/composables/business/useMerchantCartPanel'
import { useMerchantReviewTab } from '@/composables/business/useMerchantReviewTab'
import { useCartStore } from '@/stores/modules/cartStore'
import { formatFlavorText } from '@/utils/business/flavor'

export const useMerchantDetailPage = () => {
  const route = useRoute()
  const cartStore = useCartStore()
  const { addCartItem } = useCartActions()
  const merchantId = route.params.id

  const activeTab = ref(0)
  const merchantInfo = ref(null)
  const categories = ref([])
  const currentDishes = ref([])
  const loading = ref(false)
  const showFlavorPicker = ref(false)
  const selectedFood = ref(null)

  const reviewTab = useMerchantReviewTab(merchantId, activeTab)
  const cartPanel = useMerchantCartPanel(merchantId, activeTab)

  const loadMerchant = async () => {
    try {
      const res = await getMerchantDetail(merchantId)
      if (res.code === 1) {
        merchantInfo.value = res.data
      }
    } catch (error) {
      console.error(error)
    }
  }

  const loadDishes = async (category) => {
    loading.value = true

    try {
      const res = category.type == 2
        ? await getSetmealList({ categoryId: category.id, merchantId, status: 1 })
        : await getDishList({ categoryId: category.id, merchantId, status: 1 })

      if (res.code === 1) {
        currentDishes.value = category.type == 2
          ? (res.data || []).map((item) => ({ ...item, setmealId: item.id, image: item.image || item.pic }))
          : (res.data || [])
      }
    } finally {
      loading.value = false
    }
  }

  const loadCategories = async () => {
    try {
      const res = await getMerchantCategories(merchantId)
      if (res.code === 1) {
        categories.value = res.data || []
        if (categories.value.length > 0) {
          await loadDishes(categories.value[0])
        }
      }
    } catch (error) {
      console.error(error)
    }
  }

  const onTabChange = (name) => {
    activeTab.value = name
    if (name !== 0) {
      cartPanel.showCartPopup.value = false
    }
  }

  const onCategoryChange = (category) => loadDishes(category)

  const onAddItem = async (item) => {
    if (!item.setmealId && item.flavors?.length > 0) {
      selectedFood.value = item
      showFlavorPicker.value = true
      return
    }

    await addCartItem(item, { merchantId: Number(merchantId) })
  }

  const onConfirmFlavor = async (flavors) => {
    if (!selectedFood.value) return

    await addCartItem(selectedFood.value, {
      flavorSelect: flavors,
      merchantId: Number(merchantId),
    })
  }

  const showFoodDetail = () => {}

  const initialize = async () => {
    await loadMerchant()
    await loadCategories()
    await cartStore.fetchCartList(merchantId)
  }

  onMounted(() => {
    initialize()
  })

  return {
    activeRatingFilter: reviewTab.activeRatingFilter,
    activeTab,
    categories,
    currentDishes,
    currentMerchantCart: cartPanel.currentMerchantCart,
    currentMerchantTotalNum: cartPanel.currentMerchantTotalNum,
    currentMerchantTotalPrice: cartPanel.currentMerchantTotalPrice,
    formatFlavor: (value) => formatFlavorText(value, ','),
    hasImageFilter: reviewTab.hasImageFilter,
    loading,
    loadReviewList: reviewTab.loadReviewList,
    merchantInfo,
    onAddCartPopupItem: cartPanel.onAddCartPopupItem,
    onAddItem,
    onCategoryChange,
    onCheckout: cartPanel.onCheckout,
    onClearCart: cartPanel.onClearCart,
    onConfirmFlavor,
    onReviewRefresh: reviewTab.onReviewRefresh,
    onSubItem: cartPanel.onSubItem,
    onTabChange,
    reviewFinished: reviewTab.reviewFinished,
    reviewList: reviewTab.reviewList,
    reviewLoading: reviewTab.reviewLoading,
    reviewRefreshing: reviewTab.reviewRefreshing,
    reviewStatItems: reviewTab.reviewStatItems,
    selectedFood,
    setRatingFilter: reviewTab.setRatingFilter,
    shouldShowCartBar: cartPanel.shouldShowCartBar,
    showCartPopup: cartPanel.showCartPopup,
    showFlavorPicker,
    showFoodDetail,
    toggleCartPopup: cartPanel.toggleCartPopup,
    toggleHasImageFilter: reviewTab.toggleHasImageFilter,
  }
}
