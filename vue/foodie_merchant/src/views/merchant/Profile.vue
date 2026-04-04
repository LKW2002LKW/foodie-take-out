<template>
  <div class="profile-page">
    <!-- Page Header with Actions -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">商户信息管理</h1>
        <p class="page-subtitle">管理您的店铺基本资料、营业状态及配送设置</p>
      </div>
      <el-button 
        type="primary" 
        size="large" 
        class="save-btn" 
        :loading="saving" 
        @click="handleUpdateInfo" 
        icon="Select"
      >
        保存修改
      </el-button>
    </div>

    <el-row :gutter="24">
      <!-- Left Column: Identity & Status -->
      <el-col :xs="24" :sm="24" :md="8" :lg="6">
        <el-card class="profile-card brand-card" shadow="hover">
          <div class="logo-section">
            <el-upload
              class="avatar-uploader"
              action="http://localhost:8082/merchant/info/upload/logo"
              :show-file-list="false"
              :headers="requestHeaders"
              :on-success="handleLogoSuccess"
              :before-upload="beforeLogoUpload"
              name="file"
            >
              <div class="avatar-wrapper">
                <img v-if="merchantInfo.logo" :src="merchantInfo.logo" class="avatar" />
                <div v-else class="avatar-placeholder">
                  <el-icon><CameraFilled /></el-icon>
                  <span>上传Logo</span>
                </div>
                <div class="avatar-overlay">
                  <el-icon><Edit /></el-icon>
                </div>
              </div>
            </el-upload>
            <h2 class="merchant-name-display">{{ form.merchantName || '未设置名称' }}</h2>
            <div class="audit-badge">
               <el-tag :type="auditStatusType" effect="light" round>{{ auditStatusText }}</el-tag>
            </div>
          </div>

          <el-divider><span class="divider-text">营业状态</span></el-divider>

          <div class="status-selector">
             <div 
               class="status-option" 
               :class="{ active: merchantInfo.status === 1, 'is-disabled': merchantInfo.auditStatus !== 1 }"
               @click="merchantInfo.auditStatus === 1 && handleStatusChange(1)"
             >
               <div class="status-dot green"></div>
               <span>营业中</span>
             </div>
             <div 
               class="status-option" 
               :class="{ active: merchantInfo.status === 2, 'is-disabled': merchantInfo.auditStatus !== 1 }"
               @click="merchantInfo.auditStatus === 1 && handleStatusChange(2)"
             >
               <div class="status-dot orange"></div>
               <span>休息中</span>
             </div>
             <div 
               class="status-option" 
               :class="{ active: merchantInfo.status === 3, 'is-disabled': merchantInfo.auditStatus !== 1 }"
               @click="merchantInfo.auditStatus === 1 && handleStatusChange(3)"
             >
                <div class="status-dot red"></div>
               <span>已关闭</span>
             </div>
          </div>

          <el-divider><span class="divider-text">营业时间</span></el-divider>

          <div class="hours-section">
             <div class="time-picker-group">
                <el-time-select
                  v-model="startTime"
                  start="00:00"
                  step="00:30"
                  end="23:30"
                  placeholder="开始"
                  class="time-input"
                  :clearable="false"
                />
                <span class="time-separator">至</span>
                <el-time-select
                  v-model="endTime"
                  start="00:00"
                  step="00:30"
                  end="23:30"
                  :min-time="startTime"
                  placeholder="结束"
                  class="time-input"
                  :clearable="false"
                />
             </div>
             <el-button type="primary" link class="update-time-btn" @click="handleHoursChange">更新时间</el-button>
          </div>
        </el-card>
      </el-col>

      <!-- Right Column: Details Edit Form -->
      <el-col :xs="24" :sm="24" :md="16" :lg="18">
        <el-card class="profile-card detail-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="header-title">基本信息配置</span>
            </div>
          </template>
          
          <el-form :model="form" ref="formRef" label-position="top" size="large" class="modern-form">
            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="商户名称" prop="merchantName">
                  <el-input v-model="form.merchantName" placeholder="请输入店铺名称" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="联系人姓名" prop="contactName">
                  <el-input v-model="form.contactName" placeholder="请输入联系人姓名" prefix-icon="User" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="商户地址" prop="address">
              <el-input v-model="form.address" placeholder="详细经营地址" prefix-icon="Location" />
            </el-form-item>
            
            <el-form-item label="商户描述" prop="description">
              <el-input 
                v-model="form.description" 
                type="textarea" 
                :rows="4" 
                placeholder="请输入精彩的店铺介绍，吸引更多顾客..." 
                resize="none"
              />
            </el-form-item>

            <el-form-item label="商户分类" prop="bizCategoryId">
              <el-select v-model="form.bizCategoryId" placeholder="请选择商户分类" style="width: 100%">
                <el-option label="美食" :value="1" />
                <el-option label="甜点饮品" :value="2" />
                <el-option label="超市便利" :value="3" />
                <el-option label="蔬菜水果" :value="4" />
              </el-select>
            </el-form-item>
            
            <div class="form-section-header">配送设置</div>
            
            <div class="delivery-settings-box">
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="起送金额 (元)">
                     <el-input-number v-model="form.minDeliveryAmount" :precision="2" :step="1" :min="0" class="full-width-number" controls-position="right" />
                  </el-form-item>
                </el-col>
                 <el-col :span="12">
                  <el-form-item label="配送费 (元)">
                     <el-input-number v-model="form.deliveryFee" :precision="2" :step="1" :min="0" class="full-width-number" controls-position="right" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
            
            <div class="form-section-header">认证信息 (不可修改)</div>
             <el-row :gutter="24" class="readonly-info">
              <el-col :span="12">
                <div class="info-item">
                   <label>营业执照号</label>
                   <p>{{ merchantInfo.businessLicense || '未认证' }}</p>
                </div>
              </el-col>
               <el-col :span="12">
                 <div class="info-item">
                   <label>注册手机号</label>
                    <p>{{ merchantInfo.contactPhone || '-' }}</p>
                </div>
              </el-col>
            </el-row>

          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, ref, reactive, computed } from 'vue';
import { CameraFilled, Edit, Select, User, Location } from '@element-plus/icons-vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';

const loading = ref(false);
const saving = ref(false);
const startTime = ref('');
const endTime = ref('');

// Full merchant info from API
const merchantInfo = ref({
  merchantName: '',
  logo: '',
  status: 3, // 1: Open, 2: Rest, 3: Closed
  auditStatus: 1, // 0: Pending, 1: Approved, 2: Rejected
  businessHours: '',
  contactPhone: '',
  businessLicense: ''
});

// Form for updating info
const form = reactive({
  merchantName: '',
  contactName: '',
  address: '',
  description: '',
  minDeliveryAmount: 0,
  deliveryFee: 0
});

// Headers with token
const requestHeaders = computed(() => {
  const token = localStorage.getItem('merchant_token');
  return { 'Authorization': `Bearer ${token || ''}` };
});

const auditStatusText = computed(() => {
  const map = { 0: '审核中', 1: '已认证', 2: '审核未过' };
  return map[merchantInfo.value.auditStatus] || '待审核';
});

const auditStatusType = computed(() => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' };
  return map[merchantInfo.value.auditStatus] || 'info';
});

// 1. Fetch Merchant Info
const fetchMerchantInfo = async () => {
  loading.value = true;
  try {
    const response = await axios.get('http://localhost:8082/merchant/info/current', {
      headers: requestHeaders.value
    });
    
    if (response.data) {
      const data = response.data.data || response.data;
      
      merchantInfo.value = { ...data };
      
      // Populate form
      form.merchantName = data.merchantName;
      form.contactName = data.contactName;
      form.address = data.address;
      form.description = data.description;
      form.minDeliveryAmount = data.minDeliveryAmount;
      form.deliveryFee = data.deliveryFee;
      
      // Parse business hours
      if (data.businessHours && data.businessHours.includes('-')) {
        const [start, end] = data.businessHours.split('-');
        startTime.value = start;
        endTime.value = end;
      }
    }
  } catch (error) {
    ElMessage.error('获取商户信息失败');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 2. Update Merchant Info
const handleUpdateInfo = async () => {
  saving.value = true;
  const updateData = {
    merchantName: form.merchantName,
    contactName: form.contactName,
    address: form.address,
    bizCategoryId: form.bizCategoryId,
    businessHours: `${startTime.value}-${endTime.value}`,
    minDeliveryAmount: form.minDeliveryAmount,
    deliveryFee: form.deliveryFee,
    description: form.description
  };
  
  try {
    await axios.put('http://localhost:8082/merchant/info', updateData, {
      headers: requestHeaders.value
    });
    ElMessage.success('商户信息已保存');
    fetchMerchantInfo(); // Refresh
  } catch (error) {
    ElMessage.error('保存失败');
  } finally {
    saving.value = false;
  }
};

// 3. Status Management
const handleStatusChange = async (val) => {
  if (merchantInfo.value.status === val) return;
  try {
    await axios.put('http://localhost:8082/merchant/info/status', { status: val }, {
      headers: requestHeaders.value
    });
    merchantInfo.value.status = val;
    ElMessage.success('状态已切换');
  } catch (error) {
    ElMessage.error('状态更新失败');
  }
};

// 4. Business Hours Management
const handleHoursChange = async () => {
  if (!startTime.value || !endTime.value) return;
  const hours = `${startTime.value}-${endTime.value}`;
  
  try {
    await axios.put('http://localhost:8082/merchant/info/businessHours', { businessHours: hours }, {
      headers: requestHeaders.value
    });
    ElMessage.success('营业时间已更新');
  } catch (error) {
    ElMessage.error('营业时间更新失败');
  }
};

// 5. Logo Upload
const handleLogoSuccess = (response, uploadFile) => {
  merchantInfo.value.logo = URL.createObjectURL(uploadFile.raw);
  ElMessage.success('Logo上传成功');
  fetchMerchantInfo();
};

const beforeLogoUpload = (rawFile) => {
  if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
    ElMessage.error('图片格式必须为 JPG 或 PNG!');
    return false;
  } else if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error('图片大小不能超过 2MB!');
    return false;
  }
  return true;
};

onMounted(() => {
  fetchMerchantInfo();
});
</script>

<style scoped>
/* Core Page Layout */
.profile-page {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}
.page-subtitle {
  color: #606266;
  font-size: 14px;
  margin: 0;
}

.save-btn {
  padding: 12px 24px;
  font-weight: 600;
  box-shadow: 0 4px 14px 0 rgba(64, 158, 255, 0.39);
  transition: all 0.3s ease;
}
.save-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px 0 rgba(64, 158, 255, 0.45);
}

/* Common Card Styles */
.profile-card {
  border-radius: 12px;
  border: none;
  margin-bottom: 24px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
.profile-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08); /* 悬停效果 */
}

/* Logo Section */
.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}
.avatar-wrapper {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  cursor: pointer;
}
.avatar, .avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: #f5f7fa;
  color: #909399;
}
.avatar-placeholder .el-icon {
  font-size: 32px;
  margin-bottom: 8px;
}
.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s;
}
.avatar-overlay .el-icon {
  color: #fff;
  font-size: 24px;
}
.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}
.merchant-name-display {
  margin: 16px 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  text-align: center;
}
.audit-badge {
  margin-bottom: 12px;
}

/* Status Selector */
.divider-text {
  color: #909399;
  font-size: 13px;
  font-weight: 500;
}
.status-selector {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.status-option {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-radius: 8px;
  border: 1px solid #ebedf0;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}
.status-option:hover {
  background: #f5f7fa;
}
.status-option.active {
  border-color: #409EFF;
  background: #ecf5ff;
}
.status-option.active .status-dot.green { background: #67C23A; box-shadow: 0 0 0 4px rgba(103, 194, 58, 0.2); }
.status-option.active .status-dot.orange { background: #E6A23C; box-shadow: 0 0 0 4px rgba(230, 162, 60, 0.2); }
.status-option.active .status-dot.red { background: #F56C6C; box-shadow: 0 0 0 4px rgba(245, 108, 108, 0.2); }

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 12px;
  transition: all 0.2s;
}
.status-dot.green { background: #67C23A; opacity: 0.6; }
.status-dot.orange { background: #E6A23C; opacity: 0.6; }
.status-dot.red { background: #F56C6C; opacity: 0.6; }

.is-disabled {
  opacity: 0.6;
  cursor: not-allowed;
  filter: grayscale(100%);
}

/* Hours Section */
.hours-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.time-picker-group {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.time-input {
  width: 45% !important;
}
.time-separator {
  color: #909399;
  font-weight: bold;
}
.update-time-btn {
  align-self: flex-end;
}

/* Right Column Details */
.header-title {
  font-size: 16px;
  font-weight: 600;
  border-left: 4px solid #409EFF;
  padding-left: 12px;
}

.form-section-header {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin: 24px 0 16px 0;
  display: flex;
  align-items: center;
}
.form-section-header::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #ebeef5;
  margin-left: 12px;
}

.delivery-settings-box {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}
.full-width-number {
  width: 100%;
}

.readonly-info {
  color: #606266;
  font-size: 14px;
}
.info-item {
  margin-bottom: 12px;
}
.info-item label {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}
.info-item p {
  margin: 0;
  font-weight: 500;
}

/* Responsive adjustments */
@media screen and (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  .save-btn {
    width: 100%;
  }
}
</style>
