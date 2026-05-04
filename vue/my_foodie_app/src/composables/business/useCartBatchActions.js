import { showConfirmDialog, showToast } from 'vant'

export const useCartBatchActions = ({
  clearAllCart,
  clearMerchantCart,
  currentMerchant,
  currentMerchantId,
  groupedCart,
  leaveDetail,
  merchantGroupList,
  removeCartItems,
  resetManageState,
  selectedCount,
  selectedMerchantIds,
}) => {
  const onDeleteSelected = async () => {
    const itemsToDelete = currentMerchant.value.items.filter((item) => item.selected)
    if (itemsToDelete.length === 0) return

    try {
      await showConfirmDialog({
        title: '确认删除',
        message: `确定要删除这 ${itemsToDelete.length} 件商品吗？`,
      })
    } catch (error) {
      return
    }

    const result = await removeCartItems(itemsToDelete)
    if (!result.ok) {
      return
    }

    showToast('删除成功')
    if ((groupedCart.value[currentMerchantId.value]?.items || []).length === 0) {
      leaveDetail()
      return
    }

    resetManageState()
  }

  const onClearSelectedMerchants = async () => {
    if (selectedCount.value === 0) return

    const shouldClearAll = selectedCount.value === merchantGroupList.value.length
    const message = shouldClearAll
      ? '确定要清空全部商家的购物车吗？'
      : `确定要清空已选的 ${selectedCount.value} 个商家购物车吗？`

    try {
      await showConfirmDialog({
        title: '确认清空',
        message,
      })
    } catch (error) {
      return
    }

    if (shouldClearAll) {
      const result = await clearAllCart()
      if (!result.ok) {
        return
      }
    } else {
      const results = await Promise.all(
        selectedMerchantIds.value.map((merchantId) => clearMerchantCart(merchantId)),
      )

      if (results.some((result) => !result.ok)) {
        return
      }
    }

    showToast('清空成功')
    resetManageState()
  }

  return {
    onClearSelectedMerchants,
    onDeleteSelected,
  }
}
