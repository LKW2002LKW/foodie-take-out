<template>
  <div class="login">
    <div class="login__content">
      <!-- 左侧品牌展示 -->
      <div class="login__brand">
        <div class="login__logo-wrapper">
          <img :src="logoUrl" alt="Logo" class="login__logo" @error="handleLogoError">
          <h1 class="login__app-name">Foodie Take-out</h1>
        </div>
        <p class="login__tagline">为每一份美味寻找归宿</p>
        <div class="login__features">
          <div v-for="feature in features" :key="feature" class="login__feature-item">
            <el-icon><Check /></el-icon>
            <span>{{ feature }}</span>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="login__form-container">
        <div class="login__form-header">
          <h2 class="login__form-title">商户后台管理</h2>
          <p class="login__form-subtitle">请输入您的凭据以访问后台管理中心</p>
        </div>

        <el-form 
          ref="formRef" 
          :model="form" 
          :rules="rules" 
          size="large"
          @keyup.enter="onSubmit"
        >
          <el-form-item prop="username">
            <el-input 
              v-model="form.username" 
              placeholder="请输入用户名" 
              :prefix-icon="User"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="请输入密码" 
              :prefix-icon="Lock" 
              show-password
            />
          </el-form-item>

          <div class="login__actions">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <el-button link type="primary">忘记密码？</el-button>
          </div>

          <el-form-item>
            <el-button 
              type="primary" 
              class="login__submit-btn" 
              :loading="loading"
              @click="onSubmit"
            >
              登 录
            </el-button>
          </el-form-item>

          <div class="login__footer">
            <span>还没有账号？</span>
            <el-button link type="primary" @click="$router.push('/register')">立即入驻</el-button>
          </div>
        </el-form>
      </div>
    </div>

    <!-- 背景装饰 -->
    <div class="login__decoration login__decoration--top"></div>
    <div class="login__decoration login__decoration--bottom"></div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { User, Lock, Check } from '@element-plus/icons-vue'
import { useAuth } from '@/composables/useAuth'
import logoUrl from '@/assets/logo.svg'

/**
 * 登录页面逻辑
 */

// 1. 初始化认证 Composable
const { loading, handleLogin } = useAuth()

// 2. 局部状态
const formRef = ref(null)
const rememberMe = ref(false)
const form = reactive({
  username: '',
  password: ''
})

const features = ['实时订单管理', '智能化经营分析', '多维度财务报表', '全链路营销支持']

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, message: '用户名长度不能小于3个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6个字符', trigger: 'blur' }
  ]
}

// 3. 方法
const onSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      await handleLogin(form)
    }
  })
}

const handleLogoError = (e) => {
  // 如果logo加载失败，显示一个默认的文本样式
  e.target.style.display = 'none'
}
</script>

<style scoped>
.login {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f0f4f8;
  position: relative;
  overflow: hidden;
}

.login__content {
  width: 900px;
  max-width: 95%;
  height: 560px;
  display: flex;
  background: #fff;
  border-radius: 24px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  z-index: 10;
}

/* 左侧展示 */
.login__brand {
  flex: 1;
  background: linear-gradient(135deg, #2563eb 0%, #1e40af 100%);
  padding: 60px;
  color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login__logo-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.login__logo {
  width: 48px;
  height: 48px;
  object-fit: contain;
}

.login__app-name {
  font-size: 28px;
  font-weight: 800;
  margin: 0;
  letter-spacing: -0.5px;
}

.login__tagline {
  font-size: 18px;
  opacity: 0.8;
  margin-bottom: 48px;
}

.login__features {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.login__feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  font-weight: 500;
}

.login__feature-item .el-icon {
  background: rgba(255, 255, 255, 0.2);
  padding: 4px;
  border-radius: 50%;
  font-size: 12px;
}

/* 右侧表单 */
.login__form-container {
  width: 450px;
  padding: 60px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login__form-header {
  margin-bottom: 32px;
}

.login__form-title {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 12px;
}

.login__form-subtitle {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.login__actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.login__submit-btn {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: #2563eb;
  border: none;
  transition: all 0.3s;
}

.login__submit-btn:hover {
  background: #1d4ed8;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.3);
}

.login__footer {
  text-align: center;
  margin-top: 32px;
  font-size: 14px;
  color: #64748b;
}

/* 装饰元素 */
.login__decoration {
  position: absolute;
  width: 400px;
  height: 400px;
  border-radius: 50%;
  filter: blur(80px);
  z-index: 1;
}

.login__decoration--top {
  top: -100px;
  right: -100px;
  background: rgba(37, 99, 235, 0.1);
}

.login__decoration--bottom {
  bottom: -150px;
  left: -100px;
  background: rgba(37, 99, 235, 0.15);
}

@media (max-width: 900px) {
  .login__brand {
    display: none;
  }
  .login__content {
    width: 450px;
    height: auto;
    padding: 20px 0;
  }
}
</style>
