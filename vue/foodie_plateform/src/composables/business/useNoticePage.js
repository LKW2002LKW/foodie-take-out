import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import noticeApi from '@/api/modules/notice'

export const useNoticePage = () => {
  const loading = ref(false)
  const tableData = ref([])
  const total = ref(0)
  const dialogVisible = ref(false)
  const dialogTitle = ref('发布公告')
  const formRef = ref(null)

  const queryParams = reactive({
    page: 1,
    pageSize: 10,
    status: null,
  })

  const form = reactive({
    title: '',
    content: '',
    type: 1,
    target: 3,
  })

  const rules = {
    title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
    content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
  }

  const getList = async () => {
    loading.value = true
    try {
      const res = await noticeApi.getNoticePage(queryParams)
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
    queryParams.status = null
    handleSearch()
  }

  const handleDelete = async (row) => {
    await noticeApi.deleteNotice(row.id)
    ElMessage.success('删除成功')
    getList()
  }

  const handleOpenDialog = () => {
    dialogTitle.value = '发布公告'
    form.title = ''
    form.content = ''
    form.type = 1
    form.target = 3
    dialogVisible.value = true
  }

  const runSave = async (runner, message) => {
    if (!formRef.value) return
    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) return
    await runner({ ...form })
    ElMessage.success(message)
    dialogVisible.value = false
    getList()
  }

  const handlePublish = () => runSave(noticeApi.publishNotice, '发布成功')
  const handleSaveDraft = () => runSave(noticeApi.saveNoticeDraft, '已保存为草稿')

  const getTargetText = (target) => ({ 1: '商户', 2: '用户', 3: '全部' }[target] || '未知')

  onMounted(() => {
    getList()
  })

  return {
    dialogTitle,
    dialogVisible,
    form,
    formRef,
    getList,
    getTargetText,
    handleDelete,
    handleOpenDialog,
    handlePublish,
    handleSaveDraft,
    handleSearch,
    loading,
    queryParams,
    resetQuery,
    rules,
    tableData,
    total,
  }
}
