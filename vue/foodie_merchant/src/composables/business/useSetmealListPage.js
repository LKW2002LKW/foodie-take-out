import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import categoryApi from '@/api/modules/category'
import setmealApi from '@/api/modules/setmeal'

const createInitialQueryParams = () => ({
  page: 1,
  pageSize: 10,
  name: '',
  categoryId: undefined,
  status: undefined,
})

// 套餐列表页组合式，负责列表筛选、批量上下架与表单弹窗装配。
export const useSetmealListPage = () => {
  const loading = ref(false)
  const setmealList = ref([])
  const total = ref(0)
  const categoryList = ref([])
  const selection = ref([])
  const formVisible = ref(false)
  const isEdit = ref(false)
  const currentSetmealId = ref(undefined)

  const queryParams = reactive(createInitialQueryParams())

  const fetchCategoryList = async () => {
    try {
      const res = await categoryApi.getCategoryList(2)
      categoryList.value = res.data || []
    } catch (error) {
      console.error(error)
    }
  }

  const fetchSetmealList = async () => {
    loading.value = true
    try {
      const res = await setmealApi.getSetmealPage(queryParams)
      if (res.data) {
        setmealList.value = res.data.records || []
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
    fetchSetmealList()
  }

  const resetQuery = () => {
    Object.assign(queryParams, createInitialQueryParams())
    fetchSetmealList()
  }

  const handleSizeChange = (value) => {
    queryParams.pageSize = value
    fetchSetmealList()
  }

  const handleCurrentChange = (value) => {
    queryParams.page = value
    fetchSetmealList()
  }

  const handleSelectionChange = (value) => {
    selection.value = value
  }

  const handleCreate = () => {
    isEdit.value = false
    currentSetmealId.value = undefined
    formVisible.value = true
  }

  const handleUpdate = (row) => {
    isEdit.value = true
    currentSetmealId.value = row.id
    formVisible.value = true
  }

  const handleFormSuccess = () => {
    fetchSetmealList()
  }

  const handleDelete = (row) => {
    if (row.status === 1) {
      ElMessage.warning('起售中的套餐禁止删除，请先停售！')
      return
    }

    ElMessageBox.confirm(`确认删除套餐 "${row.name}" 吗?`, '警告', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
    }).then(async () => {
      try {
        await setmealApi.deleteSetmeal(row.id)
        ElMessage.success('删除成功')
        fetchSetmealList()
      } catch (error) {
        console.error(error)
      }
    }).catch(() => {})
  }

  const handleBatchDelete = () => {
    if (!selection.value.length) return

    const hasActive = selection.value.some((item) => item.status === 1)
    if (hasActive) {
      ElMessage.warning('选中的套餐中有正在起售的，无法删除！')
      return
    }

    const ids = selection.value.map((item) => item.id).join(',')
    ElMessageBox.confirm(`确认批量删除选中的 ${selection.value.length} 个套餐吗?`, '警告', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
    }).then(async () => {
      try {
        await setmealApi.deleteSetmealBatch(ids)
        ElMessage.success('批量删除成功')
        fetchSetmealList()
      } catch (error) {
        console.error(error)
      }
    }).catch(() => {})
  }

  const handleStatusChange = (row) => {
    const targetStatus = row.status === 1 ? 0 : 1
    const actionText = targetStatus === 1 ? '起售' : '停售'

    ElMessageBox.confirm(`确认${actionText}套餐 "${row.name}" 吗?`, '提示', {
      type: targetStatus === 1 ? 'success' : 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
    }).then(async () => {
      try {
        await setmealApi.updateSetmealStatus(row.id, targetStatus)
        ElMessage.success(`${actionText}成功`)
        fetchSetmealList()
      } catch (error) {
        console.error(error)
      }
    }).catch(() => {})
  }

  onMounted(() => {
    fetchCategoryList()
    fetchSetmealList()
  })

  return {
    categoryList,
    currentSetmealId,
    formVisible,
    handleBatchDelete,
    handleCreate,
    handleCurrentChange,
    handleDelete,
    handleFormSuccess,
    handleQuery,
    handleSelectionChange,
    handleSizeChange,
    handleStatusChange,
    handleUpdate,
    isEdit,
    loading,
    queryParams,
    resetQuery,
    selection,
    setmealList,
    total,
  }
}
