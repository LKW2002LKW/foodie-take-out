import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartBatchActions } from '@/composables/business/useCartBatchActions'
import { useCartActions } from '@/composables/business/useCartActions'
import { useCartMerchantGrouping } from '@/composables/business/useCartMerchantGrouping'
import { useCartPageState } from '@/composables/business/useCartPageState'
import { useCartStore } from '@/stores/modules/cartStore'
import { formatFlavorText } from '@/utils/business/flavor'

// 购物车页面总控组合式，负责拼装分组、操作和页面状态三个子模型。
export const useCartPage = () => {
  const router = useRouter()
  const cartStore = useCartStore()
  const {
    addCartItem,
    clearAllCart,
    clearMerchantCart,
    removeCartItems,
    subCartItem,
  } = useCartActions()

  const currentMerchantId = ref(null)
  const groupingModel = useCartMerchantGrouping({ cartStore, currentMerchantId })
  const stateModel = useCartPageState({
    cartStore,
    currentMerchant: groupingModel.currentMerchant,
    currentMerchantId,
    merchantGroupList: groupingModel.merchantGroupList,
    router,
  })
  const batchActionModel = useCartBatchActions({
    clearAllCart,
    clearMerchantCart,
    currentMerchant: groupingModel.currentMerchant,
    currentMerchantId,
    groupedCart: groupingModel.groupedCart,
    leaveDetail: stateModel.leaveDetail,
    merchantGroupList: groupingModel.merchantGroupList,
    removeCartItems,
    resetManageState: stateModel.resetManageState,
    selectedCount: stateModel.selectedCount,
    selectedMerchantIds: stateModel.selectedMerchantIds,
  })

  const onAddCartItem = async (item) => {
    await addCartItem(item)
  }

  const onSubCartItem = async (item) => {
    await subCartItem(item)
  }

  onMounted(() => {
    cartStore.fetchCartList()
  })

  return {
    allSelected: stateModel.allSelected,
    currentMerchant: groupingModel.currentMerchant,
    formatFlavor: (value) => formatFlavorText(value, ','),
    groupedCart: groupingModel.groupedCart,
    isDetail: stateModel.isDetail,
    isEditing: stateModel.isEditing,
    isMerchantSelected: stateModel.isMerchantSelected,
    merchantGroupList: groupingModel.merchantGroupList,
    onAddCartItem,
    onBack: stateModel.onBack,
    onCheckout: stateModel.onCheckout,
    onClearSelectedMerchants: batchActionModel.onClearSelectedMerchants,
    onDeleteSelected: batchActionModel.onDeleteSelected,
    onMerchantCardClick: stateModel.onMerchantCardClick,
    onSubCartItem,
    selectedCount: stateModel.selectedCount,
    showBottomBar: stateModel.showBottomBar,
    showManageButton: stateModel.showManageButton,
    toggleEdit: stateModel.toggleEdit,
    toggleItemSelection: stateModel.toggleItemSelection,
    toggleMerchantSelection: stateModel.toggleMerchantSelection,
    toggleSelectAll: stateModel.toggleSelectAll,
  }
}
