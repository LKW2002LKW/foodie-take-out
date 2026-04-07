<template>
  <div class="merchant-detail mobile-page">
    <van-nav-bar left-arrow title="店铺详情" fixed placeholder @click-left="$router.back()" />
    <merchant-header :merchant-info="merchantInfo" />

    <van-tabs :active="activeTab" sticky offset-top="46" shrink @change="onTabChange">
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
        <div class="review-tab">
          <div class="review-stats-card">
            <div class="review-section-title">评价统计</div>
            <div v-if="reviewStatItems.length" class="review-stats-grid">
              <div v-for="item in reviewStatItems" :key="item.key" class="stat-item">
                <div class="stat-value">{{ item.value }}</div>
                <div class="stat-label">{{ item.label }}</div>
              </div>
            </div>
            <van-empty v-else description="暂无统计数据" />
          </div>

          <div class="review-filter-card">
            <div class="review-section-title">筛选</div>
            <div class="review-filter-row">
              <div
                class="filter-pill"
                :class="{ active: activeRatingFilter === 0 && !hasImageFilter }"
                @click="setRatingFilter(0)"
              >
                全部
              </div>
              <div
                class="filter-pill"
                :class="{ active: activeRatingFilter === 5 }"
                @click="setRatingFilter(5)"
              >
                好评
              </div>
              <div
                class="filter-pill"
                :class="{ active: activeRatingFilter === 3 }"
                @click="setRatingFilter(3)"
              >
                中评
              </div>
              <div
                class="filter-pill"
                :class="{ active: activeRatingFilter === 1 }"
                @click="setRatingFilter(1)"
              >
                差评
              </div>
              <div
                class="filter-pill"
                :class="{ active: hasImageFilter }"
                @click="toggleHasImageFilter"
              >
                有图
              </div>
            </div>
          </div>

          <van-pull-refresh v-model="reviewRefreshing" @refresh="onReviewRefresh">
            <van-list
              :loading="reviewLoading"
              :finished="reviewFinished"
              finished-text="没有更多了"
              @load="loadReviewList"
            >
              <div class="review-list">
                <ReviewCard v-for="item in reviewList" :key="item.id" :review="item" />
              </div>
              <van-empty v-if="reviewFinished && reviewList.length === 0" description="暂无评价" />
            </van-list>
          </van-pull-refresh>
        </div>
      </van-tab>
    </van-tabs>

    <bottom-cart-bar
      v-if="activeTab === 0"
      :merchant-info="merchantInfo"
      @checkout="onCheckout"
      @toggle-cart-popup="toggleCartPopup"
    />

    <van-popup
      v-if="activeTab === 0"
      :show="showCartPopup"
      @update:show="showCartPopup = $event"
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
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showConfirmDialog } from 'vant'
import { useCartStore } from '../store/modules/cart'
import { getMerchantDetail, getMerchantCategories } from '../services/merchant'
import { getDishList } from '../services/dish'
import { getSetmealList } from '../services/setmeal'
import { getReviewPage, getReviewStats } from '../services/review'
import MerchantHeader from '../components/MerchantHeader.vue'
import DishList from '../components/DishList.vue'
import BottomCartBar from '../components/BottomCartBar.vue'
import ReviewCard from '../components/ReviewCard.vue'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const merchantId = route.params.id

const activeTab = ref(0)
const merchantInfo = ref(null)
const categories = ref([])
const currentDishes = ref([])
const loading = ref(false)
const showCartPopup = ref(false)

const reviewStats = ref({})
const reviewList = ref([])
const reviewLoading = ref(false)
const reviewFinished = ref(false)
const reviewRefreshing = ref(false)
const reviewPage = ref(1)
const reviewPageSize = 8
const activeRatingFilter = ref(0)
const hasImageFilter = ref(false)
const reviewInitialized = ref(false)

const statLabelMap = {
  total: '总评价数',
  totalCount: '总评价数',
  count: '总评价数',
  averageRating: '平均评分',
  avgRating: '平均评分',
  ratingAvg: '平均评分',
  goodCount: '好评数',
  mediumCount: '中评数',
  badCount: '差评数',
  imageCount: '有图数',
  hasImageCount: '有图数',
  replyCount: '已回复数',
  replyRate: '回复率',
}

const currentMerchantCart = computed(() => cartStore.list.filter(item => item.merchantId == merchantId))

const reviewStatItems = computed(() => {
  const entries = Object.entries(reviewStats.value || {})
  return entries
    .filter(([, value]) => ['string', 'number', 'boolean'].includes(typeof value))
    .map(([key, value]) => ({
      key,
      label: statLabelMap[key] || key,
      value,
    }))
})

const normalizeReviewImages = (imageList) => {
  if (!imageList) return []
  if (Array.isArray(imageList)) return imageList.filter(Boolean)
  return String(imageList)
    .split(',')
    .map(item => item.trim())
    .filter(Boolean)
}

const loadMerchant = async () => {
  try {
    const res = await getMerchantDetail(merchantId)
    if (res.code === 1) merchantInfo.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const loadCategories = async () => {
  try {
    const res = await getMerchantCategories(merchantId)
    if (res.code === 1) {
      categories.value = res.data || []
      if (categories.value.length > 0) loadDishes(categories.value[0])
    }
  } catch (error) {
    console.error(error)
  }
}

const onCategoryChange = (cat) => loadDishes(cat)

const loadDishes = async (cat) => {
  loading.value = true
  try {
    const res = cat.type == 2
      ? await getSetmealList({ categoryId: cat.id, merchantId, status: 1 })
      : await getDishList({ categoryId: cat.id, merchantId, status: 1 })
    if (res.code === 1) {
      currentDishes.value = cat.type == 2
        ? (res.data || []).map(item => ({ ...item, setmealId: item.id, image: item.image || item.pic }))
        : (res.data || [])
    }
  } finally {
    loading.value = false
  }
}

const loadReviewStatsData = async () => {
  try {
    const res = await getReviewStats(Number(merchantId))
    if (res.code === 1) {
      reviewStats.value = res.data || {}
    }
  } catch (error) {
    console.error(error)
  }
}

const onTabChange = (name) => {
  activeTab.value = name
  if (name !== 0) {
    showCartPopup.value = false
  }
}

const loadReviewList = async () => {
  if (reviewLoading.value) return

  reviewLoading.value = true
  try {
    if (reviewRefreshing.value) {
      reviewList.value = []
      reviewPage.value = 1
      reviewFinished.value = false
      reviewRefreshing.value = false
    }

    if (reviewFinished.value) return

    const params = {
      merchantId: Number(merchantId),
      page: reviewPage.value,
      pageSize: reviewPageSize,
    }

    if (activeRatingFilter.value !== 0) {
      params.ratingFilter = activeRatingFilter.value
    }

    if (hasImageFilter.value) {
      params.hasImage = true
    }

    const res = await getReviewPage(params)
    if (res.code === 1) {
      const data = res.data || {}
      const list = (data.list || []).map(item => ({
        ...item,
        imageList: normalizeReviewImages(item.imageList),
      }))

      reviewList.value.push(...list)

      if (data.hasNext === false || list.length < reviewPageSize || reviewList.value.length >= (data.total || 0)) {
        reviewFinished.value = true
      } else {
        reviewPage.value += 1
      }
      reviewInitialized.value = true
      return
    }

    reviewFinished.value = true
  } catch (error) {
    console.error(error)
    reviewFinished.value = true
  } finally {
    reviewLoading.value = false
    reviewRefreshing.value = false
  }
}

const initReviewTab = async () => {
  await loadReviewStatsData()
  await loadReviewList()
}

const setRatingFilter = async (value) => {
  activeRatingFilter.value = value
  reviewRefreshing.value = true
  await loadReviewList()
}

const toggleHasImageFilter = async () => {
  hasImageFilter.value = !hasImageFilter.value
  reviewRefreshing.value = true
  await loadReviewList()
}

const onReviewRefresh = async () => {
  reviewRefreshing.value = true
  await loadReviewList()
}

watch(activeTab, async (tab) => {
  if (tab === 1 && !reviewInitialized.value) {
    await initReviewTab()
  }
})

const onAddItem = (item) => cartStore.addToCart(item, null, Number(merchantId))
const onSubItem = (item) => cartStore.subFromCart(item)
const onCheckout = () => router.push({ path: '/order/create', query: { merchantId } })
const toggleCartPopup = () => { if (currentMerchantCart.value.length > 0) showCartPopup.value = !showCartPopup.value }
const onClearCart = () => {
  showConfirmDialog({ title: '确认清空吗？' }).then(() => {
    cartStore.clearCartAction(Number(merchantId))
    showCartPopup.value = false
  })
}
const formatFlavor = (str) => { try { return JSON.parse(str).map(f => f.value).join(',') } catch (error) { return str } }
const showFoodDetail = (item) => { /* logic for food detail popup if needed */ }

onMounted(() => {
  loadMerchant()
  loadCategories()
  cartStore.fetchCartList(merchantId)
  if (activeTab.value === 1) {
    initReviewTab()
  }
})
</script>

<style scoped>
.merchant-detail {
  padding-bottom: calc(5rem + env(safe-area-inset-bottom));
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #fff;
}

:deep(.van-tabs) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

:deep(.van-tabs__content) {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

:deep(.van-tab__pane) {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.review-tab {
  padding: 1.2rem;
  background: #f8fafc;
  min-height: 100%;
}

.review-stats-card,
.review-filter-card {
  background: #fff;
  border-radius: 1.2rem;
  padding: 1rem;
  margin-bottom: 1rem;
  box-shadow: 0 0.6rem 1.4rem rgba(15, 23, 42, 0.05);
}

.review-section-title {
  font-size: 1.4rem;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 0.8rem;
}

.review-stats-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.8rem;
}

.stat-item {
  padding: 0.8rem 1rem;
  border-radius: 999px;
  background: #f2f3f5;
  border: none;
}

.stat-value {
  font-size: 1.4rem;
  font-weight: 700;
  color: #111827;
}

.stat-label {
  margin-top: 0.2rem;
  font-size: 1.2rem;
  color: #4b5563;
}

.review-filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 0.8rem;
}

.filter-pill {
  min-height: 4.4rem;
  padding: 0 1.4rem;
  border-radius: 999px;
  background: #f2f3f5;
  color: #111827;
  font-size: 1.3rem;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.18s ease;
  user-select: none;
}

.filter-pill.active {
  background: #fff4cc;
  color: #ff7a00;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
}

.cart-popup {
  padding-bottom: calc(2rem + env(safe-area-inset-bottom));
}

.cart-header {
  display: flex;
  justify-content: space-between;
  padding: 1.2rem 1.6rem;
  background: #fcfcfc;
  border-bottom: 1px solid #eee;
  align-items: center;
}

.cart-title {
  font-size: 1.6rem;
  font-weight: bold;
}

.cart-clear {
  color: #666;
  font-size: 1.2rem;
  display: flex;
  align-items: center;
  gap: 0.4rem;
  min-height: 4.4rem;
}

.cart-item-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.6rem;
  border-bottom: 1px solid #f9f9f9;
}

.ci-info {
  flex: 1;
}

.ci-name {
  font-size: 1.5rem;
  font-weight: 500;
}

.ci-spec {
  font-size: 1.2rem;
  color: #999;
}

.ci-price {
  width: 7rem;
  text-align: right;
  color: #fb4e44;
  font-weight: 800;
}

.ci-control {
  display: flex;
  align-items: center;
  margin-left: 1.6rem;
}

.ci-control .num {
  margin: 0 1rem;
  min-width: 1.6rem;
  text-align: center;
}

.btn-sub,
.btn-add {
  width: 2.4rem;
  height: 2.4rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-sub {
  border: 1px solid #ccc;
}

.btn-add {
  background: #ffd000;
  font-weight: 800;
}
</style>
