import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import merchantApi from '@/api/modules/merchant'

/**
 * 商户管理业务逻辑 Composable
 */
export function useMerchant() {
  const loading = ref(false)
  const tableData = ref([])
  const total = ref(0)
  
  const queryParams = reactive({
    page: 1,
    pageSize: 10,
    merchantName: '',
    cityName: '',
    status: null,
    auditStatus: null
  })

  // 弹窗状态
  const detailVisible = ref(false)
  const currentMerchant = ref(null)
  const auditVisible = ref(false)
  
  const auditForm = reactive({
    id: null,
    auditStatus: 1,
    auditReason: ''
  })

  /**
   * 获取列表
   */
  const getList = async () => {
    loading.value = true
    try {
      const { data } = await merchantApi.getMerchantPage(queryParams)
      if (data) {
        tableData.value = data.records || []
        total.value = data.total || 0
      }
    } catch (e) {
      console.error('Fetch merchant page failed:', e)
    } finally {
      loading.value = false
    }
  }

  /**
   * 搜索与重置
   */
  const handleQuery = () => {
    queryParams.page = 1
    getList()
  }

  const resetQuery = () => {
    queryParams.merchantName = ''
    queryParams.cityName = ''
    queryParams.status = null
    queryParams.auditStatus = null
    handleQuery()
  }

  /**
   * 详情查看
   */
  const handleDetail = async (row) => {
    try {
      const { data } = await merchantApi.getMerchantDetail(row.id)
      if (data) {
        currentMerchant.value = data
        detailVisible.value = true
      }
    } catch (e) {
      ElMessage.error('获取详情失败')
    }
  }

  /**
   * 审核处理
   */
  const handleAudit = (row) => {
    auditForm.id = row.id
    auditForm.auditStatus = 1
    auditForm.auditReason = ''
    auditVisible.value = true
  }

  const submitAudit = async () => {
    if (auditForm.auditStatus === 3 && !auditForm.auditReason) {
      ElMessage.warning('请输入拒绝原因')
      return
    }
    try {
      await merchantApi.auditMerchant(auditForm)
      ElMessage.success('操作成功')
      auditVisible.value = false
      getList()
    } catch (e) {
      console.error('Audit failed:', e)
    }
  }

  /**
   * 启用/禁用
   */
  const toggleMerchantStatus = async (row, enable = true) => {
    const actionText = enable ? '启用' : '禁用'
    try {
      await ElMessageBox.confirm(`确认${actionText}该商户吗？`, '提示', {
        type: enable ? 'success' : 'warning'
      })
      if (enable) {
        await merchantApi.enableMerchant(row.id)
      } else {
        await merchantApi.disableMerchant(row.id)
      }
      ElMessage.success(`${actionText}成功`)
      getList()
    } catch (e) {
      // Cancelled or error
    }
  }

  return {
    loading,
    tableData,
    total,
    queryParams,
    detailVisible,
    currentMerchant,
    auditVisible,
    auditForm,
    getList,
    handleQuery,
    resetQuery,
    handleDetail,
    handleAudit,
    submitAudit,
    toggleMerchantStatus
  }
}
