import { reactive, ref } from 'vue'
import { Check, Lock, User } from '@element-plus/icons-vue'
import authApi from '@/api/modules/auth'
import { useMerchantStore } from '@/stores/modules/merchantStore'

export const loginFeatureItems = ['实时订单管理', '智能化经营分析', '多维度财务报表', '全链路营销支持']

export const loginFormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, message: '用户名长度不能小于3个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6个字符', trigger: 'blur' },
  ],
}

export const loginIconMap = {
  Check,
  Lock,
  User,
}

// 商家登录页组合式，统一处理表单校验、登录提交与登录态落库。
export const useLoginPage = ({ router, message }) => {
  const merchantStore = useMerchantStore()

  const formRef = ref(null)
  const rememberMe = ref(false)
  const loading = ref(false)
  const form = reactive({
    username: '',
    password: '',
  })

  const handleLogoError = (event) => {
    event.target.style.display = 'none'
  }

  const submitLogin = async () => {
    if (!formRef.value) return

    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) return

    loading.value = true
    try {
      const { code, msg, data } = await authApi.login(form)
      if (code !== 1) {
        message.error(msg || '登录失败')
        return
      }

      if (!data?.token) {
        message.error('登录失败：Token 不存在')
        return
      }

      merchantStore.setToken(data.token)
      if (data.merchantInfo) {
        merchantStore.setMerchantInfo(data.merchantInfo)
      }

      message.success('登录成功')
      router.push('/')
    } catch (error) {
      console.error('Login error:', error)
      message.error('系统异常，请稍后再试')
    } finally {
      loading.value = false
    }
  }

  return {
    features: loginFeatureItems,
    form,
    formRef,
    handleLogoError,
    loading,
    rememberMe,
    rules: loginFormRules,
    submitLogin,
  }
}
