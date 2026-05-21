import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import reviewApi from '@/api/modules/review'

const createInitialQueryParams = () => ({
  page: 1,
  pageSize: 10,
  rating: undefined,
  replied: undefined,
  beginTime: undefined,
  endTime: undefined,
})

const createInitialReplyForm = () => ({
  id: undefined,
  content: '',
})

// 顾客评价页组合式，负责筛选查询、回复弹窗与回复提交流程。
export const useCustomerReviewPage = () => {
  const loading = ref(false)
  const reviewList = ref([])
  const total = ref(0)
  const dateRange = ref([])
  const activeTab = ref('all')
  const replyVisible = ref(false)
  const submitting = ref(false)
  const replyFormRef = ref(null)

  const queryParams = reactive(createInitialQueryParams())
  const replyForm = reactive(createInitialReplyForm())

  const getList = async () => {
    loading.value = true
    try {
      const res = activeTab.value === 'bad'
        ? await reviewApi.getBadReviewPage({
          page: queryParams.page,
          pageSize: queryParams.pageSize,
        })
        : await reviewApi.getReviewPage(queryParams)

      if (res.data) {
        reviewList.value = res.data.records || []
        total.value = res.data.total || 0
      }
    } catch (error) {
      console.error(error)
    } finally {
      loading.value = false
    }
  }

  const handleQuery = () => {
    queryParams.page = 1
    getList()
  }

  const resetQuery = () => {
    Object.assign(queryParams, createInitialQueryParams())
    dateRange.value = []
    getList()
  }

  const handleDateChange = (value) => {
    if (value && value.length === 2) {
      queryParams.beginTime = value[0]
      queryParams.endTime = value[1]
      return
    }

    queryParams.beginTime = undefined
    queryParams.endTime = undefined
  }

  const handleTabChange = () => {
    queryParams.page = 1
    getList()
  }

  const handleSizeChange = (value) => {
    queryParams.pageSize = value
    getList()
  }

  const handleCurrentChange = (value) => {
    queryParams.page = value
    getList()
  }

  const handleOpenReply = (item) => {
    Object.assign(replyForm, {
      id: item.id,
      content: '',
    })
    replyVisible.value = true
  }

  const submitReply = async () => {
    if (!replyFormRef.value) return

    const valid = await replyFormRef.value.validate().catch(() => false)
    if (!valid) return

    submitting.value = true
    try {
      await reviewApi.replyReview({
        id: replyForm.id,
        merchantReply: replyForm.content,
      })
      ElMessage.success('回复成功')
      replyVisible.value = false
      getList()
    } catch (error) {
      console.error(error)
    } finally {
      submitting.value = false
    }
  }

  onMounted(() => {
    getList()
  })

  return {
    activeTab,
    dateRange,
    handleCurrentChange,
    handleDateChange,
    handleOpenReply,
    handleQuery,
    handleSizeChange,
    handleTabChange,
    loading,
    queryParams,
    replyForm,
    replyFormRef,
    replyVisible,
    resetQuery,
    reviewList,
    submitReply,
    submitting,
    total,
  }
}
