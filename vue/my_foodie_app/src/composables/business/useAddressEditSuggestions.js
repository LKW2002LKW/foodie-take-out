import { computed, ref } from 'vue'
import { showToast } from 'vant'
import { getStaticMapUrl, getAroundPois, inputTips, reverseGeocode } from '@/utils/location/amapLoader'
import { getCurrentLocation } from '@/utils/location.js'

// 负责地址编辑页的地点搜索建议、附近点位列表与候选项选择。
export const useAddressEditSuggestions = ({
  currentCity,
  displayAddress,
  form,
  locationConfirmed,
  locationStore,
  searchKey,
  setMapCenter,
  applyRegionInfo,
  locateCurrentAddress,
  syncCurrentCityFromCoords,
}) => {
  const tips = ref([])
  const selectedTipId = ref('')
  const showLocationPicker = ref(false)

  let searchTimer = null

  const normalizeTip = (tip, index = 0) => ({
    id: tip.id || `${tip.location || tip.name || 'tip'}-${index}`,
    name: tip.name || '',
    district: tip.district || tip.adname || '',
    address: tip.address || '',
    location: tip.location || '',
  })

  const setTipList = (items) => {
    tips.value = (items || [])
      .filter((item) => item.location && typeof item.location === 'string')
      .map((item, index) => normalizeTip(item, index))
    selectedTipId.value = tips.value[0]?.id || ''
  }

  const selectedTip = computed(() => tips.value.find((tip) => tip.id === selectedTipId.value) || null)
  const pickerLongitude = computed(() => {
    if (selectedTip.value?.location) {
      return Number(String(selectedTip.value.location).split(',')[0])
    }

    return form.longitude || locationStore.longitude || null
  })
  const pickerLatitude = computed(() => {
    if (selectedTip.value?.location) {
      return Number(String(selectedTip.value.location).split(',')[1])
    }

    return form.latitude || locationStore.latitude || null
  })
  const pickerMapUrl = computed(() => getStaticMapUrl(pickerLongitude.value, pickerLatitude.value))

  const resolveSuggestionLocation = async () => {
    if (form.longitude && form.latitude) {
      if (!currentCity.value || currentCity.value === '定位中') {
        await syncCurrentCityFromCoords(form.longitude, form.latitude)
      }
      return { lng: form.longitude, lat: form.latitude }
    }

    if (locationStore.longitude && locationStore.latitude) {
      if (!currentCity.value || currentCity.value === '定位中') {
        await syncCurrentCityFromCoords(locationStore.longitude, locationStore.latitude)
      }
      return { lng: locationStore.longitude, lat: locationStore.latitude }
    }

    try {
      const current = await getCurrentLocation()
      locationStore.setCoordinates(current.lng, current.lat)
      await syncCurrentCityFromCoords(current.lng, current.lat)
      return current
    } catch (error) {
      return null
    }
  }

  const loadNearbySuggestions = async (preferKeyword = true, baseLocation = null) => {
    const location = baseLocation || await resolveSuggestionLocation()
    if (!location) {
      showToast('请先开启定位或搜索地址')
      return
    }

    const typedKeyword = (searchKey.value || '').trim()
    const currentAddress = (displayAddress.value || '').trim()
    const aroundKeyword = preferKeyword && typedKeyword && typedKeyword !== currentAddress ? typedKeyword : ''

    let nearbyPois = await getAroundPois(location.lng, location.lat, aroundKeyword)
    if ((!nearbyPois || nearbyPois.length === 0) && aroundKeyword) {
      nearbyPois = await getAroundPois(location.lng, location.lat, '')
    }

    setTipList((nearbyPois || []).map((poi) => ({
      id: poi.id,
      name: poi.name,
      district: poi.adname || [poi.cityname, poi.pname].filter(Boolean).join(''),
      address: poi.address,
      location: poi.location,
    })))

    if (tips.value.length === 0) {
      showToast('附近暂无推荐地址')
    }
  }

  const onSearchInput = (val) => {
    showLocationPicker.value = true
    if (!val) {
      loadNearbySuggestions(false)
      return
    }

    clearTimeout(searchTimer)
    searchTimer = window.setTimeout(async () => {
      const results = await inputTips(val, currentCity.value)
      setTipList(results)
    }, 300)
  }

  const onSearchFocus = () => {
    showLocationPicker.value = true
    if (searchKey.value.trim()) return
    loadNearbySuggestions(false)
  }

  const triggerReSearch = async () => {
    showLocationPicker.value = true
    const currentLocation = await locateCurrentAddress()
    await loadNearbySuggestions(false, currentLocation)
  }

  const applyTipSelection = async (tip) => {
    const [lng, lat] = tip.location.split(',')
    form.longitude = parseFloat(lng)
    form.latitude = parseFloat(lat)
    displayAddress.value = tip.name

    const regeo = await reverseGeocode(lng, lat)
    if (regeo) {
      const cityName = applyRegionInfo(regeo)
      if (!locationStore.isManual || locationStore.manualMode !== 'city') {
        currentCity.value = cityName || currentCity.value
      }
      locationConfirmed.value = true
    }

    searchKey.value = tip.name
    setMapCenter(lng, lat)
  }

  const selectLocationTip = (tip) => {
    selectedTipId.value = tip.id
  }

  const confirmLocationSelection = async () => {
    if (!selectedTip.value) {
      showToast('请选择地址')
      return
    }

    await applyTipSelection(selectedTip.value)
    showLocationPicker.value = false
  }

  const clearSuggestionList = () => {
    tips.value = []
    selectedTipId.value = ''
  }

  const cleanupSuggestions = () => {
    clearTimeout(searchTimer)
  }

  return {
    cleanupSuggestions,
    clearSuggestionList,
    confirmLocationSelection,
    onSearchFocus,
    onSearchInput,
    pickerMapUrl,
    selectLocationTip,
    selectedTipId,
    showLocationPicker,
    tips,
    triggerReSearch,
  }
}
