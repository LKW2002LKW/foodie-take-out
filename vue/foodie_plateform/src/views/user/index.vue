<template>
  <div class="user-container">
    <el-card>
      <!-- Search -->
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="手机号">
          <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="queryParams.nickname" placeholder="请输入昵称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px;">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- Table -->
      <el-table :data="tableData" border v-loading="loading">
        <el-table-column label="头像" width="80" align="center">
          <template #default="scope">
             <el-avatar :size="40" :src="scope.row.avatar" />
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column label="性别" width="80">
          <template #default="scope">
            {{ scope.row.sex === 1 ? '男' : (scope.row.sex === 0 ? '女' : '保密') }}
          </template>
        </el-table-column>
        <el-table-column prop="birthday" label="生日" width="120" />
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="状态" width="100">
           <template #default="scope">
             <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
               {{ scope.row.status === 1 ? '正常' : '禁用' }}
             </el-tag>
           </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="handleDetail(scope.row)">详情</el-button>
            <el-button 
              v-if="scope.row.status === 1" 
              link 
              type="danger" 
              @click="handleToggleStatus(scope.row, 0)"
            >
              禁用
            </el-button>
            <el-button 
              v-else 
              link 
              type="success" 
              @click="handleToggleStatus(scope.row, 1)"
            >
              启用
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="getList"
          @current-change="getList"
        />
      </div>
    </el-card>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailVisible" title="用户详情" width="500px">
      <el-descriptions :column="1" border v-if="currentUser">
        <el-descriptions-item label="头像">
           <el-avatar :size="50" :src="currentUser.avatar" />
        </el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ currentUser.id }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ currentUser.nickname }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentUser.phone }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ currentUser.sexText }}</el-descriptions-item>
        <el-descriptions-item label="生日">{{ currentUser.birthday }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ currentUser.statusText }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ currentUser.createTime }}</el-descriptions-item>
        <el-descriptions-item label="总订单数">{{ currentUser.totalOrderCount }}</el-descriptions-item>
        <el-descriptions-item label="完成订单">{{ currentUser.completedOrderCount }}</el-descriptions-item>
        <el-descriptions-item label="总消费额">¥{{ currentUser.totalConsumption }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '../../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const detailVisible = ref(false)
const currentUser = ref(null)

const queryParams = reactive({
  page: 1,
  pageSize: 10,
  phone: '',
  nickname: '',
  status: null
})

const getList = async () => {
  loading.value = true
  try {
    const res = await request.get('/user/page', { params: queryParams })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.page = 1
  getList()
}

const resetQuery = () => {
  queryParams.phone = ''
  queryParams.nickname = ''
  queryParams.status = null
  handleSearch()
}

const handleDetail = async (row) => {
  const res = await request.get(`/user/${row.id}`)
  currentUser.value = res.data
  detailVisible.value = true
}

const handleToggleStatus = async (row, targetStatus) => {
  const actionText = targetStatus === 1 ? '启用' : '禁用'
  await ElMessageBox.confirm(`确认${actionText}该用户吗？`, '提示', { type: 'warning' })
  
  if (targetStatus === 1) {
    await request.put(`/user/enable/${row.id}`)
  } else {
    await request.put(`/user/disable/${row.id}`)
  }
  
  ElMessage.success(`${actionText}成功`)
  getList()
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style>
