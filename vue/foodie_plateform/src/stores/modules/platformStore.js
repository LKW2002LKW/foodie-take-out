import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { PLATFORM_TOKEN_KEY, PLATFORM_USER_KEY } from '@/constants/storageKeys'

const getInitialUser = () => {
  try {
    return JSON.parse(localStorage.getItem(PLATFORM_USER_KEY) || '{}')
  } catch (error) {
    return {}
  }
}

// 平台登录态仓库，统一维护令牌、管理员信息与本地持久化。
export const usePlatformStore = defineStore('platform', () => {
  const token = ref(localStorage.getItem(PLATFORM_TOKEN_KEY) || '')
  const userInfo = ref(getInitialUser())

  const isLoggedIn = computed(() => Boolean(token.value))

  const setToken = (value) => {
    token.value = value || ''
    if (token.value) {
      localStorage.setItem(PLATFORM_TOKEN_KEY, token.value)
      return
    }
    localStorage.removeItem(PLATFORM_TOKEN_KEY)
  }

  const setUserInfo = (value) => {
    userInfo.value = value || {}
    localStorage.setItem(PLATFORM_USER_KEY, JSON.stringify(userInfo.value))
  }

  const clearAuth = () => {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem(PLATFORM_TOKEN_KEY)
    localStorage.removeItem(PLATFORM_USER_KEY)
  }

  return {
    clearAuth,
    isLoggedIn,
    setToken,
    setUserInfo,
    token,
    userInfo,
  }
})
