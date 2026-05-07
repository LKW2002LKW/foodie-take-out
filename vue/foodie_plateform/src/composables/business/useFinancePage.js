import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import financeApi from '@/api/modules/finance'

// 财务中心页组合式，负责结算分页、抽成配置与报表概览装配。
export const useFinancePage = () => {
  const activeTab = ref('settlement')
  const loadingSettlement = ref(false)
  const settlementList = ref([])
  const settlementTotal = ref(0)
  const settlementParams = reactive({
    page: 1,
    pageSize: 10,
    merchantId: '',
    settlementCycle: '',
    status: null,
  })

  const loadingCommission = ref(false)
  const commissionList = ref([])
  const commissionTotal = ref(0)
  const commissionParams = reactive({
    page: 1,
    pageSize: 10,
  })

  const reportData = ref(null)
  const commissionDialogVisible = ref(false)
  const commissionForm = reactive({
    configType: 2,
    merchantId: null,
    categoryId: null,
    commissionRate: 15.0,
    effectiveTime: '',
  })

  const getSettlementList = async () => {
    loadingSettlement.value = true
    try {
      const res = await financeApi.getSettlementPage(settlementParams)
      if (res.data) {
        settlementList.value = res.data.records || []
        settlementTotal.value = res.data.total || 0
      }
    } finally {
      loadingSettlement.value = false
    }
  }

  const getCommissionList = async () => {
    loadingCommission.value = true
    try {
      const res = await financeApi.getCommissionPage(commissionParams)
      if (res.data) {
        commissionList.value = res.data.records || []
        commissionTotal.value = res.data.total || 0
      }
    } finally {
      loadingCommission.value = false
    }
  }

  const getReportData = async () => {
    const res = await financeApi.getReport()
    reportData.value = res.data
  }

  const handleOpenCommissionDialog = () => {
    Object.assign(commissionForm, {
      configType: 2,
      merchantId: null,
      categoryId: null,
      commissionRate: 15.0,
      effectiveTime: '',
    })
    commissionDialogVisible.value = true
  }

  const submitCommission = async () => {
    if (!commissionForm.effectiveTime) {
      ElMessage.warning('请选择生效时间')
      return
    }

    await financeApi.createCommission(commissionForm)
    ElMessage.success('新增成功')
    commissionDialogVisible.value = false
    getCommissionList()
  }

  const getSettlementStatusText = (status) => ({ 1: '待结算', 2: '已结算', 3: '已取消' }[status] || '未知')
  const getSettlementStatusType = (status) => {
    if (status === 2) return 'success'
    if (status === 1) return 'warning'
    return 'info'
  }
  const getConfigTypeText = (type) => ({ 1: '全局默认', 2: '商户特定', 3: '分类特定' }[type] || '未知')

  watch(activeTab, (value) => {
    if (value === 'settlement' && !settlementList.value.length) {
      getSettlementList()
    } else if (value === 'commission' && !commissionList.value.length) {
      getCommissionList()
    } else if (value === 'report' && !reportData.value) {
      getReportData()
    }
  })

  onMounted(() => {
    getSettlementList()
  })

  return {
    activeTab,
    commissionDialogVisible,
    commissionForm,
    commissionList,
    commissionParams,
    commissionTotal,
    getCommissionList,
    getConfigTypeText,
    getReportData,
    getSettlementList,
    getSettlementStatusText,
    getSettlementStatusType,
    handleOpenCommissionDialog,
    loadingCommission,
    loadingSettlement,
    reportData,
    settlementList,
    settlementParams,
    settlementTotal,
    submitCommission,
  }
}
