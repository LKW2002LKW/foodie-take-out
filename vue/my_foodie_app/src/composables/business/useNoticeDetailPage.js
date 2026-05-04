import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getNoticeDetail } from '@/api/modules/notice'
import { canReadUserNotice, formatNoticeContentHtml, getNoticeTagType } from '@/utils/business/notice'

export const useNoticeDetailPage = () => {
  const route = useRoute()
  const router = useRouter()
  const id = route.params.id

  const detail = ref(null)
  const loading = ref(true)

  const onClickLeft = () => {
    router.back()
  }

  const formattedContent = computed(() => formatNoticeContentHtml(detail.value?.content))

  const loadData = async () => {
    try {
      const res = await getNoticeDetail(id)
      if (res.code !== 200 && res.code !== 1) {
        showToast(res.msg || '获取详情失败')
        return
      }

      const noticeDetail = res.data
      if (canReadUserNotice(noticeDetail)) {
        detail.value = noticeDetail
        return
      }

      showToast('无权查看或该公告未发布')
      detail.value = null
    } catch (error) {
      showToast('网络错误')
    } finally {
      loading.value = false
    }
  }

  onMounted(() => {
    if (id) {
      loadData()
      return
    }
    loading.value = false
  })

  return {
    detail,
    formattedContent,
    getTagType: getNoticeTagType,
    loading,
    onClickLeft,
  }
}
