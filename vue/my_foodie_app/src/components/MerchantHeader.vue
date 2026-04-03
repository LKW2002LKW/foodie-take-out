<template>
  <div class="mt-merchant-header" :style="headerStyle">
    <div class="mt-header-content">
      <div class="mt-header-top">
        <van-image :src="merchantInfo?.logo || merchantInfo?.image" width="64" height="64" radius="8" class="mt-logo-img" />
        <div class="mt-header-text">
          <h1 class="mt-shop-name">{{ merchantInfo?.name || merchantInfo?.merchantName }}</h1>
          <div class="mt-shop-meta">
            <span class="mt-rating"><van-icon name="star" color="#FFD000" /> {{ merchantInfo?.rating || '4.8' }}</span>
            <span class="mt-delivery-time">配送约30分钟</span>
          </div>
        </div>
      </div>
      <div class="mt-shop-notice van-ellipsis">公告：{{ merchantInfo?.description || '欢迎光临！' }}</div>
      <div class="mt-shop-tags">
        <van-tag plain color="#FFD000" text-color="#333" class="mt-tag">满减优惠</van-tag>
        <van-tag plain color="#FFD000" text-color="#333" class="mt-tag">品质好店</van-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps({ merchantInfo: Object })
const headerStyle = computed(() => ({
  backgroundImage: `linear-gradient(rgba(0,0,0,0.4), rgba(0,0,0,0.4)), url(${props.merchantInfo?.image || props.merchantInfo?.logo})`,
  backgroundSize: 'cover', backgroundPosition: 'center'
}))
</script>

<style scoped>
.mt-merchant-header { height: 160px; padding: 60px 16px 16px; box-sizing: border-box; color: #fff; display: flex; flex-direction: column; justify-content: flex-end; }
.mt-header-top { display: flex; align-items: center; margin-bottom: 12px; }
.mt-logo-img { border: 2px solid rgba(255,255,255,0.8); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.mt-header-text { margin-left: 12px; }
.mt-shop-name { font-size: 20px; font-weight: 800; margin: 0 0 4px; text-shadow: 0 2px 4px rgba(0,0,0,0.3); }
.mt-shop-meta { display: flex; align-items: center; font-size: 12px; }
.mt-rating { margin-right: 12px; display: flex; align-items: center; gap: 2px; }
.mt-shop-notice { font-size: 11px; opacity: 0.9; margin-bottom: 8px; }
.mt-shop-tags { display: flex; gap: 8px; }
.mt-tag { background-color: rgba(255,255,255,0.9) !important; border: none !important; font-weight: 600; }
</style>
