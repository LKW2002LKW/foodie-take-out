<template>
  <div class="merchant-detail mobile-page" :class="{ 'has-cart-bar': shouldShowCartBar }">
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
      v-if="shouldShowCartBar"
      :merchant-info="merchantInfo"
      :total-num="currentMerchantTotalNum"
      :total-price="currentMerchantTotalPrice"
      @checkout="onCheckout"
      @toggle-cart-popup="toggleCartPopup"
    />

    <van-popup
      v-if="shouldShowCartBar"
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
          <van-image
            class="ci-cover"
            :src="item.image || item.pic"
            width="4.8rem"
            height="4.8rem"
            fit="cover"
            radius="0.8rem"
          >
            <template #error>
              <div class="ci-cover-fallback">
                <van-icon name="photo-o" size="1.8rem" />
              </div>
            </template>
          </van-image>
          <div class="ci-info">
            <div class="ci-name">{{ item.name }}</div>
            <div v-if="item.dishFlavor" class="ci-spec">{{ formatFlavor(item.dishFlavor) }}</div>
          </div>
          <div class="ci-price">￥{{ item.amount * item.number }}</div>
          <div class="ci-control">
            <div class="btn-sub" @click="onSubItem(item)"><van-icon name="minus" /></div>
            <div class="num">{{ item.number }}</div>
            <div class="btn-add" @click="onAddCartPopupItem(item)"><van-icon name="plus" /></div>
          </div>
        </div>
        <van-empty v-if="currentMerchantCart.length === 0" description="购物车为空" />
      </div>
    </van-popup>

    <flavor-picker
      :v-model:show="showFlavorPicker"
      :food="selectedFood"
      @confirm="onConfirmFlavor"
    />
  </div>
</template>

<script setup>
import { useMerchantDetailPage } from '@/composables/business/useMerchantDetailPage'
import MerchantHeader from '@/pages/merchant/components/MerchantHeader.vue'
import DishList from '@/pages/merchant/components/DishList.vue'
import BottomCartBar from '@/pages/merchant/components/BottomCartBar.vue'
import ReviewCard from '@/components/business/ReviewCard.vue'
import FlavorPicker from '@/pages/merchant/components/FlavorPicker.vue'

const {
  activeRatingFilter,
  activeTab,
  categories,
  currentDishes,
  currentMerchantCart,
  currentMerchantTotalNum,
  currentMerchantTotalPrice,
  formatFlavor,
  hasImageFilter,
  loading,
  loadReviewList,
  merchantInfo,
  onAddCartPopupItem,
  onAddItem,
  onCategoryChange,
  onCheckout,
  onClearCart,
  onConfirmFlavor,
  onReviewRefresh,
  onSubItem,
  onTabChange,
  reviewFinished,
  reviewList,
  reviewLoading,
  reviewRefreshing,
  reviewStatItems,
  selectedFood,
  setRatingFilter,
  shouldShowCartBar,
  showCartPopup,
  showFlavorPicker,
  showFoodDetail,
  toggleCartPopup,
  toggleHasImageFilter,
} = useMerchantDetailPage()
</script>

<style scoped>
.merchant-detail {
  padding-bottom: 0;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--mt-page-bg);
}

.merchant-detail.has-cart-bar {
  padding-bottom: calc(5rem + env(safe-area-inset-bottom));
}

:deep(.van-tabs) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

:deep(.van-tabs__nav) {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(255, 250, 239, 0.98) 100%);
  box-shadow: 0 0.4rem 1.2rem rgba(245, 194, 0, 0.06);
}

:deep(.van-tab--active) {
  font-weight: 800;
}

:deep(.van-tabs__line) {
  background: linear-gradient(90deg, var(--primary-color-dark), var(--primary-color));
  height: 0.3rem;
  border-radius: 999px;
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
  background: var(--mt-page-bg);
  min-height: 100%;
}

.review-stats-card,
.review-filter-card {
  background: linear-gradient(180deg, #FFFFFF 0%, #FFFDF7 100%);
  border-radius: 1.6rem;
  padding: 1rem;
  margin-bottom: 1rem;
  box-shadow: 0 0.8rem 2rem rgba(245, 194, 0, 0.08);
  border: 1px solid rgba(245, 194, 0, 0.12);
}

.review-section-title {
  font-size: 1.4rem;
  font-weight: 700;
  color: var(--mt-strong);
  margin-bottom: 0.8rem;
}

.review-stats-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.8rem;
}

.stat-item {
  padding: 0.8rem 1rem;
  border-radius: 1.2rem;
  background: linear-gradient(180deg, #FFF9E6 0%, #FFF5D6 100%);
  border: 1px solid rgba(245, 194, 0, 0.12);
}

.stat-value {
  font-size: 1.4rem;
  font-weight: 700;
  color: var(--mt-strong);
}

.stat-label {
  margin-top: 0.2rem;
  font-size: 1.2rem;
  color: var(--text-color-secondary);
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
  background: linear-gradient(180deg, #FFFDF8 0%, #FFF4D1 100%);
  color: var(--mt-strong);
  font-size: 1.3rem;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.18s ease;
  user-select: none;
  border: 1px solid rgba(245, 194, 0, 0.14);
}

.filter-pill.active {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-dark) 100%);
  color: var(--mt-strong);
  font-weight: 800;
  box-shadow: 0 0.6rem 1.2rem rgba(245, 194, 0, 0.2);
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
  background: linear-gradient(180deg, #FFFFFF 0%, #FFF9EB 100%);
  border-bottom: 1px solid rgba(245, 194, 0, 0.12);
  align-items: center;
}

.cart-title {
  font-size: 1.6rem;
  font-weight: bold;
}

.cart-clear {
  color: var(--text-color-secondary);
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
  gap: 1rem;
  padding: 1.2rem 1.6rem;
  border-bottom: 1px solid rgba(245, 194, 0, 0.1);
}

.ci-cover {
  flex-shrink: 0;
  border: 1px solid rgba(245, 194, 0, 0.14);
  box-shadow: 0 0.2rem 0.6rem rgba(245, 194, 0, 0.1);
}

.ci-cover-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #b6923e;
  background: linear-gradient(180deg, #FFF9E8 0%, #FFF1CC 100%);
}

.ci-info {
  flex: 1;
  min-width: 0;
}

.ci-name {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--mt-strong);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.ci-spec {
  font-size: 1.2rem;
  color: var(--mt-muted);
  margin-top: 0.2rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.ci-price {
  width: 6.4rem;
  text-align: right;
  color: var(--van-danger-color);
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
  width: 2.6rem;
  height: 2.6rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.btn-sub {
  border: 0.14rem solid #f5c200;
  background: var(--mt-card-bg);
  color: var(--mt-strong);
  box-shadow: 0 0.2rem 0.5rem rgba(245, 194, 0, 0.12);
}

.btn-add {
  background: linear-gradient(180deg, #ffe27f 0%, #f5c200 100%);
  color: var(--mt-strong);
  font-weight: 800;
  box-shadow: 0 0.3rem 0.8rem rgba(245, 194, 0, 0.24);
}

.btn-sub:active,
.btn-add:active {
  transform: scale(0.96);
}

.btn-sub :deep(.van-icon),
.btn-add :deep(.van-icon) {
  font-size: 1.3rem;
  font-weight: 900;
}
</style>
