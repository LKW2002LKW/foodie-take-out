import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuth } from '@/composables/useAuth'
import { useMerchantRegisterMap } from '@/composables/business/useMerchantRegisterMap'

export const useRegisterPage = () => {
  const { loading, handleRegister } = useAuth()

  const activeStep = ref(0)
  const formRef = ref(null)
  const form = reactive({
    merchantName: '',
    businessLicense: '',
    legalPerson: '',
    contactName: '',
    contactPhone: '',
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
    bizCategoryId: '',
    businessHours: '09:00-22:00',
    minDeliveryAmount: 0,
    deliveryFee: 0,
    username: '',
    password: '',
    name: '',
    phone: '',
  })

  const rules = {
    merchantName: [{ required: true, message: '请输入商户名称', trigger: 'blur' }],
    businessLicense: [{ required: true, message: '请输入营业执照号', trigger: 'blur' }],
    bizCategoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
    cityName: [{ required: true, message: '请输入城市', trigger: 'blur' }],
    address: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
    username: [
      { required: true, message: '请输入用户名', trigger: 'blur' },
      { min: 3, message: '最少3个字符', trigger: 'blur' },
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, message: '最少6个字符', trigger: 'blur' },
    ],
    contactPhone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  }

  const mapModel = useMerchantRegisterMap(form)

  const nextStep = async () => {
    if (!formRef.value) return

    const fieldsToValidate = ['merchantName', 'businessLicense', 'bizCategoryId', 'cityName', 'address']
    let isValid = true

    for (const field of fieldsToValidate) {
      try {
        await formRef.value.validateField(field)
      } catch (error) {
        isValid = false
      }
    }

    if (isValid && !mapModel.hasCoordinates.value) {
      ElMessage.warning('请先完成门店地址定位')
      isValid = false
    }

    if (isValid) {
      activeStep.value = 1
    }
  }

  const submitRegister = async () => {
    if (!formRef.value) return

    await formRef.value.validate(async (valid) => {
      if (!valid) return

      if (!mapModel.hasCoordinates.value) {
        ElMessage.warning('请先完成门店地址定位')
        return
      }

      form.name = form.contactName
      form.phone = form.contactPhone

      const success = await handleRegister(form)
      if (success) {
        activeStep.value = 2
      }
    })
  }

  const handleLogoError = (event) => {
    event.target.style.display = 'none'
  }

  return {
    activeStep,
    coordinateText: mapModel.coordinateText,
    form,
    formRef,
    handleAddressBlur: mapModel.handleAddressBlur,
    handleAddressFocus: mapModel.handleAddressFocus,
    handleAddressInput: mapModel.handleAddressInput,
    handleAutoLocate: mapModel.handleAutoLocate,
    handleCityInput: mapModel.handleCityInput,
    handleLocateByAddress: mapModel.handleLocateByAddress,
    handleLogoError,
    handleSuggestionSelect: mapModel.handleSuggestionSelect,
    hasCoordinates: mapModel.hasCoordinates,
    loading,
    locating: mapModel.locating,
    mapLoading: mapModel.mapLoading,
    mapRef: mapModel.mapRef,
    mapResolving: mapModel.mapResolving,
    mountRegisterMap: mapModel.mountRegisterMap,
    nextStep,
    regionText: mapModel.regionText,
    relocateMapToCurrent: mapModel.relocateMapToCurrent,
    resolvingAddress: mapModel.resolvingAddress,
    rules,
    submitRegister,
    suggestions: mapModel.suggestions,
    suggestionsVisible: mapModel.suggestionsVisible,
  }
}
