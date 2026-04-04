<template>
  <div class="home-page">
    <!-- 1. 顶部搜索 -->
    <div class="header-sticky">
      <van-search
        v-model="queryParams.name"
        shape="round"
        background="#fff"
        placeholder="想吃点什么？"
        @search="onRefresh"
        @clear="onRefresh"
      />
    </div>

    <!-- 2. 分类入口 (固定映射) -->
    <div class="category-grid">
      <div 
        v-for="cat in BIZ_CATEGORY_OPTIONS" :key="cat.id" 
        class="cat-item"
        :class="{ active: queryParams.categoryId === cat.id }"
        @click="onCategoryClick(cat.id)"
      >
        <div class="cat-icon">
          <van-icon :name="getCatIcon(cat.id)" size="32" :color="queryParams.categoryId === cat.id ? '#222' : '#666'" />
        </div>
        <span class="cat-text">{{ cat.name }}</span>
      </div>
    </div>

    <!-- 3. 筛选栏 (排序映射) -->
    <van-sticky offset-top="54">
      <div class="filter-bar">
        <van-dropdown-menu active-color="#FFD100">
          <van-dropdown-item 
            v-model="queryParams.sortType" 
            :options="sortOptions" 
            @change="onSortChange"
          />
        </van-dropdown-menu>
        <div 
          class="nearby-btn" 
          :class="{ active: queryParams.sortType === 1 }"
          @click="onNearbyClick"
        >
          附近商家
        </div>
      </div>
    </van-sticky>

    <!-- 4. 商家列表 -->
    <div class="list-container">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
        >
          <div v-if="loading && list.length === 0" class="skeleton-box">
            <van-skeleton v-for="i in 3" :key="i" avatar avatar-size="90px" :row="3" />
          </div>
          
          <merchant-card 
            v-for="item in list" 
            :key="item.id" 
            :item="item"
            @click="onMerchantClick(item)"
          />

          <van-empty v-if="!loading && list.length === 0" description="暂无相关商家" />
        </van-list>
      </van-pull-refresh>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMerchantPage } from '../services/merchant'
import { useLocationStore } from '../store/modules/location'
import { showToast } from 'vant'
import MerchantCard from '../components/MerchantCard.vue'

const router = useRouter()
const locationStore = useLocationStore()

// --- 一、统一约定 ---
const BIZ_CATEGORY_OPTIONS = [
  { id: 1, name: '美食' },
  { id: 2, name: '甜点饮品' },
  { id: 3, name: '超市便利' },
  { id: 4, name: '蔬菜水果' }
]

const sortOptions = [
  { text: '综合排序', value: 0 },
  { text: '距离优先', value: 1 },
  { text: '销量优先', value: 2 },
  { text: '评分优先', value: 3 },
  { text: '起送价最低', value: 4 },
  { text: '配送费最低', value: 5 },
]

const getCatIcon = (id) => {
  const icons = { 1: 'shop-o', 2: 'cake-o', 3: 'bag-o', 4: 'flower-o' }
  return icons[id] || 'apps-o'
}

// --- 二、状态与请求 ---
const queryParams = reactive({
  page: 1,
  pageSize: 10,
  name: '',
  categoryId: null,
  sortType: 0,
  longitude: null,
  latitude: null
})

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)

const fetchData = async () => {
  loading.value = true
  
  // 同步当前位置
  queryParams.longitude = locationStore.longitude
  queryParams.latitude = locationStore.latitude

  try {
    const res = await getMerchantPage(queryParams)
    if (res.code === 1) {
      const records = res.data.records || []
      if (queryParams.page === 1) {
        list.value = records
      } else {
        list.value.push(...records)
      }
      
      if (records.length < queryParams.pageSize) {
        finished.value = true
      } else {
        queryParams.page++
      }
    } else {
      finished.value = true
    }
  } catch (e) {
    finished.value = true
    showToast('网络请求失败')
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const onRefresh = () => {
  queryParams.page = 1
  finished.value = false
  fetchData()
}

const onLoad = () => {
  if (!loading.value && !finished.value) fetchData()
}

const onSortChange = () => onRefresh()

const onCategoryClick = (id) => {
  // 分类点击切换逻辑
  queryParams.categoryId = queryParams.categoryId === id ? null : id
  onRefresh()
}

const onNearbyClick = () => {
  if (!locationStore.longitude || !locationStore.latitude) {
    showToast('请先开启定位权限')
    queryParams.sortType = 0
  } else {
    queryParams.sortType = 1
  }
  onRefresh()
}

const onMerchantClick = (item) => {
  if (item.status === 3) return showToast('该商家已休息')
  router.push(`/merchant/${item.id}`)
}

onMounted(() => {
  onRefresh()
})
</script>

<style scoped>
.home-page { background-color: #f5f5f5; min-height: 100vh; }
.header-sticky { position: sticky; top: 0; z-index: 100; box-shadow: 0 2px 10px rgba(0,0,0,0.02); }

.category-grid {
  display: grid; grid-template-columns: repeat(4, 1fr);
  padding: 20px 12px; background: #fff; margin-bottom: 8px;
  border-radius: 0 0 16px 16px;
}
.cat-item { display: flex; flex-direction: column; align-items: center; gap: 8px; }
.cat-icon { 
  width: 52px; height: 52px; background: #f7f7f7; 
  border-radius: 18px; display: flex; align-items: center; justify-content: center;
  transition: all 0.2s;
}
.cat-item.active .cat-icon { background: #FFD100; }
.cat-item.active .cat-text { color: #222; font-weight: 800; }
.cat-text { font-size: 13px; color: #666; }

.filter-bar {
  display: flex; align-items: center; background: #fff;
  border-bottom: 1px solid #f2f2f2; padding-right: 16px;
  position: relative; z-index: 110;
}
:deep(.van-dropdown-menu__bar) { box-shadow: none; height: 44px; flex: 1; }
.nearby-btn { font-size: 14px; color: #666; padding: 0 12px; position: relative; }
.nearby-btn::before { content: ''; position: absolute; left: 0; top: 12px; bottom: 12px; width: 1px; background: #eee; }
.nearby-btn.active { color: #222; font-weight: 800; }

.list-container { padding: 12px; }
.skeleton-box { background: #fff; border-radius: 12px; padding: 12px; margin-bottom: 12px; }
</style>
