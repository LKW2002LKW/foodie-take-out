<template>
  <aside class="aside">
    <div class="logo">
      <span>吃货联盟管理平台</span>
    </div>
    <el-menu
      :default-active="activeMenu"
      class="el-menu-vertical"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409EFF"
      router
    >
      <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
        <el-icon><component :is="item.icon" /></el-icon>
        <span>{{ item.title }}</span>
      </el-menu-item>
    </el-menu>
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  Bell,
  ChatDotRound,
  List,
  Odometer,
  Setting,
  Shop,
  TrendCharts,
  User,
  Wallet,
} from '@element-plus/icons-vue'

const route = useRoute()

const menuItems = [
  { path: '/dashboard', title: '数据统计', icon: Odometer },
  { path: '/merchant', title: '商户管理', icon: Shop },
  { path: '/order', title: '订单监控', icon: List },
  { path: '/user', title: '用户管理', icon: User },
  { path: '/finance', title: '财务管理', icon: Wallet },
  { path: '/review', title: '评价管理', icon: ChatDotRound },
  { path: '/report', title: '数据报表', icon: TrendCharts },
  { path: '/notice', title: '公告管理', icon: Bell },
  { path: '/config', title: '系统配置', icon: Setting },
]

const activeMenu = computed(() => route.path)
</script>

<style scoped>
.aside {
  background-color: #304156;
  color: #fff;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 18px;
  font-weight: bold;
  color: #fff;
  background-color: #2b2f3a;
}
.el-menu-vertical {
  border-right: none;
  min-height: calc(100vh - 60px);
}
</style>
