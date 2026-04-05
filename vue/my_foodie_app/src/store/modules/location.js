import { defineStore } from 'pinia'
import { ref } from 'vue'

const LOC_STORAGE_KEY = 'mt_last_location'

export const useLocationStore = defineStore('location', () => {
  // 从本地缓存初始化，防止刷新丢失
  const cached = JSON.parse(localStorage.getItem(LOC_STORAGE_KEY) || '{}')

  const address = ref(cached.address || '正在获取位置...')
  const city = ref(cached.city || '定位中')
  const adcode = ref(cached.adcode || null)
  const longitude = ref(cached.longitude || null)
  const latitude = ref(cached.latitude || null)
  const isLocated = ref(!!cached.longitude)
  const isManual = ref(cached.isManual || false)

  const selectedAddress = ref(null)

  // 统一持久化方法
  const saveToLocal = () => {
    localStorage.setItem(LOC_STORAGE_KEY, JSON.stringify({
      address: address.value,
      city: city.value,
      adcode: adcode.value,
      longitude: longitude.value,
      latitude: latitude.value,
      isManual: isManual.value
    }))
  }

  const setLocation = (data) => {
    address.value = data.address || address.value
    city.value = data.city || city.value
    adcode.value = data.adcode || adcode.value
    longitude.value = data.longitude !== undefined ? data.longitude : longitude.value
    latitude.value = data.latitude !== undefined ? data.latitude : latitude.value
    isLocated.value = true
    isManual.value = false
    saveToLocal()
  }

  const setCoordinates = (lng, lat) => {
    longitude.value = lng
    latitude.value = lat
    isLocated.value = true
    saveToLocal()
  }

  const setAddress = (addr) => {
    address.value = addr
    saveToLocal()
  }

  const setCity = (cityName, manual = true) => {
    city.value = cityName
    if (manual) isManual.value = true
    saveToLocal()
  }

  return {
    address, city, adcode, longitude, latitude, isLocated, isManual,
    selectedAddress, setLocation, setCoordinates, setAddress, setCity
  }
})
