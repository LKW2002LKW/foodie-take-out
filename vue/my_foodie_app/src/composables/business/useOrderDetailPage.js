import { useRoute } from 'vue-router'
import { formatFlavorText } from '@/utils/business/flavor'
import { useOrderDetailData } from '@/composables/business/useOrderDetailData'
import { useOrderPaymentActions } from '@/composables/business/useOrderPaymentActions'
import { useOrderReviewActions } from '@/composables/business/useOrderReviewActions'

// 订单详情页总控组合式，将数据层、支付动作和评价动作进行装配。
export const useOrderDetailPage = () => {
  const route = useRoute()
  const routeId = route.params.id

  const detailData = useOrderDetailData(routeId)
  const paymentActions = useOrderPaymentActions(
    detailData.order,
    detailData.payTypeModel,
    detailData.loadData,
  )
  const reviewActions = useOrderReviewActions(
    detailData.order,
    detailData.loadData,
  )

  return {
    afterRead: reviewActions.afterRead,
    getFlavorText: formatFlavorText,
    getStatusText: detailData.getStatusText,
    goBack: detailData.goBack,
    handlePay: paymentActions.handlePay,
    onCancel: paymentActions.onCancel,
    onReview: reviewActions.onReview,
    order: detailData.order,
    payTypeModel: detailData.payTypeModel,
    reviewContent: reviewActions.reviewContent,
    reviewFileList: reviewActions.reviewFileList,
    reviewRating: reviewActions.reviewRating,
    routeId,
    showPayPanel: paymentActions.showPayPanel,
    showReviewPanel: reviewActions.showReviewPanel,
    showTrackPopup: detailData.showTrackPopup,
    submitReview: reviewActions.submitReview,
    trackInfo: detailData.trackInfo,
  }
}
