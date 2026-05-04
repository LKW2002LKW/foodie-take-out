import { onMounted } from 'vue'
import { useProduct } from '@/composables/useProduct'

export const useProductListPage = () => {
  const productModel = useProduct()

  onMounted(() => {
    productModel.fetchCategoryList()
    productModel.fetchDishList()
  })

  return productModel
}
