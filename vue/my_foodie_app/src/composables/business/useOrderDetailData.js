import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { closeToast, showLoadingToast, showToast } from 'vant'
import { orderStatusTextMap } from '@/constants/order'
import { getOrderDetail, getOrderDetailByNumber, getOrderTrack } from '@/api/modules/order'

// 订单详情数据层，负责详情加载、状态映射与配送轨迹获取。
export const useOrderDetailData = (routeId) => {
  const router = useRouter()

  const order = ref({})
  const payTypeModel = ref(1)
  const trackInfo = ref(null)
  const showTrackPopup = ref(false)

  const loadTrack = async (id) => {
    try {
      const res = await getOrderTrack(id)
      if (res.code === 1) {
        trackInfo.value = res.data
      }
    } catch (error) {
      console.error(error)
    }
  }

  const loadData = async () => {
    showLoadingToast({ message: '加载中...', forbidClick: true })
    try {
      const res = routeId.length > 10
        ? await getOrderDetailByNumber(routeId)
        : await getOrderDetail(routeId)

      if (res?.code === 1) {
        order.value = res.data
        payTypeModel.value = order.value.payMethod || 1

        if ([3, 4].includes(order.value.status)) {
          loadTrack(order.value.id)
        }
      } else {
        showToast('订单不存在')
        setTimeout(() => router.back(), 1000)
      }
    } catch (error) {
      console.error(error)
      showToast('网络错误')
    } finally {
      closeToast()
    }
  }

  const getStatusText = (status) => orderStatusTextMap[status] || '未知状态'

  const goBack = () => {
    if (window.history.length > 1) {
      router.back()
      return
    }
    router.push('/order/list')
  }

  onMounted(() => {
    if (routeId) {
      loadData()
    }
  })

  return {
    getStatusText,
    goBack,
    loadData,
    order,
    payTypeModel,
    showTrackPopup,
    trackInfo,
  }
}
