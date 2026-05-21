import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import orderApi from '@/api/modules/order'

const orderStatusTextMap = {
  1: '待付款',
  2: '待接单',
  3: '已接单',
  4: '派送中',
  5: '已完成',
  6: '已取消',
  7: '已退款',
}

const orderStatusTypeMap = {
  1: 'info',
  2: 'danger',
  3: 'primary',
  4: 'warning',
  5: 'success',
  6: 'info',
  7: 'info',
}

const orderActionMap = {
  cancel: { text: '取消', method: 'cancelOrder', needReason: true },
  complete: { text: '完成', method: 'completeOrder', needReason: false },
  confirm: { text: '接单', method: 'confirmOrder', needReason: false },
  delivery: { text: '派送', method: 'deliveryOrder', needReason: false },
  reject: { text: '拒单', method: 'rejectOrder', needReason: true },
}

// 订单列表页组合式，负责订单查询、统计面板与履约动作处理。
export const useOrderListPage = () => {
  const loading = ref(false)
  const orderList = ref([])
  const total = ref(0)
  const activeTab = ref('all')
  const dateRange = ref([])
  const detailVisible = ref(false)
  const currentOrder = ref(null)
  const statistics = ref({
    toBeConfirmed: 0,
    confirmed: 0,
    deliveryInProgress: 0,
  })

  const queryParams = reactive({
    page: 1,
    pageSize: 10,
    orderNumber: '',
    phone: '',
    status: undefined,
    beginTime: undefined,
    endTime: undefined,
  })

  const getStatistics = async () => {
    try {
      const res = await orderApi.getOrderStatistics()
      if (res.data) {
        statistics.value = res.data
      }
    } catch (error) {
      console.error(error)
    }
  }

  const getList = async () => {
    loading.value = true
    try {
      const res = await orderApi.getOrderPage(queryParams)
      if (res.data) {
        orderList.value = res.data.records || []
        total.value = res.data.total || 0
      }
    } catch (error) {
      console.error(error)
    } finally {
      loading.value = false
    }
  }

  const refreshPage = () => {
    getList()
    getStatistics()
  }

  const handleQuery = () => {
    queryParams.page = 1
    refreshPage()
  }

  const resetQuery = () => {
    queryParams.orderNumber = ''
    queryParams.phone = ''
    queryParams.status = activeTab.value === 'all' ? undefined : Number(activeTab.value)
    queryParams.beginTime = undefined
    queryParams.endTime = undefined
    dateRange.value = []
    handleQuery()
  }

  const handleTabChange = (name) => {
    queryParams.status = name === 'all' ? undefined : Number(name)
    handleQuery()
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

  const handleSizeChange = (value) => {
    queryParams.pageSize = value
    getList()
  }

  const handleCurrentChange = (value) => {
    queryParams.page = value
    getList()
  }

  const handleViewDetail = async (row) => {
    try {
      const res = await orderApi.getOrderDetails(row.id)
      currentOrder.value = res.data
      detailVisible.value = true
    } catch (error) {
      console.error(error)
      ElMessage.error('无法获取订单详情')
    }
  }

  const runOrderAction = async (action, row, reason = '') => {
    try {
      const payload = action.needReason ? { id: row.id, reason } : { id: row.id }
      await orderApi[action.method](payload)
      ElMessage.success(`${action.text}成功`)
      refreshPage()
    } catch (error) {
      console.error(error)
    }
  }

  const handleOrderAction = (type, row) => {
    const action = orderActionMap[type]
    if (!action) return

    if (action.needReason) {
      ElMessageBox.prompt(`请输入${action.text}原因`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /\S+/,
        inputErrorMessage: '原因不能为空',
      }).then(({ value }) => {
        runOrderAction(action, row, value)
      }).catch(() => {})
      return
    }

    ElMessageBox.confirm(`确认${action.text}该订单吗?`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: type === 'cancel' ? 'warning' : 'primary',
    }).then(() => {
      runOrderAction(action, row)
    }).catch(() => {})
  }

  const getStatusText = (status) => orderStatusTextMap[status] || '未知'
  const getStatusType = (status) => orderStatusTypeMap[status] || ''
  const getPayStatusText = (status) => (status === 1 ? '已支付' : '未支付')
  const getPayMethodText = (method) => (method === 1 ? '微信支付' : (method === 2 ? '支付宝' : '未知'))

  onMounted(() => {
    getStatistics()
    getList()
  })

  return {
    activeTab,
    currentOrder,
    dateRange,
    detailVisible,
    getPayMethodText,
    getPayStatusText,
    getStatusText,
    getStatusType,
    handleCurrentChange,
    handleDateChange,
    handleOrderAction,
    handleQuery,
    handleSizeChange,
    handleTabChange,
    handleViewDetail,
    loading,
    orderList,
    queryParams,
    resetQuery,
    statistics,
    total,
  }
}
