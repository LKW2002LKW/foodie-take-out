import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { supportedCityData } from '@/constants/location'
import { useAddressCurrentLocation } from '@/composables/business/useAddressCurrentLocation'
import { useAddressSearchSuggestions } from '@/composables/business/useAddressSearchSuggestions'
import { useSavedAddressManager } from '@/composables/business/useSavedAddressManager'
import { useLocationStore } from '@/stores/modules/locationStore'

export const useAddressListPage = () => {
  const router = useRouter()
  const locationStore = useLocationStore()

  const showCityPicker = ref(false)
  const cityIndexList = computed(() => supportedCityData.map((group) => group.initial))
  const onBack = () => router.back()
  const locationModel = useAddressCurrentLocation({ locationStore, onBack, showCityPicker })
  const searchModel = useAddressSearchSuggestions({ locationStore, router })
  const addressModel = useSavedAddressManager({ locationStore })

  onMounted(() => {
    addressModel.loadAddresses()
    locationModel.initSmartLocation()
  })

  onUnmounted(() => {
    searchModel.cleanupSuggestions()
  })

  return {
    cityData: supportedCityData,
    cityIndexList,
    isAllSelected: addressModel.isAllSelected,
    isEditMode: addressModel.isEditMode,
    isManualCityMode: locationModel.isManualCityMode,
    isSearching: searchModel.isSearching,
    locating: locationModel.locating,
    locationStore,
    onAdd: addressModel.onAdd,
    onBack,
    onBatchDelete: addressModel.onBatchDelete,
    onCitySelect: locationModel.onCitySelect,
    onEdit: addressModel.onEdit,
    onItemClick: addressModel.onItemClick,
    onRelocateAction: locationModel.onRelocateAction,
    onSearchInput: searchModel.onSearchInput,
    onSelectSuggestion: searchModel.onSelectSuggestion,
    savedAddresses: addressModel.savedAddresses,
    searchQuery: searchModel.searchQuery,
    selectedCount: addressModel.selectedCount,
    showCityPicker,
    showSearchPanel: searchModel.showSearchPanel,
    tips: searchModel.tips,
    toggleEditMode: addressModel.toggleEditMode,
    toggleSelectAll: addressModel.toggleSelectAll,
    useCurrentLoc: locationModel.useCurrentLoc,
  }
}
