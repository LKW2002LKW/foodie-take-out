import { onMounted } from 'vue'
import { useMerchant } from '@/composables/useMerchant'

export const useMerchantPage = () => {
  const merchantModel = useMerchant()

  onMounted(() => {
    merchantModel.getList()
  })

  return merchantModel
}
