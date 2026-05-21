import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'

/**
 * 仪表盘图表 Composable
 */
export function useDashboardCharts(revenueRef, orderRef) {
  let revenueChart = null
  let orderChart = null

  /**
   * 初始化并渲染营业额趋势图
   */
  const initRevenueChart = (data) => {
    if (!revenueRef.value) return
    if (!revenueChart) {
      revenueChart = echarts.init(revenueRef.value)
    }

    const option = {
      tooltip: {
        trigger: 'axis',
        backgroundColor: 'rgba(255, 255, 255, 0.9)',
        borderWidth: 0,
        boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
        textStyle: { color: '#1E40AF' }
      },
      legend: {
        data: ['营业额', '实收金额'],
        bottom: 0,
        icon: 'circle'
      },
      grid: {
        left: '4%',
        right: '4%',
        bottom: '12%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: data.dateList,
        axisLine: { lineStyle: { color: '#E5E7EB' } },
        axisLabel: { color: '#6B7280' }
      },
      yAxis: {
        type: 'value',
        splitLine: { lineStyle: { type: 'dashed', color: '#F3F4F6' } },
        axisLabel: { color: '#6B7280' }
      },
      series: [
        {
          name: '营业额',
          type: 'line',
          smooth: true,
          showSymbol: false,
          data: data.revenueList,
          itemStyle: { color: '#2563EB' },
          lineStyle: { width: 3 },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(37, 99, 235, 0.2)' },
              { offset: 1, color: 'rgba(37, 99, 235, 0)' }
            ])
          }
        },
        {
          name: '实收金额',
          type: 'line',
          smooth: true,
          showSymbol: false,
          data: data.incomeList,
          itemStyle: { color: '#10B981' },
          lineStyle: { width: 3, type: 'dashed' }
        }
      ]
    }
    revenueChart.setOption(option)
  }

  /**
   * 初始化并渲染订单量趋势图
   */
  const initOrderChart = (data) => {
    if (!orderRef.value) return
    if (!orderChart) {
      orderChart = echarts.init(orderRef.value)
    }

    const option = {
      tooltip: {
        trigger: 'axis',
        backgroundColor: 'rgba(255, 255, 255, 0.9)',
        borderWidth: 0,
        textStyle: { color: '#1E40AF' }
      },
      grid: {
        left: '4%',
        right: '4%',
        bottom: '12%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: data.dateList,
        axisLine: { lineStyle: { color: '#E5E7EB' } },
        axisLabel: { color: '#6B7280' }
      },
      yAxis: {
        type: 'value',
        splitLine: { lineStyle: { type: 'dashed', color: '#F3F4F6' } },
        axisLabel: { color: '#6B7280' }
      },
      series: [
        {
          name: '订单数',
          type: 'bar',
          data: data.orderCountList,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#F97316' },
              { offset: 1, color: '#FB923C' }
            ]),
            borderRadius: [6, 6, 0, 0]
          },
          barWidth: '35%'
        }
      ]
    }
    orderChart.setOption(option)
  }

  /**
   * 更新所有图表
   */
  const renderCharts = async (data) => {
    await nextTick()
    initRevenueChart(data)
    initOrderChart(data)
  }

  /**
   * 处理窗口调整大小
   */
  const handleResize = () => {
    revenueChart?.resize()
    orderChart?.resize()
  }

  onMounted(() => {
    window.addEventListener('resize', handleResize)
  })

  onUnmounted(() => {
    window.removeEventListener('resize', handleResize)
    revenueChart?.dispose()
    orderChart?.dispose()
  })

  return {
    renderCharts
  }
}
