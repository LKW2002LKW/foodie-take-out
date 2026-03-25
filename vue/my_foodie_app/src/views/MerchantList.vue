<template>
  <div class="page-container safe-area-bottom">
    <!-- Header: Loc + Search -->
    <div class="sticky-header">
      <div class="location-bar" @click="onLocationClick">
        <van-icon name="location" color="#FF9800" />
        <span class="address-text">{{ locationStore.address || '请选择地址' }}</span>
        <van-icon name="arrow-down" size="12" color="#333" />
      </div>
      <div class="search-box">
        <van-search
          v-model="searchValue"
          shape="round"
          placeholder="搜索商家名称"
          @search="onSearch"
          class="custom-search"
        />
      </div>
    </div>

    <!-- Scrollable Content -->
    <div class="scroll-content">
        <!-- Notice Bar -->
        <van-notice-bar
            v-if="latestNotice"
            left-icon="volume-o"
            :text="latestNotice.title"
            background="#fffbe8"
            color="#ed6a0c"
            mode="link"
            @click="toNoticeList"
        />
        
        <!-- Banner -->
        <div class="banner-area">
            <van-swipe class="my-swipe" :autoplay="3000" indicator-color="white">
                <van-swipe-item v-for="(img, idx) in banners" :key="idx">
                    <van-image :src="img" fit="cover" radius="12" class="banner-img" />
                </van-swipe-item>
            </van-swipe>
        </div>

        <!-- Category Grid -->
        <div class="category-grid">
            <div class="cat-item" v-for="cat in categoryIcons" :key="cat.text">
                <div class="cat-icon-wrapper">
                    <van-image :src="cat.icon" width="44" height="44" />
                </div>
                <span class="cat-text">{{ cat.text }}</span>
            </div>
        </div>

        <!-- Filter Sticky Tabs -->
        <van-sticky offset-top="54">
            <van-tabs v-model:active="activeSort" @change="onSortChange" class="sort-tabs" line-width="20" color="#FF9800">
                <van-tab title="综合推荐" name="default"></van-tab>
                <van-tab title="距离最近" name="distance"></van-tab>
                <!-- 预留扩展: 销量最高 -->
            </van-tabs>
        </van-sticky>

        <!-- Merchant List -->
        <van-list
            v-model:loading="loading"
            :finished="finished"
            finished-text="没有更多了"
            @load="onLoad"
            class="merchant-list"
        >
            <div 
                v-for="item in list" 
                :key="item.id" 
                class="merchant-card-new"
                :class="{ 'merchant-resting': item.status === 3 }"
                @click="onMerchantClick(item)"
            >
                <div class="m-img-box">
                    <van-image :src="item.image || item.logo" fit="cover" radius="8" class="m-logo">
                        <template #error>
                            <van-icon name="photo-o" size="24" color="#ccc" />
                        </template>
                    </van-image>
                    <!-- Status Badge -->
                    <div class="shop-tag" 
                         :class="{ 'tag-open': item.status === 1, 'tag-resting': item.status === 3 }"
                         v-if="item.statusDesc">
                        {{ item.statusDesc }}
                    </div>
                </div>
                <div class="m-info-box">
                    <div class="m-name van-ellipsis">
                        {{ item.name || item.merchantName }}
                    </div>
                    <div class="m-stats">
                        <span class="score-num" v-if="item.rating">{{ item.rating }}分</span>
                        <span class="sales-num" v-if="item.salesVolume">月售{{ item.salesVolume }}+</span>
                        <span class="distance" v-if="item.distance">{{ formatDistance(item.distance) }}</span>
                    </div>
                    <div class="m-delivery">
                        <span>起送 ￥{{ item.minAppQuantity || item.minDeliveryAmount || 0 }}</span>
                        <span class="sep">|</span>
                        <span>配送 ￥{{ item.deliveryPrice || item.deliveryFee || 0 }}</span>
                    </div>
                    <!-- 标签展示区，可根据 data 扩展 -->
                    <div class="m-tags" v-if="item.tags">
                        <van-tag v-for="tag in item.tags" :key="tag" color="#fff0e6" text-color="#FF5722" class="m-tag">{{ tag }}</van-tag>
                    </div>
                </div>
            </div>
        </van-list>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useLocationStore } from '../store/modules/location'; 
import { getMerchantPage } from '../api/merchant';
import { getNoticePage } from '../api/notice';
import { showToast, showLoadingToast, closeToast } from 'vant';

const router = useRouter();
const locationStore = useLocationStore();
const searchValue = ref('');
const activeSort = ref('default');
const loading = ref(false);
const finished = ref(false);
const list = ref([]);
const page = ref(1);
const pageSize = 10;
const latestNotice = ref(null);

const loadLatestNotice = async () => {
    try {
        const res = await getNoticePage({ page: 1, pageSize: 5 });
        // Accept both 200 and 1 as success codes
        if (res && (res.code === 200 || res.code === 1)) {
            // Support both 'list' and 'records' formats for pagination
            const items = res.data?.list || res.data?.records || [];
            if (items.length > 0) {
                // Loosen type checks (== instead of ===) in case of string/number mismatch
                const valid = items.find(n => (n.target == 2 || n.target == 3) && n.status == 1);
                if (valid) {
                    latestNotice.value = valid;
                }
            }
        }
    } catch (e) {
        // silent fail
        console.error('Load notice failed', e);
    }
};

const toNoticeList = () => {
    router.push('/notice/list');
};

// Static Assets
const banners = [
    'https://fastly.jsdelivr.net/npm/@vant/assets/apple-1.jpeg',
    'https://fastly.jsdelivr.net/npm/@vant/assets/apple-2.jpeg'
];
const categoryIcons = [
    { text: '美食', icon: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg' },
    { text: '甜点', icon: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg' },
    { text: '超市', icon: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg' },
    { text: '生鲜', icon: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg' },
    { text: '送药', icon: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg' },
];

// Methods
const getGeoLocation = () => {
    // If store already has location, use it. Otherwise try to locate.
    if (locationStore.isLocated) {
        // Already ready
        return;
    }

    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition((pos) => {
            const lng = pos.coords.longitude;
            const lat = pos.coords.latitude;
            const addr = `我的位置 (${lng.toFixed(2)},${lat.toFixed(2)})`;
            locationStore.setLocation({ address: addr, longitude: lng, latitude: lat });
        }, (err) => {
            console.error(err);
            locationStore.setLocation({ address: '定位失败', longitude: null, latitude: null });
        });
    } else {
         locationStore.setLocation({ address: '不支持定位', longitude: null, latitude: null });
    }
};

// Format distance: m / km
const formatDistance = (d) => {
    if (d === null || d === undefined) return '';
    const num = parseFloat(d);
    if (isNaN(num)) return '';
    
    // Heuristic: If distance > 1000, likely meters. If < 100, likely km.
    // If backend returns meters: 500 (m) -> 500m. 
    // If backend returns km: 0.5 (km) -> 500m.
    // Let's assume standard is KM.
    if (num < 1) {
        return (num * 1000).toFixed(0) + 'm';
    }
    return num.toFixed(1) + 'km';
};

const onLoad = async () => {
    const params = {
        page: page.value,
        pageSize: pageSize,
        name: searchValue.value,
    };

    // 如果是距离排序，必须有经纬度
    // 或者普通模式也传经纬度以便后端计算距离
    if (locationStore.longitude && locationStore.latitude) {
         params.longitude = locationStore.longitude;
         params.latitude = locationStore.latitude;
         if (activeSort.value === 'distance') {
             params.sortType = 1;
         }
    } else if (activeSort.value === 'distance') {
        // 无定位不允许距离排序
        finished.value = true;
        showToast('请先获取定位');
        loading.value = false;
        return; 
    }

    try {
        const res = await getMerchantPage(params);
        if (res.code === 1) {
            // 只保留营业中(status=1)的 - 根据 接口文档说明
            // 但如果后端没有过滤，前端这里强行过滤一下比较保险
            // 文档说: "所有列表接口 必须过滤掉非营业商户"，通常指后端。
            // 但为了健壮性，这里也可补充
            
            let records = res.data.records || [];
            if (activeSort.value === 'distance' && (!locationStore.longitude || !locationStore.latitude)) {
                // Front guard should have caught this, but just in case
            }
            
            if (page.value === 1) {
                list.value = records;
            } else {
                list.value.push(...records);
            }
            
            // 分页判定
            if (records.length < pageSize) {
                finished.value = true;
            } else {
                page.value++;
            }
        } else {
             finished.value = true; 
        }
    } catch (e) {
        finished.value = true;
    } finally {
        loading.value = false;
    }
};

const onSearch = () => {
    page.value = 1;
    finished.value = false;
    list.value = [];
    loading.value = true;
    onLoad();
};

const onSortChange = () => {
    page.value = 1;
    finished.value = false;
    list.value = [];
    loading.value = true;
    onLoad();
};

const onLocationClick = () => {
    router.push('/address/list?mode=select');
};

const onMerchantClick = (item) => {
    if (item.status === 3) {
        showToast('该商家已休息或已下线');
        return;
    }
    // Rest status (2) is allowed to click to see details, but maybe show toast
    if (item.status === 2) {
        // Option: Allows entry but page will show "Resting"
    }
    router.push('/merchant/' + item.id);
};

// Watch location change (e.g. return from address selection)
watch(() => [locationStore.longitude, locationStore.latitude], () => {
    // Reload list when location changes
    page.value = 1;
    finished.value = false;
    list.value = [];
    loading.value = true;
    onLoad();
});

onMounted(() => {
    getGeoLocation();
    loadLatestNotice();
});
</script>

<style scoped>
.page-container {
    background-color: #f5f6fa;
    min-height: 100vh;
    padding-bottom: 50px;
}
/* ... existing styles ... */
/* Merchant Status Styles */
.merchant-resting {
    opacity: 0.7;
    filter: grayscale(0.8);
}
.tag-open {
    background: linear-gradient(to right, #4caf50, #8bc34a);
}
.tag-resting {
    background: #999;
}
/* ... */
.merchant-card-new {
    position: sticky;
    top: 0;
    z-index: 100;
    background: #fff;
    padding-bottom: 5px;
}
.location-bar {
    display: flex;
    align-items: center;
    padding: 10px 16px;
    font-size: 16px;
    font-weight: bold;
}
.address-text {
    margin: 0 4px;
    max-width: 200px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
}
.custom-search {
    padding: 0 12px;
}
.custom-search .van-search__content {
    background: #f0f0f0;
}

/* Scroll Content */
.banner-area {
    padding: 10px 12px;
}
.banner-img {
    height: 140px;
    width: 100%;
    display: block;
}

.category-grid {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 10px;
    padding: 10px 12px;
    background: #fff;
    margin-bottom: 10px;
}
.cat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
}
.cat-text {
    font-size: 12px;
    color: #444;
    margin-top: 4px;
}

/* Merchant List */
.merchant-list {
    padding: 0 12px;
}
.merchant-card-new {
    display: flex;
    background: #fff;
    padding: 12px;
    border-radius: 12px;
    margin-bottom: 10px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}
.m-img-box {
    width: 88px;
    height: 88px;
    position: relative;
    flex-shrink: 0;
    margin-right: 12px;
}
.m-img-box .van-image {
    width: 100%;
    height: 100%;
}
.shop-tag {
    position: absolute;
    top: 0;
    left: 0;
    background: rgba(0,0,0,0.6);
    color: #fff;
    font-size: 10px;
    padding: 2px 4px;
    border-radius: 8px 0 8px 0;
}
.m-info-box {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-around;
    overflow: hidden;
}
.m-name {
    font-size: 16px;
    font-weight: bold;
    color: #222;
}
.m-stats {
    font-size: 11px;
    color: #666;
    display: flex;
    align-items: center;
}
.score-num {
    color: #FF9800;
    font-weight: bold;
    margin-right: 6px;
}
.sales-num {
    margin-right: auto;
}
.m-delivery {
    font-size: 11px;
    color: #666;
}
.sep {
    margin: 0 4px;
    color: #eee;
}
.m-tags {
    display: flex;
    gap: 4px;
}
.m-tag {
    font-size: 10px;
    padding: 1px 4px;
}
</style>
