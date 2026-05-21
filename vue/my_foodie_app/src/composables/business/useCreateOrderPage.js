import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { closeToast, showLoadingToast, showToast } from 'vant'
import { getAddressDetail, getDefaultAddress } from '@/api/modules/address'
import { getMerchantDetail } from '@/api/modules/merchant'
import { submitOrder } from '@/api/modules/order'
import { useCartStore } from '@/stores/modules/cartStore'
import { formatFlavorText } from '@/utils/business/flavor'

// 确认订单页组合式，串联地址、商家、购物车与最终下单动作。
export const useCreateOrderPage = () => {
  const router = useRouter()
  const route = useRoute()
  const cartStore = useCartStore()

  const address = ref(null)
  const payMethod = ref(1)
  const remark = ref('')
  const merchantInfo = ref({})
  const submitting = ref(false)
  const packAmount = ref(0)

  const resolvedMerchantId = computed(() => (
    route.query.merchantId
    || cartStore.list[0]?.merchantId
    || merchantInfo.value.merchantId
    || merchantInfo.value.id
    || ''
  ))

  const estimatedTotal = computed(() => {
    const goodsTotal = cartStore.totalPrice
    const delivery = merchantInfo.value.deliveryFee || 0
    const pack = packAmount.value

    return (goodsTotal + delivery + pack).toFixed(2)
  })

  const initData = async () => {
    await cartStore.fetchCartList()
    if (cartStore.list.length === 0) {
      showToast('购物车为空')
      setTimeout(() => router.back(), 1500)
      return
    }

    const merchantId = resolvedMerchantId.value
    if (merchantId) {
      const merchantRes = await getMerchantDetail(merchantId)
      if (merchantRes.code === 1) {
        merchantInfo.value = merchantRes.data
      }
    }

    const queryAddressId = route.query.addressId
    if (queryAddressId) {
      const addressRes = await getAddressDetail(queryAddressId)
      if (addressRes.code === 1) {
        address.value = addressRes.data
      }
      return
    }

    const defaultAddressRes = await getDefaultAddress()
    if (defaultAddressRes.code === 1 && defaultAddressRes.data) {
      address.value = defaultAddressRes.data
    }
  }

  const toAddressList = () => {
    router.push({ path: '/address/list', query: { from: 'createOrder' } })
  }

  const onSubmit = async () => {
    if (!address.value) {
      showToast('请选择收货地址')
      return
    }

    if (!resolvedMerchantId.value) {
      showToast('商户信息加载中...')
      return
    }

    if (merchantInfo.value.status !== 1) {
      showToast('店铺已休息，无法下单')
      return
    }

    const minDeliveryAmount = merchantInfo.value.minDeliveryAmount || 0
    if (cartStore.totalPrice < minDeliveryAmount) {
      showToast(`未达到起送金额￥${minDeliveryAmount}`)
      return
    }

    submitting.value = true
    showLoadingToast({ message: '正在提交...', forbidClick: true })

    try {
      const orderData = {
        addressBookId: address.value.id,
        amount: Number(estimatedTotal.value),
        merchantId: resolvedMerchantId.value,
        packAmount: packAmount.value,
        payMethod: payMethod.value,
        remark: remark.value,
      }

      const res = await submitOrder(orderData)

      if (res.code === 1) {
        await cartStore.fetchCartList()
        closeToast()

        const orderNo = typeof res.data === 'string'
          ? res.data
          : res.data.orderNumber || res.data.id

        router.replace(`/order/${orderNo}`)
        return
      }

      showToast(res.msg || '下单失败')
    } catch (error) {
      console.error(error)
      showToast('系统异常')
    } finally {
      submitting.value = false
    }
  }

  onMounted(() => {
    initData()
  })

  return {
    address,
    cartStore,
    estimatedTotal,
    getFlavorText: formatFlavorText,
    merchantInfo,
    onSubmit,
    packAmount,
    payMethod,
    remark,
    submitting,
    toAddressList,
  }
}
