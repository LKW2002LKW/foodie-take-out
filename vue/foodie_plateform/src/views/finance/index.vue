<template>
  <div class="finance-container">
    <el-tabs v-model="activeTab" type="card" class="demo-tabs">
      <el-tab-pane label="结算管理" name="settlement">
        <el-card>
          <el-form :inline="true" :model="settlementParams" class="search-form">
            <el-form-item label="商户ID">
              <el-input v-model="settlementParams.merchantId" placeholder="商户ID" clearable />
            </el-form-item>
             <el-form-item label="结算周期">
              <el-date-picker
                v-model="settlementParams.settlementCycle"
                type="month"
                placeholder="选择月份"
                value-format="YYYY-MM"
              />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="settlementParams.status" placeholder="全部" clearable style="width: 120px;">
                <el-option label="待结算" :value="1" />
                <el-option label="已结算" :value="2" />
                <el-option label="已取消" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="getSettlementList">查询</el-button>
            </el-form-item>
          </el-form>
          
           <el-table :data="settlementList" border v-loading="loadingSettlement">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="merchantName" label="商户名称" />
            <el-table-column prop="settlementCycle" label="结算周期" />
            <el-table-column prop="totalAmount" label="总金额" />
            <el-table-column prop="platformCommission" label="平台抽成" />
            <el-table-column prop="settledAmount" label="应结金额" />
            <el-table-column label="状态">
               <template #default="scope">
                 <el-tag :type="getSettlementStatusType(scope.row.status)">
                   {{ getSettlementStatusText(scope.row.status) }}
                 </el-tag>
               </template>
            </el-table-column>
            <!-- Add operations if needed (e.g., mark as settled) -->
           </el-table>
           <div class="pagination-container">
            <el-pagination
              v-model:current-page="settlementParams.page"
              v-model:page-size="settlementParams.pageSize"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              :total="settlementTotal"
              @size-change="getSettlementList"
              @current-change="getSettlementList"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="抽成配置" name="commission">
        <el-card>
          <div class="mb-20">
             <el-button type="primary" icon="Plus" @click="handleOpenCommissionDialog">新增配置</el-button>
          </div>
          <el-table :data="commissionList" border v-loading="loadingCommission">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column label="配置类型">
               <template #default="scope">
                 {{ getConfigTypeText(scope.row.configType) }}
               </template>
            </el-table-column>
            <el-table-column prop="merchantId" label="商户ID" />
             <el-table-column prop="commissionRate" label="抽成比例(%)" />
             <el-table-column prop="effectiveTime" label="生效时间" />
             <el-table-column prop="createTime" label="创建时间" />
          </el-table>
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="commissionParams.page"
              v-model:page-size="commissionParams.pageSize"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              :total="commissionTotal"
              @size-change="getCommissionList"
              @current-change="getCommissionList"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="财务报表" name="report">
        <el-card v-if="reportData">
           <el-descriptions title="财务总览" :column="2" border>
             <el-descriptions-item label="总订单数">{{ reportData.totalOrderCount }}</el-descriptions-item>
             <el-descriptions-item label="已完成订单">{{ reportData.completedOrderCount }}</el-descriptions-item>
             <el-descriptions-item label="总营收">¥{{ reportData.totalRevenue }}</el-descriptions-item>
             <el-descriptions-item label="平台总收入">¥{{ reportData.totalPlatformIncome }}</el-descriptions-item>
             <el-descriptions-item label="已结算金额">¥{{ reportData.settledAmount }}</el-descriptions-item>
             <el-descriptions-item label="待结算金额">¥{{ reportData.pendingSettlementAmount }}</el-descriptions-item>
           </el-descriptions>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- Add Commission Dialog -->
    <el-dialog v-model="commissionDialogVisible" title="新增抽成配置" width="400px">
      <el-form :model="commissionForm" label-width="100px">
        <el-form-item label="配置类型">
          <el-select v-model="commissionForm.configType">
            <el-option label="全局默认" :value="1" />
            <el-option label="商户特定" :value="2" />
            <el-option label="分类特定" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="商户ID" v-if="commissionForm.configType === 2">
          <el-input v-model="commissionForm.merchantId" placeholder="请输入商户ID" />
        </el-form-item>
        <el-form-item label="分类ID" v-if="commissionForm.configType === 3">
          <el-input v-model="commissionForm.categoryId" placeholder="请输入分类ID" />
        </el-form-item>
        <el-form-item label="抽成比例(%)">
          <el-input-number v-model="commissionForm.commissionRate" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="生效时间">
          <el-date-picker
            v-model="commissionForm.effectiveTime"
            type="datetime"
            placeholder="选择生效时间"
             value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="commissionDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCommission">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import request from '../../utils/request'
import { ElMessage } from 'element-plus'

const activeTab = ref('settlement')

// Settlement Data
const loadingSettlement = ref(false)
const settlementList = ref([])
const settlementTotal = ref(0)
const settlementParams = reactive({
  page: 1,
  pageSize: 10,
  merchantId: '',
  settlementCycle: '',
  status: null
})

// Commission Data
const loadingCommission = ref(false)
const commissionList = ref([])
const commissionTotal = ref(0)
const commissionParams = reactive({
  page: 1,
  pageSize: 10
})

// Report Data
const reportData = ref(null)

// Commission Dialog
const commissionDialogVisible = ref(false)
const commissionForm = reactive({
  configType: 2,
  merchantId: null,
  categoryId: null,
  commissionRate: 15.0,
  effectiveTime: ''
})

// Methods
const getSettlementList = async () => {
  loadingSettlement.value = true
  try {
    const res = await request.get('/finance/settlement/page', { params: settlementParams })
    if (res.data) {
      settlementList.value = res.data.records || []
      settlementTotal.value = res.data.total || 0
    }
  } finally {
    loadingSettlement.value = false
  }
}

const getCommissionList = async () => {
  loadingCommission.value = true
  try {
    const res = await request.get('/finance/commission/page', { params: commissionParams })
    if (res.data) {
      commissionList.value = res.data.records || []
      commissionTotal.value = res.data.total || 0
    }
  } finally {
    loadingCommission.value = false
  }
}

const getReportData = async () => {
  const res = await request.get('/finance/report')
  reportData.value = res.data
}

const handleOpenCommissionDialog = () => {
  commissionForm.configType = 2
  commissionForm.merchantId = null
  commissionForm.categoryId = null
  commissionForm.commissionRate = 15.0
  commissionForm.effectiveTime = ''
  commissionDialogVisible.value = true
}

const submitCommission = async () => {
  if (!commissionForm.effectiveTime) {
    ElMessage.warning('请选择生效时间')
    return
  }
  await request.post('/finance/commission', commissionForm)
  ElMessage.success('新增成功')
  commissionDialogVisible.value = false
  getCommissionList()
}

// Helpers
const getSettlementStatusText = (status) => {
  const map = { 1: '待结算', 2: '已结算', 3: '已取消' }
  return map[status] || '未知'
}
const getSettlementStatusType = (status) => {
   if (status === 2) return 'success'
   if (status === 1) return 'warning'
   return 'info'
}

const getConfigTypeText = (type) => {
  const map = { 1: '全局默认', 2: '商户特定', 3: '分类特定' }
  return map[type] || '未知'
}

// Watch tab change to fetch data lazily
watch(activeTab, (val) => {
  if (val === 'settlement' && settlementList.value.length === 0) {
    getSettlementList()
  } else if (val === 'commission' && commissionList.value.length === 0) {
    getCommissionList()
  } else if (val === 'report' && !reportData.value) {
    getReportData()
  }
})

// Initial fetch
onMounted(() => {
  getSettlementList()
})
</script>

<style scoped>
.mb-20 {
  margin-bottom: 20px;
}
.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style>
