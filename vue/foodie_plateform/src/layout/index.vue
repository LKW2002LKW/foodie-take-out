<template>
  <div class="common-layout">
    <el-container class="layout-container">
      <el-aside width="220px" class="aside">
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
          <el-menu-item index="/dashboard">
            <el-icon><Odometer /></el-icon>
            <span>数据统计</span>
          </el-menu-item>
          <el-menu-item index="/merchant">
            <el-icon><Shop /></el-icon>
            <span>商户管理</span>
          </el-menu-item>
          <el-menu-item index="/order">
            <el-icon><List /></el-icon>
            <span>订单监控</span>
          </el-menu-item>
          <el-menu-item index="/user">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/finance">
            <el-icon><Wallet /></el-icon>
            <span>财务管理</span>
          </el-menu-item>
          <el-menu-item index="/review">
            <el-icon><ChatDotRound /></el-icon>
            <span>评价管理</span>
          </el-menu-item>
          <el-menu-item index="/report">
            <el-icon><TrendCharts /></el-icon>
            <span>数据报表</span>
          </el-menu-item>
           <el-menu-item index="/notice">
            <el-icon><Bell /></el-icon>
            <span>公告管理</span>
          </el-menu-item>
           <el-menu-item index="/config">
            <el-icon><Setting /></el-icon>
            <span>系统配置</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header class="header">
          <div class="header-left">
            <!-- Breadcrumb or Collapser could go here -->
            <h2>{{ currentRouteName }}</h2>
          </div>
          <div class="header-right">
            <el-dropdown>
              <span class="el-dropdown-link">
                {{ userInfo.name || 'Admin' }}
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        <el-main class="main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => route.path)
const currentRouteName = computed(() => route.meta.title || '')

const userInfo = JSON.parse(localStorage.getItem('user_info') || '{}')

const logout = () => {
  localStorage.removeItem('platform_token')
  localStorage.removeItem('user_info')
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.aside {
  background-color: #304156;
  color: #fff;
  transition: width 0.3s;
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
}
.header {
  background-color: #fff;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}
.header-right {
  cursor: pointer;
}
.main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
