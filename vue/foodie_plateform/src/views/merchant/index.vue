<template>
  <div class="merchant-container">
    <el-card>
      <!-- Search -->
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="商户名称">
          <el-input v-model="queryParams.merchantName" placeholder="请输入商户名称" clearable />
        </el-form-item>
        <el-form-item label="城市">
          <el-input v-model="queryParams.cityName" placeholder="请输入城市" clearable />
        </el-form-item>
        <el-form-item label="商户状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px;">
            <el-option label="待审核" :value="0" />
            <el-option label="营业中" :value="1" />
            <el-option label="休息中" :value="2" />
            <el-option label="已关闭" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="queryParams.auditStatus" placeholder="全部" clearable style="width: 120px;">
            <el-option label="已通过" :value="1" />
            <el-option label="审核中" :value="2" />
            <el-option label="已拒绝" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- Table -->
      <el-table :data="tableData" border v-loading="loading">
        <el-table-column prop="merchantName" label="商户名称" min-width="150" />
        <el-table-column label="商户分类" width="120">
          <template #default="scope">
            {{ getCategoryName(scope.row.bizCategoryId) }}
          </template>
        </el-table-column>
        <el-table-column prop="cityName" label="城市" width="100" />
        <el-table-column prop="legalPerson" label="法人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="120" />
        <el-table-column prop="createTime" label="申请时间" width="180" />
        <el-table-column label="营业状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="100">
          <template #default="scope">
            <el-tag :type="getAuditStatusType(scope.row.auditStatus)">
              {{ getAuditStatusText(scope.row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="handleDetail(scope.row)">详情</el-button>
            <el-button 
              v-if="scope.row.auditStatus === 2" 
              link 
              type="success" 
              @click="handleAudit(scope.row)"
            >
              审核
            </el-button>
            <el-button 
              v-if="scope.row.auditStatus === 1 && scope.row.status === 3" 
              link 
              type="success" 
              @click="handleEnable(scope.row)"
            >
              启用
            </el-button>
             <el-button 
              v-if="scope.row.auditStatus === 1 && scope.row.status !== 3" 
              link 
              type="danger" 
              @click="handleDisable(scope.row)"
            >
              禁用
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
    <el-dialog v-model="detailVisible" title="商户详情" width="600px">
      <el-descriptions :column="2" border v-if="currentMerchant">
        <el-descriptions-item label="商户名称">{{ currentMerchant.merchantName }}</el-descriptions-item>
        <el-descriptions-item label="商户分类">
          <el-tag size="small" type="info">{{ getCategoryName(currentMerchant.bizCategoryId) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="商户代码">{{ currentMerchant.merchantCode }}</el-descriptions-item>
        <el-descriptions-item label="城市">{{ currentMerchant.cityName }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentMerchant.contactName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentMerchant.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ currentMerchant.address }}</el-descriptions-item>
        <el-descriptions-item label="营业执照" :span="2">{{ currentMerchant.businessLicense }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ currentMerchant.statusText }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">{{ currentMerchant.auditStatusText }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Audit Dialog -->
    <el-dialog v-model="auditVisible" title="商户审核" width="500px">
      <el-form :model="auditForm">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.auditStatus">
            <el-radio :label="1">通过</el-radio>
            <el-radio :label="3">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核原因" v-if="auditForm.auditStatus === 3">
           <el-input type="textarea" v-model="auditForm.auditReason" placeholder="请输入拒绝原因" />
        </el-form-item>
        <el-form-item label="备注" v-if="auditForm.auditStatus === 1">
           <el-input type="textarea" v-model="auditForm.auditReason" placeholder="请输入备注（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="auditVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAudit">确定</el-button>
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
const currentMerchant = ref(null)
const auditVisible = ref(false)
const auditForm = reactive({
  id: null,
  auditStatus: 1,
  auditReason: ''
})

// 分类映射函数
const getCategoryName = (id) => {
  const map = {
    1: '美食',
    2: '甜点饮品',
    3: '超市便利',
    4: '蔬菜水果'
  }
  return map[id] || '未分类'
}

const queryParams = reactive({
  page: 1,
  pageSize: 10,
  merchantName: '',
  cityName: '',
  status: null,
  auditStatus: null
})

const getList = async () => {
  loading.value = true
  try {
    const res = await request.get('/merchant/page', { params: queryParams })
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
  queryParams.merchantName = ''
  queryParams.cityName = ''
  queryParams.status = null
  queryParams.auditStatus = null
  handleSearch()
}

// Helpers
const getStatusText = (status) => {
  const map = { 0: '待审核', 1: '营业中', 2: '休息中', 3: '已关闭' }
  return map[status] || '未知'
}
const getStatusType = (status) => {
  if (status === 1) return 'success'
  if (status === 2) return 'warning'
  if (status === 3) return 'info'
  return ''
}
const getAuditStatusText = (status) => {
  const map = { 1: '已通过', 2: '审核中', 3: '已拒绝' }
  return map[status] || '未知'
}
const getAuditStatusType = (status) => {
  if (status === 1) return 'success'
  if (status === 2) return 'warning'
  if (status === 3) return 'danger'
  return ''
}

// Actions
const handleDetail = async (row) => {
  const res = await request.get(`/merchant/${row.id}`)
  currentMerchant.value = res.data
  detailVisible.value = true
}

const handleAudit = (row) => {
  auditForm.id = row.id
  auditForm.auditStatus = 1
  auditForm.auditReason = ''
  auditVisible.value = true
}

const submitAudit = async () => {
  if (auditForm.auditStatus === 3 && !auditForm.auditReason) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  await request.put('/merchant/audit', auditForm)
  ElMessage.success('操作成功')
  auditVisible.value = false
  getList()
}

const handleEnable = async (row) => {
  await ElMessageBox.confirm('确认启用该商户吗？', '提示', { type: 'warning' })
  await request.put(`/merchant/enable/${row.id}`)
  ElMessage.success('启用成功')
  getList()
}

const handleDisable = async (row) => {
  await ElMessageBox.confirm('确认禁用该商户吗？', '提示', { type: 'warning' })
  await request.put(`/merchant/disable/${row.id}`)
  ElMessage.success('禁用成功')
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
