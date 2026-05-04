import { onMounted } from 'vue'
import { useCategory } from '@/composables/useCategory'

export const useCategoryPage = () => {
  const categoryModel = useCategory()

  onMounted(() => {
    categoryModel.fetchList()
  })

  return categoryModel
}
