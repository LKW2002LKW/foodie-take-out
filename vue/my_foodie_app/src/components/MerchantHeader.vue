<template>
  <div class="mt-merchant-header" :style="headerStyle">
    <div class="mt-header-content">
      <div class="mt-header-top">
        <van-image :src="merchantInfo?.logo || merchantInfo?.image" width="6.4rem" height="6.4rem" radius="0.8rem" class="mt-logo-img" />
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
.mt-merchant-header { height: 16rem; padding: 6rem 1.6rem 1.6rem; box-sizing: border-box; color: #fff; display: flex; flex-direction: column; justify-content: flex-end; }
.mt-header-top { display: flex; align-items: center; margin-bottom: 1.2rem; }
.mt-logo-img { border: 0.2rem solid rgba(255,255,255,0.8); box-shadow: 0 0.4rem 1.2rem rgba(0,0,0,0.1); }
.mt-header-text { margin-left: 1.2rem; }
.mt-shop-name { font-size: 2rem; font-weight: 800; margin: 0 0 0.4rem; text-shadow: 0 0.2rem 0.4rem rgba(0,0,0,0.3); }
.mt-shop-meta { display: flex; align-items: center; font-size: 1.2rem; }
.mt-rating { margin-right: 1.2rem; display: flex; align-items: center; gap: 0.2rem; }
.mt-shop-notice { font-size: 1.1rem; opacity: 0.9; margin-bottom: 0.8rem; }
.mt-shop-tags { display: flex; gap: 0.8rem; }
.mt-tag { background-color: rgba(255,255,255,0.9) !important; border: none !important; font-weight: 600; }
</style>
