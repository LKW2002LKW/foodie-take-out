import { computed, ref } from 'vue'
import { geocode, inputTips, reverseGeocode } from '@/utils/location/amapLoader'

const getRegeoCity = (regeo) => regeo?.addressComponent?.city || regeo?.addressComponent?.province || ''

// 负责地址列表页顶部搜索建议，最终将选中的地点同步回定位仓库。
export const useAddressSearchSuggestions = ({ locationStore, router }) => {
  const searchQuery = ref('')
  const isSearching = ref(false)
  const tips = ref([])
  const showSearchPanel = computed(() => Boolean(searchQuery.value.trim()))

  let searchTimer = null

  const onSearchInput = (val) => {
    if (!val || !val.trim()) {
      tips.value = []
      isSearching.value = false
      return
    }

    isSearching.value = true
    clearTimeout(searchTimer)
    searchTimer = window.setTimeout(async () => {
      const results = await inputTips(val, locationStore.city)
      tips.value = results.filter((item) => item.location && typeof item.location === 'string')
    }, 300)
  }

  const onSelectSuggestion = async (tip) => {
    const [lng, lat] = tip.location.split(',')
    locationStore.setCoordinates(parseFloat(lng), parseFloat(lat))
    locationStore.setAddress(tip.name, true, 'address')
    const info = await reverseGeocode(lng, lat)
    if (info) {
      locationStore.setCity(getRegeoCity(info), true, 'address')
    } else {
      const geo = await geocode(tip.name)
      if (geo?.city) {
        locationStore.setCity(geo.city, true, 'address')
      }
    }
    router.push('/merchant/list')
  }

  const cleanupSuggestions = () => {
    clearTimeout(searchTimer)
  }

  return {
    cleanupSuggestions,
    isSearching,
    onSearchInput,
    onSelectSuggestion,
    searchQuery,
    showSearchPanel,
    tips,
  }
}
