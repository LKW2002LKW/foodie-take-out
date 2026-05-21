import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog } from 'vant'
import { useCartActions } from '@/composables/business/useCartActions'
import { useCartStore } from '@/stores/modules/cartStore'

// 店铺详情购物车面板逻辑，负责弹层展示、加减购和去结算跳转。
export const useMerchantCartPanel = (merchantId, activeTab) => {
  const router = useRouter()
  const cartStore = useCartStore()
  const { addCartItem, clearMerchantCart, subCartItem } = useCartActions()

  const showCartPopup = ref(false)

  const currentMerchantCart = computed(() => (
    cartStore.list.filter((item) => item.merchantId == merchantId)
  ))
  const currentMerchantTotalNum = computed(() => (
    currentMerchantCart.value.reduce((sum, item) => sum + Number(item.number || 0), 0)
  ))
  const currentMerchantTotalPrice = computed(() => (
    currentMerchantCart.value
      .reduce((sum, item) => sum + Number(item.amount || 0) * Number(item.number || 0), 0)
      .toFixed(1)
  ))
  const shouldShowCartBar = computed(() => (
    activeTab.value === 0 && currentMerchantTotalNum.value > 0
  ))

  const onSubItem = async (item) => {
    await subCartItem(item)
  }

  const onAddCartPopupItem = async (item) => {
    await addCartItem(item, { merchantId: Number(merchantId) })
  }

  const onCheckout = () => {
    router.push({ path: '/order/create', query: { merchantId } })
  }

  const toggleCartPopup = () => {
    if (currentMerchantCart.value.length > 0) {
      showCartPopup.value = !showCartPopup.value
    }
  }

  const onClearCart = async () => {
    try {
      await showConfirmDialog({ title: '确认清空吗？' })
    } catch (error) {
      return
    }

    const result = await clearMerchantCart(Number(merchantId))
    if (result.ok) {
      showCartPopup.value = false
    }
  }

  watch(currentMerchantTotalNum, (totalNum) => {
    if (totalNum <= 0) {
      showCartPopup.value = false
    }
  })

  return {
    currentMerchantCart,
    currentMerchantTotalNum,
    currentMerchantTotalPrice,
    onAddCartPopupItem,
    onCheckout,
    onClearCart,
    onSubItem,
    shouldShowCartBar,
    showCartPopup,
    toggleCartPopup,
  }
}
