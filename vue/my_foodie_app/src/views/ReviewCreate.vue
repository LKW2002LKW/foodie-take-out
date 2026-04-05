<template>
  <div class="review-create-page">
    <van-nav-bar title="提交评价" left-arrow fixed placeholder @click-left="goBack" />

    <div class="page-body" v-if="orderInfo.id">
      <div class="summary-card">
        <div class="summary-title">{{ orderInfo.merchantName || '商户' }}</div>
        <div class="summary-line">订单号：{{ orderInfo.orderNumber || orderInfo.id }}</div>
        <div class="summary-line">订单金额：￥{{ orderInfo.totalAmount || 0 }}</div>
        <div class="summary-dishes">
          <div v-for="item in orderInfo.orderDetailList || []" :key="item.id" class="summary-dish">
            <span class="dish-name">{{ item.name }}</span>
            <span class="dish-count">x{{ item.number }}</span>
          </div>
        </div>
      </div>

      <div class="review-card">
        <div class="section-title">评分</div>
        <div class="rate-row">
          <van-rate v-model="rating" :size="30" color="#ffb400" void-icon="star" void-color="#e5e5e5" />
          <span class="rate-text">{{ rating }} 星</span>
        </div>

        <div class="section-title">评价内容（选填）</div>
        <van-field
          v-model="content"
          rows="4"
          autosize
          type="textarea"
          maxlength="300"
          show-word-limit
          placeholder="说说这次用餐体验吧"
        />

        <div class="section-title">上传图片（选填）</div>
        <van-uploader
          v-model="reviewFileList"
          multiple
          :max-count="6"
          :after-read="afterRead"
          :before-delete="beforeDelete"
          preview-size="88px"
        />

        <van-button
          type="primary"
          block
          round
          class="submit-btn"
          :loading="submitting"
          @click="submitReview"
        >
          提交评价
        </van-button>
      </div>
    </div>

    <van-empty v-else description="订单加载中" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showLoadingToast, closeToast, showSuccessToast, showToast } from 'vant'
import { commonUpload } from '../api/common'
import { getOrderDetail, reviewOrder } from '../api/order'

const route = useRoute()
const router = useRouter()

const orderId = computed(() => Number(route.query.orderId || 0))
const orderInfo = ref({})
const rating = ref(5)
const content = ref('')
const reviewFileList = ref([])
const submitting = ref(false)

const loadOrder = async () => {
  if (!orderId.value) {
    showToast('订单不存在')
    router.back()
    return
  }

  showLoadingToast({ message: '加载中...', forbidClick: true })
  try {
    const res = await getOrderDetail(orderId.value)
    if (res.code === 1) {
      orderInfo.value = res.data || {}
      if (orderInfo.value.canReview === false) {
        showToast('该订单暂不可评价')
        router.back()
      }
      return
    }
    showToast(res.msg || '订单加载失败')
    router.back()
  } catch (error) {
    console.error(error)
    showToast('订单加载失败')
    router.back()
  } finally {
    closeToast()
  }
}

const goBack = () => {
  router.back()
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
      .filter(item => item.status === 'done' && item.url)
      .map(item => item.url)
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
</script>

<style scoped>
.review-create-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #fffaf4 0%, #f6f7fb 30%, #eef2ff 100%);
}

.page-body {
  padding: 12px;
  padding-bottom: 24px;
}

.summary-card,
.review-card {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.06);
  margin-bottom: 12px;
}

.summary-title {
  font-size: 18px;
  font-weight: 800;
  color: #111827;
  margin-bottom: 8px;
}

.summary-line {
  font-size: 13px;
  color: #6b7280;
  margin-top: 4px;
}

.summary-dishes {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f1f5f9;
}

.summary-dish {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 4px 0;
  font-size: 13px;
  color: #374151;
}

.dish-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.section-title {
  margin: 14px 0 10px;
  font-size: 15px;
  font-weight: 700;
  color: #111827;
}

.rate-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rate-text {
  font-size: 14px;
  color: #f59e0b;
  font-weight: 700;
}

.submit-btn {
  margin-top: 18px;
}

:deep(.van-uploader__preview) {
  margin-bottom: 10px;
}
</style>