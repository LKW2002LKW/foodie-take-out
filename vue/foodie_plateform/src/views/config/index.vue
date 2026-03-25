<template>
  <div class="config-container">
    <el-card>
      <div class="header-actions">
           <h3>系统参数配置</h3>
           <el-button type="primary" @click="handleBatchUpdate">批量保存</el-button>
      </div>

      <el-table :data="configList" border v-loading="loading" class="mt-20">
        <el-table-column prop="configKey" label="配置Key" width="200" />
        <el-table-column prop="configDesc" label="说明" width="200" />
        <el-table-column label="配置值" min-width="300">
          <template #default="scope">
             <!-- Editable Input -->
             <el-input v-model="scope.row.configValue" placeholder="请输入配置值" @change="markAsChanged(scope.row)">
                <template #append v-if="scope.row.hasChanged">
                    <el-button icon="Check" @click="handleSingleUpdate(scope.row)" />
                </template>
             </el-input>
          </template>
        </el-table-column>
        <el-table-column prop="configType" label="类型" width="100">
             <template #default="scope">
                 <el-tag>{{ scope.row.configType }}</el-tag>
             </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../utils/request'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const configList = ref([])

const getList = async () => {
    loading.value = true
    try {
        const res = await request.get('/config/list')
        configList.value = res.data.map(item => ({
            ...item,
            hasChanged: false // Track local changes
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
        await request.put('/config', {
            configKey: row.configKey,
            configValue: row.configValue
        })
        ElMessage.success('更新成功')
        row.hasChanged = false
    } catch (e) {
        console.error(e)
    }
}

const handleBatchUpdate = async () => {
    // Filter changed items
    const changedItems = configList.value.filter(item => item.hasChanged)
    if (changedItems.length === 0) {
        ElMessage.warning('没有检测到修改的配置')
        return
    }

    const payload = changedItems.map(item => ({
        configKey: item.configKey,
        configValue: item.configValue
    }))

    try {
        await request.put('/config/batch', payload)
        ElMessage.success(`批量更新 ${changedItems.length} 项配置成功`)
        // Reset flags
        changedItems.forEach(item => item.hasChanged = false)
    } catch (e) {
        console.error(e)
    }
}

onMounted(() => {
    getList()
})
</script>

<style scoped>
.header-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.mt-20 {
    margin-top: 20px;
}
</style>
