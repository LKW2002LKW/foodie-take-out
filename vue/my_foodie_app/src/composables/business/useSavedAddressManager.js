import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import { batchDeleteAddress, getAddressList } from '@/api/modules/address'

export const useSavedAddressManager = ({ locationStore }) => {
  const router = useRouter()

  const savedAddresses = ref([])
  const isEditMode = ref(false)
  const selectedCount = computed(() => savedAddresses.value.filter((item) => item.selected).length)
  const isAllSelected = computed(() => savedAddresses.value.length > 0 && savedAddresses.value.every((item) => item.selected))

  const loadAddresses = async () => {
    const res = await getAddressList()
    if (res?.code === 1) {
      savedAddresses.value = (res.data || []).map((item) => ({ ...item, selected: false }))
    }
  }

  const clearSelections = () => {
    savedAddresses.value.forEach((item) => {
      item.selected = false
    })
  }

  const toggleEditMode = () => {
    isEditMode.value = !isEditMode.value
    if (!isEditMode.value) {
      clearSelections()
    }
  }

  const toggleSelectAll = () => {
    const target = !isAllSelected.value
    savedAddresses.value.forEach((item) => {
      item.selected = target
    })
  }

  const onItemClick = (addr) => {
    if (isEditMode.value) {
      addr.selected = !addr.selected
      return
    }

    const selectedAddress = addr.fullAddress || addr.detail || [addr.provinceName, addr.cityName, addr.districtName].filter(Boolean).join('')
    locationStore.setAddress(selectedAddress, true, 'address')
    locationStore.setCity(addr.cityName || locationStore.city, true, 'address')
    locationStore.setCoordinates(addr.longitude, addr.latitude)
    router.back()
  }

  const onBatchDelete = async () => {
    const ids = savedAddresses.value.filter((item) => item.selected).map((item) => item.id)
    if (ids.length === 0) return

    try {
      await showConfirmDialog({ title: '确认删除', message: `确定要删除这 ${ids.length} 个地址吗？` })
    } catch (error) {
      return
    }

    const res = await batchDeleteAddress(ids)
    if (res.code === 1) {
      showToast('删除成功')
      isEditMode.value = false
      await loadAddresses()
    }
  }

  const onAdd = () => router.push('/address/edit')
  const onEdit = (addr) => router.push(`/address/edit?id=${addr.id}`)

  return {
    isAllSelected,
    isEditMode,
    loadAddresses,
    onAdd,
    onBatchDelete,
    onEdit,
    onItemClick,
    savedAddresses,
    selectedCount,
    toggleEditMode,
    toggleSelectAll,
  }
}
