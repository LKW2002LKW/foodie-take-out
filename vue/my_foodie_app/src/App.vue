<script setup>
import { useRoute } from 'vue-router';
const route = useRoute();
</script>

<template>
  <div class="app-container">
    <router-view />
    <van-tabbar v-if="route.meta.showTabbar" route>
      <van-tabbar-item replace to="/merchant/list" icon="shop-o">首页</van-tabbar-item>
      <van-tabbar-item replace to="/order/list" icon="orders-o">订单</van-tabbar-item>
      <van-tabbar-item replace to="/profile" icon="user-o">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<style>
/* Global styles if needed */
body {
  margin: 0;
  padding: 0;
  background-color: #f7f8fa;
}
.app-container {
  padding-bottom: 50px; /* Space for tabbar */
}
</style>
