<template>
  <div class="cart-page mobile-page">
    <van-nav-bar :title="isDetail ? currentMerchant.name : '购物车'" :left-arrow="isDetail" fixed placeholder @click-left="onBack">
      <template #right>
        <span v-if="showManageButton" class="manage-btn" @click="toggleEdit">{{ isEditing ? '完成' : '管理' }}</span>
      </template>
    </van-nav-bar>

    <div class="cart-content" :class="{ 'cart-content--with-footer': showBottomBar }">
      <!-- Grouped View -->
      <div v-if="!isDetail" class="merchant-groups">
        <van-empty v-if="Object.keys(groupedCart).length === 0" description="购物车还是空的" />
        <div
          v-for="(group, mId) in groupedCart"
          :key="mId"
          class="merchant-card"
          :class="{ 'is-managing': isEditing }"
          @click="onMerchantCardClick(group)"
        >
          <div v-if="isEditing" class="merchant-checkbox" @click.stop="toggleMerchantSelection(group.id)">
            <div class="mt-checkbox" :class="{ 'checked': isMerchantSelected(group.id) }">
              <van-icon name="success" class="check-icon" />
            </div>
          </div>
          <div class="m-header">
            <div class="m-header-left">
              <van-image :src="group.logo" width="2.4rem" height="2.4rem" radius="0.4rem" />
              <span class="m-name">{{ group.name }}</span>
            </div>
            <van-icon name="arrow" size="1.2rem" color="var(--text-color-placeholder)" />
          </div>
          <div class="m-items-preview">
            <div v-for="item in group.items.slice(0, 4)" :key="item.id" class="preview-item"><van-image :src="item.image" width="6rem" height="6rem" radius="0.8rem" /></div>
          </div>
          <div class="m-footer">共 {{ group.totalNum }} 件，合计 <span class="total-price">￥{{ group.totalPrice }}</span></div>
        </div>
      </div>

      <!-- Detail View -->
      <div v-else class="shop-detail">
        <div class="detail-list">
          <div v-for="item in currentMerchant.items" :key="item.id" class="detail-item">
            <div v-if="isEditing" class="mt-checkbox" :class="{ 'checked': item.selected }" @click="item.selected = !item.selected">
              <van-icon name="success" class="check-icon" />
            </div>
            <van-image :src="item.image" width="8rem" height="8rem" radius="0.8rem" />
            <div class="item-info">
              <div class="item-name">{{ item.name }}</div>
              <div v-if="item.dishFlavor" class="item-spec">{{ formatFlavor(item.dishFlavor) }}</div>
              <div class="item-bottom">
                <div class="item-price">￥{{ item.amount }}</div>
                <div v-if="!isEditing" class="cart-ctrl">
                  <div class="btn-sub" @click.stop="cartStore.subFromCart(item)"><van-icon name="minus" /></div>
                  <span class="num">{{ item.number }}</span>
                  <div class="btn-add" @click.stop="cartStore.addToCart(item)"><van-icon name="plus" /></div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Bottom Bars -->
    <div v-if="isDetail" class="detail-footer-wrapper">
      <div v-if="!isEditing" class="detail-footer">
        <div class="footer-left"><span class="total-amount">￥{{ currentMerchant.totalPrice }}</span></div>
        <div class="checkout-btn" @click="onCheckout">去结算</div>
      </div>
      <div v-else class="manage-footer">
        <div class="select-all-wrap" @click="toggleSelectAll">
          <div class="mt-checkbox" :class="{ 'checked': allSelected }"><van-icon name="success" class="check-icon" /></div>
          <span>全选</span>
        </div>
        <div class="delete-btn" :class="{ 'disabled': selectedCount === 0 }" @click="onDeleteSelected">删除({{ selectedCount }})</div>
      </div>
    </div>
    <div v-else-if="isEditing && merchantGroupList.length > 0" class="detail-footer-wrapper">
      <div class="manage-footer">
        <div class="select-all-wrap" @click="toggleSelectAll">
          <div class="mt-checkbox" :class="{ 'checked': allSelected }"><van-icon name="success" class="check-icon" /></div>
          <span>全选</span>
        </div>
        <div class="delete-btn" :class="{ 'disabled': selectedCount === 0 }" @click="onClearSelectedMerchants">
          {{ allSelected ? '清空全部' : `清空已选(${selectedCount})` }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../store/modules/cart'
import { showToast, showConfirmDialog } from 'vant'

const router = useRouter()
const cartStore = useCartStore()
const isDetail = ref(false)
const isEditing = ref(false)
const currentMerchantId = ref(null)
const selectedMerchantIds = ref([])

const groupedCart = computed(() => {
  const groups = {}
  cartStore.list.forEach(item => {
    const mId = item.merchantId
    if (!groups[mId]) groups[mId] = { id: mId, name: item.merchantName || '店铺', logo: item.merchantLogo || '', items: [], totalNum: 0, totalPrice: 0 }
    if (item.selected === undefined) item.selected = false
    groups[mId].items.push(item); groups[mId].totalNum += item.number; groups[mId].totalPrice += item.number * item.amount
  })
  return groups
})
const merchantGroupList = computed(() => Object.values(groupedCart.value))
const currentMerchant = computed(() => groupedCart.value[currentMerchantId.value] || {})
const showManageButton = computed(() => merchantGroupList.value.length > 0 && (isDetail.value ? currentMerchant.value.items?.length > 0 : true))
const showBottomBar = computed(() => isDetail.value || (isEditing.value && merchantGroupList.value.length > 0))
const selectedCount = computed(() => {
  if (isDetail.value) {
    return (currentMerchant.value.items || []).filter(i => i.selected).length
  }
  return selectedMerchantIds.value.length
})
const allSelected = computed(() => {
  if (isDetail.value) {
    return currentMerchant.value.items?.length > 0 && currentMerchant.value.items.every(i => i.selected)
  }
  return merchantGroupList.value.length > 0 && selectedMerchantIds.value.length === merchantGroupList.value.length
})

const resetManageState = () => {
  isEditing.value = false
  selectedMerchantIds.value = []
  cartStore.list.forEach(item => {
    item.selected = false
  })
}

const onBack = () => {
  if (isDetail.value) {
    isDetail.value = false
    currentMerchantId.value = null
    resetManageState()
    return
  }
  router.back()
}
const enterDetail = (g) => {
  currentMerchantId.value = g.id
  isDetail.value = true
  resetManageState()
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
const isMerchantSelected = (merchantId) => selectedMerchantIds.value.includes(Number(merchantId))
const toggleMerchantSelection = (merchantId) => {
  const normalizedId = Number(merchantId)
  if (isMerchantSelected(normalizedId)) {
    selectedMerchantIds.value = selectedMerchantIds.value.filter(id => id !== normalizedId)
  } else {
    selectedMerchantIds.value = [...selectedMerchantIds.value, normalizedId]
  }
}
const toggleSelectAll = () => {
  const target = !allSelected.value
  if (isDetail.value) {
    currentMerchant.value.items.forEach(i => {
      i.selected = target
    })
    return
  }
  selectedMerchantIds.value = target ? merchantGroupList.value.map(group => Number(group.id)) : []
}
const onDeleteSelected = () => {
  const toDelete = currentMerchant.value.items.filter(i => i.selected)
  if (toDelete.length === 0) return
  showConfirmDialog({ title: '确认删除', message: `确定要删除这 ${toDelete.length} 件商品吗？` }).then(async () => {
    await cartStore.batchRemoveItems(toDelete)
    showToast('删除成功')
    if ((groupedCart.value[currentMerchantId.value]?.items || []).length === 0) {
      isDetail.value = false
      currentMerchantId.value = null
    }
    resetManageState()
  })
}
const onClearSelectedMerchants = () => {
  if (selectedCount.value === 0) return
  const shouldClearAll = selectedCount.value === merchantGroupList.value.length
  const message = shouldClearAll
    ? '确定要清空全部商家的购物车吗？'
    : `确定要清空已选的 ${selectedCount.value} 个商家购物车吗？`
  showConfirmDialog({ title: '确认清空', message }).then(async () => {
    if (shouldClearAll) {
      await cartStore.clearAllCartAction()
    } else {
      await Promise.all(selectedMerchantIds.value.map(merchantId => cartStore.clearCartAction(merchantId)))
    }
    showToast('清空成功')
    resetManageState()
  }).catch(() => {})
}
const onCheckout = () => router.push({ path: '/order/create', query: { merchantId: currentMerchantId.value } })
const formatFlavor = (str) => { try { return JSON.parse(str).map(f => f.value).join(',') } catch (error) { return str } }
onMounted(() => cartStore.fetchCartList())
</script>

<style scoped>
.cart-page {
  min-height: 100vh;
  min-height: 100dvh;
  background-color: var(--mt-page-bg);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.cart-content { flex: 1; padding: 1.2rem; overflow-y: auto; -webkit-overflow-scrolling: touch; overscroll-behavior: contain; }
.cart-content--with-footer { padding-bottom: calc(13rem + env(safe-area-inset-bottom)); }
.merchant-card {
  background: var(--mt-card-bg);
  border-radius: var(--mt-card-radius);
  padding: 1.6rem;
  margin-bottom: 1.2rem;
  box-shadow: var(--shadow-sm);
  position: relative;
}
.merchant-card.is-managing {
  padding-left: 5.2rem;
}
.merchant-checkbox {
  position: absolute;
  left: 1.4rem;
  top: 50%;
  transform: translateY(-50%);
  z-index: 2;
}
.m-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 1.2rem; }
.m-header-left { display: flex; align-items: center; gap: 0.8rem; }
.m-items-preview { display: flex; gap: 0.8rem; margin-bottom: 1.2rem; }
.total-price { color: var(--van-danger-color); font-weight: 800; }
.shop-detail { padding-bottom: 8rem; }
.detail-item {
  background: var(--mt-card-bg);
  border-radius: var(--mt-card-radius);
  padding: 1.6rem;
  margin-bottom: 1.2rem;
  display: flex;
  align-items: center;
  gap: 1.2rem;
  box-shadow: var(--shadow-sm);
}
.item-info { flex: 1; min-height: 8rem; display: flex; flex-direction: column; justify-content: space-between; }
.item-name { font-size: 1.5rem; font-weight: 700; color: var(--mt-strong); }
.item-spec { font-size: 1.2rem; color: var(--mt-muted); margin-top: 0.2rem; }
.item-price { color: var(--van-danger-color); font-weight: 800; font-size: 1.8rem; }
.mt-checkbox {
  width: 2.1rem;
  height: 2.1rem;
  border-radius: 50%;
  border: 0.14rem solid rgba(232, 178, 22, 0.88);
  background: linear-gradient(180deg, #fffef9 0%, #fff7df 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 0.2rem 0.6rem rgba(245, 194, 0, 0.12);
}
.mt-checkbox.checked { background: linear-gradient(180deg, #ffe27f 0%, #f5c200 100%); border-color: #e2a900; }
.check-icon { font-size: 1.2rem; color: var(--mt-strong); opacity: 0; }
.mt-checkbox.checked .check-icon { opacity: 1; }
.cart-ctrl { display: flex; align-items: center; }
.btn-sub, .btn-add {
  width: 2.6rem;
  height: 2.6rem;
  min-height: 2.6rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}
.btn-sub {
  border: 0.14rem solid #f5c200;
  color: var(--mt-strong);
  background: var(--mt-card-bg);
  box-shadow: 0 0.2rem 0.5rem rgba(245, 194, 0, 0.12);
}
.btn-add {
  background: linear-gradient(180deg, #ffe27f 0%, #f5c200 100%);
  color: var(--mt-strong);
  font-weight: 900;
  box-shadow: 0 0.3rem 0.8rem rgba(245, 194, 0, 0.24);
}
.btn-sub:active, .btn-add:active { transform: scale(0.96); }
.btn-sub :deep(.van-icon), .btn-add :deep(.van-icon) { font-size: 1.3rem; font-weight: 900; }
.num { width: 2.8rem; text-align: center; font-size: 1.3rem; }
.detail-footer-wrapper { 
  position: fixed; 
  bottom: calc(5.8rem + env(safe-area-inset-bottom)); 
  left: 1.2rem; 
  right: 1.2rem; 
  z-index: 2100; 
}
.detail-footer,
.manage-footer {
  min-height: 5.6rem;
  background: var(--mt-card-bg);
  display: flex;
  align-items: center;
  padding: 0 1.6rem;
  box-shadow: 0 0.8rem 2rem rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(245, 194, 0, 0.12);
  border-radius: 1.8rem;
}
.total-amount { font-size: 2.2rem; font-weight: 800; color: var(--van-danger-color); }
.checkout-btn { background: var(--primary-color); color: var(--mt-strong); font-weight: 900; padding: 1rem 2.4rem; border-radius: 2.2rem; margin-left: auto; min-height: 4.4rem; display: flex; align-items: center; }
.manage-footer { justify-content: space-between; }
.select-all-wrap { display: flex; align-items: center; gap: 0.8rem; min-height: 4.4rem; }
.delete-btn { background: var(--van-danger-color); color: var(--mt-card-bg); padding: 1rem 2.4rem; border-radius: 2.2rem; min-height: 4.4rem; display: flex; align-items: center; }
.delete-btn.disabled { opacity: 0.5; }
.manage-btn {
  color: var(--secondary-color);
  font-size: 1.3rem;
  font-weight: 800;
  padding: 0.6rem 1.1rem;
  min-height: 3.2rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #fffdf7 0%, #fff2bf 100%);
  border: 1px solid rgba(245, 194, 0, 0.28);
  border-radius: 999px;
  box-shadow: 0 0.3rem 0.8rem rgba(245, 194, 0, 0.12);
}

:deep(.van-nav-bar__right) {
  padding-right: 1.2rem;
}
</style>
