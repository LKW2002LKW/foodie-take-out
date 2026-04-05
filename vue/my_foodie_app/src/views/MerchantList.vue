<template>
  <div class="page-container safe-area-bottom">
    <!-- Meituan Advanced Header -->
    <div class="meituan-header">
      <div class="top-main">
        <div class="location-trigger" @click="onAddressClick">
          <van-icon name="location" color="#FFD100" size="18" />
          <span class="addr-txt van-ellipsis">{{ locationStore.address || '正在定位...' }}</span>
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
      <van-sticky offset-top="0">
        <div class="mt-filter-bar">
          <van-dropdown-menu active-color="#FFD100">
            <van-dropdown-item v-model="sortType" :options="sortOptions" @change="onRefresh" />
          </van-dropdown-menu>
          <div class="nearby-btn" :class="{ active: sortType === 1 }" @click="onNearbyClick">附近商家</div>
        </div>
      </van-sticky>

      <!-- Merchant List -->
      <van-list :loading="loading" :finished="finished" finished-text="没有更多了" class="mt-merchant-list" @load="onLoad">
        <div v-if="loading && list.length === 0" class="mt-skeleton-list">
          <div v-for="i in 4" :key="i" class="mt-skeleton-item"><van-skeleton avatar avatar-size="88px" :row="3" /></div>
        </div>
        <merchant-card v-for="item in list" :key="item.id" :item="item" @click="router.push(`/merchant/${item.id}`)" />
        <van-empty v-if="!loading && list.length === 0" description="该区域暂无商家" />
      </van-list>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
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

const banners = ['https://img.meituan.net/waimaipicture/82df066ed50df1101971cf5c4a783097315648.jpg', 'https://img.meituan.net/waimaipicture/066ed50df1101971cf5c4a78309731564882df.jpg']
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
      if (info) locationStore.setLocation({ longitude: loc.lng, latitude: loc.lat, address: info.formatted_address, city: info.city })
    } catch(e) {}
  }
  onRefresh()
})
</script>

<style scoped>
.page-container { background-color: #f5f5f5; min-height: 100vh; }

.meituan-header { background-color: #fff; padding: 10px 16px 12px; position: sticky; top: 0; z-index: 100; }
.top-main { display: flex; align-items: center; margin-bottom: 8px; }
.location-trigger { 
  display: flex; align-items: center; gap: 6px; 
  max-width: 85%; cursor: pointer;
}
.addr-txt { font-size: 17px; font-weight: 800; color: #222; }
.arrow { font-size: 10px; color: #333; }

.search-bar-wrap { width: 100%; }
:deep(.van-search) { padding: 0; }
:deep(.van-search__content) { background-color: #f2f2f2; }

.banner-wrapper { padding: 12px; }
.mt-banner { height: 110px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
.mt-categories { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; padding: 16px 12px; background-color: #fff; border-radius: 16px 16px 0 0; }
.mt-cat-item { display: flex; flex-direction: column; align-items: center; }
.mt-cat-icon-bg { width: 48px; height: 48px; background: #f9f9f9; border-radius: 16px; display: flex; align-items: center; justify-content: center; }
.mt-cat-icon-bg.active { background: #FFD100; }
.mt-cat-svg { width: 30px; height: 30px; display: block; }
.mt-cat-text { font-size: 12px; color: #444; margin-top: 6px; }
.mt-filter-bar { display: flex; align-items: center; background: #fff; border-bottom: 1px solid #f2f2f2; padding-right: 16px; position: relative; z-index: 110; }
:deep(.van-dropdown-menu__bar) { box-shadow: none; height: 44px; flex: 1; }
.nearby-btn { font-size: 14px; color: #666; padding: 0 12px; position: relative; }
.nearby-btn.active { color: #222; font-weight: 800; }
.mt-merchant-list { padding: 12px; }
.mt-skeleton-item { background: #fff; padding: 16px; margin-bottom: 12px; border-radius: 12px; }
</style>
