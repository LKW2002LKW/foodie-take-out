<template>
  <el-menu
    :default-active="$route.path"
    class="el-menu-vertical-demo"
    :collapse="isCollapse"
    background-color="#304156"
    text-color="#bfcbd9"
    active-text-color="#409EFF"
    router
  >
    <el-menu-item index="/">
      <el-icon><Odometer /></el-icon>
      <template #title>仪表盘</template>
    </el-menu-item>

    <el-sub-menu index="/product">
      <template #title>
        <el-icon><Goods /></el-icon>
        <span>商品管理</span>
      </template>
      <el-menu-item index="/product/list">商品列表</el-menu-item>
      <el-menu-item index="/product/category">商品分类</el-menu-item>
      <el-menu-item index="/product/setmeal">套餐管理</el-menu-item>
      <!-- <el-menu-item index="/product/inventory">商品库存</el-menu-item>
      <el-menu-item index="/product/audit">商品审核</el-menu-item> -->
    </el-sub-menu>

    <el-sub-menu index="/order">
      <template #title>
        <el-icon><List /></el-icon>
        <span>订单管理</span>
      </template>
      <el-menu-item index="/order/list">订单列表</el-menu-item>
      <el-menu-item index="/order/statistics">订单统计</el-menu-item>
     <!--  <el-menu-item index="/order/aftersales">售后服务</el-menu-item> -->
    </el-sub-menu>

    <el-sub-menu index="/finance">
      <template #title>
        <el-icon><Money /></el-icon>
        <span>财务管理</span>
      </template>
      <el-menu-item index="/finance/settlement">结算记录</el-menu-item>
      <el-menu-item index="/finance/income">收入明细</el-menu-item>
      <!-- <el-menu-item index="/finance/recharge">充值记录</el-menu-item>
      <el-menu-item index="/finance/config">结算配置</el-menu-item> -->
    </el-sub-menu>

    <el-sub-menu index="/merchant">
      <template #title>
        <el-icon><Shop /></el-icon>
        <span>商户管理</span>
      </template>
      <el-menu-item index="/merchant/profile">商户资料</el-menu-item>
      <el-menu-item index="/merchant/staff">商户员工</el-menu-item>
    </el-sub-menu>

    <el-sub-menu index="/review">
      <template #title>
        <el-icon><ChatDotSquare /></el-icon>
        <span>评价管理</span>
      </template>
      <el-menu-item index="/review/customer">顾客评价</el-menu-item>
      <!-- <el-menu-item index="/review/reply">评价回复</el-menu-item> -->
    </el-sub-menu>

    <el-sub-menu index="/statistics">
      <template #title>
        <el-icon><TrendCharts /></el-icon>
        <span>统计报表</span>
      </template>
      <el-menu-item index="/statistics/sales">销售报表</el-menu-item>
      <el-menu-item index="/statistics/user">用户分析</el-menu-item>
    </el-sub-menu>
  </el-menu>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import {
  Odometer,
  Goods,
  List,
  Money,
  Shop,
  ChatDotSquare,
  TrendCharts
} from '@element-plus/icons-vue';

const route = useRoute();
defineProps({
  isCollapse: Boolean
});
</script>

<style scoped>
.el-menu-vertical-demo:not(.el-menu--collapse) {
  width: 200px;
  min-height: 400px;
}
.el-menu {
  border-right: none;
  height: 100%;
}
</style>
