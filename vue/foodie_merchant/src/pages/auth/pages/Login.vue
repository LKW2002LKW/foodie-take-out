<template>
  <div class="login">
    <div class="login__bg login__bg--left"></div>
    <div class="login__bg login__bg--right"></div>

    <div class="login__content">
      <!-- 左侧品牌展示 -->
      <div class="login__brand">
        <div class="login__brand-badge">商户版</div>
        <div class="login__logo-wrapper" aria-hidden="true">
          <svg class="login__bag-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z" stroke-linejoin="round"/>
            <line x1="3" y1="6" x2="21" y2="6" stroke-linecap="round"/>
            <path d="M16 10a4 4 0 0 1-8 0" stroke-linecap="round"/>
          </svg>
        </div>
        <div class="login__logo-row">
          <img :src="logoUrl" alt="Logo" class="login__logo" @error="handleLogoError">
          <h1 class="login__app-name">吃货联盟</h1>
        </div>

        <p class="login__tagline">美食，就在身边</p>
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
          <p class="login__form-kicker">欢迎回来</p>
          <h2 class="login__form-title">商户后台登录</h2>
          <p class="login__form-subtitle">请输入账号信息，继续管理你的门店</p>
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
              class="login__input"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="请输入密码" 
              :prefix-icon="Lock" 
              show-password
              class="login__input"
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
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import logoUrl from '@/assets/logo.svg'
import { loginIconMap, useLoginPage } from '@/composables/business/useLoginPage'

const router = useRouter()
const { Check, Lock, User } = loginIconMap
const {
  features,
  form,
  formRef,
  handleLogoError,
  loading,
  rememberMe,
  rules,
  submitLogin,
} = useLoginPage({
  router,
  message: ElMessage,
})

const onSubmit = submitLogin
</script>

<style scoped>
.login {
  --brand-yellow: #ffd000;
  --brand-yellow-dark: #e8bc00;
  --text-main: #1f1f1f;
  --text-sub: #666;
  --card-bg: #ffffff;
  --surface: #f8f8f8;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 24px;
  background:
    radial-gradient(circle at 12% 8%, #fff3b8 0%, #fff3b8 11%, transparent 42%),
    radial-gradient(circle at 88% 90%, #ffe27d 0%, #ffe27d 10%, transparent 38%),
    linear-gradient(180deg, #fffef9 0%, #fff8df 100%);
  position: relative;
  overflow: hidden;
  font-family: "PingFang SC", "Microsoft YaHei", "Segoe UI", sans-serif;
}

.login__bg {
  position: absolute;
  border-radius: 999px;
  filter: blur(55px);
  z-index: 1;
}

.login__bg--left {
  width: 240px;
  height: 240px;
  left: -80px;
  top: -70px;
  background: rgba(255, 208, 0, 0.36);
}

.login__bg--right {
  width: 280px;
  height: 280px;
  right: -95px;
  bottom: -110px;
  background: rgba(255, 172, 56, 0.33);
}

.login__content {
  width: min(980px, 100%);
  min-height: 600px;
  display: flex;
  background: var(--card-bg);
  border-radius: 24px;
  box-shadow: 0 18px 44px rgba(32, 23, 0, 0.12);
  border: 1px solid rgba(255, 208, 0, 0.2);
  overflow: hidden;
  z-index: 10;
}

/* 左侧展示 */
.login__brand {
  flex: 1;
  background:
    linear-gradient(140deg, #2a2a2a 0%, #151515 100%);
  padding: 48px;
  color: #fffaf0;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  position: relative;
}

.login__brand::after {
  content: "";
  position: absolute;
  right: -35px;
  top: 50%;
  width: 74px;
  height: 74px;
  border-radius: 20px;
  transform: translateY(-50%) rotate(15deg);
  background: rgba(255, 208, 0, 0.78);
  filter: blur(0.2px);
}

.login__brand-badge {
  width: fit-content;
  font-size: 12px;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: #2a1f00;
  background: var(--brand-yellow);
  padding: 6px 12px;
  border-radius: 999px;
  margin-bottom: 20px;
}

.login__logo-wrapper {
  width: 72px;
  height: 72px;
  background: rgba(255, 255, 255, 0.09);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 18px;
}

.login__bag-icon {
  width: 34px;
  height: 34px;
  color: var(--brand-yellow);
}

.login__logo-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.login__logo {
  width: 34px;
  height: 34px;
  object-fit: contain;
}

.login__app-name {
  font-size: 30px;
  font-weight: 800;
  margin: 0;
  letter-spacing: 0.06em;
}

.login__tagline {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.76);
  margin: 0 0 40px;
}

.login__features {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.login__feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
}

.login__feature-item .el-icon {
  background: rgba(255, 255, 255, 0.14);
  padding: 4px;
  border-radius: 50%;
  font-size: 12px;
}

/* 右侧表单 */
.login__form-container {
  width: 460px;
  max-width: 100%;
  padding: 56px 44px;
  background: linear-gradient(180deg, #ffffff 0%, #fffdf3 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login__form-header {
  margin-bottom: 28px;
}

.login__form-kicker {
  margin: 0 0 6px;
  font-size: 13px;
  color: #7f7f7f;
}

.login__form-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-main);
  margin: 0 0 8px;
}

.login__form-subtitle {
  font-size: 14px;
  color: var(--text-sub);
  margin: 0;
  line-height: 1.45;
}

.login__actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.login__submit-btn {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 999px;
  background: var(--brand-yellow);
  color: #2a2100;
  border: none;
  transition: all 0.3s;
}

.login__submit-btn:hover {
  background: var(--brand-yellow-dark);
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(255, 184, 10, 0.33);
}

.login__footer {
  text-align: center;
  margin-top: 28px;
  font-size: 14px;
  color: #717171;
}

:deep(.el-form-item) {
  margin-bottom: 16px;
}

:deep(.login__input .el-input__wrapper) {
  background: var(--surface);
  border-radius: 12px;
  box-shadow: none;
  border: 1px solid transparent;
  padding: 0 14px;
  height: 48px;
  transition: border-color 0.25s, box-shadow 0.25s;
}

:deep(.login__input .el-input__wrapper.is-focus) {
  border-color: #f6c800;
  box-shadow: 0 0 0 3px rgba(255, 208, 0, 0.2);
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: var(--brand-yellow);
  border-color: var(--brand-yellow);
}

:deep(.el-checkbox__label) {
  color: #666;
}

:deep(.el-button--primary.is-link) {
  color: #c28100;
}

:deep(.el-button--primary.is-link:hover) {
  color: #996200;
}

@media (max-width: 900px) {
  .login {
    padding: 12px;
  }

  .login__brand {
    display: none;
  }

  .login__content {
    width: 100%;
    max-width: 480px;
    height: auto;
    min-height: auto;
    border-radius: 20px;
  }

  .login__form-container {
    width: 100%;
    padding: 30px 22px;
  }

  .login__form-title {
    font-size: 24px;
  }
}
</style>
