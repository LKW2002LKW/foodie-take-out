import { onMounted, reactive, ref } from 'vue'
import financeApi from '@/api/modules/finance'

const createInitialQueryParams = () => ({
  page: 1,
  pageSize: 10,
  settlementCycle: '',
  status: undefined,
})

// 结算记录页组合式，负责条件查询与分页切换。
export const useFinanceSettlementPage = () => {
  const loading = ref(false)
  const tableData = ref([])
  const total = ref(0)
  const queryParams = reactive(createInitialQueryParams())

  const getList = async () => {
    loading.value = true
    try {
      const res = await financeApi.getSettlementPage(queryParams)
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

  const resetQuery = () => {
    Object.assign(queryParams, createInitialQueryParams())
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
    handleCurrentChange,
    handleQuery,
    handleSizeChange,
    loading,
    queryParams,
    resetQuery,
    tableData,
    total,
  }
}
