<template>
  <div class="page-container mobile-page safe-area-bottom">
    <!-- Meituan Advanced Header -->
    <div class="meituan-header">
      <div class="top-main">
        <div class="location-trigger" @click="onAddressClick">
          <van-icon name="location" color="var(--primary-color)" size="1.8rem" />
          <span class="addr-txt van-ellipsis">{{ headerLocationText }}</span>
          <van-icon name="arrow-down" class="arrow" />
        </div>
      </div>
      <div class="search-bar-wrap">
        <van-search
          v-model="searchValue"
          shape="round"
          placeholder="搜索商家或商品"
          @search="onSearch"
        />
      </div>
    </div>

    <div class="scroll-content">
      <!-- Banner (运营位) -->
      <div class="mt-banner" v-if="banners && banners.length">
        <van-swipe class="mt-swipe" :autoplay="3500" :duration="260" indicator-color="white" lazy-render>
          <van-swipe-item v-for="(img, idx) in banners" :key="idx" class="mt-swipe-item">
            <van-image :src="img" fit="cover" width="100%" height="11.2rem" radius="1.6rem" />
          </van-swipe-item>
        </van-swipe>
      </div>

      <!-- Categories -->
      <div class="mt-categories">
        <div v-for="cat in categoryIcons" :key="cat.id" class="mt-cat-item" @click="onCategoryClick(cat.id)">
          <div class="mt-cat-icon-bg" :class="{ 'active': activeCategoryId === cat.id }">
            <svg class="mt-cat-svg" aria-hidden="true">
              <use :xlink:href="`#${getCatIcon(cat.id)}`"></use>
            </svg>
          </div>
          <span class="mt-cat-text">{{ cat.name }}</span>
        </div>
      </div>

      <!-- Filters -->
      <!-- offset-top uses px; 96px ~= 9.6rem at 375px viewport (fits header height) -->
      <van-sticky :offset-top="96">
        <div class="mt-filter-bar">
          <van-dropdown-menu active-color="var(--primary-color)">
            <van-dropdown-item v-model="sortType" :options="sortOptions" @change="onRefresh" />
          </van-dropdown-menu>
          <div class="nearby-btn" :class="{ active: sortType === 1 }" @click="onNearbyClick">附近商家</div>
        </div>
      </van-sticky>

      <!-- Merchant List -->
      <van-list :loading="loading" :finished="finished" finished-text="没有更多了" class="mt-merchant-list" @load="onLoad">
        <div v-if="loading && list.length === 0" class="mt-skeleton-list">
          <div v-for="i in 4" :key="i" class="mt-skeleton-item"><van-skeleton avatar avatar-size="8.8rem" :row="3" /></div>
        </div>
        <merchant-card v-for="item in list" :key="item.id" :item="item" @click="router.push(`/merchant/${item.id}`)" />
        <van-empty v-if="!loading && list.length === 0" description="该区域暂无商家" />
      </van-list>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useLocationStore } from '../store/modules/location'
import { getMerchantPage } from '../services/merchant'
import { getCurrentLocation } from '../utils/location'
import { reverseGeocode } from '../services/amap'
import { showToast } from 'vant'
import MerchantCard from '../components/MerchantCard.vue'

const router = useRouter(); const locationStore = useLocationStore()
const searchValue = ref(''); const sortType = ref(0); const activeCategoryId = ref(null)
const loading = ref(false); const finished = ref(false); const list = ref([]); const page = ref(1); const pageSize = 10
const getRegeoCity = (regeo) => regeo?.addressComponent?.city || regeo?.addressComponent?.province || ''
const headerLocationText = computed(() => {
  if (locationStore.isManual && locationStore.manualMode === 'city') {
    return locationStore.city || locationStore.locatedCity || '定位中'
  }
  return locationStore.address || locationStore.locatedAddress || '正在定位...'
})

const banners = ['https://images.pexels.com/photos/37074051/pexels-photo-37074051.jpeg', 'https://images.pexels.com/photos/19106471/pexels-photo-19106471.jpeg']
const categoryIcons = [{ id: 1, name: '美食' }, { id: 2, name: '甜点饮品' }, { id: 3, name: '超市便利' }, { id: 4, name: '蔬菜水果' }]
const getCatIcon = (id) => ({
  1: 'icon-meishi2',
  2: 'icon-tianpinyinpin-03',
  3: 'icon-hongsezhutigouwuchekong',
  4: 'icon-tiantianxianguodian_mihoutao',
}[id])
const sortOptions = [{ text: '综合排序', value: 0 }, { text: '距离优先', value: 1 }, { text: '销量优先', value: 2 }, { text: '评分优先', value: 3 }, { text: '起送价最低', value: 4 }, { text: '配送费最低', value: 5 }]

const onRefresh = () => { page.value = 1; finished.value = false; list.value = []; onLoad() }
const onLoad = async () => {
  if (loading.value) return; loading.value = true
  try {
    const res = await getMerchantPage({ page: page.value, pageSize, name: searchValue.value, sortType: sortType.value, categoryId: activeCategoryId.value, longitude: locationStore.longitude, latitude: locationStore.latitude })
    if (res.code === 1) { const records = res.data.records || []; list.value.push(...records); if (records.length < pageSize) finished.value = true; else page.value++ }
  } finally { loading.value = false }
}

const onSearch = () => onRefresh()
const onCategoryClick = (id) => { activeCategoryId.value = activeCategoryId.value === id ? null : id; onRefresh() }
const onNearbyClick = () => { if (!locationStore.longitude) return showToast('请先选择地址'); sortType.value = 1; onRefresh() }
const onAddressClick = () => router.push('/address/list')

onMounted(async () => {
  if (!locationStore.isLocated) {
    try {
      const loc = await getCurrentLocation()
      const info = await reverseGeocode(loc.lng, loc.lat)
      if (info) locationStore.setLocation({ longitude: loc.lng, latitude: loc.lat, address: info.formatted_address, city: getRegeoCity(info) })
    } catch(e) {}
  }
  onRefresh()
})
</script>

<style scoped>
.page-container {
  background: var(--mt-page-bg);
  min-height: 100vh;
}

.meituan-header {
  background: linear-gradient(180deg, rgba(255, 244, 203, 0.98) 0%, rgba(255, 252, 244, 0.98) 100%);
  padding: 1rem 1.6rem 1.2rem;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 0.2rem 1.2rem rgba(245, 194, 0, 0.08);
  border-bottom: 1px solid rgba(245, 194, 0, 0.14);
  backdrop-filter: blur(10px);
}

.scroll-content {
  padding: 1.2rem 0 0;
}

.mt-banner {
  padding: 0 1.2rem;
  margin-bottom: 1.2rem;
}

.mt-swipe {
  border-radius: 1.6rem;
  overflow: hidden;
  box-shadow: 0 0.8rem 2rem rgba(245, 194, 0, 0.12);
  border: 1px solid rgba(245, 194, 0, 0.12);
}

.mt-swipe-item {
  border-radius: 1.6rem;
  overflow: hidden;
}

.top-main {
  display: flex;
  align-items: center;
  margin-bottom: 0.8rem;
}

.location-trigger {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  max-width: 85%;
  cursor: pointer;
  min-height: 4.4rem;
}

.addr-txt {
  font-size: 1.7rem;
  font-weight: 700;
  color: var(--mt-strong);
}

.arrow {
  font-size: 1rem;
  color: var(--mt-strong);
}

.search-bar-wrap {
  width: 100%;
}

:deep(.van-search) {
  padding: 0;
}

:deep(.van-search__content) {
  background-color: var(--van-search-content-background);
  min-height: 3.8rem;
  border-radius: 1rem;
  padding: 0 1rem;
  display: flex;
  align-items: center;
}

:deep(.van-search__field) {
  padding: 0;
}

:deep(.van-field__body) {
  min-height: 3.8rem;
  display: flex;
  align-items: center;
}

:deep(.van-field__control) {
  height: 3.8rem;
  line-height: 3.8rem;
  padding: 0;
  font-size: 1.4rem;
}

:deep(.van-field__left-icon),
:deep(.van-field__clear),
:deep(.van-field__control::placeholder) {
  line-height: 3.8rem;
}

.mt-categories {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1.2rem;
  padding: 1.6rem 1.2rem;
  background: linear-gradient(180deg, #FFFFFF 0%, #FFFDF6 100%);
  border-radius: 1.6rem;
  margin: 0 1.2rem 1.2rem;
  box-shadow: 0 0.8rem 2rem rgba(245, 194, 0, 0.08);
  border: 1px solid rgba(245, 194, 0, 0.12);
}

.mt-cat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.mt-cat-icon-bg {
  width: 4.8rem;
  height: 4.8rem;
  background: linear-gradient(180deg, #FFF9E3 0%, #FFF3C4 100%);
  border-radius: 1.6rem;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: inset 0 0.1rem 0.3rem rgba(255, 255, 255, 0.85);
}

.mt-cat-icon-bg.active {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-dark) 100%);
  box-shadow: 0 0.6rem 1.2rem rgba(245, 194, 0, 0.22);
}

.mt-cat-svg {
  width: 3rem;
  height: 3rem;
  display: block;
}

.mt-cat-text {
  font-size: 1.2rem;
  color: var(--mt-strong);
  margin-top: 0.6rem;
  font-weight: 700;
}

.mt-filter-bar {
  display: flex;
  align-items: center;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96) 0%, rgba(255, 250, 239, 0.96) 100%);
  border-bottom: 1px solid rgba(245, 194, 0, 0.12);
  padding-right: 1.6rem;
  position: relative;
  z-index: 90;
  box-shadow: 0 0.6rem 1.6rem rgba(245, 194, 0, 0.05);
}

:deep(.van-dropdown-menu__bar) {
  box-shadow: none;
  min-height: 4.4rem;
  flex: 1;
  background: transparent;
}

.nearby-btn {
  font-size: 1.4rem;
  color: var(--text-color-secondary);
  padding: 0 1.2rem;
  min-height: 4.4rem;
  display: flex;
  align-items: center;
  border-radius: 999rem;
  margin: 0.4rem 0;
  background: rgba(255, 255, 255, 0.75);
  border: 1px solid rgba(245, 194, 0, 0.12);
}

.nearby-btn.active {
  color: var(--mt-strong);
  font-weight: 700;
  background: var(--mt-soft-yellow);
}

.mt-merchant-list {
  padding: 1.2rem;
}

.mt-skeleton-item {
  background: linear-gradient(180deg, #FFFFFF 0%, #FFFDF8 100%);
  padding: 1.6rem;
  margin-bottom: 1.2rem;
  border-radius: 1.6rem;
  border: 1px solid rgba(245, 194, 0, 0.08);
  box-shadow: var(--shadow-sm);
}
</style>
