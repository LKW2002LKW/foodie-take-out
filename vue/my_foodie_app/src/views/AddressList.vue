<template>
  <div class="address-selector-page" :class="{ 'is-managing': isEditMode }">
    <!-- 1. 顶部复合搜索头 -->
    <div class="sticky-top">
      <div class="nav-bar">
        <van-icon name="arrow-left" size="20" color="#333" @click="onBack" />
        <div class="city-entry" @click="showCityPicker = true">
          <span class="curr-city van-ellipsis">{{ locationStore.city }}</span>
          <van-icon name="arrow-down" class="arrow" />
        </div>
        <div class="search-box">
          <van-search v-model="searchQuery" placeholder="找地址、写字楼、学校" shape="round" background="transparent" clearable @update:model-value="onSearchInput" @focus="isSearching = true" />
        </div>
      </div>
    </div>

    <!-- 2. 搜索联想建议层 -->
    <div v-if="isSearching && tips.length > 0" class="search-tips-overlay">
      <div v-for="tip in tips" :key="tip.id" class="suggestion-item" @click="onSelectSuggestion(tip)">
        <van-icon name="location-o" class="item-icon" />
        <div class="item-info">
          <div class="item-name">{{ tip.name }}</div>
          <div class="item-addr van-ellipsis">{{ tip.district }}{{ tip.address }}</div>
        </div>
      </div>
    </div>

    <!-- 3. 地址管理主区域 -->
    <div class="scroll-container" @click="isSearching = false">
      <div v-if="!isEditMode" class="current-loc-wrap">
        <div class="row-title">{{ locationStore.isManual ? '当前选择城市' : '当前定位' }}</div>
        <div class="loc-card" @click="useCurrentLoc">
          <div class="loc-info">
            <van-icon :name="locationStore.isManual ? 'map-marked' : 'location'" color="#FFD100" size="20" />
            <transition name="fade-slide">
              <span :key="locationStore.address" class="loc-name van-ellipsis">{{ locationStore.address }}</span>
            </transition>
          </div>
          <div class="relocate-btn" @click.stop="onRelocate">
            <van-icon name="aim" :class="{ 'loading-anim': locating }" />
            <span>{{ locationStore.isManual ? '回到当前' : '重新定位' }}</span>
          </div>
        </div>
      </div>

      <div class="address-list-wrap">
        <div class="section-header">
          <span class="title">我的收货地址</span>
          <div class="mini-actions">
            <van-button v-if="!isEditMode" size="mini" plain round icon="plus" @click="onAdd">新增</van-button>
            <van-button size="mini" :type="isEditMode ? 'warning' : 'default'" plain round @click="toggleEditMode">
              {{ isEditMode ? '取消' : '管理' }}
            </van-button>
          </div>
        </div>

        <div v-if="savedAddresses.length > 0" class="addr-cards">
          <div v-for="addr in savedAddresses" :key="addr.id" class="card-item" :class="{ 'selectable': isEditMode }" @click="onItemClick(addr)">
            <div v-if="isEditMode" class="checkbox-box">
              <div class="mt-checkbox" :class="{ 'checked': addr.selected }">
                <van-icon name="success" class="check-icon" />
              </div>
            </div>

            <div class="card-content">
              <div class="card-main">
                <span class="card-addr van-ellipsis">{{ addr.buildingName }}</span>
              </div>
              <div class="card-region van-ellipsis">{{ addr.regionStr }}</div>
              <div class="card-detail van-ellipsis">{{ addr.roomNumber }}</div>
              <div class="card-sub">
                {{ addr.consignee }} {{ addr.phone }}
                <van-tag v-if="addr.isDefault" color="#FFF9E6" text-color="#FFD100" style="margin-left: 8px">默认</van-tag>
              </div>
            </div>
            
            <van-icon v-if="!isEditMode" name="edit" class="edit-btn" @click.stop="onEdit(addr)" />
          </div>
        </div>
        <van-empty v-else image-size="60" description="暂无收货地址" />
      </div>
    </div>

    <!-- 4. 底部批量操作栏 -->
    <div v-if="isEditMode" class="batch-footer safe-area-bottom">
      <div class="select-all" @click="toggleSelectAll">
        <div class="mt-checkbox" :class="{ 'checked': isAllSelected }">
          <van-icon name="success" class="check-icon" />
        </div>
        <span>全选</span>
      </div>
      <van-button round class="batch-del-btn" :disabled="selectedCount === 0" @click="onBatchDelete">
        删除({{ selectedCount }})
      </van-button>
    </div>

    <!-- 5. 城市选择器 -->
    <van-popup v-model:show="showCityPicker" position="right" style="width: 100%; height: 100%">
      <div class="city-picker-layout">
        <van-nav-bar title="选择收货城市" left-arrow @click-left="showCityPicker = false" />
        <div class="city-scroll-box">
          <van-index-bar :index-list="cityIndexList" highlight-color="#FFD100">
            <div v-for="group in cityData" :key="group.initial">
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
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useLocationStore } from '../store/modules/location'
import { getAddressList, batchDeleteAddress } from '../services/address'
import { reverseGeocode, geocode, ipLocation, inputTips } from '../services/amap'
import { getCurrentLocation } from '../utils/location'
import { showToast, showConfirmDialog } from 'vant'

const router = useRouter(); const locationStore = useLocationStore()
const searchQuery = ref(''); const locating = ref(false); const savedAddresses = ref([])
const showCityPicker = ref(false); const isSearching = ref(false); const tips = ref([])
const isEditMode = ref(false)

const selectedCount = computed(() => savedAddresses.value.filter(a => a.selected).length)
const isAllSelected = computed(() => savedAddresses.value.length > 0 && savedAddresses.value.every(a => a.selected))

const cityData = [{ initial: 'A', list: ['阿坝', '安康', '安庆', '鞍山'] }, { initial: 'B', list: ['北京', '保定', '宝鸡', '包头', '蚌埠'] }, { initial: 'C', list: ['成都', '重庆', '长沙', '常州', '沧州'] }, { initial: 'D', list: ['大连', '东莞', '大理', '德州', '大庆'] }, { initial: 'F', list: ['佛山', '福州', '抚顺', '阜阳'] }, { initial: 'G', list: ['广州', '深圳', '贵阳', '桂林', '赣州'] }, { initial: 'H', list: ['杭州', '合肥', '哈尔滨', '海口', '邯郸', '惠州'] }, { initial: 'J', list: ['济南', '嘉兴', '金华', '九江', '吉林'] }, { initial: 'K', list: ['昆明', '开封'] }, { initial: 'L', list: ['兰州', '洛阳', '临沂', '拉萨', '廊坊'] }, { initial: 'M', list: ['马鞍山', '茂名', '绵阳'] }, { initial: 'N', list: ['南昌', '南充', '南京', '南宁', '南通', '宁波', '南特'] }, { initial: 'Q', list: ['青岛', '泉州', '秦皇岛', '齐齐哈尔'] }, { initial: 'S', list: ['上海', '苏州', '沈阳', '石家庄', '三亚', '汕头'] }, { initial: 'T', list: ['天津', '太原', '唐山', '泰州', '台州'] }, { initial: 'W', list: ['武汉', '无锡', '温州', '潍坊', '威海'] }, { initial: 'X', list: ['西安', '厦门', '西宁', '徐州', '邢台', '咸阳'] }, { initial: 'Y', list: ['扬州', '烟台', '银川', '宜昌', '延安'] }, { initial: 'Z', list: ['郑州', '珠海', '中山', '淄博', '湛江', '枣庄'] }]
const cityIndexList = computed(() => cityData.map(g => g.initial))

const onBack = () => router.back()

const loadAddresses = async () => {
  try {
    const res = await getAddressList()
    if (res && res.code === 1) {
      savedAddresses.value = (res.data || []).map(item => {
        const rawDetail = item.detail || ''
        const spaceIdx = rawDetail.indexOf(' ')
        let bName = '', rNum = ''
        if (spaceIdx > -1) { bName = rawDetail.substring(0, spaceIdx); rNum = rawDetail.substring(spaceIdx + 1) } 
        else { bName = rawDetail; rNum = '' }
        return { ...item, selected: false, buildingName: bName, roomNumber: rNum, regionStr: `${item.provinceName}${item.cityName}${item.districtName}` }
      })
    }
  } catch (err) {}
}

const toggleEditMode = () => { isEditMode.value = !isEditMode.value; if (!isEditMode.value) savedAddresses.value.forEach(a => a.selected = false) }
const toggleSelectAll = () => { const target = !isAllSelected.value; savedAddresses.value.forEach(a => a.selected = target) }

const onItemClick = (addr) => {
  if (isEditMode.value) addr.selected = !addr.selected
  else { locationStore.setAddress(addr.buildingName); locationStore.setCoordinates(addr.longitude, addr.latitude); onBack() }
}

const onBatchDelete = () => {
  const ids = savedAddresses.value.filter(a => a.selected).map(a => a.id)
  if (ids.length === 0) return
  showConfirmDialog({ title: '确认删除', message: `确定要删除这 ${ids.length} 个地址吗？` }).then(async () => {
    const res = await batchDeleteAddress(ids)
    if (res.code === 1) { showToast('删除成功'); isEditMode.value = false; loadAddresses() }
  }).catch(() => {})
}

let timer = null
const onSearchInput = (val) => {
  if (!val) { tips.value = []; isSearching.value = false; return }
  clearTimeout(timer)
  timer = setTimeout(async () => {
    const results = await inputTips(val, locationStore.city)
    tips.value = results.filter(t => t.location && typeof t.location === 'string')
  }, 300)
}

const onSelectSuggestion = async (tip) => {
  const [lng, lat] = tip.location.split(',')
  locationStore.setCoordinates(parseFloat(lng), parseFloat(lat)); locationStore.setAddress(tip.name)
  const info = await reverseGeocode(lng, lat); if (info) locationStore.setCity(info.city, true)
  router.push('/merchant/list')
}

const onRelocate = async () => {
  locating.value = true
  try {
    const loc = await getCurrentLocation()
    const isFake = Math.abs(loc.lng - 104.19) < 0.1 && Math.abs(loc.lat - 35.86) < 0.1
    if (isFake) throw new Error('fake')
    const info = await reverseGeocode(loc.lng, loc.lat)
    if (info) locationStore.setLocation({ longitude: loc.lng, latitude: loc.lat, address: info.formatted_address, city: info.city, adcode: info.adcode })
  } catch (e) {
    const ipInfo = await ipLocation()
    if (ipInfo) { locationStore.setCity(ipInfo.city, false); locationStore.setAddress(ipInfo.city)
      const geo = await geocode(ipInfo.city); if (geo) locationStore.setCoordinates(geo.lng, geo.lat)
    }
  } finally { locating.value = false }
}

const onAdd = () => router.push('/address/edit')
const onEdit = (addr) => router.push(`/address/edit?id=${addr.id}`)
const useCurrentLoc = () => onBack()
const onCitySelect = async (city) => { 
  locationStore.setCity(city, true); locationStore.setAddress(city)
  const geo = await geocode(city); if (geo) locationStore.setCoordinates(geo.lng, geo.lat); showCityPicker.value = false 
}

onMounted(() => { loadAddresses(); if (!locationStore.isLocated) onRelocate() })
</script>

<style scoped>
.address-selector-page { min-height: 100vh; background: #f7f7f7; position: relative; padding-bottom: 60px; }
.address-selector-page.is-managing { padding-bottom: 80px; }
.sticky-top { position: sticky; top: 0; z-index: 100; background: #fff; padding: 8px 12px; border-bottom: 1px solid #f5f5f5; }
.nav-bar { display: flex; align-items: center; gap: 8px; }
.city-entry { display: flex; align-items: center; gap: 2px; padding: 0 8px; border-right: 1px solid #eee; max-width: 85px; flex-shrink: 0; cursor: pointer; }
.curr-city { font-size: 14px; font-weight: bold; color: #333; }
.arrow { font-size: 8px; color: #999; }
.search-box { flex: 1; }
:deep(.van-search__content) { background: #f2f2f2; height: 32px; }
.search-tips-overlay { position: absolute; top: 54px; left: 0; right: 0; bottom: 0; background: #fff; z-index: 120; overflow-y: auto; padding: 0 16px; }
.suggestion-item { display: flex; align-items: center; gap: 12px; padding: 16px 0; border-bottom: 1px solid #f5f5f5; }
.item-icon { font-size: 18px; color: #999; }
.item-info { flex: 1; overflow: hidden; }
.item-name { font-size: 15px; font-weight: bold; color: #333; }
.item-addr { font-size: 12px; color: #999; margin-top: 4px; }
.scroll-container { padding-top: 10px; min-height: calc(100vh - 54px); }
.row-title { font-size: 12px; color: #999; padding: 10px 16px; }
.loc-card { display: flex; justify-content: space-between; align-items: center; background: #fff; margin: 0 16px 16px; padding: 14px; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.02); }
.loc-name { font-size: 14px; font-weight: 800; flex: 1; color: #333; overflow: hidden; white-space: nowrap; text-overflow: ellipsis; padding: 0 8px; }
.relocate-btn { display: flex; align-items: center; gap: 4px; color: #FFD100; font-size: 12px; font-weight: bold; }
.address-list-wrap { padding: 0 16px 20px; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; padding: 10px 0; }
.section-header .title { font-size: 12px; color: #999; }
.mini-actions { display: flex; gap: 8px; }
:deep(.van-button--mini) { height: 22px; padding: 0 10px; border-color: #eee; color: #666; }
.card-item { display: flex; align-items: center; background: #fff; border-radius: 12px; padding: 16px; margin-bottom: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.03); }
.card-item.selectable { padding-left: 8px; }
.checkbox-box { padding-right: 12px; }
.card-content { flex: 1; overflow: hidden; }
.card-main { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.card-addr { font-size: 15px; font-weight: 800; color: #222; }
.card-region { font-size: 11px; color: #999; margin-bottom: 4px; }
.card-detail { font-size: 13px; color: #666; margin-bottom: 6px; }
.card-sub { font-size: 12px; color: #999; }
.edit-btn { font-size: 18px; color: #ccc; padding-left: 10px; }
.mt-checkbox { width: 20px; height: 20px; border-radius: 50%; border: 1px solid #ccc; background: #fff; display: flex; align-items: center; justify-content: center; }
.mt-checkbox.checked { background: #FFD100; border-color: #FFD100; }
.check-icon { font-size: 14px; color: #222; opacity: 0; }
.mt-checkbox.checked .check-icon { opacity: 1; }
.batch-footer { position: fixed; bottom: 0; left: 0; right: 0; height: 64px; background: #fff; display: flex; justify-content: space-between; align-items: center; padding: 0 16px; box-shadow: 0 -2px 10px rgba(0,0,0,0.05); z-index: 200; }
.select-all { display: flex; align-items: center; gap: 8px; font-size: 14px; color: #333; }
.batch-del-btn { background: #ff3f3f; color: #fff; width: 120px; height: 40px; font-weight: 800; border: none; }
.batch-del-btn:disabled { background: #ff9999; }
.city-picker-layout { display: flex; flex-direction: column; height: 100vh; background: #fff; }
.city-scroll-box { flex: 1; overflow-y: auto; }
.loading-anim { animation: rotate 1s linear infinite; }
@keyframes rotate { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
</style>
