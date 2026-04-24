<template>
  <div class="mt-menu-container">
    <div class="mt-sidebar">
      <div v-for="(cat, index) in categories" :key="cat.id" class="mt-sidebar-item" :class="{ 'mt-sidebar-active': activeCategoryIndex === index }" @click="onCategoryChange(index)">
        <span class="mt-sidebar-text">{{ cat.name }}</span>
        <div v-if="getCategoryDishCount(cat)" class="mt-cat-badge">{{ getCategoryDishCount(cat) }}</div>
      </div>
    </div>
    <div class="mt-food-content">
      <div v-if="loading" class="mt-loading-box"><van-loading :color="'var(--primary-color)'" /></div>
      <div v-else class="mt-food-list">
        <div v-if="currentCategory" class="mt-section-title">{{ currentCategory.name }}</div>
        <div v-for="item in currentDishes" :key="item.id" class="mt-food-card">
          <van-image
            :src="item.image"
            width="9.2rem"
            height="9.2rem"
            radius="1.2rem"
            fit="cover"
            @click="emit('show-food-detail', item)"
          />
          <div class="mt-food-info">
            <div class="mt-food-name">{{ item.name }}</div>
            <div class="mt-food-desc van-multi-ellipsis--l2">{{ item.description }}</div>
            <div class="mt-food-sales">月售 {{ item.sales || item.salesVolume || 0 }}</div>
            <div class="mt-food-bottom">
              <div class="mt-food-price"><span class="mt-price-symbol">￥</span><span class="mt-price-val">{{ item.price }}</span></div>
              <div class="mt-cart-ctrl">
                <transition name="mt-slide">
                  <div v-if="getDishCountInCart(item) > 0" class="mt-ctrl-wrap">
                    <div class="mt-btn-sub" @click.stop="emit('sub-item', item)"><van-icon name="minus" /></div>
                    <span class="mt-ctrl-num">{{ getDishCountInCart(item) }}</span>
                  </div>
                </transition>
                <div class="mt-btn-add" @click.stop="emit('add-item', item)">
                  <van-icon name="plus" />
                  <span v-if="hasSpecs(item) && getDishCountInCart(item) === 0" class="mt-spec-btn">选规格</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useCartStore } from '../store/modules/cart'
const props = defineProps({ categories: Array, currentDishes: Array, loading: Boolean })
const emit = defineEmits(['category-change', 'show-food-detail', 'add-item', 'sub-item'])
const cartStore = useCartStore()
const activeCategoryIndex = ref(0)
const currentCategory = computed(() => props.categories[activeCategoryIndex.value])
const onCategoryChange = (index) => { activeCategoryIndex.value = index; emit('category-change', props.categories[index]); }
const getDishCountInCart = (item) => {
  const id = item.setmealId || item.id
  return cartStore.getCartItemCount(!!item.setmealId ? null : id, !!item.setmealId ? id : null)
}
const getCategoryDishCount = (category) => {
  let count = 0
  cartStore.list.forEach(item => {
    if (String(item.categoryId) === String(category.id)) {
      count += Number(item.number || 0)
    }
  })
  return count > 0 ? count : 0
}
const hasSpecs = (item) => !item.setmealId && item.flavors?.length > 0
</script>

<style scoped>
.mt-menu-container {
  flex: 1;
  display: flex;
  background-color: var(--mt-card-bg);
  overflow: hidden;
}

.mt-sidebar {
  width: 8.5rem;
  background: linear-gradient(180deg, #FFF9E8 0%, #FFF3CF 100%);
  overflow-y: auto;
}

.mt-sidebar-item {
  padding: 1.8rem 1.2rem;
  font-size: 1.3rem;
  color: var(--text-color-secondary);
  position: relative;
  line-height: 1.4;
}

.mt-sidebar-active {
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.96) 0%, rgba(255, 248, 235, 0.98) 100%);
  color: var(--mt-strong);
  font-weight: 900;
}

.mt-sidebar-active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 2rem;
  bottom: 2rem;
  width: 0.4rem;
  background-color: var(--primary-color);
  border-radius: 0 0.2rem 0.2rem 0;
}

.mt-cat-badge {
  position: absolute;
  top: 0.7rem;
  right: 0.7rem;
  background: linear-gradient(180deg, #ff6b6b 0%, #ee0a24 100%);
  color: #fff;
  font-size: 1rem;
  font-weight: 700;
  height: 1.8rem;
  min-width: 1.8rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  padding: 0 0.5rem;
  box-shadow: 0 0.2rem 0.6rem rgba(238, 10, 36, 0.28);
  border: 0.1rem solid rgba(255, 255, 255, 0.95);
  line-height: 1;
}

.mt-food-content {
  flex: 1;
  overflow-y: auto;
  padding-bottom: calc(10rem + env(safe-area-inset-bottom));
}

.mt-loading-box {
  padding: 2.4rem 0;
  display: flex;
  justify-content: center;
}

.mt-section-title {
  padding: 1.2rem 1.6rem;
  font-size: 1.2rem;
  font-weight: 900;
  color: var(--mt-strong);
  background-color: var(--mt-card-bg);
  position: sticky;
  top: 0;
  z-index: 10;
  border-bottom: 1px solid var(--mt-divider);
}

.mt-food-card {
  display: flex;
  padding: 1.2rem 1.6rem;
  gap: 1.2rem;
}

.mt-food-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.mt-food-name {
  font-size: 1.6rem;
  font-weight: 900;
  color: var(--mt-strong);
  margin-bottom: 0.4rem;
}

.mt-food-desc {
  font-size: 1.1rem;
  color: var(--mt-muted);
  margin-bottom: 0.6rem;
  line-height: 1.4;
}

.mt-food-sales {
  font-size: 1.1rem;
  color: var(--mt-muted);
  margin-top: auto;
  margin-bottom: 0.8rem;
}

.mt-food-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mt-food-price {
  color: var(--van-danger-color);
  font-weight: 900;
}

.mt-price-symbol {
  font-size: 1.2rem;
}

.mt-price-val {
  font-size: 1.9rem;
}

.mt-cart-ctrl {
  display: flex;
  align-items: center;
}

.mt-ctrl-wrap {
  display: flex;
  align-items: center;
}

.mt-btn-sub,
.mt-btn-add {
  width: 2.6rem;
  height: 2.6rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  box-sizing: border-box;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.mt-btn-sub {
  border: 0.14rem solid #f5c200;
  color: var(--mt-strong);
  background: var(--mt-card-bg);
  box-shadow: 0 0.2rem 0.5rem rgba(245, 194, 0, 0.1);
}

.mt-btn-add {
  background: linear-gradient(180deg, #ffe27f 0%, #f5c200 100%);
  color: var(--mt-strong);
  font-weight: 900;
  box-shadow: 0 0.3rem 0.8rem rgba(245, 194, 0, 0.24);
}

.mt-btn-sub:active,
.mt-btn-add:active {
  transform: scale(0.96);
}

.mt-ctrl-num {
  width: 2.8rem;
  text-align: center;
  font-size: 1.4rem;
  font-weight: 600;
}

.mt-spec-btn {
  font-size: 1.1rem;
  padding: 0 0.8rem;
}

.mt-btn-add:has(.mt-spec-btn) {
  width: auto;
  min-width: 2.6rem;
  border-radius: 1.4rem;
  padding: 0 0.8rem 0 0.4rem;
}

.mt-btn-sub :deep(.van-icon),
.mt-btn-add :deep(.van-icon) {
  font-size: 1.3rem;
  font-weight: 900;
}

.mt-slide-enter-active,
.mt-slide-leave-active {
  transition: all 0.3s ease;
}

.mt-slide-enter-from,
.mt-slide-leave-to {
  opacity: 0;
  transform: translateX(2rem) rotate(180deg);
}
</style>
