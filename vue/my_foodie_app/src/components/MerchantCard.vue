<template>
  <div class="merchant-card" @click="$emit('click')">
    <div class="card-left">
      <van-image :src="item.logo" width="90" height="90" radius="8" fit="cover">
        <template #error><van-icon name="photo-o" size="32" color="#eee" /></template>
      </van-image>
      <div v-if="item.status !== 1" class="status-overlay">{{ item.statusDesc }}</div>
    </div>
    <div class="card-right">
      <h3 class="m-name van-ellipsis">{{ item.merchantName || item.name }}</h3>
      
      <div class="m-stats">
        <span class="m-rating">
          <van-icon name="star" color="#FFD100" />
          {{ item.rating || '0.0' }}
        </span>
        <span class="m-sales">月售{{ item.salesVolume || 0 }}</span>
        <span class="m-distance">{{ item.distance ? item.distance + 'km' : '--' }}</span>
      </div>

      <div class="m-delivery">
        <span>起送 ￥{{ item.minDeliveryAmount || 0 }}</span>
        <span class="sep">|</span>
        <span>配送 ￥{{ item.deliveryFee || 0 }}</span>
      </div>

      <div class="m-tags">
        <van-tag v-if="item.status === 1" plain color="#FFD100" text-color="#333">准时达</van-tag>
        <van-tag plain color="#f2f2f2" text-color="#999">支持自取</van-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({ item: Object })
defineEmits(['click'])
</script>

<style scoped>
.merchant-card {
  display: flex;
  background: #fff;
  padding: 12px;
  border-radius: 12px;
  margin-bottom: 12px;
  gap: 12px;
  transition: transform 0.1s;
}
.merchant-card:active { transform: scale(0.98); }
.card-left { position: relative; flex-shrink: 0; }
.status-overlay {
  position: absolute; bottom: 0; left: 0; right: 0;
  background: rgba(0,0,0,0.6); color: #fff;
  font-size: 10px; text-align: center; border-radius: 0 0 8px 8px;
}
.card-right { flex: 1; display: flex; flex-direction: column; justify-content: space-between; overflow: hidden; }
.m-name { font-size: 16px; font-weight: 800; margin: 0; color: #222; }
.m-stats { display: flex; align-items: center; font-size: 12px; color: #666; gap: 8px; }
.m-rating { color: #FF9500; font-weight: 800; display: flex; align-items: center; gap: 2px; }
.m-distance { margin-left: auto; color: #999; }
.m-delivery { font-size: 12px; color: #666; }
.m-delivery .sep { margin: 0 4px; color: #eee; }
.m-tags { display: flex; gap: 6px; }
</style>
