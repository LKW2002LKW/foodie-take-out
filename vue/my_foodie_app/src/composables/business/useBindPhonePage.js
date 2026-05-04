import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showLoadingToast, showSuccessToast, showToast } from 'vant'
import { useSmsCountdown } from '@/composables/common/useSmsCountdown'
import { bindPhone, sendBindPhoneCode } from '@/api/modules/user'

export const useBindPhonePage = () => {
  const router = useRouter()

  const form = ref({
    phone: '',
    code: '',
    password: '',
  })
  const { countdown, isSending, start } = useSmsCountdown()

  const onClickLeft = () => {
    router.back()
  }

  const sendCode = async () => {
    if (!form.value.phone) {
      showToast('请输入手机号')
      return
    }

    if (!/^1[3-9]\d{9}$/.test(form.value.phone)) {
      showToast('手机号格式不正确')
      return
    }

    try {
      const res = await sendBindPhoneCode(form.value.phone)
      if (res.code === 1 || res.code === 200) {
        showSuccessToast('验证码发送成功')
        start()
        return
      }

      showToast(res.msg || '发送失败')
    } catch (error) {
      showToast('发送失败')
    }
  }

  const onSubmit = async () => {
    showLoadingToast({ message: '绑定中...', forbidClick: true })
    try {
      const res = await bindPhone(form.value)
      if (res.code === 1 || res.code === 200) {
        showSuccessToast('绑定成功')
        router.back()
        return
      }

      showToast(res.msg || '绑定失败')
    } catch (error) {
      showToast('绑定失败')
    }
  }

  return {
    countdown,
    form,
    isSending,
    onClickLeft,
    onSubmit,
    sendCode,
  }
}
