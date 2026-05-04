import { computed, nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { supportedCityData } from '@/constants/location'
import { useAddressEditActions } from '@/composables/business/useAddressEditActions'
import { useAddressEditMap } from '@/composables/business/useAddressEditMap'
import { useAddressEditSuggestions } from '@/composables/business/useAddressEditSuggestions'
import { useLocationStore } from '@/stores/modules/locationStore'

export const useAddressEditPage = () => {
  const route = useRoute()
  const router = useRouter()
  const locationStore = useLocationStore()

  const isEdit = !!route.query.id
  const saving = ref(false)
  const isDefaultBool = ref(false)
  const locationConfirmed = ref(false)
  const searchKey = ref('')
  const displayAddress = ref('')
  const showCityPopup = ref(false)
  const citySearchQuery = ref('')
  const mapRef = ref(null)
  const mapLoading = ref(false)
  const mapResolving = ref(false)
  const currentCity = ref(locationStore.city || locationStore.locatedCity || '北京市')

  const filteredCityData = computed(() => {
    if (!citySearchQuery.value) return supportedCityData

    return supportedCityData
      .map((group) => ({
        ...group,
        list: group.list.filter((city) => city.includes(citySearchQuery.value.trim())),
      }))
      .filter((group) => group.list.length > 0)
  })

  const filteredIndexList = computed(() => filteredCityData.value.map((group) => group.initial))

  const form = reactive({
    id: route.query.id || null,
    consignee: '',
    sex: 1,
    phone: '',
    detail: '',
    label: '家',
    longitude: null,
    latitude: null,
    isDefault: 0,
    provinceName: '',
    cityName: '',
    districtName: '',
    provinceCode: '',
    cityCode: '',
    districtCode: '',
  })

  watch(isDefaultBool, (val) => {
    form.isDefault = val ? 1 : 0
  })

  watch(
    () => [locationStore.city, locationStore.locatedCity],
    ([city, locatedCity]) => {
      const nextCity = city && city !== '定位中' ? city : locatedCity
      if (nextCity && nextCity !== '定位中') {
        currentCity.value = nextCity
      }
    },
    { immediate: true },
  )

  const mapModel = useAddressEditMap({
    currentCity,
    displayAddress,
    form,
    locationConfirmed,
    locationStore,
    mapLoading,
    mapRef,
    mapResolving,
    searchKey,
  })

  const suggestionModel = useAddressEditSuggestions({
    currentCity,
    displayAddress,
    form,
    locationConfirmed,
    locationStore,
    searchKey,
    setMapCenter: mapModel.setMapCenter,
    applyRegionInfo: mapModel.applyRegionInfo,
    locateCurrentAddress: mapModel.locateCurrentAddress,
    syncCurrentCityFromCoords: mapModel.syncCurrentCityFromCoords,
  })

  const actionModel = useAddressEditActions({
    currentCity,
    displayAddress,
    form,
    isDefaultBool,
    isEdit,
    locationConfirmed,
    router,
    saving,
    searchKey,
  })

  const onCitySelect = (city) => {
    currentCity.value = city
    locationStore.setCity(city, true, 'city')
    showCityPopup.value = false
    searchKey.value = ''
    suggestionModel.clearSuggestionList()
  }

  onMounted(async () => {
    if (isEdit) {
      await actionModel.loadAddressDetail()
    } else if (locationStore.longitude && locationStore.latitude && (!currentCity.value || currentCity.value === '定位中')) {
      await mapModel.syncCurrentCityFromCoords(locationStore.longitude, locationStore.latitude)
    }

    await nextTick()
    await mapModel.initInteractiveMap()
  })

  onUnmounted(() => {
    suggestionModel.cleanupSuggestions()
    mapModel.cleanupMap()
  })

  return {
    confirmLocationSelection: suggestionModel.confirmLocationSelection,
    currentCity,
    citySearchQuery,
    displayAddress,
    filteredCityData,
    filteredIndexList,
    form,
    isDefaultBool,
    isEdit,
    mapLoading,
    mapRef,
    mapResolving,
    onCitySelect,
    onDelete: actionModel.onDelete,
    onSave: actionModel.onSave,
    onSearchFocus: suggestionModel.onSearchFocus,
    onSearchInput: suggestionModel.onSearchInput,
    pickerMapUrl: suggestionModel.pickerMapUrl,
    relocateMapToCurrent: mapModel.relocateMapToCurrent,
    saving,
    searchKey,
    selectLocationTip: suggestionModel.selectLocationTip,
    selectedTipId: suggestionModel.selectedTipId,
    showCityPopup,
    showLocationPicker: suggestionModel.showLocationPicker,
    tips: suggestionModel.tips,
    triggerReSearch: suggestionModel.triggerReSearch,
  }
}
