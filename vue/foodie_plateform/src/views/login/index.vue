<template>
  <div class="platform-login">
    <div class="platform-login__wrapper">
      <!-- 品牌侧 -->
      <div class="platform-login__brand">
        <div class="platform-login__logo-box">
          <el-icon class="platform-login__logo-icon"><Monitor /></el-icon>
          <h1 class="platform-login__app-name">Foodie Platform</h1>
        </div>
        <div class="platform-login__brand-content">
          <h2 class="platform-login__brand-title">全域外卖调度与商户治理中心</h2>
          <p class="platform-login__brand-desc">集成大数据分析、商户生命周期管理与多端运营调度，致力于构建高效、透明的餐饮外卖生态系统。</p>
        </div>
        <div class="platform-login__footer">
          <span>&copy; 2024 Foodie Take-out Enterprise Edition</span>
        </div>
      </div>

      <!-- 登录侧 -->
      <div class="platform-login__form-side">
        <div class="platform-login__form-container">
          <header class="platform-login__form-header">
            <h2 class="platform-login__form-title">管理员登录</h2>
            <p class="platform-login__form-subtitle">系统正在进行实时监控，请使用授权账号访问</p>
          </header>

          <el-form 
            ref="formRef" 
            :model="form" 
            :rules="rules" 
            size="large"
            @keyup.enter="onLogin"
          >
            <el-form-item prop="username">
              <el-input 
                v-model="form.username" 
                placeholder="安全账号 / 用户名" 
                :prefix-icon="User"
              />
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input 
                v-model="form.password" 
                type="password" 
                placeholder="访问令牌 / 密码" 
                :prefix-icon="Lock" 
                show-password
              />
            </el-form-item>

            <div class="platform-login__options">
              <el-checkbox v-model="remember">保持登录状态</el-checkbox>
              <el-link type="primary" :underline="false">寻求技术支持</el-link>
            </div>

            <el-form-item>
              <el-button 
                type="primary" 
                class="platform-login__submit" 
                :loading="loading"
                @click="onLogin"
              >
                授权并进入系统
              </el-button>
            </el-form-item>
          </el-form>

          <div class="platform-login__security-tip">
            <el-icon><Lock /></el-icon>
            <span>此连接已通过 256 位 SSL 加密处理</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 背景流光效果 -->
    <div class="platform-login__bg-glow"></div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Monitor } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import authApi from '@/api/auth'

/**
 * 平台端安全登录
 */

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const remember = ref(true)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请提供管理员授权账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入访问凭据', trigger: 'blur' }]
}

const onLogin = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const { data, code, msg } = await authApi.login(form)
        if (code === 1) {
          localStorage.setItem('platform_token', data.token)
          localStorage.setItem('platform_user', JSON.stringify(data))
          ElMessage.success({ message: '授权成功，欢迎回来', duration: 1500 })
          router.push('/')
        } else {
          ElMessage.error(msg || '授权验证失败')
        }
      } catch (error) {
        console.error('Login error:', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.platform-login {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #0f172a;
  position: relative;
  overflow: hidden;
}

.platform-login__wrapper {
  width: 1000px;
  max-width: 95%;
  height: 640px;
  display: flex;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
  overflow: hidden;
  z-index: 10;
}

/* Brand Side */
.platform-login__brand {
  flex: 1.1;
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  padding: 60px;
  color: #fff;
  display: flex;
  flex-direction: column;
  position: relative;
}

.platform-login__brand::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: url('https://www.transparenttextures.com/patterns/carbon-fibre.png');
  opacity: 0.1;
}

.platform-login__logo-box {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 80px;
  position: relative;
}

.platform-login__logo-icon {
  font-size: 40px;
  color: #3b82f6;
}

.platform-login__app-name {
  font-size: 24px;
  font-weight: 800;
  margin: 0;
  letter-spacing: 1px;
}

.platform-login__brand-title {
  font-size: 32px;
  font-weight: 700;
  line-height: 1.3;
  margin-bottom: 24px;
  color: #f8fafc;
}

.platform-login__brand-desc {
  font-size: 16px;
  line-height: 1.6;
  color: #94a3b8;
}

.platform-login__footer {
  margin-top: auto;
  font-size: 12px;
  color: #475569;
}

/* Form Side */
.platform-login__form-side {
  width: 480px;
  padding: 60px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.platform-login__form-header {
  margin-bottom: 40px;
}

.platform-login__form-title {
  font-size: 26px;
  font-weight: 800;
  color: #1e293b;
  margin: 0 0 10px;
}

.platform-login__form-subtitle {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.platform-login__options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.platform-login__submit {
  width: 100%;
  height: 54px;
  font-size: 16px;
  font-weight: 700;
  border-radius: 12px;
  background: #2563eb;
  border: none;
  transition: all 0.3s;
}

.platform-login__submit:hover {
  background: #1d4ed8;
  box-shadow: 0 10px 15px -3px rgba(37, 99, 235, 0.4);
  transform: translateY(-1px);
}

.platform-login__security-tip {
  margin-top: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 12px;
  color: #94a3b8;
}

/* Decoration */
.platform-login__bg-glow {
  position: absolute;
  width: 1000px;
  height: 1000px;
  background: radial-gradient(circle, rgba(37, 99, 235, 0.15) 0%, rgba(15, 23, 42, 0) 70%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
}

@media (max-width: 1000px) {
  .platform-login__brand { display: none; }
  .platform-login__wrapper { width: 480px; }
}
</style>
