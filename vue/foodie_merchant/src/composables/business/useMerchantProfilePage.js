import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getCurrentMerchantInfo,
  getMerchantLogoUploadUrl,
  updateMerchantBusinessHours,
  updateMerchantInfo,
  updateMerchantStatus,
} from '@/api/modules/merchant'
import { useMerchantStore } from '@/stores/modules/merchantStore'
import { useMerchantProfileMap } from '@/composables/business/useMerchantProfileMap'

// 商户资料页总控组合式，串联资料加载、上传、营业状态与地图定位。
export const useMerchantProfilePage = () => {
  const merchantStore = useMerchantStore()

  const formRef = ref(null)
  const loading = ref(false)
  const saving = ref(false)
  const startTime = ref('')
  const endTime = ref('')

  const merchantInfo = ref({
    merchantName: '',
    logo: '',
    status: 3,
    auditStatus: 1,
    businessHours: '',
    contactPhone: '',
    businessLicense: '',
  })

  const form = reactive({
    merchantName: '',
    contactName: '',
    provinceCode: '',
    provinceName: '',
    cityCode: '',
    cityName: '',
    districtCode: '',
    districtName: '',
    address: '',
    longitude: null,
    latitude: null,
    description: '',
    bizCategoryId: null,
    minDeliveryAmount: 0,
    deliveryFee: 0,
  })

  const mapModel = useMerchantProfileMap(form)

  const requestHeaders = computed(() => ({
    Authorization: `Bearer ${merchantStore.token || ''}`,
  }))

  const uploadData = computed(() => {
    const merchantId = merchantInfo.value.merchantId || merchantInfo.value.id || localStorage.getItem('merchantId') || ''
    return { merchantId }
  })

  const logoUploadAction = computed(() => getMerchantLogoUploadUrl())

  const auditStatusText = computed(() => {
    const map = { 0: '审核中', 1: '已认证', 2: '审核未过' }
    return map[merchantInfo.value.auditStatus] || '待审核'
  })

  const auditStatusType = computed(() => {
    const map = { 0: 'warning', 1: 'success', 2: 'danger' }
    return map[merchantInfo.value.auditStatus] || 'info'
  })

  const toNumberOrNull = (value) => {
    if (value === null || value === undefined || value === '') return null
    const num = Number(value)
    return Number.isFinite(num) ? num : null
  }

  const fetchMerchantInfo = async () => {
    loading.value = true
    try {
      const response = await getCurrentMerchantInfo()
      const data = response.data || response
      if (!data) return

      merchantInfo.value = { ...data }
      merchantStore.updateMerchantInfo(data)
      if (data.merchantId || data.id) {
        localStorage.setItem('merchantId', data.merchantId || data.id)
      }

      form.merchantName = data.merchantName || ''
      form.contactName = data.contactName || ''
      form.provinceCode = data.provinceCode || ''
      form.provinceName = data.provinceName || ''
      form.cityCode = data.cityCode || ''
      form.cityName = data.cityName || ''
      form.districtCode = data.districtCode || ''
      form.districtName = data.districtName || ''
      form.address = data.address || ''
      form.longitude = toNumberOrNull(data.longitude)
      form.latitude = toNumberOrNull(data.latitude)
      form.description = data.description || ''
      form.bizCategoryId = data.bizCategoryId ?? null
      form.minDeliveryAmount = Number(data.minDeliveryAmount || 0)
      form.deliveryFee = Number(data.deliveryFee || 0)

      if (data.businessHours && data.businessHours.includes('-')) {
        const [start, end] = data.businessHours.split('-')
        startTime.value = start
        endTime.value = end
      }

      if (mapModel.hasCoordinates.value) {
        await mapModel.mountMerchantProfileMap({
          lng: Number(form.longitude),
          lat: Number(form.latitude),
        })
      } else {
        await mapModel.mountMerchantProfileMap()
      }
    } catch (error) {
      ElMessage.error('获取商户信息失败')
      console.error(error)
    } finally {
      loading.value = false
    }
  }

  const handleUpdateInfo = async () => {
    if (!form.address.trim()) {
      ElMessage.warning('请输入商户地址')
      return
    }

    if (!mapModel.hasCoordinates.value) {
      ElMessage.warning('请先完成地址定位，再保存资料')
      return
    }

    if (!startTime.value || !endTime.value) {
      ElMessage.warning('请先完善营业时间')
      return
    }

    saving.value = true
    try {
      await updateMerchantInfo({
        merchantName: form.merchantName,
        contactName: form.contactName,
        provinceCode: form.provinceCode,
        provinceName: form.provinceName,
        cityCode: form.cityCode,
        cityName: form.cityName,
        districtCode: form.districtCode,
        districtName: form.districtName,
        address: form.address,
        longitude: form.longitude,
        latitude: form.latitude,
        bizCategoryId: form.bizCategoryId,
        businessHours: `${startTime.value}-${endTime.value}`,
        minDeliveryAmount: form.minDeliveryAmount,
        deliveryFee: form.deliveryFee,
        description: form.description,
      })

      ElMessage.success('商户信息已保存')
      await fetchMerchantInfo()
    } catch (error) {
      ElMessage.error('保存失败')
    } finally {
      saving.value = false
    }
  }

  const handleStatusChange = async (status) => {
    if (merchantInfo.value.status === status) return

    try {
      await updateMerchantStatus(status)
      merchantInfo.value.status = status
      merchantStore.updateMerchantInfo({ status })
      ElMessage.success('状态已切换')
    } catch (error) {
      ElMessage.error('状态更新失败')
    }
  }

  const handleHoursChange = async () => {
    if (!startTime.value || !endTime.value) return

    try {
      await updateMerchantBusinessHours(`${startTime.value}-${endTime.value}`)
      ElMessage.success('营业时间已更新')
    } catch (error) {
      ElMessage.error('营业时间更新失败')
    }
  }

  const handleLogoSuccess = (response, uploadFile) => {
    merchantInfo.value.logo = URL.createObjectURL(uploadFile.raw)
    ElMessage.success('Logo上传成功')
    fetchMerchantInfo()
  }

  const beforeLogoUpload = (rawFile) => {
    if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
      ElMessage.error('图片格式必须为 JPG 或 PNG!')
      return false
    }

    if (rawFile.size / 1024 / 1024 > 2) {
      ElMessage.error('图片大小不能超过 2MB!')
      return false
    }

    return true
  }

  return {
    auditStatusText,
    auditStatusType,
    beforeLogoUpload,
    coordinateText: mapModel.coordinateText,
    fetchMerchantInfo,
    form,
    formRef,
    handleAddressBlur: mapModel.handleAddressBlur,
    handleAddressFocus: mapModel.handleAddressFocus,
    handleAddressInput: mapModel.handleAddressInput,
    handleAutoLocate: mapModel.handleAutoLocate,
    handleHoursChange,
    handleLocateByAddress: mapModel.handleLocateByAddress,
    handleLogoSuccess,
    handleStatusChange,
    handleSuggestionSelect: mapModel.handleSuggestionSelect,
    handleUpdateInfo,
    hasCoordinates: mapModel.hasCoordinates,
    loading,
    locating: mapModel.locating,
    logoUploadAction,
    mapLoading: mapModel.mapLoading,
    mapRef: mapModel.mapRef,
    mapResolving: mapModel.mapResolving,
    merchantInfo,
    regionText: mapModel.regionText,
    relocateMapToCurrent: mapModel.relocateMapToCurrent,
    requestHeaders,
    resolvingAddress: mapModel.resolvingAddress,
    saving,
    startTime,
    endTime,
    suggestions: mapModel.suggestions,
    suggestionsVisible: mapModel.suggestionsVisible,
    uploadData,
  }
}
