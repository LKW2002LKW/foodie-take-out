import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import userApi from '@/api/modules/user'

export const useUserPage = () => {
  const loading = ref(false)
  const tableData = ref([])
  const total = ref(0)
  const detailVisible = ref(false)
  const currentUser = ref(null)

  const queryParams = reactive({
    page: 1,
    pageSize: 10,
    phone: '',
    nickname: '',
    status: null,
  })

  const getList = async () => {
    loading.value = true
    try {
      const res = await userApi.getUserPage(queryParams)
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
    queryParams.phone = ''
    queryParams.nickname = ''
    queryParams.status = null
    handleSearch()
  }

  const handleDetail = async (row) => {
    const res = await userApi.getUserDetail(row.id)
    currentUser.value = res.data
    detailVisible.value = true
  }

  const handleToggleStatus = async (row, targetStatus) => {
    const actionText = targetStatus === 1 ? '启用' : '禁用'
    await ElMessageBox.confirm(`确认${actionText}该用户吗？`, '提示', { type: 'warning' })

    if (targetStatus === 1) {
      await userApi.enableUser(row.id)
    } else {
      await userApi.disableUser(row.id)
    }

    ElMessage.success(`${actionText}成功`)
    getList()
  }

  onMounted(() => {
    getList()
  })

  return {
    currentUser,
    detailVisible,
    getList,
    handleDetail,
    handleSearch,
    handleToggleStatus,
    loading,
    queryParams,
    resetQuery,
    tableData,
    total,
  }
}
