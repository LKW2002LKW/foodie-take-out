import { showToast } from 'vant'
import { getCurrentLocation } from '@/utils/location.js'
import { loadAmapSdk, reverseGeocode } from '@/utils/location/amapLoader'

// 负责地址编辑页地图交互，包括定位、拖拽选点与逆地理解析。
export const useAddressEditMap = ({
  currentCity,
  displayAddress,
  form,
  locationConfirmed,
  locationStore,
  mapLoading,
  mapRef,
  mapResolving,
  searchKey,
}) => {
  let mapInstance = null
  let mapMoveTimer = null
  let ignoreNextMoveEnd = false

  const normalizeCityName = (city, province = '') => {
    if (Array.isArray(city)) {
      return city[0] || province || ''
    }

    return city || province || ''
  }

  const syncCurrentCityFromCoords = async (lng, lat) => {
    if (!lng || !lat) return null

    const regeo = await reverseGeocode(lng, lat)
    const city = normalizeCityName(regeo?.addressComponent?.city, regeo?.addressComponent?.province)
    if (city) {
      locationStore.setLocatedContext('', city)
      currentCity.value = locationStore.city && locationStore.city !== '定位中' ? locationStore.city : city
    }

    return regeo
  }

  const applyRegionInfo = (regeo) => {
    if (!regeo?.addressComponent) return ''

    const comp = regeo.addressComponent
    const cityName = normalizeCityName(comp.city, comp.province)
    const districtCode = String(comp.adcode || '')

    form.provinceName = comp.province || ''
    form.cityName = cityName
    form.districtName = comp.district || ''
    form.districtCode = districtCode
    form.provinceCode = districtCode ? `${districtCode.substring(0, 2)}0000` : ''
    form.cityCode = districtCode ? `${districtCode.substring(0, 4)}00` : ''

    return cityName
  }

  const setMapCenter = (lng, lat, zoom = 16) => {
    if (!mapInstance || !lng || !lat) return

    ignoreNextMoveEnd = true
    mapInstance.setZoomAndCenter(zoom, [Number(lng), Number(lat)])
    window.setTimeout(() => {
      ignoreNextMoveEnd = false
    }, 260)
  }

  const syncAddressByCoords = async (lng, lat, { updateLocationStore = false, moveMap = false } = {}) => {
    if (!lng || !lat) return false

    mapResolving.value = true

    try {
      const regeo = await reverseGeocode(lng, lat)
      if (!regeo) return false

      form.longitude = Number(lng)
      form.latitude = Number(lat)

      const cityName = applyRegionInfo(regeo)
      const addressText = regeo.formatted_address || displayAddress.value || ''
      displayAddress.value = addressText
      searchKey.value = addressText
      locationConfirmed.value = true

      if (!locationStore.isManual || locationStore.manualMode !== 'city') {
        currentCity.value = cityName || currentCity.value
      }

      if (updateLocationStore) {
        locationStore.setLocation({
          longitude: Number(lng),
          latitude: Number(lat),
          address: addressText,
          city: cityName,
          adcode: regeo.addressComponent?.adcode || '',
        })
      }

      if (moveMap) {
        setMapCenter(lng, lat)
      }

      return true
    } finally {
      mapResolving.value = false
    }
  }

  const handleMapMoveEnd = () => {
    if (!mapInstance || ignoreNextMoveEnd) return

    const center = mapInstance.getCenter()
    clearTimeout(mapMoveTimer)
    mapMoveTimer = window.setTimeout(() => {
      syncAddressByCoords(center.lng, center.lat)
    }, 220)
  }

  const locateCurrentAddress = async () => {
    let baseLocation = null

    try {
      baseLocation = await getCurrentLocation()
    } catch (error) {
      if (locationStore.locatedLongitude && locationStore.locatedLatitude) {
        baseLocation = { lng: locationStore.locatedLongitude, lat: locationStore.locatedLatitude }
      } else if (locationStore.longitude && locationStore.latitude) {
        baseLocation = { lng: locationStore.longitude, lat: locationStore.latitude }
      } else if (form.longitude && form.latitude) {
        baseLocation = { lng: form.longitude, lat: form.latitude }
      }
    }

    if (!baseLocation) {
      showToast('请先开启定位权限')
      return null
    }

    const regeo = await reverseGeocode(baseLocation.lng, baseLocation.lat)
    if (!regeo) {
      showToast('定位失败，请稍后重试')
      return null
    }

    form.longitude = Number(baseLocation.lng)
    form.latitude = Number(baseLocation.lat)

    const cityName = applyRegionInfo(regeo)
    const locatedAddressText = regeo.formatted_address || displayAddress.value || ''
    displayAddress.value = locatedAddressText
    searchKey.value = locatedAddressText
    locationConfirmed.value = true

    locationStore.setLocation({
      longitude: Number(baseLocation.lng),
      latitude: Number(baseLocation.lat),
      address: locatedAddressText,
      city: cityName,
      adcode: regeo.addressComponent?.adcode || '',
    })

    if (!locationStore.isManual || locationStore.manualMode !== 'city') {
      currentCity.value = cityName || currentCity.value
    } else {
      currentCity.value = locationStore.city || currentCity.value
    }

    return {
      lng: Number(baseLocation.lng),
      lat: Number(baseLocation.lat),
    }
  }

  const resolveInitialMapLocation = async () => {
    if (form.longitude && form.latitude) {
      return { lng: Number(form.longitude), lat: Number(form.latitude) }
    }
    if (locationStore.longitude && locationStore.latitude) {
      return { lng: Number(locationStore.longitude), lat: Number(locationStore.latitude) }
    }

    return await locateCurrentAddress()
  }

  const initInteractiveMap = async () => {
    if (mapInstance || !mapRef.value) return

    mapLoading.value = true

    try {
      const AMap = await loadAmapSdk()
      const initialLocation = await resolveInitialMapLocation()
      if (!initialLocation) return

      mapInstance = new AMap.Map(mapRef.value, {
        zoom: 17,
        center: [initialLocation.lng, initialLocation.lat],
        viewMode: '2D',
        resizeEnable: true,
        dragEnable: true,
        zoomEnable: true,
        doubleClickZoom: true,
      })

      new AMap.TileLayer({
        zIndex: 1,
      }).setMap(mapInstance)

      window.setTimeout(() => {
        mapInstance?.resize()
        setMapCenter(initialLocation.lng, initialLocation.lat, 17)
      }, 120)

      mapInstance.on('moveend', handleMapMoveEnd)
      mapInstance.on('zoomend', handleMapMoveEnd)

      if (!displayAddress.value) {
        await syncAddressByCoords(initialLocation.lng, initialLocation.lat)
      } else {
        setMapCenter(initialLocation.lng, initialLocation.lat)
      }
    } catch (error) {
      showToast('地图加载失败')
    } finally {
      mapLoading.value = false
    }
  }

  const relocateMapToCurrent = async () => {
    const currentLocation = await locateCurrentAddress()
    if (!currentLocation) return
    setMapCenter(currentLocation.lng, currentLocation.lat)
  }

  const cleanupMap = () => {
    clearTimeout(mapMoveTimer)
    if (mapInstance) {
      mapInstance.destroy()
      mapInstance = null
    }
  }

  return {
    applyRegionInfo,
    cleanupMap,
    initInteractiveMap,
    locateCurrentAddress,
    relocateMapToCurrent,
    setMapCenter,
    syncCurrentCityFromCoords,
  }
}
