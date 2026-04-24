<template>
  <div class="register">
    <div class="register__container">
      <!-- 头部 -->
      <header class="register__header">
        <div class="register__brand" @click="$router.push('/login')">
          <img :src="logoUrl" alt="Logo" class="register__logo" @error="handleLogoError">
          <h1 class="register__app-name">Foodie Merchant</h1>
        </div>
        <div class="register__steps">
          <el-steps :active="activeStep" finish-status="success" simple>
            <el-step title="商户信息" />
            <el-step title="账号设置" />
            <el-step title="入驻成功" />
          </el-steps>
        </div>
      </header>

      <!-- 主体内容 -->
      <main class="register__main">
        <el-card shadow="never" class="register__card">
          <el-form 
            ref="formRef" 
            :model="form" 
            :rules="rules" 
            label-position="top"
            size="large"
          >
            <!-- 第一步：商户基本信息 -->
            <div v-show="activeStep === 0" class="register__step-content">
              <h2 class="register__step-title">填写商户基本信息</h2>
              
              <el-row :gutter="24">
                <el-col :span="24">
                  <el-form-item label="商户名称" prop="merchantName">
                    <el-input v-model="form.merchantName" placeholder="请与营业执照名称保持一致" />
                  </el-form-item>
                </el-col>
                
                <el-col :span="12">
                  <el-form-item label="营业执照号" prop="businessLicense">
                    <el-input v-model="form.businessLicense" placeholder="统一社会信用代码" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="商户分类" prop="bizCategoryId">
                    <el-select v-model="form.bizCategoryId" placeholder="请选择主营分类" style="width: 100%">
                      <el-option label="美食" :value="1" />
                      <el-option label="甜点饮品" :value="2" />
                      <el-option label="超市便利" :value="3" />
                      <el-option label="蔬菜水果" :value="4" />
                    </el-select>
                  </el-form-item>
                </el-col>

                <el-col :span="12">
                  <el-form-item label="法人姓名" prop="legalPerson">
                    <el-input v-model="form.legalPerson" placeholder="法人姓名" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="所在城市" prop="cityName">
                    <el-input v-model="form.cityName" placeholder="例如：北京市" @input="handleCityInput" />
                  </el-form-item>
                </el-col>

                <el-col :span="24">
                  <el-form-item label="详细地址" prop="address">
                    <div class="register__address-block">
                      <div class="register__map-preview">
                        <div ref="mapRef" class="register__map-canvas"></div>
                        <div v-if="mapLoading" class="register__map-loading-mask">
                          <span>地图加载中...</span>
                        </div>
                        <div class="register__map-center-pin" :class="{ 'is-loading': mapResolving }">
                          <div class="register__map-center-pin-dot"></div>
                        </div>
                        <div class="register__map-address-tip">
                          {{ form.address || '拖动地图选择准确门店地址' }}
                        </div>
                        <button type="button" class="register__map-locate-btn" @click="relocateMapToCurrent">
                          回到当前位置
                        </button>
                      </div>

                      <div class="register__address-toolbar">
                        <el-input
                          v-model="form.address"
                          placeholder="街道、楼牌号等详细地址"
                          clearable
                          @input="handleAddressInput"
                          @focus="handleAddressFocus"
                          @blur="handleAddressBlur"
                        />
                        <div class="register__address-actions">
                          <el-button
                            type="primary"
                            plain
                            :loading="resolvingAddress"
                            :disabled="!form.address.trim()"
                            @click="handleLocateByAddress"
                          >
                            根据输入地址定位
                          </el-button>
                          <el-button
                            type="primary"
                            :loading="locating"
                            @click="handleAutoLocate"
                          >
                            自动定位
                          </el-button>
                        </div>
                      </div>

                      <div v-if="suggestionsVisible && suggestions.length" class="register__suggestion-list">
                        <button
                          v-for="item in suggestions"
                          :key="item.id"
                          type="button"
                          class="register__suggestion-item"
                          @mousedown.prevent="handleSuggestionSelect(item)"
                        >
                          <span class="register__suggestion-name">{{ item.name }}</span>
                          <span class="register__suggestion-address">{{ item.district }}{{ item.address }}</span>
                        </button>
                      </div>

                      <div class="register__location-summary">
                        <div class="register__location-row">
                          <el-tag :type="hasCoordinates ? 'success' : 'warning'" effect="light" round>
                            {{ hasCoordinates ? '定位已确认' : '待重新定位' }}
                          </el-tag>
                          <span class="register__location-hint">
                            {{ hasCoordinates ? '注册时会一起提交商户地址经纬度。' : '进入下一步前需要先完成门店定位。' }}
                          </span>
                        </div>
                        <div class="register__location-meta">
                          <span>经度：{{ coordinateText.longitude }}</span>
                          <span>纬度：{{ coordinateText.latitude }}</span>
                          <span>行政区：{{ regionText }}</span>
                        </div>
                      </div>
                    </div>
                  </el-form-item>
                </el-col>

                <el-col :span="24">
                  <el-form-item label="商户描述" prop="description">
                    <el-input 
                      v-model="form.description" 
                      type="textarea" 
                      :rows="3"
                      placeholder="介绍您的店铺特色" 
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <div class="register__footer">
                <el-button @click="$router.push('/login')">返回登录</el-button>
                <el-button type="primary" @click="nextStep">下一步：账号设置</el-button>
              </div>
            </div>

            <!-- 第二步：管理员账号信息 -->
            <div v-show="activeStep === 1" class="register__step-content">
              <h2 class="register__step-title">设置管理员账号</h2>
              
              <el-row :gutter="24">
                <el-col :span="24">
                  <el-divider content-position="left">账号凭据</el-divider>
                </el-col>
                
                <el-col :span="12">
                  <el-form-item label="登录用户名" prop="username">
                    <el-input v-model="form.username" placeholder="用于后台登录" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="登录密码" prop="password">
                    <el-input v-model="form.password" type="password" show-password placeholder="6-20位字符" />
                  </el-form-item>
                </el-col>

                <el-col :span="24">
                  <el-divider content-position="left">联系人信息</el-divider>
                </el-col>

                <el-col :span="12">
                  <el-form-item label="联系人姓名" prop="contactName">
                    <el-input v-model="form.contactName" placeholder="负责人姓名" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="联系电话" prop="contactPhone">
                    <el-input v-model="form.contactPhone" placeholder="负责人手机号" />
                  </el-form-item>
                </el-col>

                <el-col :span="12">
                  <el-form-item label="配送起送额" prop="minDeliveryAmount">
                    <el-input-number 
                      v-model="form.minDeliveryAmount" 
                      :precision="2" 
                      :min="0" 
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="配送费" prop="deliveryFee">
                    <el-input-number 
                      v-model="form.deliveryFee" 
                      :precision="2" 
                      :min="0" 
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <div class="register__footer">
                <el-button @click="activeStep = 0">上一步</el-button>
                <el-button type="primary" :loading="loading" @click="onSubmit">提交入驻申请</el-button>
              </div>
            </div>
          </el-form>
        </el-card>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuth } from '@/composables/useAuth'
import { geocode, inputTips, loadAmapSdk, reverseGeocode } from '@/services/amap'
import logoUrl from '@/assets/logo.svg'

/**
 * 注册页面逻辑
 */

// 1. 初始化认证 Composable
const { loading, handleRegister } = useAuth()

// 2. 局部状态
const activeStep = ref(0)
const formRef = ref(null)
const mapRef = ref(null)
const locating = ref(false)
const resolvingAddress = ref(false)
const mapLoading = ref(false)
const mapResolving = ref(false)
const suggestions = ref([])
const suggestionsVisible = ref(false)

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
  name: '', // 管理员姓名，同步使用 contactName
  phone: ''  // 管理员电话，同步使用 contactPhone
})

const rules = {
  merchantName: [{ required: true, message: '请输入商户名称', trigger: 'blur' }],
  businessLicense: [{ required: true, message: '请输入营业执照号', trigger: 'blur' }],
  bizCategoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  cityName: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  address: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, message: '最少3个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '最少6个字符', trigger: 'blur' }
  ],
  contactPhone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

const hasCoordinates = computed(() => Number.isFinite(form.longitude) && Number.isFinite(form.latitude))

const coordinateText = computed(() => ({
  longitude: hasCoordinates.value ? Number(form.longitude).toFixed(6) : '--',
  latitude: hasCoordinates.value ? Number(form.latitude).toFixed(6) : '--'
}))

const regionText = computed(() => {
  const text = [form.provinceName, form.cityName, form.districtName].filter(Boolean).join(' / ')
  return text || '--'
})

let suggestionTimer = null
let blurTimer = null
let mapInstance = null
let mapMoveTimer = null
let ignoreNextMoveEnd = false

const clearLocationState = ({ keepCity = true } = {}) => {
  form.provinceCode = ''
  form.provinceName = ''
  form.cityCode = ''
  if (!keepCity) {
    form.cityName = ''
  }
  form.districtCode = ''
  form.districtName = ''
  form.longitude = null
  form.latitude = null
}

const parseCityName = (city, province) => {
  if (Array.isArray(city)) {
    return city[0] || province || ''
  }
  return city || province || ''
}

const applyAddressComponent = (regeo, fallbackAddress = '') => {
  const component = regeo?.addressComponent || {}
  const adcode = String(component.adcode || '')
  form.provinceName = component.province || ''
  form.cityName = parseCityName(component.city, component.province)
  form.districtName = component.district || ''
  form.provinceCode = adcode ? `${adcode.slice(0, 2)}0000` : ''
  form.cityCode = adcode ? `${adcode.slice(0, 4)}00` : ''
  form.districtCode = adcode || ''
  form.address = regeo?.formatted_address || fallbackAddress || form.address
}

const setMapCenter = (lng, lat, zoom = 16) => {
  if (!mapInstance || !lng || !lat) return
  ignoreNextMoveEnd = true
  mapInstance.setZoomAndCenter(zoom, [Number(lng), Number(lat)])
  window.setTimeout(() => {
    ignoreNextMoveEnd = false
  }, 260)
}

const resolveInitialMapLocation = async () => {
  if (hasCoordinates.value) {
    return { lng: Number(form.longitude), lat: Number(form.latitude) }
  }

  const cityKeyword = form.cityName || form.provinceName || ''
  if (cityKeyword) {
    const cityGeo = await geocode(cityKeyword, cityKeyword)
    if (cityGeo) {
      return { lng: cityGeo.lng, lat: cityGeo.lat }
    }
  }

  return { lng: 116.397428, lat: 39.90923 }
}

const initInteractiveMap = async (location = null) => {
  if (!mapRef.value) return

  mapLoading.value = true
  try {
    const AMap = await loadAmapSdk()
    const initialLocation = location || await resolveInitialMapLocation()
    if (!initialLocation) return

    if (!mapInstance) {
      mapInstance = new AMap.Map(mapRef.value, {
        zoom: 16,
        center: [initialLocation.lng, initialLocation.lat],
        viewMode: '2D',
        resizeEnable: true,
        dragEnable: true,
        zoomEnable: true,
        doubleClickZoom: true
      })

      new AMap.TileLayer({
        zIndex: 1
      }).setMap(mapInstance)

      mapInstance.on('moveend', handleMapMoveEnd)
      mapInstance.on('zoomend', handleMapMoveEnd)
    }

    window.setTimeout(() => {
      mapInstance?.resize()
      setMapCenter(initialLocation.lng, initialLocation.lat)
    }, 120)
  } catch (error) {
    ElMessage.error('地图加载失败，请检查高德地图配置')
  } finally {
    mapLoading.value = false
  }
}

const syncLocationByCoords = async (lng, lat, fallbackAddress = '', { moveMap = false } = {}) => {
  mapResolving.value = true
  const regeo = await reverseGeocode(lng, lat)
  try {
    if (!regeo) {
      throw new Error('定位结果解析失败')
    }

    form.longitude = Number(lng)
    form.latitude = Number(lat)
    applyAddressComponent(regeo, fallbackAddress)

    if (moveMap) {
      setMapCenter(lng, lat)
    }
  } finally {
    mapResolving.value = false
  }
}

const handleMapMoveEnd = () => {
  if (!mapInstance || ignoreNextMoveEnd) return
  const center = mapInstance.getCenter()
  window.clearTimeout(mapMoveTimer)
  mapMoveTimer = window.setTimeout(() => {
    syncLocationByCoords(center.lng, center.lat)
  }, 220)
}

const loadSuggestions = async (keyword) => {
  const cityKeyword = form.cityName || form.provinceName || ''
  const result = await inputTips(keyword, cityKeyword)
  suggestions.value = result.map((item, index) => ({
    id: item.id || `${item.location}-${index}`,
    name: item.name || '',
    district: item.district || '',
    address: item.address || '',
    location: item.location || ''
  }))
  suggestionsVisible.value = suggestions.value.length > 0
}

const handleAddressInput = (value) => {
  clearLocationState()
  suggestionsVisible.value = false

  if (suggestionTimer) {
    window.clearTimeout(suggestionTimer)
  }

  if (!String(value || '').trim()) {
    suggestions.value = []
    return
  }

  suggestionTimer = window.setTimeout(() => {
    loadSuggestions(String(value).trim())
  }, 250)
}

const handleCityInput = () => {
  clearLocationState()
  suggestionsVisible.value = false
  suggestions.value = []
}

const handleAddressFocus = () => {
  if (form.address.trim() && suggestions.value.length) {
    suggestionsVisible.value = true
  }
}

const handleAddressBlur = () => {
  blurTimer = window.setTimeout(() => {
    suggestionsVisible.value = false
  }, 120)
}

const handleSuggestionSelect = async (item) => {
  if (blurTimer) {
    window.clearTimeout(blurTimer)
  }
  suggestionsVisible.value = false
  suggestions.value = []

  const [lng, lat] = String(item.location || '').split(',')
  if (!lng || !lat) {
    ElMessage.error('该地址缺少坐标信息')
    return
  }

  try {
    resolvingAddress.value = true
    await syncLocationByCoords(Number(lng), Number(lat), `${item.district}${item.address || item.name}`)
    await initInteractiveMap({ lng: Number(lng), lat: Number(lat) })
    ElMessage.success('地址定位成功')
  } catch (error) {
    ElMessage.error(error.message || '地址定位失败')
  } finally {
    resolvingAddress.value = false
  }
}

const handleLocateByAddress = async () => {
  const keyword = [form.cityName, form.address].filter(Boolean).join('')
  if (!keyword.trim()) {
    ElMessage.warning('请先输入城市和地址')
    return
  }

  resolvingAddress.value = true
  try {
    const geoResult = await geocode(keyword, form.cityName || '')
    if (!geoResult) {
      throw new Error('未能解析该地址，请完善地址后重试')
    }

    await syncLocationByCoords(geoResult.lng, geoResult.lat, form.address)
    await initInteractiveMap({ lng: geoResult.lng, lat: geoResult.lat })
    suggestionsVisible.value = false
    suggestions.value = []
    ElMessage.success('地址定位成功')
  } catch (error) {
    ElMessage.error(error.message || '地址定位失败')
  } finally {
    resolvingAddress.value = false
  }
}

const getCurrentPosition = () => {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error('当前浏览器不支持定位'))
      return
    }

    navigator.geolocation.getCurrentPosition(
      (position) => resolve(position.coords),
      () => reject(new Error('定位失败，请检查浏览器定位权限')),
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0
      }
    )
  })
}

const handleAutoLocate = async () => {
  locating.value = true
  try {
    const coords = await getCurrentPosition()
    await syncLocationByCoords(coords.longitude, coords.latitude)
    await initInteractiveMap({ lng: Number(coords.longitude), lat: Number(coords.latitude) })
    ElMessage.success('自动定位成功')
  } catch (error) {
    ElMessage.error(error.message || '自动定位失败')
  } finally {
    locating.value = false
  }
}

const relocateMapToCurrent = async () => {
  locating.value = true
  try {
    const coords = await getCurrentPosition()
    await syncLocationByCoords(coords.longitude, coords.latitude, '', { moveMap: true })
    await initInteractiveMap({ lng: Number(coords.longitude), lat: Number(coords.latitude) })
  } catch (error) {
    ElMessage.error(error.message || '定位失败')
  } finally {
    locating.value = false
  }
}

// 3. 方法
const nextStep = async () => {
  if (!formRef.value) return
  
  // 只校验当前步骤的字段
  const fieldsToValidate = ['merchantName', 'businessLicense', 'bizCategoryId', 'cityName', 'address']
  let isValid = true
  
  for (const field of fieldsToValidate) {
    try {
      await formRef.value.validateField(field)
    } catch (e) {
      isValid = false
    }
  }

  if (isValid && !hasCoordinates.value) {
    ElMessage.warning('请先完成门店地址定位')
    isValid = false
  }

  if (isValid) {
    activeStep.value = 1
  }
}

const onSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (!hasCoordinates.value) {
        ElMessage.warning('请先完成门店地址定位')
        return
      }

      // 业务逻辑：同步管理员信息
      form.name = form.contactName
      form.phone = form.contactPhone
      
      const success = await handleRegister(form)
      if (success) {
        activeStep.value = 2
      }
    }
  })
}

const handleLogoError = (e) => {
  e.target.style.display = 'none'
}

onMounted(async () => {
  await nextTick()
  await initInteractiveMap()
})

onBeforeUnmount(() => {
  if (suggestionTimer) {
    window.clearTimeout(suggestionTimer)
  }
  if (blurTimer) {
    window.clearTimeout(blurTimer)
  }
  if (mapMoveTimer) {
    window.clearTimeout(mapMoveTimer)
  }
  mapInstance?.destroy()
  mapInstance = null
})
</script>

<style scoped>
.register {
  min-height: 100vh;
  background-color: #f8fafc;
  padding: 40px 20px;
}

.register__container {
  max-width: 900px;
  margin: 0 auto;
}

/* Header */
.register__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.register__brand {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.register__logo {
  width: 40px;
  height: 40px;
}

.register__app-name {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.register__steps {
  width: 500px;
}

/* Card */
.register__card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  padding: 24px;
}

.register__step-title {
  font-size: 20px;
  font-weight: 700;
  color: #334155;
  margin: 0 0 32px;
  text-align: center;
}

.register__footer {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 48px;
  padding-top: 24px;
  border-top: 1px solid #f1f5f9;
}

.register__footer .el-button {
  padding: 12px 32px;
  font-size: 16px;
  border-radius: 10px;
}

.register__address-block {
  width: 100%;
}

.register__map-preview {
  position: relative;
  margin-bottom: 14px;
  border-radius: 14px;
  overflow: hidden;
  background: #edf2f7;
  border: 1px solid #dbe5f0;
}

.register__map-canvas {
  width: 100%;
  height: 280px;
}

.register__map-loading-mask {
  position: absolute;
  inset: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  background: rgba(255, 255, 255, 0.82);
  z-index: 3;
  color: #475569;
}

.register__map-center-pin {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -100%);
  width: 28px;
  height: 38px;
  pointer-events: none;
  z-index: 2;
}

.register__map-center-pin::before {
  content: '';
  position: absolute;
  left: 50%;
  top: 0;
  width: 24px;
  height: 24px;
  transform: translateX(-50%);
  border-radius: 50% 50% 50% 0;
  rotate: -45deg;
  background: #409eff;
  box-shadow: 0 6px 14px rgba(64, 158, 255, 0.28);
}

.register__map-center-pin-dot {
  position: absolute;
  left: 50%;
  top: 7px;
  width: 8px;
  height: 8px;
  transform: translateX(-50%);
  border-radius: 50%;
  background: #fff;
  z-index: 1;
}

.register__map-center-pin.is-loading {
  animation: register-pin-bounce 0.7s ease-in-out infinite alternate;
}

.register__map-address-tip {
  position: absolute;
  left: 14px;
  right: 86px;
  bottom: 14px;
  padding: 10px 12px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.92);
  color: #334155;
  font-size: 13px;
  backdrop-filter: blur(8px);
  z-index: 2;
}

.register__map-locate-btn {
  position: absolute;
  right: 14px;
  bottom: 14px;
  min-width: 64px;
  height: 38px;
  border: 0;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.96);
  color: #1f2937;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 10px 18px rgba(15, 23, 42, 0.12);
  z-index: 2;
}

.register__address-toolbar {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.register__address-actions {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}

.register__suggestion-list {
  margin-top: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
}

.register__suggestion-item {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px 14px;
  border: 0;
  border-bottom: 1px solid #f1f5f9;
  background: #fff;
  text-align: left;
  cursor: pointer;
}

.register__suggestion-item:last-child {
  border-bottom: 0;
}

.register__suggestion-item:hover {
  background: #f8fbff;
}

.register__suggestion-name {
  font-weight: 600;
  color: #334155;
}

.register__suggestion-address {
  font-size: 13px;
  color: #64748b;
}

.register__location-summary {
  margin-top: 12px;
  padding: 14px 16px;
  border-radius: 10px;
  border: 1px solid #dbeafe;
  background: linear-gradient(135deg, #f8fbff 0%, #f1f7ff 100%);
}

.register__location-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.register__location-hint {
  font-size: 13px;
  color: #475569;
}

.register__location-meta {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  font-size: 13px;
  color: #64748b;
}

@keyframes register-pin-bounce {
  from {
    transform: translate(-50%, -100%);
  }
  to {
    transform: translate(-50%, -112%);
  }
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: #475569;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}

@media (max-width: 768px) {
  .register__header {
    flex-direction: column;
    gap: 24px;
    align-items: flex-start;
  }
  .register__steps {
    width: 100%;
  }

  .register__address-toolbar {
    flex-direction: column;
  }

  .register__address-actions {
    width: 100%;
  }

  .register__address-actions .el-button {
    flex: 1;
  }

  .register__location-row,
  .register__location-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
