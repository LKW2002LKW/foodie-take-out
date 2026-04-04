import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useLocationStore = defineStore('location', () => {
  const address = ref('定位中...')
  const city = ref('正在定位')
  const adcode = ref(null)
  const longitude = ref(null)
  const latitude = ref(null)
  const isLocated = ref(false)
  const isManual = ref(false) // 【新增】标记是否为手动选择模式

  const selectedAddress = ref(null)

  const setLocation = (data) => {
    address.value = data.address || address.value
    city.value = data.city || city.value
    adcode.value = data.adcode || adcode.value
    longitude.value = data.longitude !== undefined ? data.longitude : longitude.value
    latitude.value = data.latitude !== undefined ? data.latitude : latitude.value
    isLocated.value = true
    isManual.value = false // 自动定位会重置手动标记
  }

  const setCoordinates = (lng, lat) => {
    longitude.value = lng
    latitude.value = lat
    isLocated.value = true
  }

  const setAddress = (addr) => {
    address.value = addr
  }

  const setCity = (cityName, manual = true) => {
    city.value = cityName
    if (manual) isManual.value = true // 如果是手动选城，激活标记
  }

  return {
    address,
    city,
    adcode,
    longitude,
    latitude,
    isLocated,
    isManual,
    selectedAddress,
    setLocation,
    setCoordinates,
    setAddress,
    setCity
  }
})
