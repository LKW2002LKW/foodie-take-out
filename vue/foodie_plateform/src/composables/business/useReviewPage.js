import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import reviewApi from '@/api/modules/review'

// 评价管理页组合式，负责评价查询、差评预警切换与删除操作。
export const useReviewPage = () => {
  const activeTab = ref('all')
  const loading = ref(false)
  const tableData = ref([])
  const total = ref(0)
  const dateRange = ref([])

  const queryParams = reactive({
    page: 1,
    pageSize: 10,
    merchantId: '',
    rating: null,
    beginTime: '',
    endTime: '',
  })

  const handleDateChange = (value) => {
    if (value) {
      queryParams.beginTime = value[0]
      queryParams.endTime = value[1]
      return
    }
    queryParams.beginTime = ''
    queryParams.endTime = ''
  }

  const getList = async () => {
    loading.value = true
    try {
      const res = activeTab.value === 'warning'
        ? await reviewApi.getWarningReviewPage(queryParams)
        : await reviewApi.getReviewPage(queryParams)
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    } finally {
      loading.value = false
    }
  }

  const handleSearch = () => {
    queryParams.page = 1
    getList()
  }

  const resetQuery = () => {
    queryParams.merchantId = ''
    queryParams.rating = null
    dateRange.value = []
    handleDateChange(null)
    handleSearch()
  }

  const handleTabChange = () => {
    queryParams.page = 1
    getList()
  }

  const handleDelete = async (row) => {
    await reviewApi.deleteReview(row.id)
    ElMessage.success('删除成功')
    getList()
  }

  onMounted(() => {
    getList()
  })

  return {
    activeTab,
    dateRange,
    getList,
    handleDateChange,
    handleDelete,
    handleSearch,
    handleTabChange,
    loading,
    queryParams,
    resetQuery,
    tableData,
    total,
  }
}
