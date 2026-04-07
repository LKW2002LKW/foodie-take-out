import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dishApi from '@/api/dish'
import categoryApi from '@/api/category'

/**
 * 菜品管理业务逻辑 Composable
 */
export function useProduct() {
  const loading = ref(false)
  const dishList = ref([])
  const total = ref(0)
  const categoryList = ref([])
  const selection = ref([])

  // 1. 查询参数
  const queryParams = reactive({
    page: 1,
    pageSize: 10,
    name: '',
    categoryId: undefined,
    status: undefined
  })

  // 2. 表单弹窗状态
  const formVisible = ref(false)
  const isEdit = ref(false)
  const currentDishId = ref(undefined)

  /**
   * 获取菜品列表
   */
  const fetchDishList = async () => {
    loading.value = true
    try {
      const { data } = await dishApi.getDishPage(queryParams)
      if (data) {
        dishList.value = data.records || []
        total.value = data.total || 0
      }
    } catch (error) {
      console.error('Fetch dish list error:', error)
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取全部分类列表 (用于筛选下拉)
   */
  const fetchCategoryList = async () => {
    try {
      const { data } = await categoryApi.getCategoryList(1) // 1 表示菜品分类
      categoryList.value = data || []
    } catch (error) {
      console.error('Fetch categories error:', error)
    }
  }

  /**
   * 处理搜索查询
   */
  const handleQuery = () => {
    queryParams.page = 1
    fetchDishList()
  }

  /**
   * 重置查询参数
   */
  const resetQuery = () => {
    queryParams.name = ''
    queryParams.categoryId = undefined
    queryParams.status = undefined
    handleQuery()
  }

  /**
   * 开启新增弹窗
   */
  const handleCreate = () => {
    isEdit.value = false
    currentDishId.value = undefined
    formVisible.value = true
  }

  /**
   * 开启编辑弹窗
   */
  const handleUpdate = (row) => {
    isEdit.value = true
    currentDishId.value = row.id
    formVisible.value = true
  }

  /**
   * 处理单个删除
   */
  const handleDelete = (row) => {
    ElMessageBox.confirm(`确认删除菜品 "${row.name}" 吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '确定删除',
      confirmButtonClass: 'el-button--danger'
    }).then(async () => {
      try {
        await dishApi.deleteDish(row.id)
        ElMessage.success('删除成功')
        fetchDishList()
      } catch (error) {
        console.error('Delete dish error:', error)
      }
    }).catch(() => {})
  }

  /**
   * 处理批量删除
   */
  const handleBatchDelete = () => {
    if (!selection.value.length) return
    const ids = selection.value.map(item => item.id).join(',')
    
    ElMessageBox.confirm(`确认批量删除选中的 ${selection.value.length} 个菜品吗？`, '批量删除', {
      type: 'error',
      confirmButtonText: '全部删除',
      confirmButtonClass: 'el-button--danger'
    }).then(async () => {
      try {
        await dishApi.deleteDishBatch(ids)
        ElMessage.success('批量删除成功')
        fetchDishList()
      } catch (error) {
        console.error('Batch delete error:', error)
      }
    }).catch(() => {})
  }

  /**
   * 更新菜品售卖状态
   */
  const toggleStatus = (row) => {
    const targetStatus = row.status === 1 ? 0 : 1
    const text = targetStatus === 1 ? '上架' : '下架'
    
    ElMessageBox.confirm(`确认${text}菜品 "${row.name}" 吗？`, '状态变更', {
      type: targetStatus === 1 ? 'success' : 'warning'
    }).then(async () => {
      try {
        await dishApi.updateDishStatus(row.id, targetStatus)
        ElMessage.success(`${text}成功`)
        fetchDishList()
      } catch (error) {
        console.error('Update status error:', error)
      }
    }).catch(() => {})
  }

  return {
    loading,
    dishList,
    total,
    categoryList,
    selection,
    queryParams,
    formVisible,
    isEdit,
    currentDishId,
    fetchDishList,
    fetchCategoryList,
    handleQuery,
    resetQuery,
    handleCreate,
    handleUpdate,
    handleDelete,
    handleBatchDelete,
    toggleStatus
  }
}
