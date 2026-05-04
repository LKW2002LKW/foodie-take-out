import { computed, nextTick, onBeforeUnmount, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { geocode, inputTips, loadAmapSdk, reverseGeocode } from '@/services/amap'

export const useMerchantProfileMap = (form) => {
  const mapRef = ref(null)
  const locating = ref(false)
  const resolvingAddress = ref(false)
  const mapLoading = ref(false)
  const mapResolving = ref(false)
  const suggestions = ref([])
  const suggestionsVisible = ref(false)

  const hasCoordinates = computed(() => Number.isFinite(form.longitude) && Number.isFinite(form.latitude))
  const coordinateText = computed(() => ({
    longitude: hasCoordinates.value ? Number(form.longitude).toFixed(6) : '--',
    latitude: hasCoordinates.value ? Number(form.latitude).toFixed(6) : '--',
  }))
  const regionText = computed(() => {
    const text = [form.provinceName, form.cityName, form.districtName].filter(Boolean).join(' / ')
    return text || '--'
  })

  let suggestionTimer = null
  let blurTimer = null
  let mapInstance = null
  let mapMoveTimer = null
  let ignoreNextMoveEnd = false

  const buildSearchAddress = () => (
    [form.provinceName, form.cityName, form.districtName, form.address].filter(Boolean).join('')
  )

  const clearLocationState = () => {
    form.provinceCode = ''
    form.provinceName = ''
    form.cityCode = ''
    form.cityName = ''
    form.districtCode = ''
    form.districtName = ''
    form.longitude = null
    form.latitude = null
  }

  const parseCityName = (city, province) => {
    if (Array.isArray(city)) {
      return city[0] || province || ''
    }
    return city || province || ''
  }

  const applyAddressComponent = (regeo, fallbackAddress = '') => {
    const component = regeo?.addressComponent || {}
    const adcode = String(component.adcode || '')
    form.provinceName = component.province || ''
    form.cityName = parseCityName(component.city, component.province)
    form.districtName = component.district || ''
    form.provinceCode = adcode ? `${adcode.slice(0, 2)}0000` : ''
    form.cityCode = adcode ? `${adcode.slice(0, 4)}00` : ''
    form.districtCode = adcode || ''
    form.address = regeo?.formatted_address || fallbackAddress || form.address
  }

  const setMapCenter = (lng, lat, zoom = 16) => {
    if (!mapInstance || !lng || !lat) return
    ignoreNextMoveEnd = true
    mapInstance.setZoomAndCenter(zoom, [Number(lng), Number(lat)])
    window.setTimeout(() => {
      ignoreNextMoveEnd = false
    }, 260)
  }

  const resolveInitialMapLocation = async () => {
    if (hasCoordinates.value) {
      return { lng: Number(form.longitude), lat: Number(form.latitude) }
    }

    const cityKeyword = form.cityName || form.provinceName || ''
    if (cityKeyword) {
      const cityGeo = await geocode(cityKeyword, cityKeyword)
      if (cityGeo) {
        return { lng: cityGeo.lng, lat: cityGeo.lat }
      }
    }

    return { lng: 116.397428, lat: 39.90923 }
  }

  const initInteractiveMap = async (location = null) => {
    if (!mapRef.value) return

    mapLoading.value = true
    try {
      const AMap = await loadAmapSdk()
      const initialLocation = location || await resolveInitialMapLocation()
      if (!initialLocation) return

      if (!mapInstance) {
        mapInstance = new AMap.Map(mapRef.value, {
          zoom: 16,
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

        mapInstance.on('moveend', handleMapMoveEnd)
        mapInstance.on('zoomend', handleMapMoveEnd)
      }

      window.setTimeout(() => {
        mapInstance?.resize()
        setMapCenter(initialLocation.lng, initialLocation.lat)
      }, 120)
    } catch (error) {
      ElMessage.error('地图加载失败，请检查高德地图配置')
    } finally {
      mapLoading.value = false
    }
  }

  const syncLocationByCoords = async (lng, lat, fallbackAddress = '', { moveMap = false } = {}) => {
    mapResolving.value = true
    const regeo = await reverseGeocode(lng, lat)
    try {
      if (!regeo) {
        throw new Error('定位结果解析失败')
      }

      form.longitude = Number(lng)
      form.latitude = Number(lat)
      applyAddressComponent(regeo, fallbackAddress)

      if (moveMap) {
        setMapCenter(lng, lat)
      }
    } finally {
      mapResolving.value = false
    }
  }

  const handleMapMoveEnd = () => {
    if (!mapInstance || ignoreNextMoveEnd) return
    const center = mapInstance.getCenter()
    window.clearTimeout(mapMoveTimer)
    mapMoveTimer = window.setTimeout(() => {
      syncLocationByCoords(center.lng, center.lat)
    }, 220)
  }

  const loadSuggestions = async (keyword) => {
    const cityKeyword = form.cityName || form.provinceName || ''
    const result = await inputTips(keyword, cityKeyword)
    suggestions.value = result.map((item, index) => ({
      id: item.id || `${item.location}-${index}`,
      name: item.name || '',
      district: item.district || '',
      address: item.address || '',
      location: item.location || '',
    }))
    suggestionsVisible.value = suggestions.value.length > 0
  }

  const handleAddressInput = (value) => {
    clearLocationState()
    suggestionsVisible.value = false

    if (suggestionTimer) {
      window.clearTimeout(suggestionTimer)
    }

    if (!String(value || '').trim()) {
      suggestions.value = []
      return
    }

    suggestionTimer = window.setTimeout(() => {
      loadSuggestions(String(value).trim())
    }, 250)
  }

  const handleAddressFocus = () => {
    if (form.address.trim() && suggestions.value.length) {
      suggestionsVisible.value = true
    }
  }

  const handleAddressBlur = () => {
    blurTimer = window.setTimeout(() => {
      suggestionsVisible.value = false
    }, 120)
  }

  const handleSuggestionSelect = async (item) => {
    if (blurTimer) {
      window.clearTimeout(blurTimer)
    }
    suggestionsVisible.value = false
    suggestions.value = []

    const [lng, lat] = String(item.location || '').split(',')
    if (!lng || !lat) {
      ElMessage.error('该地址缺少坐标信息')
      return
    }

    try {
      resolvingAddress.value = true
      await syncLocationByCoords(Number(lng), Number(lat), `${item.district}${item.address || item.name}`)
      await initInteractiveMap({ lng: Number(lng), lat: Number(lat) })
      ElMessage.success('地址定位成功')
    } catch (error) {
      ElMessage.error(error.message || '地址定位失败')
    } finally {
      resolvingAddress.value = false
    }
  }

  const getCurrentPosition = () => new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error('当前浏览器不支持定位'))
      return
    }

    navigator.geolocation.getCurrentPosition(
      (position) => resolve(position.coords),
      () => reject(new Error('定位失败，请检查浏览器定位权限')),
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0,
      },
    )
  })

  const handleLocateByAddress = async () => {
    const keyword = buildSearchAddress()
    if (!keyword) {
      ElMessage.warning('请先输入地址')
      return
    }

    resolvingAddress.value = true
    try {
      const geoResult = await geocode(keyword, form.cityName || form.provinceName || '')
      if (!geoResult) {
        throw new Error('未能解析该地址，请完善地址后重试')
      }

      await syncLocationByCoords(geoResult.lng, geoResult.lat, form.address)
      await initInteractiveMap({ lng: geoResult.lng, lat: geoResult.lat })
      suggestionsVisible.value = false
      suggestions.value = []
      ElMessage.success('地址定位成功')
    } catch (error) {
      ElMessage.error(error.message || '地址定位失败')
    } finally {
      resolvingAddress.value = false
    }
  }

  const handleAutoLocate = async () => {
    locating.value = true
    try {
      const coords = await getCurrentPosition()
      await syncLocationByCoords(coords.longitude, coords.latitude)
      await initInteractiveMap({ lng: Number(coords.longitude), lat: Number(coords.latitude) })
      ElMessage.success('自动定位成功')
    } catch (error) {
      ElMessage.error(error.message || '自动定位失败')
    } finally {
      locating.value = false
    }
  }

  const relocateMapToCurrent = async () => {
    locating.value = true
    try {
      const coords = await getCurrentPosition()
      await syncLocationByCoords(coords.longitude, coords.latitude, '', { moveMap: true })
      await initInteractiveMap({ lng: Number(coords.longitude), lat: Number(coords.latitude) })
    } catch (error) {
      ElMessage.error(error.message || '定位失败')
    } finally {
      locating.value = false
    }
  }

  const mountMerchantProfileMap = async (location = null) => {
    await nextTick()
    if (location) {
      await initInteractiveMap(location)
      return
    }
    await initInteractiveMap()
  }

  const cleanupMerchantProfileMap = () => {
    if (suggestionTimer) {
      window.clearTimeout(suggestionTimer)
    }
    if (blurTimer) {
      window.clearTimeout(blurTimer)
    }
    if (mapMoveTimer) {
      window.clearTimeout(mapMoveTimer)
    }
    mapInstance?.destroy()
    mapInstance = null
  }

  onBeforeUnmount(() => {
    cleanupMerchantProfileMap()
  })

  return {
    coordinateText,
    handleAddressBlur,
    handleAddressFocus,
    handleAddressInput,
    handleAutoLocate,
    handleLocateByAddress,
    handleSuggestionSelect,
    hasCoordinates,
    locating,
    mapLoading,
    mapRef,
    mapResolving,
    mountMerchantProfileMap,
    regionText,
    relocateMapToCurrent,
    resolvingAddress,
    suggestions,
    suggestionsVisible,
  }
}
