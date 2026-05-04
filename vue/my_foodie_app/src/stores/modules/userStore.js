import { defineStore } from 'pinia'
import { ref } from 'vue'
import { TOKEN_STORAGE_KEY } from '@/constants/storageKeys'
import { getLocal, removeLocal, setLocal } from '@/utils/common/storage'

export const useUserStore = defineStore('user', () => {
  const token = ref(getLocal(TOKEN_STORAGE_KEY, ''))
  const userInfo = ref({})

  function setToken(newToken) {
    token.value = newToken
    setLocal(TOKEN_STORAGE_KEY, newToken)
  }

  function clearToken() {
    token.value = ''
    userInfo.value = {}
    removeLocal(TOKEN_STORAGE_KEY)
  }

  function setUserInfo(info) {
    userInfo.value = info
  }

  return { token, userInfo, setToken, clearToken, setUserInfo }
})
