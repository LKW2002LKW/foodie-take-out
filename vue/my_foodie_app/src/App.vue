<script setup>
import { useRoute } from 'vue-router'
const route = useRoute()
</script>

<template>
  <div class="app-container" :class="{ 'has-tabbar': route.meta.showTabbar }">
    <router-view v-slot="{ Component }">
      <transition name="mt-fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>

    <!-- Meituan Style Tabbar -->
    <van-tabbar 
      v-if="route.meta.showTabbar" 
      route 
      active-color="#222" 
      inactive-color="#999"
      class="mt-tabbar"
    >
      <van-tabbar-item replace to="/merchant/list">
        <template #icon="props">
          <van-icon :name="props.active ? 'shop' : 'shop-o'" />
        </template>
        首页
      </van-tabbar-item>
      <van-tabbar-item replace to="/cart">
        <template #icon="props">
          <van-icon :name="props.active ? 'shopping-cart' : 'shopping-cart-o'" />
        </template>
        购物车
      </van-tabbar-item>
      <van-tabbar-item replace to="/order/list">
        <template #icon="props">
          <van-icon :name="props.active ? 'orders' : 'orders-o'" />
        </template>
        订单
      </van-tabbar-item>
      <van-tabbar-item replace to="/profile">
        <template #icon="props">
          <van-icon :name="props.active ? 'manager' : 'user-o'" />
        </template>
        我的
      </van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<style>
.app-container { min-height: 100vh; display: flex; flex-direction: column; }
.has-tabbar { padding-bottom: 50px; }
.mt-tabbar { box-shadow: 0 -2px 10px rgba(0,0,0,0.05); }
:deep(.van-tabbar-item--active) { font-weight: 800; }

.mt-fade-enter-active, .mt-fade-leave-active { transition: opacity 0.2s ease; }
.mt-fade-enter-from, .mt-fade-leave-to { opacity: 0; }
</style>
