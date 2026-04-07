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
                    <el-input v-model="form.cityName" placeholder="例如：北京市" />
                  </el-form-item>
                </el-col>

                <el-col :span="24">
                  <el-form-item label="详细地址" prop="address">
                    <el-input v-model="form.address" placeholder="街道、楼牌号等详细地址" />
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
import { ref, reactive } from 'vue'
import { useAuth } from '@/composables/useAuth'
import logoUrl from '@/assets/vue.svg'

/**
 * 注册页面逻辑
 */

// 1. 初始化认证 Composable
const { loading, handleRegister } = useAuth()

// 2. 局部状态
const activeStep = ref(0)
const formRef = ref(null)

const form = reactive({
  merchantName: '',
  businessLicense: '',
  legalPerson: '',
  contactName: '',
  contactPhone: '',
  cityName: '',
  address: '',
  description: '',
  bizCategoryId: '',
  businessHours: '09:00-22:00',
  minDeliveryAmount: 0,
  deliveryFee: 0,
  username: '',
  password: '',
  name: '', // 管理员姓名，同步使用 contactName
  phone: ''  // 管理员电话，同步使用 contactPhone
})

const rules = {
  merchantName: [{ required: true, message: '请输入商户名称', trigger: 'blur' }],
  businessLicense: [{ required: true, message: '请输入营业执照号', trigger: 'blur' }],
  bizCategoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  cityName: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  address: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, message: '最少3个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '最少6个字符', trigger: 'blur' }
  ],
  contactPhone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

// 3. 方法
const nextStep = async () => {
  if (!formRef.value) return
  
  // 只校验当前步骤的字段
  const fieldsToValidate = ['merchantName', 'businessLicense', 'bizCategoryId', 'cityName', 'address']
  let isValid = true
  
  for (const field of fieldsToValidate) {
    try {
      await formRef.value.validateField(field)
    } catch (e) {
      isValid = false
    }
  }

  if (isValid) {
    activeStep.value = 1
  }
}

const onSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      // 业务逻辑：同步管理员信息
      form.name = form.contactName
      form.phone = form.contactPhone
      
      const success = await handleRegister(form)
      if (success) {
        activeStep.value = 2
      }
    }
  })
}

const handleLogoError = (e) => {
  e.target.style.display = 'none'
}
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
}
</style>
