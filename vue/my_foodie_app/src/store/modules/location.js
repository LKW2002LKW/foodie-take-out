import { defineStore } from 'pinia'
import { ref } from 'vue'

const LOC_STORAGE_KEY = 'mt_last_location'

export const useLocationStore = defineStore('location', () => {
  // 从本地缓存初始化，防止刷新丢失
  const cached = JSON.parse(localStorage.getItem(LOC_STORAGE_KEY) || '{}')

  const address = ref(cached.address || cached.locatedAddress || '正在获取位置...')
  const city = ref(cached.city || cached.locatedCity || '定位中')
  const locatedAddress = ref(cached.locatedAddress || cached.address || '正在获取位置...')
  const locatedCity = ref(cached.locatedCity || cached.city || '定位中')
  const adcode = ref(cached.adcode || null)
  const longitude = ref(cached.longitude || null)
  const latitude = ref(cached.latitude || null)
  const locatedLongitude = ref(cached.locatedLongitude || cached.longitude || null)
  const locatedLatitude = ref(cached.locatedLatitude || cached.latitude || null)
  const isLocated = ref(!!(cached.locatedLongitude || cached.longitude))
  const isManual = ref(cached.isManual || false)
  const manualMode = ref(cached.manualMode || '')

  const selectedAddress = ref(null)

  // 统一持久化方法
  const saveToLocal = () => {
    localStorage.setItem(LOC_STORAGE_KEY, JSON.stringify({
      address: address.value,
      city: city.value,
      locatedAddress: locatedAddress.value,
      locatedCity: locatedCity.value,
      adcode: adcode.value,
      longitude: longitude.value,
      latitude: latitude.value,
      locatedLongitude: locatedLongitude.value,
      locatedLatitude: locatedLatitude.value,
      isManual: isManual.value,
      manualMode: manualMode.value
    }))
  }

  const setLocation = (data) => {
    locatedAddress.value = data.address || locatedAddress.value
    locatedCity.value = data.city || locatedCity.value
    locatedLongitude.value = data.longitude !== undefined ? data.longitude : locatedLongitude.value
    locatedLatitude.value = data.latitude !== undefined ? data.latitude : locatedLatitude.value

    // 非“手动选城市”模式下，首页/当前地址直接跟随最新定位详情
    if (!isManual.value || manualMode.value !== 'city') {
      address.value = data.address || address.value
      city.value = data.city || city.value
      longitude.value = data.longitude !== undefined ? data.longitude : longitude.value
      latitude.value = data.latitude !== undefined ? data.latitude : latitude.value
      isManual.value = false
      manualMode.value = ''
    }

    adcode.value = data.adcode || adcode.value
    isLocated.value = true
    saveToLocal()
  }

  const setCoordinates = (lng, lat) => {
    longitude.value = lng
    latitude.value = lat
    isLocated.value = true
    saveToLocal()
  }

  const setAddress = (addr, manual = true, mode = 'address') => {
    address.value = addr
    if (manual) {
      isManual.value = true
      manualMode.value = mode
    }
    saveToLocal()
  }

  const setCity = (cityName, manual = true, mode = 'city') => {
    if (manual) {
      city.value = cityName
      isManual.value = true
      manualMode.value = mode
    } else {
      if (!isManual.value || manualMode.value !== 'city') {
        city.value = cityName
        manualMode.value = ''
      }
    }
    saveToLocal()
  }

  const setLocatedContext = (addressText, cityText) => {
    if (addressText) {
      locatedAddress.value = addressText
      if (!isManual.value || manualMode.value !== 'city') {
        address.value = addressText
      }
    }
    if (cityText) {
      locatedCity.value = cityText
      if (!isManual.value || manualMode.value !== 'city') {
        city.value = cityText
      }
    }
    saveToLocal()
  }

  const useLocatedLocation = () => {
    address.value = locatedAddress.value
    city.value = locatedCity.value
    longitude.value = locatedLongitude.value
    latitude.value = locatedLatitude.value
    isManual.value = false
    manualMode.value = ''
    saveToLocal()
  }

  return {
    address, city, locatedAddress, locatedCity, adcode, longitude, latitude, locatedLongitude, locatedLatitude, isLocated, isManual, manualMode,
    selectedAddress, setLocation, setCoordinates, setAddress, setCity, setLocatedContext, useLocatedLocation
  }
})
