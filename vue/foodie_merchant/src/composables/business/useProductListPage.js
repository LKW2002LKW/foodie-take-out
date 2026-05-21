import { onMounted } from 'vue'
import { useProduct } from '@/composables/useProduct'

// 商品列表页总控组合式，装配通用商品模型并触发分类、列表初始化。
export const useProductListPage = () => {
  const productModel = useProduct()

  onMounted(() => {
    productModel.fetchCategoryList()
    productModel.fetchDishList()
  })

  return productModel
}
