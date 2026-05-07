import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showFailToast, showSuccessToast, showToast } from 'vant'
import { loginByCode, loginByPassword, loginByWeChat as loginByWechat, sendCode } from '@/api/modules/user'
import { useSmsCountdown } from '@/composables/common/useSmsCountdown'
import { useUserStore } from '@/stores/modules/userStore'

// 登录页组合式，统一管理验证码登录、密码登录和微信登录入口。
export const useLoginPage = () => {
  const router = useRouter()
  const userStore = useUserStore()

  const activeTab = ref(0)
  const form = reactive({
    phone: '',
    code: '',
    password: '',
  })
  const isWechatSubmitting = ref(false)
  const { countdown: countDown, isSending, start } = useSmsCountdown()

  const onTabChange = (value) => {
    activeTab.value = value
  }

  const goRegister = () => {
    router.push('/register')
  }

  const handleLoginSuccess = (res, successText = '登录成功') => {
    if (res.code !== 1 || !res.data?.token) {
      showFailToast(res.msg || '登录失败')
      return false
    }

    userStore.setToken(res.data.token)
    userStore.setUserInfo(res.data)
    showSuccessToast(successText)
    router.replace('/merchant/list')
    return true
  }

  const sendSmsCode = async () => {
    if (isSending.value) return
    if (!/^1[3-9]\d{9}$/.test(form.phone)) {
      showToast('请输入正确的手机号')
      return
    }

    try {
      const res = await sendCode(form.phone)
      if (res.code === 1) {
        showSuccessToast('验证码已发送')
        start()
        return
      }

      showFailToast(res.msg || '发送失败')
    } catch (error) {
      console.error('Send sms code error:', error)
      showFailToast('发送失败，请稍后再试')
    }
  }

  const onSubmit = async () => {
    try {
      const res = activeTab.value === 0
        ? await loginByCode(form.phone, form.code)
        : await loginByPassword(form.phone, form.password)

      handleLoginSuccess(res)
    } catch (error) {
      console.error('Login error:', error)
    }
  }

  const onWechatLogin = async () => {
    if (isWechatSubmitting.value) return

    try {
      isWechatSubmitting.value = true
      const mockCode = `mock_code_${Date.now()}`
      const res = await loginByWechat(mockCode)
      handleLoginSuccess(res, '微信登录成功')
    } catch (error) {
      console.error('Wechat login error:', error)
      showFailToast('微信登录失败，请稍后再试')
    } finally {
      isWechatSubmitting.value = false
    }
  }

  return {
    activeTab,
    countDown,
    form,
    goRegister,
    isSending,
    isWechatSubmitting,
    onSubmit,
    onTabChange,
    onWechatLogin,
    sendSmsCode,
  }
}
