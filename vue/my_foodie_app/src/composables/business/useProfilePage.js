import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { closeToast, showConfirmDialog, showLoadingToast, showSuccessToast, showToast } from 'vant'
import { bindWeChat, getUserProfile, logout } from '@/api/modules/user'
import { useUserStore } from '@/stores/modules/userStore'

// 个人中心首页组合式，负责资料拉取、微信绑定和退出登录。
export const useProfilePage = () => {
  const router = useRouter()
  const userStore = useUserStore()
  const userInfo = computed(() => userStore.userInfo || {})

  const initData = async () => {
    try {
      const res = await getUserProfile()
      if (res.code === 1 || res.code === 200) {
        userStore.setUserInfo(res.data)
      }
    } catch (error) {
      console.error('Fetch profile failed', error)
    }
  }

  const mockWeChatLogin = () => new Promise((resolve) => {
    showLoadingToast({ message: '正在调起微信授权...', forbidClick: true, duration: 0 })
    setTimeout(() => {
      closeToast()
      const randomId = `wx_openid_${Math.random().toString(36).slice(2, 11)}`
      resolve(randomId)
    }, 1500)
  })

  const goProfileInfo = () => {
    router.push('/profile/info')
  }

  const handleBindWeChat = async () => {
    try {
      const openid = await mockWeChatLogin()
      showLoadingToast({ message: '绑定中...', forbidClick: true })

      const res = await bindWeChat({ openid })
      if (res.code === 1 || res.code === 200) {
        showSuccessToast('微信绑定成功')
        await initData()
        return
      }

      showToast(res.msg || '绑定失败')
    } catch (error) {
      console.error(error)
      showToast('操作失败，请重试')
    }
  }

  const handleLogout = async () => {
    try {
      await showConfirmDialog({
        title: '提示',
        message: '确认退出登录吗？',
      })
    } catch (error) {
      return
    }

    try {
      await logout()
    } catch (error) {
      console.error(error)
    } finally {
      userStore.clearToken()
      showSuccessToast('已退出')
      router.replace('/login')
    }
  }

  onMounted(() => {
    initData()
  })

  return {
    goProfileInfo,
    handleBindWeChat,
    handleLogout,
    userInfo,
  }
}
