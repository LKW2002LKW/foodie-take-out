<template>
  <div class="config-container">
    <!-- 系统配置页作为装配层，配置读取与保存动作统一来自 useConfigPage。 -->
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
import { useConfigPage } from '@/composables/business/useConfigPage'

const {
  configList,
  handleBatchUpdate,
  handleSingleUpdate,
  loading,
  markAsChanged,
} = useConfigPage()
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
