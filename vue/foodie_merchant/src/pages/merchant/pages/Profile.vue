<template>
  <div class="profile-page">
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
        icon="Select"
        @click="handleUpdateInfo"
      >
        保存修改
      </el-button>
    </div>

    <el-row :gutter="24">
      <el-col :xs="24" :sm="24" :md="8" :lg="6">
        <el-card class="profile-card brand-card" shadow="hover">
          <div class="logo-section">
              <el-upload
              class="avatar-uploader"
              :action="logoUploadAction"
              :show-file-list="false"
              :data="uploadData"
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

      <el-col :xs="24" :sm="24" :md="16" :lg="18">
        <el-card class="profile-card detail-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="header-title">基本信息配置</span>
            </div>
          </template>

          <el-form ref="formRef" :model="form" label-position="top" size="large" class="modern-form">
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
              <div class="address-block">
                <div class="map-preview">
                  <div ref="mapRef" class="map-canvas"></div>
                  <div v-if="mapLoading" class="map-loading-mask">
                    <span>地图加载中...</span>
                  </div>
                  <div class="map-center-pin" :class="{ 'is-loading': mapResolving }">
                    <div class="map-center-pin__dot"></div>
                  </div>
                  <div class="map-address-tip">
                    {{ form.address || '拖动地图选择准确门店地址' }}
                  </div>
                  <button type="button" class="map-locate-btn" @click="relocateMapToCurrent">
                    回到当前位置
                  </button>
                </div>

                <div class="address-toolbar">
                  <el-input
                    v-model="form.address"
                    placeholder="输入经营地址后点击定位，或直接使用自动定位"
                    prefix-icon="Location"
                    clearable
                    @input="handleAddressInput"
                    @focus="handleAddressFocus"
                    @blur="handleAddressBlur"
                  />
                  <div class="address-actions">
                    <el-button
                      type="primary"
                      plain
                      :loading="resolvingAddress"
                      :disabled="!form.address.trim()"
                      @click="handleLocateByAddress"
                    >
                      根据输入地址定位
                    </el-button>
                    <el-button
                      type="primary"
                      :loading="locating"
                      @click="handleAutoLocate"
                    >
                      自动定位
                    </el-button>
                  </div>
                </div>

                <div v-if="suggestionsVisible && suggestions.length" class="suggestion-list">
                  <button
                    v-for="item in suggestions"
                    :key="item.id"
                    type="button"
                    class="suggestion-item"
                    @mousedown.prevent="handleSuggestionSelect(item)"
                  >
                    <span class="suggestion-name">{{ item.name }}</span>
                    <span class="suggestion-address">{{ item.district }}{{ item.address }}</span>
                  </button>
                </div>

                <div class="location-summary">
                  <div class="location-summary__row">
                    <el-tag :type="hasCoordinates ? 'success' : 'warning'" effect="light" round>
                      {{ hasCoordinates ? '定位已确认' : '待重新定位' }}
                    </el-tag>
                    <span class="location-hint">
                      {{ hasCoordinates ? '当前经纬度会随资料一起保存到数据库。' : '地址改动后坐标已失效，请先完成定位。' }}
                    </span>
                  </div>
                  <div class="location-summary__meta">
                    <span>经度：{{ coordinateText.longitude }}</span>
                    <span>纬度：{{ coordinateText.latitude }}</span>
                    <span>行政区：{{ regionText }}</span>
                  </div>
                </div>

              </div>
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
                    <el-input-number
                      v-model="form.minDeliveryAmount"
                      :precision="2"
                      :step="1"
                      :min="0"
                      class="full-width-number"
                      controls-position="right"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="配送费 (元)">
                    <el-input-number
                      v-model="form.deliveryFee"
                      :precision="2"
                      :step="1"
                      :min="0"
                      class="full-width-number"
                      controls-position="right"
                    />
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
import { onMounted } from 'vue'
import { CameraFilled, Edit, Location } from '@element-plus/icons-vue'
import { useMerchantProfilePage } from '@/composables/business/useMerchantProfilePage'

const {
  auditStatusText,
  auditStatusType,
  beforeLogoUpload,
  coordinateText,
  fetchMerchantInfo,
  form,
  formRef,
  handleAddressBlur,
  handleAddressFocus,
  handleAddressInput,
  handleAutoLocate,
  handleHoursChange,
  handleLocateByAddress,
  handleLogoSuccess,
  handleStatusChange,
  handleSuggestionSelect,
  handleUpdateInfo,
  hasCoordinates,
  loading,
  locating,
  logoUploadAction,
  mapLoading,
  mapRef,
  mapResolving,
  merchantInfo,
  regionText,
  relocateMapToCurrent,
  requestHeaders,
  resolvingAddress,
  saving,
  startTime,
  endTime,
  suggestions,
  suggestionsVisible,
  uploadData,
} = useMerchantProfilePage()

onMounted(() => {
  fetchMerchantInfo()
})
</script>

<style scoped>
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
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
}

.page-subtitle {
  margin: 0;
  font-size: 14px;
  color: #606266;
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

.profile-card {
  border-radius: 12px;
  border: none;
  margin-bottom: 24px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.profile-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

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
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  cursor: pointer;
}

.avatar,
.avatar-placeholder {
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
  inset: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  background: rgba(0, 0, 0, 0.5);
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-overlay .el-icon {
  font-size: 24px;
  color: #fff;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.merchant-name-display {
  margin: 16px 0 8px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  text-align: center;
}

.audit-badge {
  margin-bottom: 12px;
}

.divider-text {
  font-size: 13px;
  font-weight: 500;
  color: #909399;
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
  border-color: #409eff;
  background: #ecf5ff;
}

.status-option.active .status-dot.green {
  background: #67c23a;
  box-shadow: 0 0 0 4px rgba(103, 194, 58, 0.2);
}

.status-option.active .status-dot.orange {
  background: #e6a23c;
  box-shadow: 0 0 0 4px rgba(230, 162, 60, 0.2);
}

.status-option.active .status-dot.red {
  background: #f56c6c;
  box-shadow: 0 0 0 4px rgba(245, 108, 108, 0.2);
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 12px;
  transition: all 0.2s;
}

.status-dot.green {
  background: #67c23a;
  opacity: 0.6;
}

.status-dot.orange {
  background: #e6a23c;
  opacity: 0.6;
}

.status-dot.red {
  background: #f56c6c;
  opacity: 0.6;
}

.is-disabled {
  opacity: 0.6;
  cursor: not-allowed;
  filter: grayscale(100%);
}

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
  font-weight: 700;
  color: #909399;
}

.update-time-btn {
  align-self: flex-end;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  border-left: 4px solid #409eff;
  padding-left: 12px;
}

.address-block {
  width: 100%;
}

.map-preview {
  position: relative;
  margin-bottom: 14px;
  border-radius: 14px;
  overflow: hidden;
  background: #edf2f7;
  border: 1px solid #dbe5f0;
}

.map-canvas {
  width: 100%;
  height: 280px;
}

.map-loading-mask {
  position: absolute;
  inset: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  background: rgba(255, 255, 255, 0.82);
  z-index: 3;
  color: #475569;
}

.map-center-pin {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -100%);
  width: 28px;
  height: 38px;
  pointer-events: none;
  z-index: 2;
}

.map-center-pin::before {
  content: '';
  position: absolute;
  left: 50%;
  top: 0;
  width: 24px;
  height: 24px;
  transform: translateX(-50%);
  border-radius: 50% 50% 50% 0;
  rotate: -45deg;
  background: #409eff;
  box-shadow: 0 6px 14px rgba(64, 158, 255, 0.28);
}

.map-center-pin__dot {
  position: absolute;
  left: 50%;
  top: 7px;
  width: 8px;
  height: 8px;
  transform: translateX(-50%);
  border-radius: 50%;
  background: #fff;
  z-index: 1;
}

.map-center-pin.is-loading {
  animation: pin-bounce 0.7s ease-in-out infinite alternate;
}

.map-address-tip {
  position: absolute;
  left: 14px;
  right: 86px;
  bottom: 14px;
  padding: 10px 12px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.92);
  color: #334155;
  font-size: 13px;
  backdrop-filter: blur(8px);
  z-index: 2;
}

.map-locate-btn {
  position: absolute;
  right: 14px;
  bottom: 14px;
  min-width: 64px;
  height: 38px;
  border: 0;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.96);
  color: #1f2937;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 10px 18px rgba(15, 23, 42, 0.12);
  z-index: 2;
}

.address-toolbar {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.address-actions {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}

.suggestion-list {
  margin-top: 12px;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
}

.suggestion-item {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px 14px;
  border: 0;
  border-bottom: 1px solid #f2f3f5;
  background: #fff;
  text-align: left;
  cursor: pointer;
}

.suggestion-item:last-child {
  border-bottom: 0;
}

.suggestion-item:hover {
  background: #f6faff;
}

.suggestion-name {
  font-weight: 600;
  color: #303133;
}

.suggestion-address {
  font-size: 13px;
  color: #909399;
}

.location-summary {
  margin-top: 12px;
  padding: 14px 16px;
  border-radius: 10px;
  background: linear-gradient(135deg, #f8fbff 0%, #f4f8ff 100%);
  border: 1px solid #e2ecfb;
}

.location-summary__row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.location-summary__meta {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  font-size: 13px;
  color: #606266;
}

.location-hint {
  font-size: 13px;
  color: #606266;
}

@keyframes pin-bounce {
  from {
    transform: translate(-50%, -100%);
  }
  to {
    transform: translate(-50%, -112%);
  }
}

.form-section-header {
  display: flex;
  align-items: center;
  margin: 24px 0 16px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.form-section-header::after {
  content: '';
  flex: 1;
  height: 1px;
  margin-left: 12px;
  background: #ebeef5;
}

.delivery-settings-box {
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  background: #f8f9fa;
}

.full-width-number {
  width: 100%;
}

.readonly-info {
  font-size: 14px;
  color: #606266;
}

.info-item {
  margin-bottom: 12px;
}

.info-item label {
  display: block;
  margin-bottom: 4px;
  font-size: 12px;
  color: #909399;
}

.info-item p {
  margin: 0;
  font-weight: 500;
}

@media screen and (max-width: 992px) {
  .address-toolbar {
    flex-direction: column;
  }

  .address-actions {
    width: 100%;
  }

  .address-actions .el-button {
    flex: 1;
  }
}

@media screen and (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .save-btn {
    width: 100%;
  }

  .location-summary__row,
  .location-summary__meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
