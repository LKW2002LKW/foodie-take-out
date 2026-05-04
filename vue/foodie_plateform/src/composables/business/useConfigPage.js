import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import configApi from '@/api/modules/config'

export const useConfigPage = () => {
  const loading = ref(false)
  const configList = ref([])

  const getList = async () => {
    loading.value = true
    try {
      const res = await configApi.getConfigList()
      configList.value = (res.data || []).map((item) => ({
        ...item,
        hasChanged: false,
      }))
    } finally {
      loading.value = false
    }
  }

  const markAsChanged = (row) => {
    row.hasChanged = true
  }

  const handleSingleUpdate = async (row) => {
    try {
      await configApi.updateConfig({
        configKey: row.configKey,
        configValue: row.configValue,
      })
      ElMessage.success('更新成功')
      row.hasChanged = false
    } catch (error) {
      console.error(error)
    }
  }

  const handleBatchUpdate = async () => {
    const changedItems = configList.value.filter((item) => item.hasChanged)
    if (!changedItems.length) {
      ElMessage.warning('没有检测到修改的配置')
      return
    }

    const payload = changedItems.map((item) => ({
      configKey: item.configKey,
      configValue: item.configValue,
    }))

    try {
      await configApi.updateConfigBatch(payload)
      ElMessage.success(`批量更新 ${changedItems.length} 项配置成功`)
      changedItems.forEach((item) => {
        item.hasChanged = false
      })
    } catch (error) {
      console.error(error)
    }
  }

  onMounted(() => {
    getList()
  })

  return {
    configList,
    getList,
    handleBatchUpdate,
    handleSingleUpdate,
    loading,
    markAsChanged,
  }
}
