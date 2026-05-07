import { computed, ref } from 'vue'

// 购物车页面状态层，专门维护详情态、编辑态和选中态。
export const useCartPageState = ({
  cartStore,
  currentMerchant,
  currentMerchantId,
  merchantGroupList,
  router,
}) => {
  const isDetail = ref(false)
  const isEditing = ref(false)
  const selectedMerchantIds = ref([])

  const showManageButton = computed(() => (
    merchantGroupList.value.length > 0
      && (isDetail.value ? currentMerchant.value.items?.length > 0 : true)
  ))
  const showBottomBar = computed(() => (
    isDetail.value || (isEditing.value && merchantGroupList.value.length > 0)
  ))
  const selectedCount = computed(() => {
    if (isDetail.value) {
      return (currentMerchant.value.items || []).filter((item) => item.selected).length
    }
    return selectedMerchantIds.value.length
  })
  const allSelected = computed(() => {
    if (isDetail.value) {
      return currentMerchant.value.items?.length > 0
        && currentMerchant.value.items.every((item) => item.selected)
    }
    return merchantGroupList.value.length > 0
      && selectedMerchantIds.value.length === merchantGroupList.value.length
  })

  const resetManageState = () => {
    isEditing.value = false
    selectedMerchantIds.value = []
    cartStore.list.forEach((item) => {
      item.selected = false
    })
  }

  const leaveDetail = () => {
    isDetail.value = false
    currentMerchantId.value = null
    resetManageState()
  }

  const onBack = () => {
    if (isDetail.value) {
      leaveDetail()
      return
    }
    router.back()
  }

  const enterDetail = (group) => {
    currentMerchantId.value = group.id
    isDetail.value = true
    resetManageState()
  }

  const isMerchantSelected = (merchantId) => (
    selectedMerchantIds.value.includes(Number(merchantId))
  )

  const toggleMerchantSelection = (merchantId) => {
    const normalizedId = Number(merchantId)
    if (isMerchantSelected(normalizedId)) {
      selectedMerchantIds.value = selectedMerchantIds.value.filter((id) => id !== normalizedId)
      return
    }
    selectedMerchantIds.value = [...selectedMerchantIds.value, normalizedId]
  }

  const onMerchantCardClick = (group) => {
    if (isEditing.value) {
      toggleMerchantSelection(group.id)
      return
    }
    enterDetail(group)
  }

  const toggleEdit = () => {
    if (isEditing.value) {
      resetManageState()
      return
    }
    isEditing.value = true
  }

  const toggleItemSelection = (item) => {
    item.selected = !item.selected
  }

  const toggleSelectAll = () => {
    const target = !allSelected.value

    if (isDetail.value) {
      currentMerchant.value.items.forEach((item) => {
        item.selected = target
      })
      return
    }

    selectedMerchantIds.value = target
      ? merchantGroupList.value.map((group) => Number(group.id))
      : []
  }

  const onCheckout = () => {
    router.push({ path: '/order/create', query: { merchantId: currentMerchantId.value } })
  }

  return {
    allSelected,
    isDetail,
    isEditing,
    isMerchantSelected,
    leaveDetail,
    onBack,
    onCheckout,
    onMerchantCardClick,
    resetManageState,
    selectedCount,
    selectedMerchantIds,
    showBottomBar,
    showManageButton,
    toggleEdit,
    toggleItemSelection,
    toggleMerchantSelection,
    toggleSelectAll,
  }
}
