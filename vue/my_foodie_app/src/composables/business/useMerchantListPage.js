import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getMerchantPage } from '@/api/modules/merchant'
import {
  merchantBanners,
  merchantCategoryIconMap,
  merchantCategoryIcons,
  merchantSortOptions,
} from '@/constants/merchant'
import { DEFAULT_PAGE_SIZE } from '@/constants/pagination'
import { useLocationStore } from '@/stores/modules/locationStore'
import { reverseGeocode } from '@/utils/location/amapLoader'
import { getCurrentLocation } from '@/utils/location.js'

const getRegeoCity = (regeo) => regeo?.addressComponent?.city || regeo?.addressComponent?.province || ''

export const useMerchantListPage = () => {
  const router = useRouter()
  const locationStore = useLocationStore()

  const searchValue = ref('')
  const sortType = ref(0)
  const activeCategoryId = ref(null)
  const loading = ref(false)
  const finished = ref(false)
  const list = ref([])
  const page = ref(1)

  const headerLocationText = computed(() => {
    if (locationStore.isManual && locationStore.manualMode === 'city') {
      return locationStore.city || locationStore.locatedCity || '定位中'
    }
    return locationStore.address || locationStore.locatedAddress || '正在定位...'
  })

  const getCatIcon = (id) => merchantCategoryIconMap[id]

  const resetListState = () => {
    page.value = 1
    finished.value = false
    list.value = []
  }

  const onLoad = async () => {
    if (loading.value || finished.value) return

    loading.value = true
    try {
      const res = await getMerchantPage({
        page: page.value,
        pageSize: DEFAULT_PAGE_SIZE,
        name: searchValue.value,
        sortType: sortType.value,
        categoryId: activeCategoryId.value,
        longitude: locationStore.longitude,
        latitude: locationStore.latitude,
      })

      if (res.code !== 1) {
        finished.value = true
        showToast(res.msg || '商家加载失败')
        return
      }

      const records = res.data.records || []
      list.value.push(...records)

      if (records.length < DEFAULT_PAGE_SIZE) {
        finished.value = true
      } else {
        page.value += 1
      }
    } catch (error) {
      console.error(error)
      finished.value = true
      showToast('商家加载失败')
    } finally {
      loading.value = false
    }
  }

  const onRefresh = () => {
    resetListState()
    onLoad()
  }

  const onSearch = () => {
    onRefresh()
  }

  const onCategoryClick = (id) => {
    activeCategoryId.value = activeCategoryId.value === id ? null : id
    onRefresh()
  }

  const onNearbyClick = () => {
    if (!locationStore.longitude) {
      showToast('请先选择地址')
      return
    }

    sortType.value = 1
    onRefresh()
  }

  const onAddressClick = () => {
    router.push('/address/list')
  }

  const openMerchant = (merchantId) => {
    router.push(`/merchant/${merchantId}`)
  }

  const initLocation = async () => {
    if (locationStore.isLocated) return

    try {
      const loc = await getCurrentLocation()
      const info = await reverseGeocode(loc.lng, loc.lat)
      if (info) {
        locationStore.setLocation({
          longitude: loc.lng,
          latitude: loc.lat,
          address: info.formatted_address,
          city: getRegeoCity(info),
        })
      }
    } catch (error) {
      console.error(error)
    }
  }

  onMounted(async () => {
    await initLocation()
    onRefresh()
  })

  return {
    activeCategoryId,
    banners: merchantBanners,
    categoryIcons: merchantCategoryIcons,
    finished,
    getCatIcon,
    headerLocationText,
    list,
    loading,
    onAddressClick,
    onCategoryClick,
    onLoad,
    onNearbyClick,
    onRefresh,
    onSearch,
    openMerchant,
    searchValue,
    sortOptions: merchantSortOptions,
    sortType,
  }
}
