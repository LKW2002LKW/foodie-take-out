import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import categoryApi from '@/api/category'

/**
 * 分类管理业务逻辑 Composable
 */
export function useCategory() {
  const loading = ref(false)
  const total = ref(0)
  const categoryList = ref([])
  
  // 1. 查询参数
  const queryParams = reactive({
    page: 1,
    pageSize: 10,
    name: '',
    type: ''
  })

  // 2. 表单弹窗状态
  const dialogVisible = ref(false)
  const isEdit = ref(false)
  const submitLoading = ref(false)
  const formRef = ref(null)
  
  const form = reactive({
    id: undefined,
    name: '',
    type: 1,
    sort: 0
  })

  // 3. 校验规则
  const rules = {
    name: [
      { required: true, message: '请输入分类名称', trigger: 'blur' },
      { min: 1, max: 20, message: '长度在 1 到 20 个字符', trigger: 'blur' }
    ],
    type: [{ required: true, message: '请选择分类类型', trigger: 'change' }],
    sort: [{ required: true, message: '请输入排序值', trigger: 'blur' }]
  }

  /**
   * 获取分页列表
   */
  const fetchList = async () => {
    loading.value = true
    try {
      const { data } = await categoryApi.getCategoryPage(queryParams)
      if (data) {
        categoryList.value = data.records || []
        total.value = data.total || 0
      }
    } catch (error) {
      console.error('Fetch category list error:', error)
    } finally {
      loading.value = false
    }
  }

  /**
   * 处理查询
   */
  const handleQuery = () => {
    queryParams.page = 1
    fetchList()
  }

  /**
   * 重置查询
   */
  const resetQuery = () => {
    queryParams.name = ''
    queryParams.type = ''
    handleQuery()
  }

  /**
   * 重置表单
   */
  const resetForm = () => {
    form.id = undefined
    form.name = ''
    form.type = 1
    form.sort = 0
    formRef.value?.resetFields()
  }

  /**
   * 打开新增弹窗
   */
  const handleCreate = () => {
    resetForm()
    isEdit.value = false
    dialogVisible.value = true
  }

  /**
   * 打开编辑弹窗
   */
  const handleUpdate = async (row) => {
    resetForm()
    isEdit.value = true
    try {
      const { data } = await categoryApi.getCategoryById(row.id)
      if (data) {
        Object.assign(form, data)
        dialogVisible.value = true
      }
    } catch (error) {
      ElMessage.error('获取分类详情失败')
    }
  }

  /**
   * 提交表单
   */
  const submitForm = async () => {
    if (!formRef.value) return
    
    await formRef.value.validate(async (valid) => {
      if (valid) {
        submitLoading.value = true
        try {
          if (isEdit.value) {
            await categoryApi.updateCategory(form)
            ElMessage.success('修改成功')
          } else {
            await categoryApi.addCategory(form)
            ElMessage.success('新增成功')
          }
          dialogVisible.value = false
          fetchList()
        } catch (error) {
          console.error('Submit category error:', error)
        } finally {
          submitLoading.value = false
        }
      }
    })
  }

  /**
   * 删除分类
   */
  const handleDelete = (row) => {
    ElMessageBox.confirm(
      `确认删除分类 "${row.name}" 吗？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(async () => {
      try {
        await categoryApi.deleteCategory(row.id)
        ElMessage.success('删除成功')
        fetchList()
      } catch (error) {
        console.error('Delete category error:', error)
      }
    }).catch(() => {})
  }

  /**
   * 更新状态
   */
  const handleStatusChange = async (row) => {
    const originalStatus = row.status === 1 ? 0 : 1
    try {
      await categoryApi.updateCategoryStatus(row.id, row.status)
      ElMessage.success('状态更新成功')
    } catch (error) {
      row.status = originalStatus
    }
  }

  return {
    loading,
    total,
    categoryList,
    queryParams,
    dialogVisible,
    isEdit,
    submitLoading,
    form,
    formRef,
    rules,
    fetchList,
    handleQuery,
    resetQuery,
    handleCreate,
    handleUpdate,
    submitForm,
    handleDelete,
    handleStatusChange
  }
}
