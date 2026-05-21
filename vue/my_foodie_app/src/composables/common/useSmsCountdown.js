import { onBeforeUnmount, ref } from 'vue'

// 短信验证码倒计时逻辑抽离为通用组合式，便于登录、注册、绑手机共用。
export const useSmsCountdown = (initialSeconds = 60) => {
  const isSending = ref(false)
  const countdown = ref(initialSeconds)

  let timer = null

  const clear = () => {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
  }

  const start = () => {
    clear()
    isSending.value = true
    countdown.value = initialSeconds

    timer = setInterval(() => {
      countdown.value -= 1
      if (countdown.value <= 0) {
        clear()
        isSending.value = false
        countdown.value = initialSeconds
      }
    }, 1000)
  }

  onBeforeUnmount(() => {
    clear()
  })

  return {
    clear,
    countdown,
    isSending,
    start,
  }
}
