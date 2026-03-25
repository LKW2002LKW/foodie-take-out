import { defineStore } from 'pinia'

export const useAddressStore = defineStore('address', {
  state: () => ({
    addressList: [],
    selectedAddressId: null
  }),
  actions: {
    setAddressList(list) {
      this.addressList = list
    },
    selectAddress(id) {
      this.selectedAddressId = id
    }
  }
})
