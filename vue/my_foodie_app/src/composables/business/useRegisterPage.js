import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { showFailToast, showSuccessToast, showToast } from 'vant'
import { register, sendCode } from '@/api/modules/user'
import { useSmsCountdown } from '@/composables/common/useSmsCountdown'
import { useUserStore } from '@/stores/modules/userStore'

// 注册页组合式，负责短信验证码发送、注册提交与注册后登录态落库。
export const useRegisterPage = () => {
  const router = useRouter()
  const userStore = useUserStore()

  const form = reactive({
    phone: '',
    code: '',
    password: '',
    nickname: '',
  })
  const { countdown: countDown, isSending, start } = useSmsCountdown()

  const goBack = () => {
    router.back()
  }

  const sendSmsCode = async () => {
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
      console.error(error)
      showFailToast('发送失败')
    }
  }

  const onRegister = async () => {
    try {
      const res = await register(form.phone, form.code, form.password, form.nickname)
      if (res.code === 1) {
        showSuccessToast('注册成功')
        if (res.data?.token) {
          userStore.setToken(res.data.token)
          userStore.setUserInfo(res.data)
          router.push('/profile')
          return
        }

        router.push('/login')
        return
      }

      showFailToast(res.msg || '注册失败')
    } catch (error) {
      console.error(error)
      showFailToast('系统错误')
    }
  }

  return {
    countDown,
    form,
    goBack,
    isSending,
    onRegister,
    sendSmsCode,
  }
}
