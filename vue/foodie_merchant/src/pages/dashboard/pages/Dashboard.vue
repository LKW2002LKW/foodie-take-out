<template>
  <div class="dashboard">
    <!-- 头部欢迎区域 -->
    <header class="dashboard__header">
      <div class="dashboard__welcome">
        <h1 class="dashboard__title">工作台</h1>
        <p class="dashboard__subtitle">欢迎回来，商家端管理中心</p>
      </div>
      <div class="dashboard__timer">
        <el-icon class="dashboard__timer-icon"><Clock /></el-icon>
        <span class="dashboard__timer-text">{{ currentTime }}</span>
      </div>
    </header>

    <!-- 今日经营概况 -->
    <section class="dashboard__section">
      <div class="dashboard__section-header">
        <h2 class="dashboard__section-title">今日经营概况</h2>
        <el-button 
          link 
          type="primary" 
          :loading="store.loading.today"
          @click="store.fetchTodayStats"
        >
          刷新数据
        </el-button>
      </div>
      
      <el-row :gutter="24">
        <el-col v-for="card in statCards" :key="card.title" :xs="24" :sm="12" :lg="6">
          <div :class="['dashboard__card', `dashboard__card--${card.type}`]">
            <div class="dashboard__card-content">
              <span class="dashboard__card-label">{{ card.title }}</span>
              <div class="dashboard__card-value">
                <span v-if="card.unit" class="dashboard__card-unit">{{ card.unit }}</span>
                {{ card.value }}
              </div>
              <div class="dashboard__card-footer">
                {{ card.subTitle }}: {{ card.subUnit }}{{ card.subValue }}
              </div>
            </div>
            <el-icon class="dashboard__card-icon">
              <component :is="card.icon" />
            </el-icon>
          </div>
        </el-col>
      </el-row>
    </section>

    <!-- 累计经营数据 -->
    <section class="dashboard__section">
      <div class="dashboard__section-header">
        <h2 class="dashboard__section-title">数据概览</h2>
      </div>
      <div class="dashboard__overview">
        <div v-for="item in overviewItems" :key="item.label" class="dashboard__overview-item">
          <span class="dashboard__overview-label">{{ item.label }}</span>
          <div class="dashboard__overview-value">
            <template v-if="item.type === 'rate'">
              <el-rate v-model="item.value" disabled show-score text-color="#F97316" />
            </template>
            <template v-else>
              <span v-if="item.unit" class="dashboard__overview-unit">{{ item.unit }}</span>
              {{ item.value }}
            </template>
          </div>
        </div>
      </div>
    </section>

    <!-- 经营趋势图表 -->
    <section class="dashboard__section">
      <el-card shadow="never" class="dashboard__chart-card">
        <template #header>
          <div class="dashboard__chart-header">
            <span class="dashboard__chart-title">经营趋势分析</span>
            <el-radio-group v-model="trendDays" size="small" @change="handleTrendChange">
              <el-radio-button :label="7">近7天</el-radio-button>
              <el-radio-button :label="30">近30天</el-radio-button>
            </el-radio-group>
          </div>
        </template>
        
        <el-row :gutter="24" v-loading="store.loading.trend">
          <el-col :span="12">
            <div ref="revenueChartRef" class="dashboard__chart-box"></div>
          </el-col>
          <el-col :span="12">
            <div ref="orderChartRef" class="dashboard__chart-box"></div>
          </el-col>
        </el-row>
      </el-card>
    </section>
  </div>
</template>

<script setup>
import { useDashboardPage } from '@/composables/business/useDashboardPage'

const {
  currentTime,
  handleTrendChange,
  orderChartRef,
  overviewItems,
  revenueChartRef,
  statCards,
  store,
  timerIcon: Clock,
  trendDays,
} = useDashboardPage()
</script>

<style scoped>
/* 
 * BEM 命名规范: 
 * Block: dashboard
 * Element: dashboard__xxx
 * Modifier: dashboard__xxx--yyy
 */

.dashboard {
  padding: 24px;
  background-color: #f8fafc;
  min-height: 100%;
}

/* Header */
.dashboard__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 32px;
}

.dashboard__title {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 8px;
}

.dashboard__subtitle {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.dashboard__timer {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff;
  padding: 8px 16px;
  border-radius: 99px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  color: #2563eb;
  font-family: monospace;
  font-weight: 600;
}

/* Sections */
.dashboard__section {
  margin-bottom: 32px;
}

.dashboard__section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.dashboard__section-title {
  font-size: 18px;
  font-weight: 600;
  color: #334155;
  margin: 0;
  position: relative;
  padding-left: 12px;
}

.dashboard__section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 18px;
  background-color: #2563eb;
  border-radius: 2px;
}

/* Cards */
.dashboard__card {
  position: relative;
  padding: 24px;
  border-radius: 16px;
  color: #fff;
  overflow: hidden;
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: default;
  margin-bottom: 16px;
}

.dashboard__card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 20px -8px rgba(0,0,0,0.15);
}

.dashboard__card-content {
  position: relative;
  z-index: 1;
}

.dashboard__card-label {
  font-size: 14px;
  font-weight: 500;
  opacity: 0.9;
  display: block;
  margin-bottom: 12px;
}

.dashboard__card-value {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 12px;
}

.dashboard__card-unit {
  font-size: 18px;
  margin-right: 2px;
}

.dashboard__card-footer {
  font-size: 12px;
  opacity: 0.8;
  padding-top: 12px;
  border-top: 1px solid rgba(255,255,255,0.2);
}

.dashboard__card-icon {
  position: absolute;
  right: -12px;
  bottom: -12px;
  font-size: 84px;
  opacity: 0.15;
  transform: rotate(15deg);
}

/* Card Modifiers */
.dashboard__card--primary { background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%); }
.dashboard__card--success { background: linear-gradient(135deg, #059669 0%, #10b981 100%); }
.dashboard__card--warning { background: linear-gradient(135deg, #d97706 0%, #f59e0b 100%); }
.dashboard__card--danger { background: linear-gradient(135deg, #dc2626 0%, #ef4444 100%); }

/* Overview Stats */
.dashboard__overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  background: #fff;
  padding: 32px;
  border-radius: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.02);
}

.dashboard__overview-item {
  text-align: center;
  border-right: 1px solid #f1f5f9;
}

.dashboard__overview-item:last-child {
  border-right: none;
}

.dashboard__overview-label {
  display: block;
  font-size: 14px;
  color: #64748b;
  margin-bottom: 12px;
}

.dashboard__overview-value {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
}

.dashboard__overview-unit {
  font-size: 14px;
  color: #94a3b8;
  margin-right: 2px;
}

/* Charts */
.dashboard__chart-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 1px 3px rgba(0,0,0,0.02);
}

.dashboard__chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dashboard__chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.dashboard__chart-box {
  height: 400px;
  width: 100%;
  margin-top: 16px;
}

@media (max-width: 1024px) {
  .dashboard__overview {
    grid-template-columns: repeat(2, 1fr);
  }
  .dashboard__overview-item:nth-child(2) {
    border-right: none;
  }
}

@media (max-width: 768px) {
  .dashboard__overview {
    grid-template-columns: 1fr;
  }
  .dashboard__overview-item {
    border-right: none;
    border-bottom: 1px solid #f1f5f9;
    padding-bottom: 16px;
  }
  .dashboard__overview-item:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }
}
</style>
