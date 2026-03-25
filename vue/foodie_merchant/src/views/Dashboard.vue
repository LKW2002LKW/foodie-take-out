<template>
  <div class="dashboard-container">
    <div class="welcome-section">
      <h2 class="title">工作台</h2>
      <p class="subtitle">欢迎回来，开始您的一天！</p>
    </div>

    <!-- 今日数据 -->
    <div class="section-header">
      <span class="section-title">今日经营概况</span>
      <span class="update-time">数据更新至 {{ currentTime }}</span>
    </div>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="data-card gradient-1">
          <div class="card-content">
            <div class="card-title">今日营业额</div>
            <div class="card-value">￥{{ Number(todayStats.todayRevenue).toFixed(2) }}</div>
            <div class="card-sub">实收: ￥{{ Number(todayStats.todayIncome).toFixed(2) }}</div>
          </div>
          <el-icon class="card-icon"><Money /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="data-card gradient-2">
           <div class="card-content">
            <div class="card-title">今日订单数</div>
            <div class="card-value">{{ todayStats.totalOrderCount }}</div>
            <div class="card-sub">已取消: {{ todayStats.cancelledOrderCount }}</div>
          </div>
          <el-icon class="card-icon"><List /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="data-card gradient-3">
           <div class="card-content">
            <div class="card-title">待处理订单</div>
            <div class="card-value">{{ todayStats.pendingOrderCount }}</div>
             <div class="card-sub">急需处理</div>
          </div>
          <el-icon class="card-icon"><Bell /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="data-card gradient-4">
           <div class="card-content">
            <div class="card-title">今日完成率</div>
            <div class="card-value">{{ todayStats.completionRate }}%</div>
             <div class="card-sub">
                <el-progress :percentage="Number(todayStats.completionRate)" :show-text="false" :stroke-width="6" color="#fff" />
             </div>
          </div>
          <el-icon class="card-icon"><TrendCharts /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <!-- 历史总览 -->
    <div class="section-header mt-4">
      <span class="section-title">累计经营数据</span>
    </div>
    <el-row :gutter="20">
        <el-col :span="6">
            <div class="stat-item">
                <div class="stat-label">累计营业额</div>
                <div class="stat-num">￥{{ Number(overviewStats.totalRevenue).toFixed(2) }}</div>
            </div>
        </el-col>
        <el-col :span="6">
            <div class="stat-item">
                <div class="stat-label">累计订单</div>
                <div class="stat-num">{{ overviewStats.totalOrders }}</div>
            </div>
        </el-col>
        <el-col :span="6">
            <div class="stat-item">
                <div class="stat-label">平均客单价</div>
                 <div class="stat-num">￥{{ Number(overviewStats.avgOrderAmount).toFixed(2) }}</div>
            </div>
        </el-col>
        <el-col :span="6">
             <div class="stat-item">
                <div class="stat-label">店铺评分 ({{ overviewStats.totalReviews }}评价)</div>
                <div class="stat-num">
                    <el-rate
                        v-model="overviewStats.merchantRating"
                        disabled
                        show-score
                        text-color="#ff9900"
                    />
                </div>
            </div>
        </el-col>
    </el-row>


    <!-- 趋势图表 -->
    <el-card class="chart-container mt-4" v-loading="loadingChart">
        <template #header>
            <div class="chart-header">
                <span class="chart-title">经营趋势分析</span>
                <el-radio-group v-model="trendDays" size="small" @change="fetchTrend">
                    <el-radio-button :label="7">近7天</el-radio-button>
                    <el-radio-button :label="30">近30天</el-radio-button>
                </el-radio-group>
            </div>
        </template>
        
        <el-row :gutter="20">
            <el-col :span="12">
                <div ref="revenueChartRef" class="chart-box"></div>
            </el-col>
            <el-col :span="12">
                 <div ref="orderChartRef" class="chart-box"></div>
            </el-col>
        </el-row>
    </el-card>

  </div>
</template>

<script setup>
import { ref, onMounted, reactive, nextTick, onUnmounted } from 'vue';
import { Money, List, Bell, TrendCharts } from '@element-plus/icons-vue';
import statisticsApi from '@/api/statistics';
import * as echarts from 'echarts';

// State
const currentTime = ref('');
const todayStats = reactive({
    todayRevenue: 0,
    todayIncome: 0,
    totalOrderCount: 0,
    pendingOrderCount: 0,
    cancelledOrderCount: 0,
    completionRate: 0
});

const overviewStats = reactive({
    totalRevenue: 0,
    totalOrders: 0,
    avgOrderAmount: 0,
    merchantRating: 0,
    totalReviews: 0
});

const trendDays = ref(7);
const loadingChart = ref(false);
const revenueChartRef = ref(null);
const orderChartRef = ref(null);
let revenueChart = null;
let orderChart = null;

// Lifecycle
onMounted(() => {
    updateTime();
    fetchToday();
    fetchOverview();
    fetchTrend();
    
    window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
    window.removeEventListener('resize', handleResize);
    if(revenueChart) revenueChart.dispose();
    if(orderChart) orderChart.dispose();
});

// Methods
const updateTime = () => {
    const now = new Date();
    currentTime.value = now.toLocaleTimeString();
    setInterval(() => {
        currentTime.value = new Date().toLocaleTimeString();
    }, 60000); // update every minute
};

const fetchToday = async () => {
    try {
        const res = await statisticsApi.getTodayStatistics();
        if (res.data) {
            Object.assign(todayStats, res.data);
        }
    } catch (e) { console.error(e); }
};

const fetchOverview = async () => {
    try {
        const res = await statisticsApi.getOverviewStatistics();
        if (res.data) {
            Object.assign(overviewStats, res.data);
            // Convert to number for rate component if it's string
            overviewStats.merchantRating = Number(overviewStats.merchantRating); 
        }
    } catch (e) { console.error(e); }
};

const fetchTrend = async () => {
    loadingChart.value = true;
    try {
        const res = await statisticsApi.getTrendStatistics({ days: trendDays.value });
        if (res.data) {
            await nextTick();
            initCharts(res.data);
        }
    } catch (e) { console.error(e); }
    finally {
        loadingChart.value = false;
    }
};

const initCharts = (data) => {
    // 1. Revenue Chart
    if (!revenueChart) {
        revenueChart = echarts.init(revenueChartRef.value);
    }
    
    const revenueOption = {
        title: { text: '营业额趋势', left: 'center' },
        tooltip: { trigger: 'axis' },
        legend: { data: ['营业额', '实收金额'], bottom: 0 },
        grid: { left: '3%', right: '4%', bottom: '10%', containLabel: true },
        xAxis: { type: 'category', boundaryGap: false, data: data.dateList },
        yAxis: { type: 'value' },
        series: [
            {
                name: '营业额',
                type: 'line',
                smooth: true,
                data: data.revenueList,
                itemStyle: { color: '#409EFF' },
                areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{offset: 0, color: '#409EFF88'}, {offset: 1, color: '#409EFF00'}]) }
            },
            {
                name: '实收金额',
                type: 'line',
                smooth: true,
                data: data.incomeList,
                itemStyle: { color: '#67C23A' }
            }
        ]
    };
    revenueChart.setOption(revenueOption);

    // 2. Order Chart
    if (!orderChart) {
        orderChart = echarts.init(orderChartRef.value);
    }

    const orderOption = {
        title: { text: '订单量趋势', left: 'center' },
        tooltip: { trigger: 'axis' },
        grid: { left: '3%', right: '4%', bottom: '10%', containLabel: true },
        xAxis: { type: 'category', data: data.dateList },
        yAxis: { type: 'value' },
        series: [
            {
                name: '订单数',
                type: 'bar',
                data: data.orderCountList,
                itemStyle: { color: '#E6A23C', borderRadius: [5, 5, 0, 0] },
                barWidth: '40%'
            }
        ]
    };
    orderChart.setOption(orderOption);
};

const handleResize = () => {
    if (revenueChart) revenueChart.resize();
    if (orderChart) orderChart.resize();
};

</script>

<style scoped>
.dashboard-container {
    padding: 0;
}
.welcome-section {
    margin-bottom: 25px;
}
.title {
    font-size: 26px;
    font-weight: 700;
    margin: 0 0 5px;
    color: #303133;
}
.subtitle {
    color: #909399;
    margin: 0;
}

.section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
    border-left: 5px solid #409EFF;
    padding-left: 10px;
}
.section-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
}
.update-time {
    font-size: 13px;
    color: #909399;
}
.mt-4 { margin-top: 30px; }

/* Data Cards */
.data-card {
    border: none;
    color: #fff;
    border-radius: 8px;
    position: relative;
    overflow: hidden;
}
.card-content {
    position: relative;
    z-index: 1;
}
.card-title {
    font-size: 14px;
    opacity: 0.9;
    margin-bottom: 10px;
}
.card-value {
    font-size: 28px;
    font-weight: bold;
    margin-bottom: 10px;
}
.card-sub {
    font-size: 12px;
    opacity: 0.8;
}
.card-icon {
    position: absolute;
    right: -10px;
    top: -10px;
    font-size: 100px;
    opacity: 0.15;
    transform: rotate(15deg);
    z-index: 0;
}

/* Gradients */
.gradient-1 { background: linear-gradient(135deg, #409EFF 0%, #0d6efd 100%); }
.gradient-2 { background: linear-gradient(135deg, #67C23A 0%, #198754 100%); }
.gradient-3 { background: linear-gradient(135deg, #E6A23C 0%, #fd7e14 100%); }
.gradient-4 { background: linear-gradient(135deg, #F56C6C 0%, #dc3545 100%); }

/* Stat Items (Overview) */
.stat-item {
    background: #fff;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
    text-align: center;
    border: 1px solid #EBEEF5;
    transition: all 0.3s;
}
.stat-item:hover {
    transform: translateY(-5px);
    box-shadow: 0 5px 15px 0 rgba(0,0,0,0.1);
}
.stat-label {
    color: #909399;
    font-size: 14px;
    margin-bottom: 10px;
}
.stat-num {
    font-size: 24px;
    font-weight: bold;
    color: #303133;
}

/* Chart */
.chart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.chart-box {
    height: 350px;
    width: 100%;
}
</style>