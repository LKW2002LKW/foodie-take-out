import { ref, onMounted, onUnmounted } from 'vue'

/**
 * 实时时钟 Composable
 */
export function useTimer() {
  const currentTime = ref('')
  let timerId = null

  const updateTime = () => {
    const now = new Date()
    currentTime.value = now.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false
    })
  }

  onMounted(() => {
    updateTime()
    timerId = setInterval(updateTime, 1000)
  })

  onUnmounted(() => {
    if (timerId) clearInterval(timerId)
  })

  return {
    currentTime
  }
}
