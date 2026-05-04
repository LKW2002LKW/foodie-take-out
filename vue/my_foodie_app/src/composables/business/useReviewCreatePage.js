import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { closeToast, showLoadingToast, showSuccessToast, showToast } from 'vant'
import { commonUpload } from '@/api/modules/common'
import { getOrderDetail, reviewOrder } from '@/api/modules/order'

export const useReviewCreatePage = () => {
  const route = useRoute()
  const router = useRouter()

  const orderId = computed(() => Number(route.query.orderId || 0))
  const orderInfo = ref({})
  const rating = ref(5)
  const content = ref('')
  const reviewFileList = ref([])
  const submitting = ref(false)

  const goBack = () => {
    router.back()
  }

  const loadOrder = async () => {
    if (!orderId.value) {
      showToast('订单不存在')
      router.back()
      return
    }

    showLoadingToast({ message: '加载中...', forbidClick: true })
    try {
      const res = await getOrderDetail(orderId.value)
      if (res.code !== 1) {
        showToast(res.msg || '订单加载失败')
        router.back()
        return
      }

      orderInfo.value = res.data || {}
      if (orderInfo.value.canReview === false) {
        showToast('该订单暂不可评价')
        router.back()
      }
    } catch (error) {
      console.error(error)
      showToast('订单加载失败')
      router.back()
    } finally {
      closeToast()
    }
  }

  const uploadSingleFile = async (fileItem) => {
    fileItem.status = 'uploading'
    fileItem.message = '上传中...'

    try {
      const formData = new FormData()
      formData.append('file', fileItem.file)
      const res = await commonUpload(formData)
      if (res.code === 1) {
        fileItem.status = 'done'
        fileItem.message = '上传成功'
        fileItem.url = res.data
        return
      }

      fileItem.status = 'failed'
      fileItem.message = res.msg || '上传失败'
    } catch (error) {
      console.error(error)
      fileItem.status = 'failed'
      fileItem.message = '上传失败'
    }
  }

  const afterRead = async (file) => {
    if (Array.isArray(file)) {
      for (const item of file) {
        await uploadSingleFile(item)
      }
      return
    }
    await uploadSingleFile(file)
  }

  const beforeDelete = (file, detail) => {
    reviewFileList.value.splice(detail.index, 1)
    return false
  }

  const submitReview = async () => {
    if (!rating.value || rating.value < 1 || rating.value > 5) {
      showToast('请先选择评分')
      return
    }

    submitting.value = true
    showLoadingToast({ message: '提交中...', forbidClick: true })
    let submittedSuccessfully = false

    try {
      const images = reviewFileList.value
        .filter((item) => item.status === 'done' && item.url)
        .map((item) => item.url)
        .join(',')

      const res = await reviewOrder({
        orderId: orderInfo.value.id,
        rating: rating.value,
        content: content.value.trim(),
        images,
      })

      if (res.code === 1) {
        submittedSuccessfully = true
        closeToast()
        showSuccessToast('评价成功')
        router.replace(`/order/${orderInfo.value.id}`)
        return
      }

      showToast(res.msg || '评价失败')
    } catch (error) {
      console.error(error)
      showToast('评价提交失败')
    } finally {
      submitting.value = false
      if (!submittedSuccessfully) {
        closeToast()
      }
    }
  }

  onMounted(() => {
    loadOrder()
  })

  return {
    afterRead,
    beforeDelete,
    content,
    goBack,
    orderInfo,
    rating,
    reviewFileList,
    submitReview,
    submitting,
  }
}
