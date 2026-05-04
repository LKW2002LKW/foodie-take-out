import { onMounted, reactive, ref } from 'vue'
import orderApi from '@/api/modules/order'

export const useOrderPage = () => {
  const loading = ref(false)
  const tableData = ref([])
  const total = ref(0)
  const detailVisible = ref(false)
  const currentOrder = ref(null)
  const abnormalVisible = ref(false)
  const abnormalData = ref([])
  const dateRange = ref([])

  const queryParams = reactive({
    page: 1,
    pageSize: 10,
    orderNumber: '',
    phone: '',
    status: null,
    beginTime: '',
    endTime: '',
  })

  const handleDateChange = (value) => {
    if (value) {
      queryParams.beginTime = `${value[0]} 00:00:00`
      queryParams.endTime = `${value[1]} 23:59:59`
      return
    }
    queryParams.beginTime = ''
    queryParams.endTime = ''
  }

  const getList = async () => {
    loading.value = true
    try {
      const res = await orderApi.getOrderPage(queryParams)
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

  const handleDetail = async (row) => {
    const res = await orderApi.getOrderDetail(row.id)
    currentOrder.value = res.data
    detailVisible.value = true
  }

  const showAbnormal = async () => {
    const res = await orderApi.getAbnormalOrderPage({ page: 1, pageSize: 100 })
    abnormalData.value = res.data.records || []
    abnormalVisible.value = true
  }

  const getStatusText = (status) => ({
    1: '待支付',
    2: '待接单',
    3: '已接单',
    4: '配送中',
    5: '已完成',
    6: '已取消',
  }[status] || '未知')

  onMounted(() => {
    getList()
  })

  return {
    abnormalData,
    abnormalVisible,
    currentOrder,
    dateRange,
    detailVisible,
    getList,
    getStatusText,
    handleDateChange,
    handleDetail,
    handleSearch,
    loading,
    queryParams,
    showAbnormal,
    tableData,
    total,
  }
}
