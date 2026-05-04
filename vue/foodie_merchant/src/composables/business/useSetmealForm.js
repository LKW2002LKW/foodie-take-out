import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import categoryApi from '@/api/modules/category'
import setmealApi from '@/api/modules/setmeal'
import { appEnv } from '@/config/env'

const createInitialForm = () => ({
  id: undefined,
  name: '',
  categoryId: undefined,
  price: undefined,
  image: '',
  description: '',
  status: 1,
  setmealDishes: [],
})

export const useSetmealForm = (props, emit) => {
  const loading = ref(false)
  const formRef = ref(null)
  const categoryList = ref([])

  const uploadUrl = `${appEnv.apiBaseUrl}/merchant/common/upload`
  const headers = computed(() => ({
    Authorization: `Bearer ${localStorage.getItem('merchant_token') || ''}`,
  }))

  const form = reactive(createInitialForm())

  const originTotalPrice = computed(() => {
    if (!form.setmealDishes.length) {
      return '0.00'
    }

    const total = form.setmealDishes.reduce((sum, item) => {
      return sum + (Number(item.price) * Number(item.copies))
    }, 0)
    return total.toFixed(2)
  })

  const rules = {
    name: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
    categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
    price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
    image: [{ required: true, message: '请上传图片', trigger: 'change' }],
    setmealDishes: [
      { required: true, message: '请添加至少一个菜品', trigger: 'change' },
      {
        validator: (_, value, callback) => {
          if (!value || !value.length) {
            callback(new Error('请添加至少一个菜品'))
            return
          }
          callback()
        },
        trigger: 'change',
      },
    ],
  }

  const title = computed(() => (props.isEdit ? '编辑套餐' : '新增套餐'))

  const resetForm = () => {
    Object.assign(form, createInitialForm())
    formRef.value?.clearValidate()
  }

  const fetchCategoryList = async () => {
    try {
      const res = await categoryApi.getCategoryList(2)
      categoryList.value = res.data || []
    } catch (error) {
      console.error(error)
    }
  }

  const fetchDetail = async (id) => {
    loading.value = true
    try {
      const res = await setmealApi.getSetmealById(id)
      if (res.data) {
        Object.assign(form, res.data)
      }
    } catch (error) {
      console.error(error)
      ElMessage.error('加载失败')
    } finally {
      loading.value = false
    }
  }

  const handleAvatarSuccess = (res) => {
    if (res.code === 1 || res.code === 200) {
      form.image = res.data
      formRef.value?.validateField('image')
      return
    }

    ElMessage.error(res.msg || '上传失败')
  }

  const beforeAvatarUpload = (file) => {
    const isImage = file.type === 'image/jpeg' || file.type === 'image/png'
    const isLt2M = file.size / 1024 / 1024 < 2

    if (!isImage) {
      ElMessage.error('格式错误')
    }
    if (!isLt2M) {
      ElMessage.error('大小超过2MB')
    }

    return isImage && isLt2M
  }

  const handleClose = () => {
    emit('update:visible', false)
  }

  const submitForm = async () => {
    if (!formRef.value) return

    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) return

    loading.value = true
    try {
      const payload = { ...form }
      if (props.isEdit) {
        await setmealApi.updateSetmeal(payload)
        ElMessage.success('更新成功')
      } else {
        await setmealApi.addSetmeal(payload)
        ElMessage.success('新增成功')
      }

      emit('success')
      handleClose()
    } catch (error) {
      console.error(error)
    } finally {
      loading.value = false
    }
  }

  watch(
    () => props.visible,
    async (visible) => {
      if (!visible) return

      await fetchCategoryList()
      if (props.isEdit && props.setmealId) {
        await fetchDetail(props.setmealId)
        return
      }

      resetForm()
    },
  )

  return {
    beforeAvatarUpload,
    categoryList,
    form,
    formRef,
    handleAvatarSuccess,
    handleClose,
    headers,
    loading,
    originTotalPrice,
    rules,
    submitForm,
    title,
    uploadUrl,
  }
}
