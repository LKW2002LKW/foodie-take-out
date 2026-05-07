<template>
  <!-- 平台端主布局负责装配侧边导航、顶部栏与页面内容区。 -->
  <div class="common-layout">
    <el-container class="layout-container">
      <el-aside width="220px">
        <SidebarNav />
      </el-aside>
      <el-container>
        <el-header class="layout-header">
          <HeaderBar />
        </el-header>
        <el-main class="main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import HeaderBar from '@/layouts/components/HeaderBar.vue'
import SidebarNav from '@/layouts/components/SidebarNav.vue'
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.layout-header {
  padding: 0;
}
.main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
