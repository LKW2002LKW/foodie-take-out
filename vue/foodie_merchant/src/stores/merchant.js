import { defineStore } from 'pinia'

export const useMerchantStore = defineStore('merchant', {
  state: () => ({
    merchantInfo: {
      name: '美味汉堡店 (演示商户)',
      id: 'MERCH_001',
      avatar: ''
    },
    isLoggedIn: true
  }),
  actions: {
    logout() {
      this.isLoggedIn = false
      // In real app, clear token etc.
    },
    updateInfo(info) {
      this.merchantInfo = { ...this.merchantInfo, ...info }
    }
  }
})
