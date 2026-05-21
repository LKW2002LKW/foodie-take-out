import { showConfirmDialog, showFailToast } from 'vant'
import { CART_ACTION_CODE, useCartStore } from '@/stores/modules/cartStore'

const showActionError = (result, fallbackMessage) => {
  if (result?.ok || result?.cancelled) {
    return result
  }

  showFailToast(result?.message || fallbackMessage)
  return result
}

// 购物车动作层，统一处理加购、减购和跨商家冲突确认。
export const useCartActions = () => {
  const cartStore = useCartStore()

  const addCartItem = async (item, options = {}) => {
    const { flavorSelect = null, merchantId } = options

    let result = await cartStore.addToCart(item, flavorSelect, merchantId)

    if (result.code === CART_ACTION_CODE.CROSS_MERCHANT_CONFLICT) {
      try {
        await showConfirmDialog({
          title: '提示',
          message: result.message || '存在其他商家商品，是否清空？',
        })
      } catch (error) {
        return {
          ok: false,
          cancelled: true,
          code: 'USER_CANCELLED',
        }
      }

      const clearResult = await cartStore.clearCartAction(result.merchantId)
      if (!clearResult.ok) {
        return showActionError(clearResult, '清空失败')
      }

      result = await cartStore.addToCart(item, flavorSelect, merchantId)
    }

    return showActionError(result, '操作失败')
  }

  const subCartItem = async (item) => {
    const result = await cartStore.subFromCart(item)
    return showActionError(result, '操作失败')
  }

  const clearMerchantCart = async (merchantId) => {
    const result = await cartStore.clearCartAction(merchantId)
    return showActionError(result, '清空失败')
  }

  const clearAllCart = async () => {
    const result = await cartStore.clearAllCartAction()
    return showActionError(result, '清空失败')
  }

  const removeCartItems = async (items) => {
    const result = await cartStore.batchRemoveItems(items)
    return showActionError(result, '删除失败')
  }

  return {
    addCartItem,
    clearAllCart,
    clearMerchantCart,
    removeCartItems,
    subCartItem,
  }
}
