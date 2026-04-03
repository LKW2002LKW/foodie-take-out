<template>
  <div class="page-container safe-area-bottom">
    <div class="meituan-header">
      <div class="location-section" @click="onLocationClick">
        <van-icon name="location" class="loc-icon" />
        <span class="address-text">{{ locationStore.address || '正在获取位置...' }}</span>
        <van-icon name="arrow" class="arrow-icon" />
      </div>
      <div class="search-section">
        <van-search v-model="searchValue" shape="round" placeholder="想吃点什么？" class="meituan-search" @search="onSearch" />
      </div>
    </div>

    <div class="scroll-content">
      <div class="banner-wrapper">
        <van-swipe class="mt-banner" :autoplay="3000" indicator-color="#FFD000">
          <van-swipe-item v-for="(img, idx) in banners" :key="idx"><van-image :src="img" fit="cover" radius="8" /></van-swipe-item>
        </van-swipe>
      </div>

      <div class="mt-categories">
        <div v-for="cat in categoryIcons" :key="cat.text" class="mt-cat-item">
          <div class="mt-cat-icon-bg"><van-image :src="cat.icon" width="40" height="40" /></div>
          <span class="mt-cat-text">{{ cat.text }}</span>
        </div>
      </div>

      <van-sticky offset-top="0">
        <div class="mt-filter-bar">
          <van-tabs v-model:active="activeSort" line-width="20" color="#FFD000" @change="onSortChange">
            <van-tab title="综合排序" name="default" /><van-tab title="销量" name="sales" /><van-tab title="距离" name="distance" /><van-tab title="筛选" name="filter" />
          </van-tabs>
        </div>
      </van-sticky>

      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" class="mt-merchant-list" @load="onLoad">
        <div v-if="loading && list.length === 0" class="mt-skeleton-list">
          <div v-for="i in 4" :key="i" class="mt-skeleton-item"><van-skeleton avatar avatar-size="88px" :row="3" /></div>
        </div>
        <div v-for="item in list" :key="item.id" class="mt-merchant-card" @click="onMerchantClick(item)">
          <div class="mt-m-left"><van-image :src="item.image || item.logo" fit="cover" radius="8" class="mt-m-logo" /></div>
          <div class="mt-m-right">
            <div class="mt-m-name">{{ item.name || item.merchantName }}</div>
            <div class="mt-m-stats">
              <span class="mt-score"><van-icon name="star" color="#FFD000" /> {{ item.rating || '4.5' }}</span>
              <span class="mt-sales">月售{{ item.salesVolume || 0 }}+</span>
              <span class="mt-distance">{{ formatDistance(item.distance) }}</span>
            </div>
            <div class="mt-m-delivery">
              <span>起送 ￥{{ item.minAppQuantity || item.minDeliveryAmount || 0 }}</span>
              <span class="mt-sep">|</span>
              <span>配送 ￥{{ item.deliveryPrice || item.deliveryFee || 0 }}</span>
            </div>
            <div class="mt-m-tags">
              <span v-for="tag in (item.tags || ['准时达', '品质店'])" :key="tag" class="mt-tag-chip">{{ tag }}</span>
            </div>
          </div>
        </div>
      </van-list>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useLocationStore } from '../store/modules/location'
import { getMerchantPage } from '../services/merchant'
import { showToast } from 'vant'

const router = useRouter(); const locationStore = useLocationStore(); const searchValue = ref(''); const activeSort = ref('default'); const loading = ref(false); const finished = ref(false); const list = ref([]); const page = ref(1); const pageSize = 10;
const banners = ['https://img.meituan.net/waimaipicture/82df066ed50df1101971cf5c4a783097315648.jpg', 'https://img.meituan.net/waimaipicture/066ed50df1101971cf5c4a78309731564882df.jpg']
const categoryIcons = [{ text: '美食', icon: 'https://p1.meituan.net/jptoppic/aa5da63ef00ba30993e990697930a09533780.png' }, { text: '甜点饮品', icon: 'https://p0.meituan.net/jptoppic/2d7971202ecfa8cf260773a4741347ad3516.png' }, { text: '超市便利', icon: 'https://p1.meituan.net/jptoppic/5da63ef00ba30993e990697930a095aa33780.png' }, { text: '蔬菜水果', icon: 'https://p0.meituan.net/jptoppic/2ecfa8cf260773a4741347ad2d7971203516.png' }, { text: '送药上门', icon: 'https://p1.meituan.net/jptoppic/aa5da63ef00ba30993e990697930a09533780.png' }]
const formatDistance = (d) => { if (!d) return ''; const num = parseFloat(d); return num < 1 ? (num * 1000).toFixed(0) + 'm' : num.toFixed(1) + 'km' }
const onLoad = async () => {
  if (loading.value) return; loading.value = true
  const params = { page: page.value, pageSize, name: searchValue.value }
  if (locationStore.longitude && locationStore.latitude) { params.longitude = locationStore.longitude; params.latitude = locationStore.latitude; if (activeSort.value === 'distance') params.sortType = 1 }
  try {
    const res = await getMerchantPage(params)
    if (res.code === 1) { const records = res.data.records || []; if (page.value === 1) list.value = records; else list.value.push(...records); if (records.length < pageSize) finished.value = true; else page.value++ } else finished.value = true
  } catch (e) { finished.value = true } finally { loading.value = false }
}
const onSearch = () => { page.value = 1; finished.value = false; list.value = []; onLoad() }
const onSortChange = () => onSearch()
const onLocationClick = () => router.push('/address/list?mode=select')
const onMerchantClick = (item) => { if (item.status === 3) return showToast('该商家已休息'); router.push('/merchant/' + item.id) }
watch(() => [locationStore.longitude, locationStore.latitude], () => onSearch())
onMounted(() => onLoad())
</script>

<style scoped>
.page-container { background-color: #f5f5f5; min-height: 100vh; }
.meituan-header { background-color: #fff; padding: 10px 16px 5px; position: sticky; top: 0; z-index: 100; }
.location-section { display: flex; align-items: center; margin-bottom: 8px; }
.address-text { font-size: 16px; font-weight: 800; color: #222; margin: 0 4px; max-width: 240px; overflow: hidden; white-space: nowrap; text-overflow: ellipsis; }
.meituan-search { padding: 0; }
:deep(.van-search__content) { background-color: #f2f2f2; }
.banner-wrapper { padding: 12px; }
.mt-banner { height: 110px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
.mt-categories { display: grid; grid-template-columns: repeat(5, 1fr); gap: 12px; padding: 16px 12px; background-color: #fff; border-radius: 16px 16px 0 0; }
.mt-cat-item { display: flex; flex-direction: column; align-items: center; }
.mt-cat-text { font-size: 12px; color: #444; margin-top: 6px; font-weight: 500; }
.mt-filter-bar { background-color: #fff; border-bottom: 1px solid #f2f2f2; }
:deep(.van-tab--active) { color: #222; font-weight: 800; }
.mt-merchant-list { padding: 12px; }
.mt-merchant-card { display: flex; background-color: #fff; padding: 12px; border-radius: 12px; margin-bottom: 12px; transition: transform 0.2s; }
.mt-m-logo { width: 90px; height: 90px; background-color: #f9f9f9; }
.mt-m-right { flex: 1; margin-left: 12px; display: flex; flex-direction: column; justify-content: space-between; }
.mt-m-name { font-size: 17px; font-weight: 800; color: #222; margin-bottom: 4px; }
.mt-m-stats { display: flex; align-items: center; font-size: 12px; color: #666; margin-bottom: 4px; }
.mt-score { color: #ff9500; font-weight: 800; margin-right: 8px; }
.mt-m-delivery { font-size: 12px; color: #666; margin-bottom: 6px; }
.mt-m-tags { display: flex; gap: 6px; flex-wrap: wrap; }
.mt-tag-chip { font-size: 10px; color: #ff4b33; background-color: #fff0ec; padding: 1px 6px; border-radius: 4px; }
.mt-skeleton-item { background: #fff; padding: 16px; margin-bottom: 12px; border-radius: 12px; }
</style>
