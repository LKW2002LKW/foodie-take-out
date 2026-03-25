import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useLocationStore = defineStore('location', () => {
  const address = ref('定位中...')
  const longitude = ref(null)
  const latitude = ref(null)
  const isLocated = ref(false)

  const setLocation = (data) => {
    address.value = data.address || '未知位置'
    longitude.value = data.longitude
    latitude.value = data.latitude
    isLocated.value = true
  }

  return {
    address,
    longitude,
    latitude,
    isLocated,
    setLocation
  }
})
