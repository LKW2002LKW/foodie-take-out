<template>
  <!-- 顶部栏只负责展示当前页面标题和管理员退出入口。 -->
  <header class="header">
    <div class="header__left">
      <h2 class="header__title">{{ currentRouteTitle }}</h2>
    </div>
    <div class="header__right">
      <el-dropdown>
        <span class="header__dropdown-link">
          {{ platformStore.userInfo.name || platformStore.userInfo.username || 'Admin' }}
          <el-icon class="el-icon--right"><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown } from '@element-plus/icons-vue'
import { routerConfig } from '@/config/router'
import { usePlatformStore } from '@/stores/modules/platformStore'

const route = useRoute()
const router = useRouter()
const platformStore = usePlatformStore()

const currentRouteTitle = computed(() => route.meta.title || '')

const logout = () => {
  platformStore.clearAuth()
  router.push(routerConfig.loginPath)
}
</script>

<style scoped>
.header {
  background-color: #fff;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 100%;
}
.header__title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
}
.header__dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
}
</style>
