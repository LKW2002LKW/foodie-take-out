import { onMounted, reactive, ref } from 'vue'
import financeApi from '@/api/modules/finance'

const createInitialQueryParams = () => ({
  page: 1,
  pageSize: 10,
  orderNumber: '',
  beginDate: '',
  endDate: '',
})

// 收入明细页组合式，负责分页查询与日期区间筛选。
export const useFinanceIncomePage = () => {
  const loading = ref(false)
  const tableData = ref([])
  const total = ref(0)
  const dateRange = ref([])
  const queryParams = reactive(createInitialQueryParams())

  const getList = async () => {
    loading.value = true
    try {
      const res = await financeApi.getIncomePage(queryParams)
      if (res.data) {
        tableData.value = res.data.records || []
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

  const handleDateChange = (value) => {
    if (value && value.length === 2) {
      queryParams.beginDate = value[0]
      queryParams.endDate = value[1]
      return
    }

    queryParams.beginDate = ''
    queryParams.endDate = ''
  }

  const resetQuery = () => {
    Object.assign(queryParams, createInitialQueryParams())
    dateRange.value = []
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

  onMounted(() => {
    getList()
  })

  return {
    dateRange,
    handleCurrentChange,
    handleDateChange,
    handleQuery,
    handleSizeChange,
    loading,
    queryParams,
    resetQuery,
    tableData,
    total,
  }
}
