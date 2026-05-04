import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { closeToast, showLoadingToast, showSuccessToast, showToast } from 'vant'
import { commonUpload } from '@/api/modules/common'
import { reviewOrder } from '@/api/modules/order'

export const useOrderReviewActions = (order, reloadOrder) => {
  const router = useRouter()

  const showReviewPanel = ref(false)
  const reviewContent = ref('')
  const reviewRating = ref(5)
  const reviewFileList = ref([])

  const onReview = () => {
    if (!order.value.id) {
      showToast('订单不存在')
      return
    }
    router.push({ path: '/review/create', query: { orderId: order.value.id } })
  }

  const afterRead = async (file) => {
    file.status = 'uploading'
    file.message = '上传中...'
    try {
      const formData = new FormData()
      formData.append('file', file.file)
      const res = await commonUpload(formData)
      if (res.code === 1) {
        file.status = 'done'
        file.message = '上传成功'
        file.url = res.data
      } else {
        file.status = 'failed'
        file.message = '上传失败'
      }
    } catch (error) {
      file.status = 'failed'
      file.message = '上传失败'
    }
  }

  const submitReview = async () => {
    if (!reviewContent.value.trim()) {
      showToast('请输入评价内容')
      return
    }

    const images = reviewFileList.value
      .filter((item) => item.status === 'done' && item.url)
      .map((item) => item.url)
      .join(',')

    showLoadingToast({ message: '提交中...', forbidClick: true })
    try {
      const res = await reviewOrder({
        orderId: order.value.id,
        rating: reviewRating.value,
        content: reviewContent.value,
        images,
      })
      if (res.code === 1) {
        showSuccessToast('评价成功')
        showReviewPanel.value = false
        order.value.canReview = false
        order.value.status = 5
        reloadOrder()
      } else {
        showToast(res.msg || '评价失败')
      }
    } catch (error) {
      showToast('提交失败')
    } finally {
      closeToast()
    }
  }

  return {
    afterRead,
    onReview,
    reviewContent,
    reviewFileList,
    reviewRating,
    showReviewPanel,
    submitReview,
  }
}
