<template>
  <div class="mt-menu-container">
    <div class="mt-sidebar">
      <div v-for="(cat, index) in categories" :key="cat.id" class="mt-sidebar-item" :class="{ 'mt-sidebar-active': activeCategoryIndex === index }" @click="onCategoryChange(index)">
        <span class="mt-sidebar-text">{{ cat.name }}</span>
        <div v-if="getCategoryDishCount(cat)" class="mt-cat-badge">{{ getCategoryDishCount(cat) }}</div>
      </div>
    </div>
    <div class="mt-food-content">
      <div v-if="loading" class="mt-loading-box"><van-loading color="#FFD000" /></div>
      <div v-else class="mt-food-list">
        <div v-if="currentCategory" class="mt-section-title">{{ currentCategory.name }}</div>
        <div v-for="item in currentDishes" :key="item.id" class="mt-food-card">
          <van-image :src="item.image" width="92" height="92" radius="8" fit="cover" @click="emit('show-food-detail', item)" />
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
  let count = 0; cartStore.list.forEach(item => { if (item.categoryId === category.id) count += item.number });
  return count > 0 ? count : 0
}
const hasSpecs = (item) => !item.setmealId && item.flavors?.length > 0
</script>

<style scoped>
.mt-menu-container { flex: 1; display: flex; background-color: #fff; overflow: hidden; }
.mt-sidebar { width: 85px; background-color: #f7f7f7; overflow-y: auto; }
.mt-sidebar-item { padding: 18px 12px; font-size: 13px; color: #666; position: relative; line-height: 1.4; }
.mt-sidebar-active { background-color: #fff; color: #222; font-weight: 800; }
.mt-sidebar-active::before { content: ''; position: absolute; left: 0; top: 20px; bottom: 20px; width: 4px; background-color: #FFD000; border-radius: 0 2px 2px 0; }
.mt-cat-badge { position: absolute; top: 6px; right: 6px; background-color: #FF4B33; color: #fff; font-size: 9px; height: 14px; min-width: 14px; display: flex; align-items: center; justify-content: center; border-radius: 7px; padding: 0 3px; }
.mt-food-content { flex: 1; overflow-y: auto; padding-bottom: 100px; }
.mt-section-title { padding: 12px 16px; font-size: 12px; font-weight: 800; color: #333; background-color: #fff; position: sticky; top: 0; z-index: 10; }
.mt-food-card { display: flex; padding: 12px 16px; gap: 12px; }
.mt-food-info { flex: 1; display: flex; flex-direction: column; }
.mt-food-name { font-size: 16px; font-weight: 800; color: #222; margin-bottom: 4px; }
.mt-food-desc { font-size: 11px; color: #999; margin-bottom: 6px; line-height: 1.4; }
.mt-food-sales { font-size: 11px; color: #999; margin-top: auto; margin-bottom: 8px; }
.mt-food-bottom { display: flex; justify-content: space-between; align-items: center; }
.mt-food-price { color: #FB4E44; font-weight: 800; }
.mt-price-symbol { font-size: 12px; }
.mt-price-val { font-size: 19px; }
.mt-cart-ctrl { display: flex; align-items: center; }
.mt-ctrl-wrap { display: flex; align-items: center; }
.mt-btn-sub, .mt-btn-add { width: 22px; height: 22px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 12px; }
.mt-btn-sub { border: 1px solid #ccc; color: #999; background-color: #fff; }
.mt-btn-add { background-color: #FFD000; color: #222; font-weight: 800; }
.mt-ctrl-num { width: 28px; text-align: center; font-size: 14px; font-weight: 500; }
.mt-spec-btn { font-size: 11px; padding: 0 8px; }
.mt-btn-add:has(.mt-spec-btn) { width: auto; height: 24px; border-radius: 12px; }
.mt-slide-enter-active, .mt-slide-leave-active { transition: all 0.3s ease; }
.mt-slide-enter-from, .mt-slide-leave-to { opacity: 0; transform: translateX(20px) rotate(180deg); }
</style>
