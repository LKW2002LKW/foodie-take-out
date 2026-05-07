import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { DEFAULT_PAGE_SIZE } from '@/constants/pagination'
import { getNoticePage } from '@/api/modules/notice'
import { formatNoticeTime, getNoticeBrief, getNoticeTagType } from '@/utils/business/notice'

const getNoticeRows = (data) => data?.list || data?.records || []

// 公告列表页组合式，负责分页加载、用户可见过滤和详情跳转。
export const useNoticeListPage = () => {
  const router = useRouter()

  const activeTab = ref(0)
  const list = ref([])
  const loading = ref(false)
  const finished = ref(false)
  const refreshing = ref(false)
  const page = ref(1)

  const filteredList = computed(() => (
    list.value.filter((item) => {
      const targetMatch = item.target === 2 || item.target === 3
      const statusMatch = item.status === 1
      return targetMatch && statusMatch
    })
  ))

  const onClickLeft = () => {
    router.back()
  }

  const onLoad = async () => {
    if (refreshing.value) {
      list.value = []
      refreshing.value = false
    }

    try {
      const params = {
        page: page.value,
        pageSize: DEFAULT_PAGE_SIZE,
      }

      if (activeTab.value !== 0) {
        params.type = activeTab.value
      }

      const res = await getNoticePage(params)
      if (res.code !== 200 && res.code !== 1) {
        finished.value = true
        showToast(res.msg || '公告加载失败')
        return
      }

      const rows = getNoticeRows(res.data)
      if (page.value === 1) {
        list.value = rows
      } else {
        list.value = [...list.value, ...rows]
      }

      const total = res.data?.total || 0
      if (rows.length < DEFAULT_PAGE_SIZE || list.value.length >= total) {
        finished.value = true
      } else {
        page.value += 1
      }
    } catch (error) {
      finished.value = true
      showToast('公告加载失败')
    } finally {
      loading.value = false
    }
  }

  const onRefresh = () => {
    finished.value = false
    loading.value = true
    page.value = 1
    onLoad()
  }

  const onTabChange = () => {
    list.value = []
    page.value = 1
    finished.value = false
    loading.value = true
    onLoad()
  }

  const toDetail = (id) => {
    router.push(`/notice/${id}`)
  }

  return {
    activeTab,
    filteredList,
    finished,
    formatTime: formatNoticeTime,
    getBrief: getNoticeBrief,
    getTagType: getNoticeTagType,
    loading,
    onClickLeft,
    onLoad,
    onRefresh,
    onTabChange,
    refreshing,
    toDetail,
  }
}
