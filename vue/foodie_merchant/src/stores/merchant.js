import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/**
 * 商户信息 Store
 */
export const useMerchantStore = defineStore('merchant', () => {
  // 1. 状态 (State)
  const token = ref(localStorage.getItem('merchant_token') || '')
  const merchantInfo = ref(JSON.parse(localStorage.getItem('merchant_info') || '{}'))

  // 2. 计算属性 (Getters)
  const isLoggedIn = computed(() => !!token.value)
  const merchantName = computed(() => merchantInfo.value.merchantName || '演示商户')

  // 3. 动作 (Actions)
  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('merchant_token', newToken)
  }

  const setMerchantInfo = (info) => {
    merchantInfo.value = info
    localStorage.setItem('merchant_info', JSON.stringify(info))
  }

  const clearAuth = () => {
    token.value = ''
    merchantInfo.value = {}
    localStorage.removeItem('merchant_token')
    localStorage.removeItem('merchant_info')
  }

  const updateMerchantInfo = (info) => {
    merchantInfo.value = { ...merchantInfo.value, ...info }
    localStorage.setItem('merchant_info', JSON.stringify(merchantInfo.value))
  }

  return {
    token,
    merchantInfo,
    isLoggedIn,
    merchantName,
    setToken,
    setMerchantInfo,
    clearAuth,
    updateMerchantInfo
  }
})
