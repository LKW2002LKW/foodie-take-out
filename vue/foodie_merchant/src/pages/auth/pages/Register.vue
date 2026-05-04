<template>
  <div class="register">
    <div class="register__container">
      <!-- 头部 -->
      <header class="register__header">
        <div class="register__brand" @click="$router.push('/login')">
          <img :src="logoUrl" alt="Logo" class="register__logo" @error="handleLogoError">
          <h1 class="register__app-name">Foodie Merchant</h1>
        </div>
        <div class="register__steps">
          <el-steps :active="activeStep" finish-status="success" simple>
            <el-step title="商户信息" />
            <el-step title="账号设置" />
            <el-step title="入驻成功" />
          </el-steps>
        </div>
      </header>

      <!-- 主体内容 -->
      <main class="register__main">
        <el-card shadow="never" class="register__card">
          <el-form 
            ref="formRef" 
            :model="form" 
            :rules="rules" 
            label-position="top"
            size="large"
          >
            <!-- 第一步：商户基本信息 -->
            <div v-show="activeStep === 0" class="register__step-content">
              <h2 class="register__step-title">填写商户基本信息</h2>
              
              <el-row :gutter="24">
                <el-col :span="24">
                  <el-form-item label="商户名称" prop="merchantName">
                    <el-input v-model="form.merchantName" placeholder="请与营业执照名称保持一致" />
                  </el-form-item>
                </el-col>
                
                <el-col :span="12">
                  <el-form-item label="营业执照号" prop="businessLicense">
                    <el-input v-model="form.businessLicense" placeholder="统一社会信用代码" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="商户分类" prop="bizCategoryId">
                    <el-select v-model="form.bizCategoryId" placeholder="请选择主营分类" style="width: 100%">
                      <el-option label="美食" :value="1" />
                      <el-option label="甜点饮品" :value="2" />
                      <el-option label="超市便利" :value="3" />
                      <el-option label="蔬菜水果" :value="4" />
                    </el-select>
                  </el-form-item>
                </el-col>

                <el-col :span="12">
                  <el-form-item label="法人姓名" prop="legalPerson">
                    <el-input v-model="form.legalPerson" placeholder="法人姓名" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="所在城市" prop="cityName">
                    <el-input v-model="form.cityName" placeholder="例如：北京市" @input="handleCityInput" />
                  </el-form-item>
                </el-col>

                <el-col :span="24">
                  <el-form-item label="详细地址" prop="address">
                    <div class="register__address-block">
                      <div class="register__map-preview">
                        <div ref="mapRef" class="register__map-canvas"></div>
                        <div v-if="mapLoading" class="register__map-loading-mask">
                          <span>地图加载中...</span>
                        </div>
                        <div class="register__map-center-pin" :class="{ 'is-loading': mapResolving }">
                          <div class="register__map-center-pin-dot"></div>
                        </div>
                        <div class="register__map-address-tip">
                          {{ form.address || '拖动地图选择准确门店地址' }}
                        </div>
                        <button type="button" class="register__map-locate-btn" @click="relocateMapToCurrent">
                          回到当前位置
                        </button>
                      </div>

                      <div class="register__address-toolbar">
                        <el-input
                          v-model="form.address"
                          placeholder="街道、楼牌号等详细地址"
                          clearable
                          @input="handleAddressInput"
                          @focus="handleAddressFocus"
                          @blur="handleAddressBlur"
                        />
                        <div class="register__address-actions">
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

                      <div v-if="suggestionsVisible && suggestions.length" class="register__suggestion-list">
                        <button
                          v-for="item in suggestions"
                          :key="item.id"
                          type="button"
                          class="register__suggestion-item"
                          @mousedown.prevent="handleSuggestionSelect(item)"
                        >
                          <span class="register__suggestion-name">{{ item.name }}</span>
                          <span class="register__suggestion-address">{{ item.district }}{{ item.address }}</span>
                        </button>
                      </div>

                      <div class="register__location-summary">
                        <div class="register__location-row">
                          <el-tag :type="hasCoordinates ? 'success' : 'warning'" effect="light" round>
                            {{ hasCoordinates ? '定位已确认' : '待重新定位' }}
                          </el-tag>
                          <span class="register__location-hint">
                            {{ hasCoordinates ? '注册时会一起提交商户地址经纬度。' : '进入下一步前需要先完成门店定位。' }}
                          </span>
                        </div>
                        <div class="register__location-meta">
                          <span>经度：{{ coordinateText.longitude }}</span>
                          <span>纬度：{{ coordinateText.latitude }}</span>
                          <span>行政区：{{ regionText }}</span>
                        </div>
                      </div>
                    </div>
                  </el-form-item>
                </el-col>

                <el-col :span="24">
                  <el-form-item label="商户描述" prop="description">
                    <el-input 
                      v-model="form.description" 
                      type="textarea" 
                      :rows="3"
                      placeholder="介绍您的店铺特色" 
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <div class="register__footer">
                <el-button @click="$router.push('/login')">返回登录</el-button>
                <el-button type="primary" @click="nextStep">下一步：账号设置</el-button>
              </div>
            </div>

            <!-- 第二步：管理员账号信息 -->
            <div v-show="activeStep === 1" class="register__step-content">
              <h2 class="register__step-title">设置管理员账号</h2>
              
              <el-row :gutter="24">
                <el-col :span="24">
                  <el-divider content-position="left">账号凭据</el-divider>
                </el-col>
                
                <el-col :span="12">
                  <el-form-item label="登录用户名" prop="username">
                    <el-input v-model="form.username" placeholder="用于后台登录" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="登录密码" prop="password">
                    <el-input v-model="form.password" type="password" show-password placeholder="6-20位字符" />
                  </el-form-item>
                </el-col>

                <el-col :span="24">
                  <el-divider content-position="left">联系人信息</el-divider>
                </el-col>

                <el-col :span="12">
                  <el-form-item label="联系人姓名" prop="contactName">
                    <el-input v-model="form.contactName" placeholder="负责人姓名" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="联系电话" prop="contactPhone">
                    <el-input v-model="form.contactPhone" placeholder="负责人手机号" />
                  </el-form-item>
                </el-col>

                <el-col :span="12">
                  <el-form-item label="配送起送额" prop="minDeliveryAmount">
                    <el-input-number 
                      v-model="form.minDeliveryAmount" 
                      :precision="2" 
                      :min="0" 
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="配送费" prop="deliveryFee">
                    <el-input-number 
                      v-model="form.deliveryFee" 
                      :precision="2" 
                      :min="0" 
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <div class="register__footer">
                <el-button @click="activeStep = 0">上一步</el-button>
                <el-button type="primary" :loading="loading" @click="onSubmit">提交入驻申请</el-button>
              </div>
            </div>
          </el-form>
        </el-card>
      </main>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import logoUrl from '@/assets/logo.svg'
import { useRegisterPage } from '@/composables/business/useRegisterPage'

const {
  activeStep,
  coordinateText,
  form,
  formRef,
  handleAddressBlur,
  handleAddressFocus,
  handleAddressInput,
  handleAutoLocate,
  handleCityInput,
  handleLocateByAddress,
  handleLogoError,
  handleSuggestionSelect,
  hasCoordinates,
  loading,
  locating,
  mapLoading,
  mapRef,
  mapResolving,
  mountRegisterMap,
  nextStep,
  regionText,
  relocateMapToCurrent,
  resolvingAddress,
  rules,
  submitRegister,
  suggestions,
  suggestionsVisible,
} = useRegisterPage()

const onSubmit = submitRegister

onMounted(() => {
  mountRegisterMap()
})
</script>

<style scoped>
.register {
  min-height: 100vh;
  background-color: #f8fafc;
  padding: 40px 20px;
}

.register__container {
  max-width: 900px;
  margin: 0 auto;
}

/* Header */
.register__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.register__brand {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.register__logo {
  width: 40px;
  height: 40px;
}

.register__app-name {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.register__steps {
  width: 500px;
}

/* Card */
.register__card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  padding: 24px;
}

.register__step-title {
  font-size: 20px;
  font-weight: 700;
  color: #334155;
  margin: 0 0 32px;
  text-align: center;
}

.register__footer {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 48px;
  padding-top: 24px;
  border-top: 1px solid #f1f5f9;
}

.register__footer .el-button {
  padding: 12px 32px;
  font-size: 16px;
  border-radius: 10px;
}

.register__address-block {
  width: 100%;
}

.register__map-preview {
  position: relative;
  margin-bottom: 14px;
  border-radius: 14px;
  overflow: hidden;
  background: #edf2f7;
  border: 1px solid #dbe5f0;
}

.register__map-canvas {
  width: 100%;
  height: 280px;
}

.register__map-loading-mask {
  position: absolute;
  inset: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  background: rgba(255, 255, 255, 0.82);
  z-index: 3;
  color: #475569;
}

.register__map-center-pin {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -100%);
  width: 28px;
  height: 38px;
  pointer-events: none;
  z-index: 2;
}

.register__map-center-pin::before {
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

.register__map-center-pin-dot {
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

.register__map-center-pin.is-loading {
  animation: register-pin-bounce 0.7s ease-in-out infinite alternate;
}

.register__map-address-tip {
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

.register__map-locate-btn {
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

.register__address-toolbar {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.register__address-actions {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}

.register__suggestion-list {
  margin-top: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
}

.register__suggestion-item {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px 14px;
  border: 0;
  border-bottom: 1px solid #f1f5f9;
  background: #fff;
  text-align: left;
  cursor: pointer;
}

.register__suggestion-item:last-child {
  border-bottom: 0;
}

.register__suggestion-item:hover {
  background: #f8fbff;
}

.register__suggestion-name {
  font-weight: 600;
  color: #334155;
}

.register__suggestion-address {
  font-size: 13px;
  color: #64748b;
}

.register__location-summary {
  margin-top: 12px;
  padding: 14px 16px;
  border-radius: 10px;
  border: 1px solid #dbeafe;
  background: linear-gradient(135deg, #f8fbff 0%, #f1f7ff 100%);
}

.register__location-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.register__location-hint {
  font-size: 13px;
  color: #475569;
}

.register__location-meta {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  font-size: 13px;
  color: #64748b;
}

@keyframes register-pin-bounce {
  from {
    transform: translate(-50%, -100%);
  }
  to {
    transform: translate(-50%, -112%);
  }
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: #475569;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}

@media (max-width: 768px) {
  .register__header {
    flex-direction: column;
    gap: 24px;
    align-items: flex-start;
  }
  .register__steps {
    width: 100%;
  }

  .register__address-toolbar {
    flex-direction: column;
  }

  .register__address-actions {
    width: 100%;
  }

  .register__address-actions .el-button {
    flex: 1;
  }

  .register__location-row,
  .register__location-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
