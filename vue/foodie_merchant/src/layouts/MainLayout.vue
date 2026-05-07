<template>
  <!-- 商家端主布局负责装配侧边菜单、顶部栏与路由内容区。 -->
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '200px'" class="aside">
      <SidebarNav :isCollapse="isCollapse" />
    </el-aside>
    <el-container>
      <el-header height="60px" style="padding: 0;">
        <HeaderBar :isCollapse="isCollapse" @toggle-collapse="toggleCollapse" />
      </el-header>
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
      <el-footer height="40px" class="footer">
        © 2026 Foodie Merchant Admin. Version 1.0.0 | <el-link type="primary">帮助与支持</el-link>
      </el-footer>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import HeaderBar from '@/layouts/components/HeaderBar.vue'
import SidebarNav from '@/layouts/components/SidebarNav.vue'

const isCollapse = ref(false)
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.aside {
  background-color: #304156;
  transition: width 0.3s;
  overflow: hidden;
}
.main-content {
  background-color: #f0f2f5;
  padding: 20px;
}
.footer {
  text-align: center;
  line-height: 40px;
  color: #999;
  font-size: 12px;
  background: #fff;
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
