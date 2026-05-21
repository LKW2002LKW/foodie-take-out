import { onMounted } from 'vue'
import { useCategory } from '@/composables/useCategory'

// 商品分类页面总控组合式，仅负责装配通用分类模型并触发首屏加载。
export const useCategoryPage = () => {
  const categoryModel = useCategory()

  onMounted(() => {
    categoryModel.fetchList()
  })

  return categoryModel
}
