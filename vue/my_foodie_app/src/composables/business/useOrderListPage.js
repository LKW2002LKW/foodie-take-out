import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { DEFAULT_PAGE_SIZE } from '@/constants/pagination'
import { getOrderPage } from '@/api/modules/order'

export const useOrderListPage = () => {
  const router = useRouter()

  const activeTab = ref(0)
  const list = ref([])
  const loading = ref(false)
  const finished = ref(false)
  const refreshing = ref(false)
  const page = ref(1)

  const resetListState = () => {
    list.value = []
    page.value = 1
    finished.value = false
  }

  const onLoad = async () => {
    try {
      if (refreshing.value) {
        list.value = []
        refreshing.value = false
      }

      const res = await getOrderPage({
        page: page.value,
        pageSize: DEFAULT_PAGE_SIZE,
        status: activeTab.value === 0 ? undefined : activeTab.value,
      })

      if (res.code !== 1) {
        finished.value = true
        showToast(res.msg || '订单加载失败')
        return
      }

      const records = res.data.records || []
      list.value.push(...records)

      if (records.length < DEFAULT_PAGE_SIZE || list.value.length >= res.data.total) {
        finished.value = true
      } else {
        page.value += 1
      }
    } catch (error) {
      console.error(error)
      finished.value = true
      showToast('订单加载失败')
    } finally {
      loading.value = false
    }
  }

  const onTabChange = (name) => {
    activeTab.value = Number(name)
    resetListState()
    loading.value = true
    onLoad()
  }

  const onRefresh = () => {
    resetListState()
    refreshing.value = true
    loading.value = true
    onLoad()
  }

  const goBack = () => {
    router.back()
  }

  const toDetail = (item) => {
    router.push(`/order/${item.id}`)
  }

  const toPay = (item) => {
    router.push(`/order/${item.id}`)
  }

  const toReview = (item) => {
    router.push({ path: '/review/create', query: { orderId: item.id } })
  }

  const getStatusClass = (status) => {
    if (status === 1) return 's-orange'
    if (status === 5) return 's-gray'
    return 's-black'
  }

  return {
    activeTab,
    finished,
    getStatusClass,
    goBack,
    list,
    loading,
    onLoad,
    onRefresh,
    onTabChange,
    refreshing,
    toDetail,
    toPay,
    toReview,
  }
}
