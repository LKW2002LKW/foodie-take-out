import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import authApi from '@/api/modules/auth'
import { useMerchantStore } from '@/stores/modules/merchantStore'

/**
 * 认证逻辑 Composable
 */
export function useAuth() {
  const router = useRouter()
  const merchantStore = useMerchantStore()
  const loading = ref(false)

  /**
   * 登录逻辑
   * @param {Object} loginData { username, password }
   */
  const handleLogin = async (loginData) => {
    loading.value = true
    try {
      const { code, msg, data } = await authApi.login(loginData)
      
      if (code === 1) {
        // 保存登录凭证
        if (data.token) {
           merchantStore.setToken(data.token)
           // 存入 merchantInfo (假设后端返回了基本信息，如果没有则后续获取)
           if (data.merchantInfo) {
             merchantStore.setMerchantInfo(data.merchantInfo)
           }
           
           ElMessage.success('登录成功')
           router.push('/')
        } else {
           ElMessage.error('登录失败：Token 不存在')
        }
      } else {
        ElMessage.error(msg || '登录失败')
      }
    } catch (error) {
      console.error('Login error:', error)
      ElMessage.error('系统异常，请稍后再试')
    } finally {
      loading.value = false
    }
  }

  /**
   * 注册逻辑
   * @param {Object} registerData 注册表单数据
   */
  const handleRegister = async (registerData) => {
    loading.value = true
    try {
      const { code, msg } = await authApi.register(registerData)
      
      if (code === 1) {
        ElMessage.success('注册成功，请登录')
        router.push('/login')
        return true
      } else {
        ElMessage.error(msg || '注册失败')
        return false
      }
    } catch (error) {
      console.error('Register error:', error)
      ElMessage.error('注册异常，请检查网络或稍后重试')
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * 退出登录逻辑
   */
  const handleLogout = () => {
    merchantStore.clearAuth()
    ElMessage.info('已退出登录')
    router.push('/login')
  }

  return {
    loading,
    handleLogin,
    handleRegister,
    handleLogout
  }
}
