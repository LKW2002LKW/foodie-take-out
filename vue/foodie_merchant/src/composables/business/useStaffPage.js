import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import employeeApi from '@/api/modules/employee'

const createInitialQueryParams = () => ({
  page: 1,
  pageSize: 10,
  keyword: '',
  role: undefined,
  status: undefined,
})

const createInitialForm = () => ({
  id: undefined,
  username: '',
  password: '',
  name: '',
  phone: '',
  idCard: '',
  role: 3,
  status: 1,
})

const createInitialPasswordForm = () => ({
  id: undefined,
  newPassword: '',
})

export const useStaffPage = () => {
  const loading = ref(false)
  const employeeList = ref([])
  const total = ref(0)
  const submitting = ref(false)
  const pwdSubmitting = ref(false)
  const formRef = ref(null)
  const pwdFormRef = ref(null)

  const queryParams = reactive(createInitialQueryParams())
  const dialog = reactive({
    visible: false,
    title: '新增员工',
  })
  const form = reactive(createInitialForm())
  const pwdDialog = reactive({
    visible: false,
  })
  const pwdForm = reactive(createInitialPasswordForm())

  const rules = {
    username: [
      { required: true, message: '请输入用户名', trigger: 'blur' },
      { min: 4, max: 20, message: '长度在 4 到 20 个字符', trigger: 'blur' },
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
    ],
    name: [
      { required: true, message: '请输入姓名', trigger: 'blur' },
    ],
    phone: [
      { required: true, message: '请输入手机号', trigger: 'blur' },
      { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' },
    ],
    role: [
      { required: true, message: '请选择角色', trigger: 'change' },
    ],
  }

  const pwdRules = {
    newPassword: [
      { required: true, message: '请输入新密码', trigger: 'blur' },
      { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
    ],
  }

  const getList = async () => {
    loading.value = true
    try {
      const res = await employeeApi.getEmployeePage(queryParams)
      if (res && res.data) {
        employeeList.value = res.data.list || []
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

  const getRoleTagType = (role) => {
    switch (role) {
      case 1:
        return 'danger'
      case 2:
        return 'warning'
      case 3:
        return 'info'
      default:
        return ''
    }
  }

  const resetForm = () => {
    Object.assign(form, createInitialForm())
    formRef.value?.clearValidate()
  }

  const handleAdd = () => {
    dialog.title = '新增员工'
    dialog.visible = true
    resetForm()
  }

  const handleEdit = async (row) => {
    dialog.title = '编辑员工'
    dialog.visible = true
    resetForm()

    try {
      const res = await employeeApi.getEmployeeDetail(row.id)
      if (res.data) {
        Object.assign(form, res.data)
        form.status = res.data.status
      }
    } catch (error) {
      console.error(error)
      ElMessage.error('获取员工详情失败')
    }
  }

  const handleDialogClose = () => {
    formRef.value?.clearValidate()
  }

  const submitForm = async () => {
    if (!formRef.value) return

    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) return

    submitting.value = true
    try {
      if (form.id) {
        await employeeApi.updateEmployee(form)
        ElMessage.success('更新成功')
      } else {
        await employeeApi.addEmployee(form)
        ElMessage.success('新增成功')
      }

      dialog.visible = false
      getList()
    } catch (error) {
      console.error(error)
    } finally {
      submitting.value = false
    }
  }

  const handleStatusChange = async (row) => {
    const newStatus = row.status === 1 ? 0 : 1
    const actionText = newStatus === 1 ? '启用' : '禁用'

    try {
      await ElMessageBox.confirm(`确认要${actionText}员工"${row.name}"吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })

      await employeeApi.updateStatus(row.id, newStatus)
      ElMessage.success(`${actionText}成功`)
      getList()
    } catch (error) {
      if (error !== 'cancel') {
        console.error(error)
      }
    }
  }

  const handleDelete = async (row) => {
    try {
      await ElMessageBox.confirm(`确认要删除员工"${row.name}"吗? 此操作不可恢复。`, '警告', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'error',
      })

      await employeeApi.deleteEmployee(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      if (error !== 'cancel') {
        console.error(error)
      }
    }
  }

  const handleResetPwd = (row) => {
    pwdDialog.visible = true
    Object.assign(pwdForm, {
      id: row.id,
      newPassword: '',
    })
    pwdFormRef.value?.clearValidate()
  }

  const submitResetPwd = async () => {
    if (!pwdFormRef.value) return

    const valid = await pwdFormRef.value.validate().catch(() => false)
    if (!valid) return

    pwdSubmitting.value = true
    try {
      await employeeApi.resetPassword(pwdForm)
      ElMessage.success('密码重置成功')
      pwdDialog.visible = false
    } catch (error) {
      console.error(error)
    } finally {
      pwdSubmitting.value = false
    }
  }

  onMounted(() => {
    getList()
  })

  return {
    dialog,
    employeeList,
    form,
    formRef,
    getRoleTagType,
    handleAdd,
    handleCurrentChange,
    handleDelete,
    handleDialogClose,
    handleEdit,
    handleQuery,
    handleResetPwd,
    handleSizeChange,
    handleStatusChange,
    loading,
    pwdDialog,
    pwdForm,
    pwdFormRef,
    pwdRules,
    pwdSubmitting,
    queryParams,
    resetQuery,
    rules,
    submitForm,
    submitResetPwd,
    submitting,
    total,
  }
}
