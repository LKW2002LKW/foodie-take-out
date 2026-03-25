<template>
  <div class="header-container">
    <div class="left-panel">
      <!-- Collapse Button -->
      <el-icon class="collapse-btn" @click="$emit('toggle-collapse')">
        <Expand v-if="isCollapse" />
        <Fold v-else />
      </el-icon>
      <!-- Breadcrumb could go here -->
      <span class="merchant-name">{{ merchantStore.merchantInfo.name }}</span>
    </div>
    <div class="right-panel">
      <el-tooltip content="订单管理" placement="bottom">
        <el-button icon="List" circle @click="$router.push('/order/list')" />
      </el-tooltip>
      <el-tooltip content="财务结算" placement="bottom">
        <el-button icon="Money" circle @click="$router.push('/finance/settlement')" />
      </el-tooltip>
      
      <el-dropdown trigger="click" @command="handleCommand">
        <span class="el-dropdown-link">
          <el-avatar :size="32" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
          <span class="username">Admin</span>
          <el-icon class="el-icon--right"><arrow-down /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">商户信息</el-dropdown-item>
            <el-dropdown-item command="staff">员工管理</el-dropdown-item>
            <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { Fold, Expand, ArrowDown, List, Money } from '@element-plus/icons-vue';
import { useMerchantStore } from '../stores/merchant';
import { useRouter } from 'vue-router';

const merchantStore = useMerchantStore();
const router = useRouter();

defineProps({
  isCollapse: Boolean
});

defineEmits(['toggle-collapse']);

const handleCommand = (command) => {
  if (command === 'logout') {
    merchantStore.logout();
    router.push('/login');
  } else if (command === 'profile') {
    router.push('/merchant/profile');
  } else if (command === 'staff') {
    router.push('/merchant/staff');
  }
};
</script>

<style scoped>
.header-container {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
}
.left-panel {
  display: flex;
  align-items: center;
}
.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  margin-right: 20px;
}
.merchant-name {
  font-weight: bold;
  font-size: 16px;
}
.right-panel {
  display: flex;
  align-items: center;
  gap: 15px;
}
.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
}
.username {
  margin-left: 8px;
  margin-right: 4px;
}
</style>
