import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { MERCHANT_INFO_KEY, MERCHANT_TOKEN_KEY } from '@/constants/storageKeys'

export const useMerchantStore = defineStore('merchant', () => {
  const token = ref(localStorage.getItem(MERCHANT_TOKEN_KEY) || '')
  const merchantInfo = ref(JSON.parse(localStorage.getItem(MERCHANT_INFO_KEY) || '{}'))

  const isLoggedIn = computed(() => !!token.value)
  const merchantName = computed(() => (
    merchantInfo.value.merchantName
    || merchantInfo.value.name
    || '演示商户'
  ))

  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem(MERCHANT_TOKEN_KEY, newToken)
  }

  const setMerchantInfo = (info) => {
    merchantInfo.value = info || {}
    localStorage.setItem(MERCHANT_INFO_KEY, JSON.stringify(merchantInfo.value))
  }

  const updateMerchantInfo = (info) => {
    merchantInfo.value = { ...merchantInfo.value, ...(info || {}) }
    localStorage.setItem(MERCHANT_INFO_KEY, JSON.stringify(merchantInfo.value))
  }

  const clearAuth = () => {
    token.value = ''
    merchantInfo.value = {}
    localStorage.removeItem(MERCHANT_TOKEN_KEY)
    localStorage.removeItem(MERCHANT_INFO_KEY)
  }

  return {
    clearAuth,
    isLoggedIn,
    merchantInfo,
    merchantName,
    setMerchantInfo,
    setToken,
    token,
    updateMerchantInfo,
  }
})
