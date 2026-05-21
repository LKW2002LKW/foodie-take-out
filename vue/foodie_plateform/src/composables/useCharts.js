import { onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'

/**
 * 仪表盘趋势图表渲染 Composable
 */
export function useCharts(chartRef) {
  let chartInstance = null

  /**
   * 渲染图表
   */
  const renderChart = (data) => {
    if (!chartRef.value) return
    if (!chartInstance) {
      chartInstance = echarts.init(chartRef.value)
    }

    const option = {
      tooltip: {
        trigger: 'axis',
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        borderWidth: 0,
        boxShadow: '0 4px 12px rgba(0,0,0,0.1)'
      },
      legend: {
        data: ['订单量', '营收额', '新增用户'],
        bottom: 0,
        icon: 'circle'
      },
      grid: {
        left: '4%',
        right: '4%',
        bottom: '10%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: data.dateList,
        axisLine: { lineStyle: { color: '#E5E7EB' } }
      },
      yAxis: [
        { type: 'value', name: '数量', splitLine: { lineStyle: { type: 'dashed' } } },
        { type: 'value', name: '金额', position: 'right', splitLine: { show: false } }
      ],
      series: [
        {
          name: '订单量',
          type: 'line',
          smooth: true,
          data: data.orderCountList,
          itemStyle: { color: '#2563EB' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(37, 99, 235, 0.15)' },
              { offset: 1, color: 'rgba(37, 99, 235, 0)' }
            ])
          }
        },
        {
          name: '营收额',
          type: 'line',
          yAxisIndex: 1,
          smooth: true,
          data: data.revenueList,
          itemStyle: { color: '#F97316' }
        },
        {
          name: '新增用户',
          type: 'line',
          smooth: true,
          data: data.userCountList,
          itemStyle: { color: '#10B981' }
        }
      ]
    }
    chartInstance.setOption(option)
  }

  const handleResize = () => chartInstance?.resize()

  onMounted(() => {
    window.addEventListener('resize', handleResize)
  })

  onUnmounted(() => {
    window.removeEventListener('resize', handleResize)
    chartInstance?.dispose()
  })

  return {
    renderChart
  }
}
