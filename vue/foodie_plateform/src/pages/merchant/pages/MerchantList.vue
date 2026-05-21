<template>
  <div class="merchant-manage">
    <!-- 商户管理页只负责筛选、表格与弹窗渲染，业务逻辑由 useMerchantPage 托管。 -->
    <el-card shadow="never" class="merchant-manage__filter-card">
      <el-form :inline="true" :model="queryParams" class="merchant-manage__filter-form">
        <el-form-item label="商户名称">
          <el-input v-model="queryParams.merchantName" placeholder="搜索商户名" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="所属城市">
          <el-input v-model="queryParams.cityName" placeholder="搜索城市" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="营业状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 140px;" @change="handleQuery">
            <el-option label="待审核" :value="0" />
            <el-option label="营业中" :value="1" />
            <el-option label="休息中" :value="2" />
            <el-option label="已关闭" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="queryParams.auditStatus" placeholder="全部" clearable style="width: 140px;" @change="handleQuery">
            <el-option label="已通过" :value="1" />
            <el-option label="审核中" :value="2" />
            <el-option label="已拒绝" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="merchant-manage__list-card">
      <el-table v-loading="loading" :data="tableData" class="merchant-manage__table" stripe>
        <el-table-column prop="merchantName" label="商户基本信息" min-width="200">
          <template #default="{ row }">
            <div class="merchant-manage__info">
              <span class="merchant-manage__name">{{ row.merchantName }}</span>
              <span class="merchant-manage__code">#{{ row.merchantCode || row.id }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="所属分类" width="120" align="center">
          <template #default="{ row }">
            <el-tag effect="plain" round size="small">{{ getCategoryName(row.bizCategoryId) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="cityName" label="城市" width="100" align="center" />
        <el-table-column label="联系方式" width="160">
          <template #default="{ row }">
            <div class="merchant-manage__contact">
              <span>{{ row.legalPerson }}</span>
              <span class="merchant-manage__phone">{{ row.contactPhone }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="营业状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="dark" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="120" align="center">
          <template #default="{ row }">
            <div :class="['merchant-manage__audit', `merchant-manage__audit--${getAuditStatusClass(row.auditStatus)}`]">
              {{ getAuditStatusText(row.auditStatus) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
            <el-divider direction="vertical" />
            <template v-if="row.auditStatus === 2">
              <el-button link type="warning" icon="CircleCheck" @click="handleAudit(row)">去审核</el-button>
            </template>
            <template v-else-if="row.auditStatus === 1">
              <el-button
                v-if="row.status === 3"
                link
                type="success"
                icon="VideoPlay"
                @click="toggleMerchantStatus(row, true)"
              >启用</el-button>
              <el-button
                v-else
                link
                type="danger"
                icon="VideoPause"
                @click="toggleMerchantStatus(row, false)"
              >禁用</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div class="merchant-manage__pagination">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          background
          @size-change="getList"
          @current-change="getList"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="商户完整档案" width="700px" class="merchant-manage__dialog">
      <el-descriptions v-if="currentMerchant" :column="2" border class="merchant-manage__detail">
        <el-descriptions-item label="商户全称" :span="2">
          <span class="merchant-manage__detail-important">{{ currentMerchant.merchantName }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="主营分类">
          <el-tag size="small">{{ getCategoryName(currentMerchant.bizCategoryId) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="唯一识别码">{{ currentMerchant.merchantCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="法人代表">{{ currentMerchant.legalPerson }}</el-descriptions-item>
        <el-descriptions-item label="所在城市">{{ currentMerchant.cityName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentMerchant.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentMerchant.createTime }}</el-descriptions-item>
        <el-descriptions-item label="注册地址" :span="2">{{ currentMerchant.address }}</el-descriptions-item>
        <el-descriptions-item label="营业执照号" :span="2">{{ currentMerchant.businessLicense }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag :type="getStatusType(currentMerchant.status)">{{ getStatusText(currentMerchant.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审核结果">
          <el-tag :type="getAuditStatusType(currentMerchant.auditStatus)">{{ getAuditStatusText(currentMerchant.auditStatus) }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭窗口</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="auditVisible" title="入驻资质审核" width="500px">
      <el-form :model="auditForm" label-position="top">
        <el-form-item label="审核决策">
          <el-radio-group v-model="auditForm.auditStatus" class="merchant-manage__audit-radio">
            <el-radio :label="1" border>准予入驻</el-radio>
            <el-radio :label="3" border>驳回申请</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="auditForm.auditStatus === 3 ? '驳回原因' : '备注信息'">
          <el-input
            v-model="auditForm.auditReason"
            type="textarea"
            :rows="4"
            :placeholder="auditForm.auditStatus === 3 ? '请详细说明驳回原因，以便商户修改...' : '请输入审核备注（选填）'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { useMerchantPage } from '@/composables/business/useMerchantPage'

const {
  auditForm,
  auditVisible,
  currentMerchant,
  detailVisible,
  getList,
  handleAudit,
  handleDetail,
  handleQuery,
  loading,
  queryParams,
  resetQuery,
  submitAudit,
  tableData,
  toggleMerchantStatus,
  total,
} = useMerchantPage()

const getCategoryName = (id) => ({ 1: '美食', 2: '甜点饮品', 3: '超市便利', 4: '蔬菜水果' }[id] || '未分类')
const getStatusText = (status) => ({ 0: '待审核', 1: '营业中', 2: '休息中', 3: '已禁用' }[status] || '未知')
const getStatusType = (status) => ({ 1: 'success', 2: 'warning', 3: 'danger', 0: 'info' }[status] || '')
const getAuditStatusText = (status) => ({ 1: '已通过', 2: '待审核', 3: '已驳回' }[status] || '未知')
const getAuditStatusType = (status) => ({ 1: 'success', 2: 'warning', 3: 'danger' }[status] || '')
const getAuditStatusClass = (status) => ({ 1: 'pass', 2: 'waiting', 3: 'fail' }[status] || '')
</script>

<style scoped>
.merchant-manage {
  padding: 0;
}
.merchant-manage__filter-card {
  margin-bottom: 24px;
  border-radius: 12px;
  border: none;
}
.merchant-manage__filter-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #475569;
}
.merchant-manage__list-card {
  border-radius: 12px;
  border: none;
}
.merchant-manage__info {
  display: flex;
  flex-direction: column;
}
.merchant-manage__name {
  font-weight: 700;
  color: #1e293b;
  font-size: 15px;
}
.merchant-manage__code {
  font-size: 12px;
  color: #94a3b8;
  font-family: monospace;
}
.merchant-manage__contact {
  display: flex;
  flex-direction: column;
  font-size: 13px;
}
.merchant-manage__phone {
  color: #64748b;
}
.merchant-manage__audit {
  font-weight: 600;
  font-size: 13px;
}
.merchant-manage__audit--pass { color: #10b981; }
.merchant-manage__audit--waiting { color: #f59e0b; }
.merchant-manage__audit--fail { color: #ef4444; }
.merchant-manage__pagination {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}
.merchant-manage__detail-important {
  font-weight: 700;
  color: #2563eb;
  font-size: 16px;
}
.merchant-manage__audit-radio {
  display: flex;
  gap: 16px;
}
:deep(.el-table__header th) {
  background-color: #f8fafc;
  color: #475569;
  font-weight: 700;
}
</style>
