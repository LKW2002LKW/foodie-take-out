import { computed, ref } from 'vue'
import { useCartStore } from '@/stores/modules/cartStore'

// 店铺详情中的菜品列表逻辑，负责分类切换与购物车数量映射。
export const useDishList = (props, emit) => {
  const cartStore = useCartStore()
  const activeCategoryIndex = ref(0)

  const currentCategory = computed(() => props.categories[activeCategoryIndex.value])

  const onCategoryChange = (index) => {
    activeCategoryIndex.value = index
    emit('category-change', props.categories[index])
  }

  const getDishCountInCart = (item) => {
    const id = item.setmealId || item.id
    return cartStore.getCartItemCount(!!item.setmealId ? null : id, !!item.setmealId ? id : null)
  }

  const getCategoryDishCount = (category) => {
    let count = 0
    cartStore.list.forEach((item) => {
      if (String(item.categoryId) === String(category.id)) {
        count += Number(item.number || 0)
      }
    })
    return count > 0 ? count : 0
  }

  const hasSpecs = (item) => !item.setmealId && item.flavors?.length > 0

  return {
    activeCategoryIndex,
    currentCategory,
    getCategoryDishCount,
    getDishCountInCart,
    hasSpecs,
    onCategoryChange,
  }
}
