import { onMounted } from 'vue'
import { useMerchant } from '@/composables/useMerchant'

// 商户管理页总控组合式，仅装配通用商户管理模型并触发首屏加载。
export const useMerchantPage = () => {
  const merchantModel = useMerchant()

  onMounted(() => {
    merchantModel.getList()
  })

  return merchantModel
}
