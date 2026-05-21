<template>
  <div class="platform-dashboard">
    <!-- 工作台页面只负责统计与图表渲染，数据刷新逻辑下沉到 useDashboardPage。 -->
    <header class="platform-dashboard__header">
      <div class="platform-dashboard__welcome">
        <h1 class="platform-dashboard__title">系统概览</h1>
        <p class="platform-dashboard__subtitle">实时监控全平台运营状况与关键指标</p>
      </div>
      <el-button type="primary" icon="Refresh" @click="handleRefresh">刷新实时数据</el-button>
    </header>

    <section class="platform-dashboard__stats" v-loading="loading">
      <el-row :gutter="24">
        <el-col v-for="item in coreStats" :key="item.label" :xs="24" :sm="12" :lg="6">
          <div :class="['platform-dashboard__card', `platform-dashboard__card--${item.type}`]">
            <div class="platform-dashboard__card-body">
              <span class="platform-dashboard__card-label">{{ item.label }}</span>
              <div class="platform-dashboard__card-value">
                <span v-if="item.unit" class="platform-dashboard__card-unit">{{ item.unit }}</span>
                {{ item.value }}
              </div>
            </div>
            <el-icon class="platform-dashboard__card-icon">
              <component :is="item.icon" />
            </el-icon>
          </div>
        </el-col>
      </el-row>
    </section>

    <div class="platform-dashboard__overview">
      <div v-for="item in detailedStats" :key="item.label" class="platform-dashboard__overview-item">
        <span class="platform-dashboard__overview-label">{{ item.label }}</span>
        <span class="platform-dashboard__overview-value">{{ item.value }}</span>
      </div>
    </div>

    <el-card shadow="never" class="platform-dashboard__chart-card">
      <template #header>
        <div class="platform-dashboard__chart-header">
          <h3 class="platform-dashboard__chart-title">全平台经营趋势</h3>
          <el-radio-group v-model="days" size="small" @change="fetchTrend(days)">
            <el-radio-button :label="7">近 7 天</el-radio-button>
            <el-radio-button :label="30">近 30 天</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div ref="chartRef" class="platform-dashboard__chart-box"></div>
    </el-card>
  </div>
</template>

<script setup>
import { useDashboardPage } from '@/composables/business/useDashboardPage'

const {
  chartRef,
  coreStats,
  days,
  detailedStats,
  fetchTrend,
  handleRefresh,
  loading,
} = useDashboardPage()
</script>

<style scoped>
.platform-dashboard {
  padding: 24px;
  background-color: #f0f4f8;
  min-height: 100%;
}
.platform-dashboard__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}
.platform-dashboard__title {
  font-size: 26px;
  font-weight: 700;
  color: #1a202c;
  margin: 0 0 4px;
}
.platform-dashboard__subtitle {
  font-size: 14px;
  color: #718096;
  margin: 0;
}
.platform-dashboard__stats {
  margin-bottom: 24px;
}
.platform-dashboard__card {
  position: relative;
  padding: 24px;
  border-radius: 12px;
  color: #fff;
  margin-bottom: 16px;
  overflow: hidden;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}
.platform-dashboard__card--primary { background: linear-gradient(135deg, #3182ce 0%, #2b6cb0 100%); }
.platform-dashboard__card--success { background: linear-gradient(135deg, #38a169 0%, #2f855a 100%); }
.platform-dashboard__card--info { background: linear-gradient(135deg, #4299e1 0%, #3182ce 100%); }
.platform-dashboard__card--warning { background: linear-gradient(135deg, #ed8936 0%, #dd6b20 100%); }
.platform-dashboard__card-body {
  position: relative;
  z-index: 2;
}
.platform-dashboard__card-label {
  font-size: 14px;
  opacity: 0.9;
  font-weight: 500;
}
.platform-dashboard__card-value {
  font-size: 32px;
  font-weight: 700;
  margin-top: 8px;
}
.platform-dashboard__card-unit {
  font-size: 18px;
  margin-right: 2px;
}
.platform-dashboard__card-icon {
  position: absolute;
  right: -10px;
  bottom: -10px;
  font-size: 96px;
  opacity: 0.15;
  transform: rotate(15deg);
}
.platform-dashboard__overview {
  display: flex;
  background: #fff;
  padding: 20px 32px;
  border-radius: 12px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}
.platform-dashboard__overview-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  border-right: 1px solid #edf2f7;
  padding: 0 24px;
}
.platform-dashboard__overview-item:last-child {
  border-right: none;
}
.platform-dashboard__overview-label {
  font-size: 13px;
  color: #718096;
}
.platform-dashboard__overview-value {
  font-size: 20px;
  font-weight: 600;
  color: #2d3748;
}
.platform-dashboard__chart-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}
.platform-dashboard__chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.platform-dashboard__chart-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
}
.platform-dashboard__chart-box {
  height: 450px;
  width: 100%;
}
</style>
