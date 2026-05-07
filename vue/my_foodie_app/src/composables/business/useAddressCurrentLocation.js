import { computed, ref } from 'vue'
import { showToast } from 'vant'
import { geocode, ipLocation, reverseGeocode } from '@/utils/location/amapLoader'
import { getCurrentLocation } from '@/utils/location.js'

const getRegeoCity = (regeo) => regeo?.addressComponent?.city || regeo?.addressComponent?.province || ''
const getRegeoAdcode = (regeo) => regeo?.addressComponent?.adcode || ''

// 负责地址列表页的当前定位、定位回退与手动城市模式判断。
export const useAddressCurrentLocation = ({ locationStore, onBack, showCityPicker }) => {
  const locating = ref(false)
  const isManualCityMode = computed(() => locationStore.isManual && locationStore.manualMode === 'city')

  const initSmartLocation = async (force = false, allowIpFallback = !force) => {
    if (!force && locationStore.isManual) return

    locating.value = true
    try {
      const gpsPromise = getCurrentLocation()
      const timeoutPromise = new Promise((_, reject) => setTimeout(() => reject('timeout'), 6000))
      const loc = await Promise.race([gpsPromise, timeoutPromise])
      const isFake = Math.abs(loc.lng - 104.19) < 0.1 && Math.abs(loc.lat - 35.86) < 0.1
      if (isFake) throw new Error('fake')

      const info = await reverseGeocode(loc.lng, loc.lat)
      if (info) {
        locationStore.setLocation({
          longitude: loc.lng,
          latitude: loc.lat,
          address: info.formatted_address,
          city: getRegeoCity(info),
          adcode: getRegeoAdcode(info),
        })
        return true
      }
    } catch (error) {
      console.warn('GPS location attempt finished with error or timeout:', error)
      if (allowIpFallback && !locationStore.locatedLongitude) {
        const ipInfo = await ipLocation()
        if (ipInfo) {
          const geo = await geocode(ipInfo.city)
          locationStore.setLocation({
            longitude: geo?.lng,
            latitude: geo?.lat,
            address: ipInfo.city,
            city: ipInfo.city,
            adcode: ipInfo.adcode,
          })
          return true
        }
      }
      return false
    } finally {
      locating.value = false
    }
  }

  const switchToCurrentLoc = () => locationStore.useLocatedLocation()

  const onForceRelocate = () => initSmartLocation(true, false)

  const onRelocateAction = async () => {
    if (isManualCityMode.value) {
      const relocated = await onForceRelocate()
      if (relocated) {
        switchToCurrentLoc()
      } else {
        showToast('当前定位失败，请检查定位权限')
      }
      return
    }

    await onForceRelocate()
  }

  const useCurrentLoc = () => {
    switchToCurrentLoc()
    onBack()
  }

  const onCitySelect = async (city) => {
    locationStore.setCity(city, true, 'city')
    const geo = await geocode(city)
    if (geo) {
      locationStore.setCoordinates(geo.lng, geo.lat)
    }
    showCityPicker.value = false
  }

  return {
    initSmartLocation,
    isManualCityMode,
    locating,
    onCitySelect,
    onRelocateAction,
    useCurrentLoc,
  }
}
