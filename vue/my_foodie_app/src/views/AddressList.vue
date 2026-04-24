<template>
  <div class="address-selector-page mobile-page" :class="{ 'is-managing': isEditMode }">
    <!-- 1. 顶部复合搜索头 -->
    <div class="sticky-top">
      <div class="nav-bar">
        <van-icon name="arrow-left" size="2rem" color="var(--mt-strong)" @click="onBack" />
        <div class="city-entry" @click="showCityPicker = true">
          <span class="curr-city van-ellipsis">{{ locationStore.city }}</span>
          <van-icon name="arrow-down" class="arrow" />
        </div>
        <div class="search-box">
          <van-search v-model="searchQuery" placeholder="找地址、写字楼、学校" shape="round" background="transparent" clearable @update:model-value="onSearchInput" @focus="isSearching = true" />
        </div>
      </div>
    </div>

    <!-- 2. 搜索结果 -->
    <div v-if="showSearchPanel" class="search-tips-overlay">
      <div v-for="tip in tips" :key="tip.id" class="suggestion-item" @click="onSelectSuggestion(tip)">
        <van-icon name="location-o" class="item-icon" />
        <div class="item-info">
          <div class="item-name">{{ tip.name }}</div>
          <div class="item-addr van-ellipsis">{{ tip.district }}{{ tip.address }}</div>
        </div>
      </div>
      <van-empty v-if="tips.length === 0" image-size="72" description="未搜索到相关地址" class="search-empty" />
    </div>

    <!-- 3. 地址管理 -->
    <div v-else class="scroll-container" @click="isSearching = false">
      <div v-if="!isEditMode" class="current-loc-wrap">
        <div class="row-title">{{ isManualCityMode ? '当前选择城市' : '当前定位' }}</div>
        <div class="loc-card" @click="useCurrentLoc">
          <div class="loc-info">
            <van-icon :name="isManualCityMode ? 'map-marked' : 'location'" color="var(--primary-color)" size="2rem" />
            <transition name="fade-slide">
              <span :key="locationStore.locatedAddress" class="loc-name van-ellipsis">{{ locationStore.locatedAddress }}</span>
            </transition>
          </div>
          <div class="relocate-btn" @click.stop="onRelocateAction">
            <van-icon name="aim" :class="{ 'loading-anim': locating }" />
            <span>{{ isManualCityMode ? '回到当前' : '重新定位' }}</span>
          </div>
        </div>
      </div>

      <div class="address-list-wrap">
        <div class="section-header">
          <span class="title">我的收货地址</span>
          <div class="mini-actions">
            <van-button v-if="!isEditMode" size="mini" class="add-btn" plain round icon="plus" @click="onAdd">新增</van-button>
            <van-button size="mini" class="manage-btn" :type="isEditMode ? 'warning' : 'default'" plain round @click="toggleEditMode">
              {{ isEditMode ? '取消' : '管理' }}
            </van-button>
          </div>
        </div>

        <div v-if="savedAddresses.length > 0" class="addr-cards">
          <div v-for="addr in savedAddresses" :key="addr.id" class="card-item" :class="{ 'selectable': isEditMode }" @click="onItemClick(addr)">
            <div v-if="isEditMode" class="checkbox-box">
              <div class="mt-checkbox" :class="{ 'checked': addr.selected }"><van-icon name="success" class="check-icon" /></div>
            </div>
            <div class="card-content">
              <div class="card-main">
                <span class="card-addr">{{ addr.fullAddress || addr.detail || [addr.provinceName, addr.cityName, addr.districtName].filter(Boolean).join('') }}</span>
              </div>
              <div class="card-sub">
                <span>{{ addr.consignee }}</span>
                <span>{{ addr.phone }}</span>
                <van-tag v-if="addr.isDefault" color="var(--primary-color-light)" text-color="var(--primary-color)" class="default-tag">默认</van-tag>
                <van-tag
                  v-if="addr.label"
                  class="addr-label-tag"
                  :class="`addr-label-tag--${addr.label}`"
                >
                  {{ addr.label }}
                </van-tag>
              </div>
            </div>
            <van-icon v-if="!isEditMode" name="edit" class="edit-btn" @click.stop="onEdit(addr)" />
          </div>
        </div>
        <van-empty v-else image-size="60" description="暂无收货地址" />
      </div>
    </div>

    <!-- 4. 批量操作 & 5. 城市选择器 (代码保持) -->
    <div v-if="isEditMode" class="batch-footer safe-area-bottom">
      <div class="select-all" @click="toggleSelectAll"><div class="mt-checkbox" :class="{ 'checked': isAllSelected }"><van-icon name="success" class="check-icon" /></div><span>全选</span></div>
      <van-button round class="batch-del-btn" :disabled="selectedCount === 0" @click="onBatchDelete">删除({{ selectedCount }})</van-button>
    </div>
    <van-popup :show="showCityPicker" @update:show="showCityPicker = $event" position="right" style="width: 100%; height: 100%">
      <div class="city-picker-layout">
        <van-nav-bar title="选择收货城市" left-arrow @click-left="showCityPicker = false" />
        <div class="city-scroll-box">
          <van-index-bar :index-list="cityIndexList" :highlight-color="'var(--primary-color)'">
            <div v-for="group in cityData" :key="group.initial"><van-index-anchor :index="group.initial" /><van-cell v-for="city in group.list" :key="city" :title="city" @click="onCitySelect(city)" /></div>
          </van-index-bar>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useLocationStore } from '../store/modules/location'
import { getAddressList, batchDeleteAddress } from '../services/address'
import { reverseGeocode, geocode, ipLocation, inputTips } from '../services/amap'
import { getCurrentLocation } from '../utils/location'
import { showToast, showConfirmDialog } from 'vant'

const router = useRouter(); const locationStore = useLocationStore()
const searchQuery = ref(''); const locating = ref(false); const savedAddresses = ref([]); const showCityPicker = ref(false)
const isSearching = ref(false); const tips = ref([]); const isEditMode = ref(false)
const isManualCityMode = computed(() => locationStore.isManual && locationStore.manualMode === 'city')
const showSearchPanel = computed(() => Boolean(searchQuery.value.trim()))

const cityData = [{ initial: 'A', list: ['阿坝', '安康', '安庆', '鞍山'] }, { initial: 'B', list: ['北京', '保定', '宝鸡', '包头', '蚌埠'] }, { initial: 'C', list: ['成都', '重庆', '长沙', '常州', '沧州'] }, { initial: 'D', list: ['大连', '东莞', '大理', '德州', '大庆'] }, { initial: 'F', list: ['佛山', '福州', '抚顺', '阜阳'] }, { initial: 'G', list: ['广州', '深圳', '贵阳', '桂林', '赣州'] }, { initial: 'H', list: ['杭州', '合肥', '哈尔滨', '海口', '邯郸', '惠州'] }, { initial: 'J', list: ['济南', '嘉兴', '金华', '九江', '吉林'] }, { initial: 'K', list: ['昆明', '开封'] }, { initial: 'L', list: ['兰州', '洛阳', '临沂', '拉萨', '廊坊'] }, { initial: 'M', list: ['马鞍山', '茂名', '绵阳'] }, { initial: 'N', list: ['南昌', '南充', '南京', '南宁', '南通', '宁波', '南特'] }, { initial: 'Q', list: ['青岛', '泉州', '秦皇岛', '齐齐哈尔'] }, { initial: 'S', list: ['上海', '苏州', '沈阳', '石家庄', '三亚', '汕头'] }, { initial: 'T', list: ['天津', '太原', '唐山', '泰州', '台州'] }, { initial: 'W', list: ['武汉', '无锡', '温州', '潍坊', '威海'] }, { initial: 'X', list: ['西安', '厦门', '西宁', '徐州', '邢台', '咸阳'] }, { initial: 'Y', list: ['扬州', '烟台', '银川', '宜昌', '延安'] }, { initial: 'Z', list: ['郑州', '珠海', '中山', '淄博', '湛江', '枣庄'] }]
const cityIndexList = computed(() => cityData.map(g => g.initial))
const getRegeoCity = (regeo) => regeo?.addressComponent?.city || regeo?.addressComponent?.province || ''
const getRegeoAdcode = (regeo) => regeo?.addressComponent?.adcode || ''

const onBack = () => router.back()
const loadAddresses = async () => { const res = await getAddressList(); if (res && res.code === 1) savedAddresses.value = (res.data || []).map(a => ({ ...a, selected: false })) }

// 核心：升级后的初始化定位 (延长GPS时间，加入持久化感应)
const initSmartLocation = async (force = false, allowIpFallback = !force) => {
  // 如果不是强制刷新，且当前已经是手动模式，不自动触发
  if (!force && locationStore.isManual) return

  locating.value = true
  try {
    // 准则 1: 延长至 6 秒竞速时间，给 GPS 足够机会
    const gpsPromise = getCurrentLocation()
    const timeoutPromise = new Promise((_, r) => setTimeout(() => r('timeout'), 6000))
    
    const loc = await Promise.race([gpsPromise, timeoutPromise])
    
    // 拦截 104.19, 35.86 伪坐标
    const isFake = Math.abs(loc.lng - 104.19) < 0.1 && Math.abs(loc.lat - 35.86) < 0.1
    if (isFake) throw new Error('fake')

    const info = await reverseGeocode(loc.lng, loc.lat)
    if (info) {
      locationStore.setLocation({ 
        longitude: loc.lng, latitude: loc.lat, 
        address: info.formatted_address, city: getRegeoCity(info), adcode: getRegeoAdcode(info)
      })
      return true
    }
  } catch (e) {
    console.warn('GPS location attempt finished with error or timeout:', e)
    // 非强制“回到当前”时，且完全没有定位缓存，才允许 IP 兜底
    if (allowIpFallback && !locationStore.locatedLongitude) {
      const ipInfo = await ipLocation()
      if (ipInfo) {
        const geo = await geocode(ipInfo.city)
        locationStore.setLocation({
          longitude: geo?.lng,
          latitude: geo?.lat,
          address: ipInfo.city,
          city: ipInfo.city,
          adcode: ipInfo.adcode
        })
        return true
      }
    }
    return false
  } finally { locating.value = false }
}

const onForceRelocate = () => initSmartLocation(true, false)
const switchToCurrentLoc = () => { locationStore.useLocatedLocation() }
const onRelocateAction = async () => {
  if (isManualCityMode.value) {
    const relocated = await onForceRelocate()
    if (relocated) {
      switchToCurrentLoc()
    } else {
      showToast('当前定位失败，请检查定位权限')
    }
    return
  }
  await onForceRelocate()
}
const toggleEditMode = () => { isEditMode.value = !isEditMode.value; if (!isEditMode.value) savedAddresses.value.forEach(a => a.selected = false) }
const toggleSelectAll = () => { const target = !isAllSelected.value; savedAddresses.value.forEach(a => a.selected = target) }
const onItemClick = (addr) => {
  if (isEditMode.value) {
    addr.selected = !addr.selected
  } else {
    const selectedAddress = addr.fullAddress || addr.detail || [addr.provinceName, addr.cityName, addr.districtName].filter(Boolean).join('')
    locationStore.setAddress(selectedAddress, true, 'address')
    locationStore.setCity(addr.cityName || locationStore.city, true, 'address')
    locationStore.setCoordinates(addr.longitude, addr.latitude)
    onBack()
  }
}
const onBatchDelete = () => {
  const ids = savedAddresses.value.filter(a => a.selected).map(a => a.id)
  if (ids.length === 0) return
  showConfirmDialog({ title: '确认删除', message: `确定要删除这 ${ids.length} 个地址吗？` }).then(async () => {
    const res = await batchDeleteAddress(ids); if (res.code === 1) { showToast('删除成功'); isEditMode.value = false; loadAddresses() }
  }).catch(() => {})
}

let timer = null
const onSearchInput = (val) => {
  if (!val || !val.trim()) { tips.value = []; isSearching.value = false; return }
  isSearching.value = true
  clearTimeout(timer); timer = setTimeout(async () => {
    const results = await inputTips(val, locationStore.city); tips.value = results.filter(t => t.location && typeof t.location === 'string')
  }, 300)
}
const onSelectSuggestion = async (tip) => {
  const [lng, lat] = tip.location.split(',')
  locationStore.setCoordinates(parseFloat(lng), parseFloat(lat))
  locationStore.setAddress(tip.name, true, 'address')
  const info = await reverseGeocode(lng, lat)
  if (info) {
    locationStore.setCity(getRegeoCity(info), true, 'address')
  }
  router.push('/merchant/list')
}

const onAdd = () => router.push('/address/edit'); const onEdit = (addr) => router.push(`/address/edit?id=${addr.id}`)
const useCurrentLoc = () => { switchToCurrentLoc(); onBack() }
const onCitySelect = async (city) => {
  locationStore.setCity(city, true, 'city')
  const geo = await geocode(city)
  if (geo) locationStore.setCoordinates(geo.lng, geo.lat)
  showCityPicker.value = false
}
const selectedCount = computed(() => savedAddresses.value.filter(a => a.selected).length)
const isAllSelected = computed(() => savedAddresses.value.length > 0 && savedAddresses.value.every(a => a.selected))

onMounted(() => { loadAddresses(); initSmartLocation() })
</script>

<style scoped>
.address-selector-page { min-height: 100vh; background: var(--mt-page-bg); position: relative; padding-bottom: 6rem; }
.address-selector-page.is-managing { padding-bottom: 8rem; }
.sticky-top { position: sticky; top: 0; z-index: 100; background: linear-gradient(180deg, rgba(255, 233, 157, 0.96) 0%, rgba(255, 248, 235, 0.96) 100%); padding: 0.8rem 1.2rem 1rem; border-bottom: 1px solid rgba(245, 194, 0, 0.16); box-shadow: 0 0.2rem 1.2rem rgba(245, 194, 0, 0.08); backdrop-filter: blur(10px); }
.nav-bar { display: flex; align-items: center; gap: 0.8rem; }
.city-entry { display: flex; align-items: center; gap: 0.2rem; padding: 0 0.9rem; border-right: 1px solid rgba(245, 194, 0, 0.2); max-width: 8.5rem; flex-shrink: 0; cursor: pointer; min-height: 4.4rem; background: rgba(255, 255, 255, 0.45); border-radius: 999rem 0 0 999rem; margin-right: 0.2rem; }
.curr-city { font-size: 1.4rem; font-weight: 900; color: var(--mt-strong); }
.arrow { font-size: 0.8rem; color: var(--primary-color-dark); }
.search-box { flex: 1; }
.search-box :deep(.van-search) { padding: 0; }
:deep(.van-search__content) { background: var(--van-search-content-background); min-height: 3.8rem; padding: 0 1rem; display: flex; align-items: center; border: 1px solid rgba(245, 194, 0, 0.18); box-shadow: inset 0 0 0 0.1rem rgba(255,255,255,0.7), 0 0.4rem 1.2rem rgba(245, 194, 0, 0.08); }
:deep(.van-search__content--round) { border-radius: 999rem; }
:deep(.van-field__left-icon) { color: var(--primary-color-dark); }
:deep(.van-search__field) { padding: 0; }
:deep(.van-field__body) { min-height: 3.8rem; display: flex; align-items: center; }
:deep(.van-field__control) { height: 3.8rem; line-height: 3.8rem; padding: 0; font-size: 1.4rem; }
:deep(.van-field__left-icon),
:deep(.van-field__clear),
:deep(.van-field__control::placeholder) { line-height: 3.8rem; }
.search-tips-overlay { position: absolute; top: 5.4rem; left: 0; right: 0; bottom: 0; background: var(--mt-card-bg); z-index: 120; overflow-y: auto; padding: 0 1.6rem; }
.suggestion-item { display: flex; align-items: center; gap: 1.2rem; padding: 1.6rem 0; border-bottom: 1px solid var(--mt-divider); }
.item-icon { font-size: 1.8rem; color: var(--mt-muted); }
.item-info { flex: 1; overflow: hidden; }
.item-name { font-size: 1.5rem; font-weight: 900; color: var(--mt-strong); }
.item-addr { font-size: 1.2rem; color: var(--mt-muted); margin-top: 0.4rem; }
.scroll-container { padding-top: 1rem; min-height: calc(100vh - 5.4rem); }
.row-title { font-size: 1.2rem; color: var(--mt-muted); padding: 1rem 1.6rem; }
.loc-card { display: flex; justify-content: space-between; align-items: center; background: linear-gradient(135deg, #FFFDF5 0%, #FFFFFF 50%, #FFF6D8 100%); margin: 0 1.6rem 1.6rem; padding: 1.4rem; border-radius: var(--mt-card-radius); box-shadow: var(--shadow-sm); min-height: 4.4rem; border: 1px solid rgba(255, 208, 0, 0.18); position: relative; overflow: hidden; }
.loc-card::before { content: ''; position: absolute; left: 0; top: 0; bottom: 0; width: 0.4rem; background: linear-gradient(180deg, var(--primary-color) 0%, var(--primary-color-dark) 100%); }
.loc-info { display: flex; align-items: center; flex: 1; min-width: 0; }
.loc-name { font-size: 1.4rem; font-weight: 900; flex: 1; color: var(--mt-strong); overflow: hidden; white-space: nowrap; text-overflow: ellipsis; padding: 0 0.8rem; }
.relocate-btn { display: flex; align-items: center; gap: 0.4rem; color: var(--primary-color-dark); font-size: 1.2rem; font-weight: 900; min-height: 4.4rem; background: rgba(255, 208, 0, 0.14); padding: 0 1rem; border-radius: 999rem; }
.address-list-wrap { padding: 0 1.6rem 2rem; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.2rem; padding: 1rem 0; position: relative; }
.section-header .title { position: relative; padding-left: 1rem; font-size: 1.4rem; font-weight: 900; color: var(--mt-strong); }
.section-header .title::before { content: ''; position: absolute; left: 0; top: 50%; width: 0.4rem; height: 1.4rem; transform: translateY(-50%); border-radius: 999rem; background: linear-gradient(180deg, var(--primary-color) 0%, var(--primary-color-dark) 100%); }
.mini-actions { display: flex; gap: 0.8rem; }
:deep(.van-button--mini) { min-height: 2.8rem; padding: 0 1rem; border-color: var(--border-color); color: var(--text-color-secondary); font-size: 1.1rem; }
:deep(.add-btn) { border-color: rgba(245, 194, 0, 0.3); color: var(--secondary-color); background: var(--mt-soft-yellow); font-weight: 800; }
:deep(.manage-btn) { border-color: rgba(245, 194, 0, 0.3); color: var(--secondary-color); background: rgba(255, 255, 255, 0.86); font-weight: 800; }
.card-item { display: flex; align-items: center; background: var(--mt-card-bg); border-radius: var(--mt-card-radius); padding: 1.6rem; margin-bottom: 1.2rem; box-shadow: var(--shadow-sm); min-height: 4.4rem; }
.card-item.selectable { padding-left: 0.8rem; }
.checkbox-box { padding-right: 1.2rem; }
.card-content { flex: 1; overflow: hidden; }
.card-main { display: flex; align-items: flex-start; gap: 0.8rem; margin-bottom: 0.8rem; }
.card-addr {
  font-size: 1.5rem;
  font-weight: 900;
  color: var(--mt-strong);
  line-height: 1.5;
  white-space: normal;
  word-break: break-all;
}
.card-sub {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.6rem 1rem;
  font-size: 1.2rem;
  color: var(--text-color-secondary);
}
.default-tag { margin-left: 0.8rem; }
.addr-label-tag {
  border-radius: 999px;
  padding: 0 0.8rem;
  border: none;
  font-size: 1.1rem;
  line-height: 1.8rem;
}
.addr-label-tag--家 {
  background: rgba(245, 194, 0, 0.16);
  color: #9a6a00;
}
.addr-label-tag--学校 {
  background: rgba(76, 175, 80, 0.16);
  color: #2e7d32;
}
.addr-label-tag--公司 {
  background: rgba(33, 150, 243, 0.16);
  color: #1565c0;
}
.edit-btn { font-size: 1.8rem; color: var(--text-color-placeholder); padding-left: 1rem; }
.mt-checkbox { width: 2rem; height: 2rem; border-radius: 50%; border: 1px solid rgba(245, 194, 0, 0.35); background: rgba(255, 252, 240, 0.98); display: flex; align-items: center; justify-content: center; }
.mt-checkbox.checked { background: var(--primary-color); border-color: var(--primary-color); }
.check-icon { font-size: 1.4rem; color: var(--mt-strong); opacity: 0; }
.mt-checkbox.checked .check-icon { opacity: 1; }
.batch-footer { position: fixed; bottom: 0; left: 0; right: 0; min-height: 6.4rem; background: linear-gradient(180deg, rgba(255, 255, 255, 0.96) 0%, rgba(255, 248, 235, 0.98) 100%); display: flex; justify-content: space-between; align-items: center; padding: 0 1.6rem; box-shadow: 0 -0.2rem 1rem rgba(245, 194, 0, 0.08); border-top: 1px solid rgba(245, 194, 0, 0.16); z-index: 200; }
.select-all { display: flex; align-items: center; gap: 0.8rem; font-size: 1.4rem; color: var(--mt-strong); min-height: 4.4rem; }
.batch-del-btn { background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-dark) 100%); color: var(--secondary-color); width: 12rem; min-height: 4.4rem; font-weight: 800; border: none; box-shadow: 0 0.6rem 1.2rem rgba(245, 194, 0, 0.22); }
.batch-del-btn:disabled { opacity: 0.5; }
.city-picker-layout { display: flex; flex-direction: column; height: 100vh; background: linear-gradient(180deg, #FFF9EC 0%, #FFFFFF 100%); }
.city-scroll-box { flex: 1; overflow-y: auto; }
.loading-anim { animation: rotate 1s linear infinite; }
@keyframes rotate { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
</style>
