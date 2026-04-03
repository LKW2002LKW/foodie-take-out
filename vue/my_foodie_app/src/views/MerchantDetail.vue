<template>
  <div class="merchant-detail">
    <van-nav-bar left-arrow title="店铺详情" fixed placeholder @click-left="$router.back()" />
    <merchant-header :merchant-info="merchantInfo" />
    <van-tabs v-model:active="activeTab" sticky offset-top="46" shrink>
      <van-tab title="点餐">
        <dish-list 
          :categories="categories" 
          :current-dishes="currentDishes" 
          :loading="loading" 
          @category-change="onCategoryChange" 
          @show-food-detail="showFoodDetail" 
          @add-item="onAddItem" 
          @sub-item="onSubItem" 
        />
      </van-tab>
      <van-tab title="评价">
        <div class="review-container">
          <van-empty description="暂无评价" />
        </div>
      </van-tab>
    </van-tabs>

    <bottom-cart-bar 
      :merchant-info="merchantInfo" 
      @checkout="onCheckout" 
      @toggle-cart-popup="toggleCartPopup" 
    />

    <!-- 购物车详情弹窗 -->
    <van-popup
      v-model:show="showCartPopup"
      position="bottom"
      round
      class="cart-popup safe-area-bottom"
      :style="{ maxHeight: '50%' }"
    >
      <div class="cart-header">
        <div class="cart-title">购物车</div>
        <div class="cart-clear" @click="onClearCart">
          <van-icon name="delete-o" />
          <span>清空</span>
        </div>
      </div>
      <div class="cart-list">
        <div v-for="(item, idx) in currentMerchantCart" :key="idx" class="cart-item-row">
          <div class="ci-info">
            <div class="ci-name">{{ item.name }}</div>
            <div v-if="item.dishFlavor" class="ci-spec">{{ formatFlavor(item.dishFlavor) }}</div>
          </div>
          <div class="ci-price">￥{{ item.amount * item.number }}</div>
          <div class="ci-control">
            <div class="btn-sub" @click="cartStore.subFromCart(item)"><van-icon name="minus" /></div>
            <div class="num">{{ item.number }}</div>
            <div class="btn-add" @click="cartStore.addToCart(item, null, Number(merchantId))"><van-icon name="plus" /></div>
          </div>
        </div>
        <van-empty v-if="currentMerchantCart.length === 0" description="购物车为空" />
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { useCartStore } from '../store/modules/cart'
import { getMerchantDetail, getMerchantCategories } from '../services/merchant'
import { getDishList } from '../services/dish'
import { getSetmealList } from '../services/setmeal'
import MerchantHeader from '../components/MerchantHeader.vue'
import DishList from '../components/DishList.vue'
import BottomCartBar from '../components/BottomCartBar.vue'

const route = useRoute(); const router = useRouter(); const cartStore = useCartStore(); const merchantId = route.params.id
const activeTab = ref(0); const merchantInfo = ref(null); const categories = ref([]); const currentDishes = ref([]); const loading = ref(false)
const showCartPopup = ref(false)

const currentMerchantCart = computed(() => cartStore.list.filter(item => item.merchantId == merchantId))

const loadMerchant = async () => { try { const res = await getMerchantDetail(merchantId); if (res.code === 1) merchantInfo.value = res.data } catch(e){} }
const loadCategories = async () => { try { const res = await getMerchantCategories(merchantId); if (res.code === 1) { categories.value = res.data || []; if (categories.value.length > 0) loadDishes(categories.value[0]) } } catch(e){} }
const onCategoryChange = (cat) => loadDishes(cat)
const loadDishes = async (cat) => {
  loading.value = true; try {
    const res = cat.type == 2 ? await getSetmealList({ categoryId: cat.id, merchantId, status: 1 }) : await getDishList({ categoryId: cat.id, merchantId, status: 1 })
    if (res.code === 1) currentDishes.value = cat.type == 2 ? res.data.map(i => ({ ...i, setmealId: i.id, image: i.image || i.pic })) : res.data
  } finally { loading.value = false }
}

const onAddItem = (item) => cartStore.addToCart(item, null, Number(merchantId))
const onSubItem = (item) => cartStore.subFromCart(item)
const onCheckout = () => router.push({ path: '/order/create', query: { merchantId } })
const toggleCartPopup = () => { if (currentMerchantCart.value.length > 0) showCartPopup.value = !showCartPopup.value }
const onClearCart = () => { showConfirmDialog({ title: '确认清空吗？' }).then(() => { cartStore.clearCartAction(Number(merchantId)); showCartPopup.value = false }) }
const formatFlavor = (str) => { try { return JSON.parse(str).map(f => f.value).join(',') } catch(e) { return str } }
const showFoodDetail = (item) => { /* logic for food detail popup if needed */ }

onMounted(() => { loadMerchant(); loadCategories(); cartStore.fetchCartList(merchantId) })
</script>

<style scoped>
.merchant-detail { padding-bottom: 50px; height: 100vh; display: flex; flex-direction: column; background: #fff; }
:deep(.van-tabs) { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
:deep(.van-tabs__content) { flex: 1; overflow: hidden; display: flex; flex-direction: column; }
:deep(.van-tab__pane) { height: 100%; display: flex; flex-direction: column; overflow-y: auto; }

.cart-popup { padding-bottom: 20px; }
.cart-header { display: flex; justify-content: space-between; padding: 12px 16px; background: #fcfcfc; border-bottom: 1px solid #eee; align-items: center; }
.cart-title { font-size: 16px; font-weight: bold; }
.cart-clear { color: #666; font-size: 12px; display: flex; align-items: center; gap: 4px; }
.cart-item-row { display: flex; justify-content: space-between; align-items: center; padding: 16px; border-bottom: 1px solid #f9f9f9; }
.ci-info { flex: 1; }
.ci-name { font-size: 15px; font-weight: 500; }
.ci-spec { font-size: 11px; color: #999; }
.ci-price { width: 70px; text-align: right; color: #fb4e44; font-weight: 800; }
.ci-control { display: flex; align-items: center; margin-left: 16px; }
.ci-control .num { margin: 0 10px; min-width: 16px; text-align: center; }
.btn-sub, .btn-add { width: 22px; height: 22px; border-radius: 50%; display: flex; align-items: center; justify-content: center; }
.btn-sub { border: 1px solid #ccc; }
.btn-add { background: #ffd000; font-weight: 800; }
</style>
