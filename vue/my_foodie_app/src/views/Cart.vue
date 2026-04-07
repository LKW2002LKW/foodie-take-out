<template>
  <div class="cart-page mobile-page">
    <van-nav-bar :title="isDetail ? currentMerchant.name : '购物车'" :left-arrow="isDetail" @click-left="onBack">
      <template #right><span v-if="isDetail" class="manage-btn" @click="toggleEdit">{{ isEditing ? '完成' : '管理' }}</span></template>
    </van-nav-bar>

    <div class="cart-content">
      <!-- Grouped View -->
      <div v-if="!isDetail" class="merchant-groups">
        <van-empty v-if="Object.keys(groupedCart).length === 0" description="购物车还是空的" />
        <div v-for="(group, mId) in groupedCart" :key="mId" class="merchant-card" @click="enterDetail(group)">
          <div class="m-header">
            <div class="m-header-left">
              <van-image :src="group.logo" width="2.4rem" height="2.4rem" radius="0.4rem" />
              <span class="m-name">{{ group.name }}</span>
            </div>
            <van-icon name="arrow" size="1.2rem" color="#999" />
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
      <div v-if="!isEditing" class="detail-footer safe-area-bottom">
        <div class="footer-left"><span class="total-amount">￥{{ currentMerchant.totalPrice }}</span></div>
        <div class="checkout-btn" @click="onCheckout">去结算</div>
      </div>
      <div v-else class="manage-footer safe-area-bottom">
        <div class="select-all-wrap" @click="toggleSelectAll">
          <div class="mt-checkbox" :class="{ 'checked': allSelected }"><van-icon name="success" class="check-icon" /></div>
          <span>全选</span>
        </div>
        <div class="delete-btn" :class="{ 'disabled': selectedCount === 0 }" @click="onDeleteSelected">删除({{ selectedCount }})</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../store/modules/cart'
import { showToast, showConfirmDialog } from 'vant'

const router = useRouter(); const cartStore = useCartStore(); const isDetail = ref(false); const isEditing = ref(false); const currentMerchantId = ref(null)

const groupedCart = computed(() => {
  const groups = {}; cartStore.list.forEach(item => {
    const mId = item.merchantId
    if (!groups[mId]) groups[mId] = { id: mId, name: item.merchantName || '店铺', logo: item.merchantLogo || '', items: [], totalNum: 0, totalPrice: 0 }
    if (item.selected === undefined) item.selected = false
    groups[mId].items.push(item); groups[mId].totalNum += item.number; groups[mId].totalPrice += item.number * item.amount
  }); return groups
})
const currentMerchant = computed(() => groupedCart.value[currentMerchantId.value] || {})
const selectedCount = computed(() => (currentMerchant.value.items || []).filter(i => i.selected).length)
const allSelected = computed(() => currentMerchant.value.items?.length > 0 && currentMerchant.value.items.every(i => i.selected))

const onBack = () => { if (isDetail.value) { isDetail.value = false; currentMerchantId.value = null; isEditing.value = false } else router.back() }
const enterDetail = (g) => { currentMerchantId.value = g.id; isDetail.value = true }
const toggleEdit = () => { isEditing.value = !isEditing.value; if (!isEditing.value) currentMerchant.value.items?.forEach(i => i.selected = false) }
const toggleSelectAll = () => { const target = !allSelected.value; currentMerchant.value.items.forEach(i => i.selected = target) }
const onDeleteSelected = () => {
  const toDelete = currentMerchant.value.items.filter(i => i.selected)
  if (toDelete.length === 0) return
  showConfirmDialog({ title: '确认删除', message: `确定要删除这 ${toDelete.length} 件商品吗？` }).then(async () => {
    await cartStore.batchRemoveItems(toDelete); showToast('删除成功')
    if (currentMerchant.value.items.length === 0) isDetail.value = false
  })
}
const onCheckout = () => router.push({ path: '/order/create', query: { merchantId: currentMerchantId.value } })
onMounted(() => cartStore.fetchCartList())
</script>

<style scoped>
.cart-page { min-height: 100vh; background-color: #f5f5f5; display: flex; flex-direction: column; }
.cart-content { flex: 1; padding: 1.2rem; overflow-y: auto; }
.merchant-card { background: #fff; border-radius: 1.2rem; padding: 1.6rem; margin-bottom: 1.2rem; }
.m-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 1.2rem; }
.m-header-left { display: flex; align-items: center; gap: 0.8rem; }
.m-items-preview { display: flex; gap: 0.8rem; margin-bottom: 1.2rem; }
.total-price { color: #fb4e44; font-weight: 800; }
.shop-detail { padding-bottom: 8rem; }
.detail-item { background: #fff; border-radius: 1.2rem; padding: 1.6rem; margin-bottom: 1.2rem; display: flex; align-items: center; gap: 1.2rem; }
.item-info { flex: 1; min-height: 8rem; display: flex; flex-direction: column; justify-content: space-between; }
.item-price { color: #fb4e44; font-weight: 800; font-size: 1.8rem; }
.mt-checkbox { width: 2rem; height: 2rem; border-radius: 50%; border: 1px solid #ccc; background: #fff; display: flex; align-items: center; justify-content: center; }
.mt-checkbox.checked { background: #FFD000; border-color: #FFD000; }
.check-icon { font-size: 1.4rem; color: #222; opacity: 0; }
.mt-checkbox.checked .check-icon { opacity: 1; }
.cart-ctrl { display: flex; align-items: center; }
.btn-sub, .btn-add { width: 2.2rem; height: 2.2rem; border-radius: 50%; display: flex; align-items: center; justify-content: center; min-height: 2.2rem; }
.btn-sub { border: 1px solid #ccc; }
.btn-add { background: #ffd000; font-weight: 800; }
.num { width: 2.8rem; text-align: center; font-size: 1.3rem; }
.detail-footer-wrapper { position: fixed; bottom: 0; left: 0; right: 0; z-index: 100; }
.detail-footer, .manage-footer { min-height: 5.6rem; background: #fff; display: flex; align-items: center; padding: 0 1.6rem; box-shadow: 0 -0.2rem 1rem rgba(0,0,0,0.05); }
.total-amount { font-size: 2.2rem; font-weight: 800; color: #fb4e44; }
.checkout-btn { background: #ffd000; font-weight: 800; padding: 1rem 2.4rem; border-radius: 2.2rem; margin-left: auto; min-height: 4.4rem; display: flex; align-items: center; }
.manage-footer { justify-content: space-between; }
.select-all-wrap { display: flex; align-items: center; gap: 0.8rem; min-height: 4.4rem; }
.delete-btn { background: #ff3f3f; color: #fff; padding: 1rem 2.4rem; border-radius: 2.2rem; min-height: 4.4rem; display: flex; align-items: center; }
.delete-btn.disabled { opacity: 0.5; }
</style>
