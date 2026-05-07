import { ref } from 'vue'
import { closeToast, showConfirmDialog, showLoadingToast, showSuccessToast, showToast } from 'vant'
import { cancelOrder, paySuccess, payment } from '@/api/modules/order'

// 订单支付动作层，统一处理支付面板、模拟支付成功与取消订单。
export const useOrderPaymentActions = (order, payTypeModel, reloadOrder) => {
  const showPayPanel = ref(false)

  const handlePay = async () => {
    showLoadingToast({ message: '支付中...', forbidClick: true })
    try {
      const orderNo = order.value.orderNumber
      await payment({ orderNumber: orderNo, payMethod: payTypeModel.value })

      setTimeout(async () => {
        await paySuccess(orderNo)
        closeToast()
        showSuccessToast('支付成功')
        showPayPanel.value = false
        reloadOrder()
      }, 1000)
    } catch (error) {
      console.error(error)
      closeToast()
      showToast('支付异常')
    }
  }

  const onCancel = async () => {
    try {
      await showConfirmDialog({
        title: '提示',
        message: '确定要取消订单吗？',
      })
    } catch (error) {
      return
    }

    try {
      const res = await cancelOrder({ id: order.value.id, cancelReason: '用户取消' })
      if (res.code === 1) {
        showSuccessToast('订单已取消')
        reloadOrder()
      } else {
        showToast(res.msg || '取消失败')
      }
    } catch (error) {
      showToast('操作失败')
    }
  }

  return {
    handlePay,
    onCancel,
    showPayPanel,
  }
}
