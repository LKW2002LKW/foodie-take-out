import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import authApi from '@/api/modules/auth'
import { routerConfig } from '@/config/router'
import { usePlatformStore } from '@/stores/modules/platformStore'

export const useLoginPage = () => {
  const router = useRouter()
  const platformStore = usePlatformStore()
  const formRef = ref(null)
  const loading = ref(false)
  const remember = ref(true)

  const form = reactive({
    username: '',
    password: '',
  })

  const rules = {
    username: [{ required: true, message: '请提供管理员授权账号', trigger: 'blur' }],
    password: [{ required: true, message: '请输入访问凭据', trigger: 'blur' }],
  }

  const onLogin = async () => {
    if (!formRef.value) return

    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) return

    loading.value = true
    try {
      const { data, code, msg } = await authApi.login(form)
      if (code === 1) {
        platformStore.setToken(data.token)
        platformStore.setUserInfo(data)
        ElMessage.success({ message: '授权成功，欢迎回来', duration: 1500 })
        router.push(routerConfig.homePath)
      } else {
        ElMessage.error(msg || '授权验证失败')
      }
    } catch (error) {
      console.error('Login error:', error)
    } finally {
      loading.value = false
    }
  }

  return {
    form,
    formRef,
    loading,
    onLogin,
    remember,
    rules,
  }
}
