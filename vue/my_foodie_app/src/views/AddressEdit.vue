<template>
  <div class="address-edit-page mobile-page">
    <van-nav-bar
      :title="isEdit ? '修改收货地址' : '新增收货地址'"
      left-arrow
      fixed
      placeholder
      @click-left="$router.back()"
    />

    <div class="content-box">
      <div class="mt-search-wrap">
        <div class="city-box" @click="showCityPopup = true">
          <span class="city-txt van-ellipsis">{{ currentCity }}</span>
          <van-icon name="arrow-down" class="arrow" />
        </div>
        <div class="search-input-box">
          <van-search
            v-model="searchKey"
            placeholder="搜索收货地址"
            background="transparent"
            class="custom-search"
            @update:model-value="onSearchInput"
            @focus="onSearchFocus"
          />
        </div>
      </div>

      <div class="map-preview">
        <div ref="mapRef" class="map-canvas"></div>
        <div v-if="mapLoading" class="map-loading-mask">
          <van-loading size="2.2rem" color="var(--primary-color)" vertical>地图加载中...</van-loading>
        </div>
        <div class="map-center-pin" :class="{ 'is-loading': mapResolving }">
          <div class="map-center-pin__dot"></div>
        </div>
        <div class="map-address-tip van-ellipsis">
          {{ displayAddress || '拖动地图选择准确地址' }}
        </div>
        <button type="button" class="map-locate-btn" @click="relocateMapToCurrent">
          <van-icon name="aim" size="1.9rem" color="var(--mt-strong)" />
        </button>
      </div>

      <!-- 信息表单 -->
      <van-cell-group inset class="form-group">
        <van-field v-model="form.consignee" label="收货人" placeholder="请填写收货人姓名" required />
        <van-cell title="性别">
          <template #value>
            <van-radio-group v-model="form.sex" direction="horizontal">
              <van-radio :name="1" checked-color="#ffe28a">先生</van-radio>
              <van-radio :name="2" checked-color="#ffe28a">女士</van-radio>
            </van-radio-group>
          </template>
        </van-cell>
        <van-field v-model="form.phone" label="手机号" type="tel" placeholder="请填写收货人手机号" required />
        
        <!-- 核心优化：点击定位地址触发重新搜索 -->
        <van-field 
          v-model="displayAddress" 
          label="定位地址" 
          readonly 
          placeholder="点击选择地址" 
          required 
          is-link
          @click="triggerReSearch"
        />
        
        <van-field v-model="form.detail" rows="2" autosize label="门牌号" type="textarea" placeholder="例: 16号楼501室" required />
        
        <van-cell title="标签">
          <template #value>
            <van-radio-group v-model="form.label" direction="horizontal">
              <van-radio name="家" checked-color="#ffe28a">家</van-radio>
              <van-radio name="公司" checked-color="#ffe28a">公司</van-radio>
              <van-radio name="学校" checked-color="#ffe28a">学校</van-radio>
            </van-radio-group>
          </template>
        </van-cell>
        <van-cell title="设为默认地址">
          <template #right-icon>
            <van-switch v-model="isDefaultBool" size="2rem" active-color="#ffe28a" />
          </template>
        </van-cell>
      </van-cell-group>

      <div class="action-bar">
        <van-button
          v-if="isEdit"
          round
          plain
          class="action-btn action-btn--delete"
          @click="onDelete"
        >
          删除地址
        </van-button>
        <van-button
          round
          class="action-btn action-btn--save"
          :loading="saving"
          @click="onSave"
        >
          保存地址
        </van-button>
      </div>
    </div>

    <van-popup
      :show="showLocationPicker"
      @update:show="showLocationPicker = $event"
      position="bottom"
      round
      class="location-picker-popup"
      :style="{ height: '56%' }"
    >
      <div class="location-picker-layout">
        <div class="picker-sheet-header">
          <div class="picker-sheet-title">{{ searchKey ? '搜索结果' : '附近地址推荐' }}</div>
          <div class="picker-sheet-close" @click="showLocationPicker = false">
            <van-icon name="cross" />
          </div>
        </div>

        <div class="picker-map-preview">
          <van-image v-if="pickerMapUrl" :src="pickerMapUrl" width="100%" height="16rem" fit="cover" />
          <div v-else class="map-placeholder">
            <van-icon name="map-marked" size="4rem" color="var(--mt-divider)" />
            <p>请选择附近推荐地址</p>
          </div>
        </div>

        <div class="picker-list-wrap">
          <div
            v-for="tip in tips"
            :key="tip.id"
            class="picker-tip-item"
            :class="{ 'picker-tip-item--active': selectedTipId === tip.id }"
            @click="selectLocationTip(tip)"
          >
            <div class="picker-tip-info">
              <div class="tip-main">{{ tip.name }}</div>
              <div class="tip-sub">{{ tip.district }}{{ tip.address }}</div>
            </div>
            <div class="picker-tip-check">
              <div class="mt-checkbox" :class="{ 'checked': selectedTipId === tip.id }">
                <van-icon name="success" class="check-icon" />
              </div>
            </div>
          </div>
          <van-empty v-if="tips.length === 0" image-size="60" description="暂无可选地址" />
        </div>

        <div class="picker-action-bar safe-area-bottom">
          <van-button
            round
            block
            type="primary"
            color="var(--primary-color)"
            text-color="var(--mt-strong)"
            :disabled="!selectedTipId"
            @click="confirmLocationSelection"
          >
            确认所选地址
          </van-button>
        </div>
      </div>
    </van-popup>

    <!-- 城市选择弹窗 -->
    <van-popup :show="showCityPopup" @update:show="showCityPopup = $event" position="right" style="width: 100%; height: 100%">
      <div class="city-picker-layout">
        <van-nav-bar title="选择收货城市" left-arrow @click-left="showCityPopup = false" />
        <div class="city-search-box">
          <van-search v-model="citySearchQuery" placeholder="输入城市名筛选" shape="round" />
        </div>
        <div class="city-list-container">
          <van-index-bar :index-list="filteredIndexList" :highlight-color="'var(--primary-color)'">
            <div v-for="group in filteredCityData" :key="group.initial">
              <van-index-anchor :index="group.initial" />
              <van-cell v-for="city in group.list" :key="city" :title="city" @click="onCitySelect(city)" />
            </div>
          </van-index-bar>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { inputTips, getAroundPois, getStaticMapUrl, reverseGeocode, loadAmapSdk } from '../services/amap'
import { addAddress, updateAddress, getAddressDetail, deleteAddress } from '../services/address'
import { useLocationStore } from '../store/modules/location'
import { getCurrentLocation } from '../utils/location'

const route = useRoute(); const router = useRouter(); const locationStore = useLocationStore()
const isEdit = !!route.query.id; const saving = ref(false)
const isDefaultBool = ref(false); const locationConfirmed = ref(false)
const searchKey = ref(''); const tips = ref([]); const displayAddress = ref('')
const showCityPopup = ref(false); const citySearchQuery = ref(''); const showLocationPicker = ref(false)
const selectedTipId = ref('')
const mapRef = ref(null)
const mapLoading = ref(false)
const mapResolving = ref(false)
const currentCity = ref(locationStore.city || locationStore.locatedCity || '北京市')
let mapInstance = null
let mapMoveTimer = null
let ignoreNextMoveEnd = false

const cityData = [{ initial: 'A', list: ['阿坝', '安康', '安庆', '鞍山'] }, { initial: 'B', list: ['北京', '保定', '宝鸡', '包头', '蚌埠'] }, { initial: 'C', list: ['成都', '重庆', '长沙', '常州', '沧州'] }, { initial: 'D', list: ['大连', '东莞', '大理', '德州', '大庆'] }, { initial: 'F', list: ['佛山', '福州', '抚顺', '阜阳'] }, { initial: 'G', list: ['广州', '深圳', '贵阳', '桂林', '赣州'] }, { initial: 'H', list: ['杭州', '合肥', '哈尔滨', '海口', '邯郸', '惠州'] }, { initial: 'J', list: ['济南', '嘉兴', '金华', '九江', '吉林'] }, { initial: 'K', list: ['昆明', '开封'] }, { initial: 'L', list: ['兰州', '洛阳', '临沂', '拉萨', '廊坊'] }, { initial: 'M', list: ['马鞍山', '茂名', '绵阳'] }, { initial: 'N', list: ['南京', '南昌', '南宁', '宁波', '南通'] }, { initial: 'Q', list: ['青岛', '泉州', '秦皇岛', '齐齐哈尔'] }, { initial: 'S', list: ['上海', '苏州', '沈阳', '石家庄', '三亚', '汕头'] }, { initial: 'T', list: ['天津', '太原', '唐山', '泰州', '台州'] }, { initial: 'W', list: ['武汉', '无锡', '温州', '潍坊', '威海'] }, { initial: 'X', list: ['西安', '厦门', '西宁', '徐州', '邢台', '咸阳'] }, { initial: 'Y', list: ['扬州', '烟台', '银川', '宜昌', '延安'] }, { initial: 'Z', list: ['郑州', '珠海', '中山', '淄博', '湛江', '枣庄'] }]
const filteredCityData = computed(() => {
  if (!citySearchQuery.value) return cityData
  return cityData.map(g => ({ ...g, list: g.list.filter(c => c.includes(citySearchQuery.value.trim())) })).filter(g => g.list.length > 0)
})
const filteredIndexList = computed(() => filteredCityData.value.map(g => g.initial))

const form = reactive({
  id: route.query.id || null, consignee: '', sex: 1, phone: '', detail: '', label: '家',
  longitude: null, latitude: null, isDefault: 0,
  provinceName: '', cityName: '', districtName: '',
  provinceCode: '', cityCode: '', districtCode: ''
})

watch(isDefaultBool, (val) => { form.isDefault = val ? 1 : 0 })
watch(() => [locationStore.city, locationStore.locatedCity], ([city, locatedCity]) => {
  const nextCity = city && city !== '定位中' ? city : locatedCity
  if (nextCity && nextCity !== '定位中') {
    currentCity.value = nextCity
  }
}, { immediate: true })
const selectedTip = computed(() => tips.value.find(tip => tip.id === selectedTipId.value) || null)
const pickerLongitude = computed(() => {
  if (selectedTip.value?.location) {
    return Number(String(selectedTip.value.location).split(',')[0])
  }
  return form.longitude || locationStore.longitude || null
})
const pickerLatitude = computed(() => {
  if (selectedTip.value?.location) {
    return Number(String(selectedTip.value.location).split(',')[1])
  }
  return form.latitude || locationStore.latitude || null
})
const pickerMapUrl = computed(() => getStaticMapUrl(pickerLongitude.value, pickerLatitude.value))
const normalizeCityName = (city, province = '') => {
  if (Array.isArray(city)) {
    return city[0] || province || ''
  }
  return city || province || ''
}

const normalizeTip = (tip, index = 0) => ({
  id: tip.id || `${tip.location || tip.name || 'tip'}-${index}`,
  name: tip.name || '',
  district: tip.district || tip.adname || '',
  address: tip.address || '',
  location: tip.location || '',
})

const setTipList = (items) => {
  tips.value = (items || [])
    .filter(item => item.location && typeof item.location === 'string')
    .map((item, index) => normalizeTip(item, index))
  selectedTipId.value = tips.value[0]?.id || ''
}

const onSearchInput = (val) => {
  showLocationPicker.value = true
  if (!val) {
    loadNearbySuggestions(false)
    return
  }
  clearTimeout(searchTimer)
  searchTimer = setTimeout(async () => {
    const results = await inputTips(val, currentCity.value)
    setTipList(results)
  }, 300)
}
let searchTimer = null

const syncCurrentCityFromCoords = async (lng, lat) => {
  if (!lng || !lat) return
  const regeo = await reverseGeocode(lng, lat)
  const city = normalizeCityName(regeo?.addressComponent?.city, regeo?.addressComponent?.province)
  if (city) {
    locationStore.setLocatedContext('', city)
    currentCity.value = locationStore.city && locationStore.city !== '定位中' ? locationStore.city : city
  }
  return regeo
}

const setMapCenter = (lng, lat, zoom = 16) => {
  if (!mapInstance || !lng || !lat) return
  ignoreNextMoveEnd = true
  mapInstance.setZoomAndCenter(zoom, [Number(lng), Number(lat)])
  window.setTimeout(() => {
    ignoreNextMoveEnd = false
  }, 260)
}

const syncAddressByCoords = async (lng, lat, { updateLocationStore = false, moveMap = false } = {}) => {
  if (!lng || !lat) return false

  mapResolving.value = true
  try {
    const regeo = await reverseGeocode(lng, lat)
    if (!regeo) return false

    form.longitude = Number(lng)
    form.latitude = Number(lat)

    const cityName = applyRegionInfo(regeo)
    const addressText = regeo.formatted_address || displayAddress.value || ''
    displayAddress.value = addressText
    searchKey.value = addressText
    locationConfirmed.value = true

    if (!locationStore.isManual || locationStore.manualMode !== 'city') {
      currentCity.value = cityName || currentCity.value
    }

    if (updateLocationStore) {
      locationStore.setLocation({
        longitude: Number(lng),
        latitude: Number(lat),
        address: addressText,
        city: cityName,
        adcode: regeo.addressComponent?.adcode || ''
      })
    }

    if (moveMap) {
      setMapCenter(lng, lat)
    }

    return true
  } finally {
    mapResolving.value = false
  }
}

const handleMapMoveEnd = () => {
  if (!mapInstance || ignoreNextMoveEnd) return
  const center = mapInstance.getCenter()
  clearTimeout(mapMoveTimer)
  mapMoveTimer = window.setTimeout(() => {
    syncAddressByCoords(center.lng, center.lat)
  }, 220)
}

const resolveInitialMapLocation = async () => {
  if (form.longitude && form.latitude) {
    return { lng: Number(form.longitude), lat: Number(form.latitude) }
  }
  if (locationStore.longitude && locationStore.latitude) {
    return { lng: Number(locationStore.longitude), lat: Number(locationStore.latitude) }
  }
  return await locateCurrentAddress()
}

const initInteractiveMap = async () => {
  if (mapInstance || !mapRef.value) return

  mapLoading.value = true
  try {
    const AMap = await loadAmapSdk()
    const initialLocation = await resolveInitialMapLocation()
    if (!initialLocation) return

    mapInstance = new AMap.Map(mapRef.value, {
      zoom: 17,
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

    window.setTimeout(() => {
      mapInstance?.resize()
      setMapCenter(initialLocation.lng, initialLocation.lat, 17)
    }, 120)

    mapInstance.on('moveend', handleMapMoveEnd)
    mapInstance.on('zoomend', handleMapMoveEnd)

    if (!displayAddress.value) {
      await syncAddressByCoords(initialLocation.lng, initialLocation.lat)
    } else {
      setMapCenter(initialLocation.lng, initialLocation.lat)
    }
  } catch (error) {
    showToast('地图加载失败')
  } finally {
    mapLoading.value = false
  }
}

const onSearchFocus = () => {
  showLocationPicker.value = true
  if (searchKey.value.trim()) return
  loadNearbySuggestions(false)
}

const resolveSuggestionLocation = async () => {
  if (form.longitude && form.latitude) {
    if (!currentCity.value || currentCity.value === '定位中') {
      await syncCurrentCityFromCoords(form.longitude, form.latitude)
    }
    return { lng: form.longitude, lat: form.latitude }
  }
  if (locationStore.longitude && locationStore.latitude) {
    if (!currentCity.value || currentCity.value === '定位中') {
      await syncCurrentCityFromCoords(locationStore.longitude, locationStore.latitude)
    }
    return { lng: locationStore.longitude, lat: locationStore.latitude }
  }
  try {
    const current = await getCurrentLocation()
    locationStore.setCoordinates(current.lng, current.lat)
    await syncCurrentCityFromCoords(current.lng, current.lat)
    return current
  } catch (error) {
    return null
  }
}

const applyRegionInfo = (regeo) => {
  if (!regeo?.addressComponent) return ''
  const comp = regeo.addressComponent
  const cityName = normalizeCityName(comp.city, comp.province)
  const districtCode = String(comp.adcode || '')

  form.provinceName = comp.province || ''
  form.cityName = cityName
  form.districtName = comp.district || ''
  form.districtCode = districtCode
  form.provinceCode = districtCode ? `${districtCode.substring(0, 2)}0000` : ''
  form.cityCode = districtCode ? `${districtCode.substring(0, 4)}00` : ''

  return cityName
}

const locateCurrentAddress = async () => {
  let baseLocation = null

  try {
    baseLocation = await getCurrentLocation()
  } catch (error) {
    if (locationStore.locatedLongitude && locationStore.locatedLatitude) {
      baseLocation = { lng: locationStore.locatedLongitude, lat: locationStore.locatedLatitude }
    } else if (locationStore.longitude && locationStore.latitude) {
      baseLocation = { lng: locationStore.longitude, lat: locationStore.latitude }
    } else if (form.longitude && form.latitude) {
      baseLocation = { lng: form.longitude, lat: form.latitude }
    }
  }

  if (!baseLocation) {
    showToast('请先开启定位权限')
    return null
  }

  const regeo = await reverseGeocode(baseLocation.lng, baseLocation.lat)
  if (!regeo) {
    showToast('定位失败，请稍后重试')
    return null
  }

  form.longitude = Number(baseLocation.lng)
  form.latitude = Number(baseLocation.lat)

  const cityName = applyRegionInfo(regeo)
  const locatedAddressText = regeo.formatted_address || displayAddress.value || ''
  displayAddress.value = locatedAddressText
  searchKey.value = locatedAddressText
  locationConfirmed.value = true

  locationStore.setLocation({
    longitude: Number(baseLocation.lng),
    latitude: Number(baseLocation.lat),
    address: locatedAddressText,
    city: cityName,
    adcode: regeo.addressComponent?.adcode || ''
  })

  if (!locationStore.isManual || locationStore.manualMode !== 'city') {
    currentCity.value = cityName || currentCity.value
  } else {
    currentCity.value = locationStore.city || currentCity.value
  }

  return {
    lng: Number(baseLocation.lng),
    lat: Number(baseLocation.lat)
  }
}

const relocateMapToCurrent = async () => {
  const currentLocation = await locateCurrentAddress()
  if (!currentLocation) return
  setMapCenter(currentLocation.lng, currentLocation.lat)
}

const loadNearbySuggestions = async (preferKeyword = true, baseLocation = null) => {
  const location = baseLocation || await resolveSuggestionLocation()
  if (!location) {
    showToast('请先开启定位或搜索地址')
    return
  }

  const typedKeyword = (searchKey.value || '').trim()
  const currentAddress = (displayAddress.value || '').trim()
  const aroundKeyword = preferKeyword && typedKeyword && typedKeyword !== currentAddress ? typedKeyword : ''

  let nearbyPois = await getAroundPois(location.lng, location.lat, aroundKeyword)
  if ((!nearbyPois || nearbyPois.length === 0) && aroundKeyword) {
    nearbyPois = await getAroundPois(location.lng, location.lat, '')
  }
  setTipList((nearbyPois || []).map((poi) => ({
    id: poi.id,
    name: poi.name,
    district: poi.adname || [poi.cityname, poi.pname].filter(Boolean).join(''),
    address: poi.address,
    location: poi.location,
  })))
  if (tips.value.length === 0) {
    showToast('附近暂无推荐地址')
  }
}

// 点击“定位地址”时先拿真实定位，再展开下半页附近地址
const triggerReSearch = async () => {
  showLocationPicker.value = true
  const currentLocation = await locateCurrentAddress()
  await loadNearbySuggestions(false, currentLocation)
}

const applyTipSelection = async (tip) => {
  const [lng, lat] = tip.location.split(',')
  form.longitude = parseFloat(lng); form.latitude = parseFloat(lat)
  displayAddress.value = tip.name
  
  const regeo = await reverseGeocode(lng, lat)
  if (regeo) {
    const cityName = applyRegionInfo(regeo)
    if (!locationStore.isManual || locationStore.manualMode !== 'city') {
      currentCity.value = cityName || currentCity.value
    }
    locationConfirmed.value = true
  }
  searchKey.value = tip.name
  setMapCenter(lng, lat)
}

const selectLocationTip = (tip) => {
  selectedTipId.value = tip.id
}

const confirmLocationSelection = async () => {
  if (!selectedTip.value) {
    showToast('请选择地址')
    return
  }
  await applyTipSelection(selectedTip.value)
  showLocationPicker.value = false
}

const onSave = async () => {
  if (!form.consignee || !form.phone || !locationConfirmed.value || !form.detail) return showToast('信息不完整')
  // 合并保存：定位地址 + 空格 + 门牌号
  const finalDetail = `${displayAddress.value} ${form.detail.trim()}`
  const payload = { ...form, detail: finalDetail }
  saving.value = true
  try {
    const res = isEdit ? await updateAddress(payload) : await addAddress(payload)
    if (res.code === 1) { showToast('保存成功'); router.back() } else showToast(res.msg || '保存失败')
  } catch (e) { showToast('提交失败') } finally { saving.value = false }
}

const onDelete = () => {
  showConfirmDialog({ title: '提醒', message: '确认删除该地址？' }).then(async () => {
    await deleteAddress(form.id); router.back()
  })
}

const onCitySelect = (city) => {
  currentCity.value = city
  locationStore.setCity(city, true, 'city')
  showCityPopup.value = false
  searchKey.value = ''
  tips.value = []
}

onMounted(async () => {
  if (isEdit) {
    const res = await getAddressDetail(form.id)
    if (res.code === 1) {
      const data = res.data
      Object.assign(form, data)
      form.sex = Number(data.sex) === 2 ? 2 : 1
      form.isDefault = Number(data.isDefault) === 1 ? 1 : 0
      form.label = typeof data.label === 'string' && data.label.trim()
        ? data.label.trim()
        : '家'
      
      // 【核心逻辑】拆分 detail 为：定位地址 + 门牌号
      const rawDetail = data.detail || ''
      const spaceIndex = rawDetail.indexOf(' ')
      if (spaceIndex > -1) {
        displayAddress.value = rawDetail.substring(0, spaceIndex)
        form.detail = rawDetail.substring(spaceIndex + 1)
      } else {
        displayAddress.value = data.address || '' // 兼容无空格老数据
        form.detail = rawDetail
      }
      
      searchKey.value = displayAddress.value
      currentCity.value = data.cityName || currentCity.value
      locationConfirmed.value = true
      isDefaultBool.value = Number(form.isDefault) === 1
    }
  } else if (locationStore.longitude && locationStore.latitude && (!currentCity.value || currentCity.value === '定位中')) {
    await syncCurrentCityFromCoords(locationStore.longitude, locationStore.latitude)
  }

  await nextTick()
  await initInteractiveMap()
})

onUnmounted(() => {
  clearTimeout(mapMoveTimer)
  if (mapInstance) {
    mapInstance.destroy()
    mapInstance = null
  }
})
</script>

<style scoped>
.address-edit-page {
  min-height: 100vh;
  background: var(--mt-page-bg);
  padding-bottom: calc(10rem + env(safe-area-inset-bottom));
}

.content-box {
  padding: 1.2rem 1.2rem 0;
}

.mt-search-wrap {
  display: flex;
  align-items: center;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(255, 249, 233, 0.98) 100%);
  padding: 0.8rem 1rem;
  border: 1px solid rgba(245, 194, 0, 0.14);
  border-radius: 1.6rem;
  box-shadow: 0 0.6rem 1.6rem rgba(245, 194, 0, 0.08);
  margin: 1.2rem 1.2rem 0;
}

.city-box {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0 1rem 0 0.2rem;
  border-right: 1px solid rgba(245, 194, 0, 0.16);
  max-width: 8.8rem;
  flex-shrink: 0;
  min-height: 4.4rem;
}

.city-txt {
  font-size: 1.4rem;
  font-weight: 700;
  color: var(--mt-strong);
}

.arrow { font-size: 0.8rem; color: var(--primary-color-dark); }
.search-input-box { flex: 1; }
.custom-search { padding: 0 0 0 1rem; }

:deep(.custom-search .van-search__content) {
  min-height: 4rem;
  border-radius: 999px;
  background: linear-gradient(180deg, #fffef9 0%, #fff7de 100%);
  border: 1px solid rgba(245, 194, 0, 0.14);
  box-shadow: inset 0 0 0 0.1rem rgba(255, 255, 255, 0.7);
}

:deep(.custom-search .van-field__control) {
  font-size: 1.6rem;
}

:deep(.custom-search .van-field__control::placeholder) {
  font-size: 1.6rem;
}

.tip-main { font-size: 1.5rem; font-weight: 700; }
.tip-sub { font-size: 1.2rem; color: var(--mt-muted); margin-top: 0.4rem; }

.map-preview {
  height: 21rem;
  background: linear-gradient(180deg, #fffdf5 0%, #fff3cf 100%);
  position: relative;
  border-radius: 1.8rem;
  overflow: hidden;
  border: 1px solid rgba(245, 194, 0, 0.14);
  box-shadow: 0 0.8rem 1.8rem rgba(245, 194, 0, 0.08);
}

.map-canvas {
  width: 100%;
  height: 100%;
}

.map-loading-mask {
  position: absolute;
  inset: 0;
  z-index: 5;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 253, 245, 0.78);
  backdrop-filter: blur(1px);
}

.map-center-pin {
  position: absolute;
  left: 50%;
  top: 50%;
  z-index: 6;
  width: 2.6rem;
  height: 3.6rem;
  transform: translate(-50%, calc(-100% + 1.2rem));
  pointer-events: none;
}

.map-center-pin::before {
  content: '';
  position: absolute;
  left: 50%;
  top: 0;
  width: 2.6rem;
  height: 2.6rem;
  border-radius: 50% 50% 50% 0;
  transform: translateX(-50%) rotate(-45deg);
  background: linear-gradient(180deg, #ffe37b 0%, var(--primary-color) 100%);
  box-shadow: 0 0.4rem 1rem rgba(245, 194, 0, 0.26);
}

.map-center-pin__dot {
  position: absolute;
  left: 50%;
  top: 0.8rem;
  width: 0.9rem;
  height: 0.9rem;
  border-radius: 50%;
  transform: translateX(-50%);
  background: var(--mt-strong);
  z-index: 1;
}

.map-center-pin.is-loading {
  animation: map-pin-bounce 0.9s ease-in-out infinite;
}

.map-address-tip {
  position: absolute;
  left: 1rem;
  right: 5.8rem;
  bottom: 1rem;
  z-index: 6;
  padding: 0.7rem 1rem;
  border-radius: 999rem;
  background: rgba(255, 255, 255, 0.88);
  color: var(--mt-strong);
  font-size: 1.15rem;
  font-weight: 700;
  box-shadow: 0 0.5rem 1.2rem rgba(0, 0, 0, 0.08);
}

.map-locate-btn {
  position: absolute;
  right: 1rem;
  bottom: 1rem;
  z-index: 6;
  width: 3.8rem;
  height: 3.8rem;
  border: none;
  border-radius: 50%;
  background: linear-gradient(180deg, #fffdf4 0%, #ffe9a0 100%);
  box-shadow: 0 0.6rem 1.2rem rgba(245, 194, 0, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.amap-logo),
:deep(.amap-copyright) {
  opacity: 0.72;
}

.map-placeholder {
  text-align: center;
  font-size: 1.2rem;
  color: var(--mt-muted);
}

@keyframes map-pin-bounce {
  0%, 100% { transform: translate(-50%, calc(-100% + 1.2rem)); }
  50% { transform: translate(-50%, calc(-100% + 0.8rem)); }
}

.form-group {
  margin-top: 1.2rem;
  background: transparent;
}

.action-bar {
  margin-top: 1.2rem;
  margin-bottom: calc(2.4rem + env(safe-area-inset-bottom));
  padding: 1.2rem;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(255, 248, 235, 0.98) 100%);
  border: 1px solid rgba(245, 194, 0, 0.14);
  border-radius: 1.8rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  box-shadow: 0 0.8rem 2rem rgba(0, 0, 0, 0.08);
}

.action-btn {
  min-height: 4.4rem;
  flex: 1;
}

.action-btn--delete {
  border-color: rgba(255, 120, 120, 0.22);
  color: #d64b4b;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96) 0%, rgba(255, 245, 245, 0.98) 100%);
}

.action-btn--save {
  background: linear-gradient(180deg, #ffe27f 0%, #f5c200 100%) !important;
  color: #1f1f1f !important;
  border: none !important;
  box-shadow: 0 0.8rem 1.6rem rgba(245, 194, 0, 0.2);
}

:deep(.action-btn--save .van-button__text) {
  color: #1f1f1f !important;
}

.location-picker-layout {
  height: 100%;
  background: var(--mt-page-bg);
  display: flex;
  flex-direction: column;
}

.location-picker-popup {
  overflow: hidden;
}

.picker-sheet-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.4rem 1.6rem 0.8rem;
}

.picker-sheet-title {
  font-size: 1.5rem;
  font-weight: 800;
  color: var(--mt-strong);
}

.picker-sheet-close {
  width: 3rem;
  height: 3rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--mt-muted);
  background: rgba(255, 255, 255, 0.76);
  border: 1px solid rgba(245, 194, 0, 0.12);
}

.picker-map-preview {
  margin: 0 1.2rem 1.2rem;
  height: 12rem;
  background: linear-gradient(180deg, #fffdf5 0%, #fff3cf 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 1.8rem;
  overflow: hidden;
  border: 1px solid rgba(245, 194, 0, 0.14);
  box-shadow: 0 0.8rem 1.8rem rgba(245, 194, 0, 0.08);
}

.picker-list-wrap {
  flex: 1;
  overflow-y: auto;
  padding: 0 1.2rem 8.8rem;
}

.picker-tip-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.4rem 1.2rem;
  margin-bottom: 1rem;
  background: linear-gradient(180deg, #ffffff 0%, #fffdf7 100%);
  border: 1px solid rgba(245, 194, 0, 0.1);
  border-radius: 1.6rem;
  box-shadow: 0 0.4rem 1rem rgba(245, 194, 0, 0.05);
}

.picker-tip-item--active {
  border-color: rgba(245, 194, 0, 0.34);
  box-shadow: 0 0.6rem 1.4rem rgba(245, 194, 0, 0.12);
}

.picker-tip-info {
  flex: 1;
  min-width: 0;
}

.picker-tip-check {
  flex-shrink: 0;
}

.picker-action-bar {
  position: fixed;
  left: 1.2rem;
  right: 1.2rem;
  bottom: 1rem;
  padding: 1.2rem;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(255, 248, 235, 0.98) 100%);
  border: 1px solid rgba(245, 194, 0, 0.14);
  border-radius: 1.8rem;
  box-shadow: 0 0.8rem 2rem rgba(0, 0, 0, 0.08);
  z-index: 20;
}

.city-picker-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: var(--mt-card-bg);
}

.city-search-box { padding: 0 1.2rem; }

.city-list-container { flex: 1; overflow-y: auto; position: relative; }

:deep(.van-index-bar__sidebar) { right: 0.4rem; z-index: 200; }

:deep(.van-field),
:deep(.van-cell) {
  min-height: 5rem;
}

:deep(.van-field__label),
:deep(.van-field__control),
:deep(.van-cell__title),
:deep(.van-cell__value) {
  font-size: 1.4rem;
}

:deep(.form-group.van-cell-group--inset) {
  margin: 1.2rem 0 0;
  border-radius: 1.8rem;
  overflow: hidden;
  box-shadow: 0 0.8rem 2rem rgba(245, 194, 0, 0.08);
  border: 1px solid rgba(245, 194, 0, 0.12);
}

:deep(.form-group .van-cell) {
  padding: 1.4rem 1.6rem;
  background: linear-gradient(180deg, #ffffff 0%, #fffdf7 100%);
}

:deep(.form-group .van-cell:not(:last-child)::after) {
  left: 1.6rem;
  right: 1.6rem;
  border-color: rgba(245, 194, 0, 0.12);
}

:deep(.form-group .van-field__label),
:deep(.form-group .van-cell__title) {
  color: var(--mt-strong);
  font-weight: 700;
  width: 7.2rem;
  flex: none;
}

:deep(.form-group .van-field__control) {
  color: #1f1f1f;
  font-size: 1.6rem;
  font-weight: 500;
}

:deep(.form-group .van-field__control::placeholder) {
  color: var(--text-color-placeholder);
  font-size: 1.6rem;
}

:deep(.form-group textarea.van-field__control) {
  font-size: 1.6rem;
  line-height: 1.5;
  color: #1f1f1f;
}

:deep(.form-group .van-field__value),
:deep(.form-group .van-cell__value),
:deep(.form-group .van-field__word-limit) {
  color: #2a2a2a;
}

:deep(.form-group .van-radio__label),
:deep(.form-group .van-cell__right-icon) {
  color: #1f1f1f;
}

:deep(.form-group .van-radio-group--horizontal) {
  gap: 1.4rem;
}

:deep(.form-group .van-radio) {
  align-items: center;
}

:deep(.form-group .van-radio__icon .van-badge__wrapper) {
  width: 1.8rem;
  height: 1.8rem;
  border: 1px solid rgba(245, 194, 0, 0.5);
  background: #fffdf7;
  color: transparent;
}

:deep(.form-group .van-radio__icon--checked .van-badge__wrapper) {
  background: #fff1a8;
  border-color: #f5c200;
  color: #b57a00;
}

:deep(.form-group .van-radio__label) {
  color: var(--mt-strong);
}

:deep(.form-group .van-switch) {
  transform: scale(0.96);
}

:deep(.form-group .van-switch--on) {
  background: #ffe7a3;
  border-color: #f2cb4d;
}

:deep(.form-group .van-switch__node) {
  background: #fffdf7;
}

:deep(.location-picker-layout .van-nav-bar) {
  background: var(--mt-card-bg);
}
</style>
