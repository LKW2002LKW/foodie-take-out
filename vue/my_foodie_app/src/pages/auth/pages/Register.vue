<template>
  <div class="register-page mobile-page">
    <!-- 注册页仅负责表单呈现，注册业务逻辑由 useRegisterPage 统一维护。 -->
    <van-nav-bar class="register-nav" title="注册" left-arrow fixed placeholder @click-left="goBack" />

    <section class="register-content">
      <header class="register-header">
        <h1 class="register-title">创建用户账号</h1>
        <p class="register-subtitle">使用手机号完成验证后即可进入吃货联盟</p>
      </header>

      <van-form class="register-form" @submit="onRegister">
        <van-cell-group inset class="register-group">
        <van-field
          v-model="form.phone"
          name="phone"
          label="手机号"
          placeholder="请输入手机号"
          :rules="[{ required: true, message: '请填写手机号' }, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式错误' }]"
        />
        
        <van-field
          v-model="form.code"
          center
          clearable
          label="验证码"
          placeholder="验证码"
          :rules="[{ required: true, message: '请填写验证码' }]"
        >
          <template #button>
            <van-button
              size="small"
              type="default"
              native-type="button"
              class="code-btn"
              :disabled="isSending"
              @click="sendSmsCode"
            >
              {{ isSending ? countDown + 's后重发' : '发送验证码' }}
            </van-button>
          </template>
        </van-field>

        <van-field
          v-model="form.password"
          type="password"
          name="password"
          label="密码"
          placeholder="请输入密码 (选填)"
        />
        <van-field
          v-model="form.nickname"
          name="nickname"
          label="昵称"
          placeholder="请输入昵称 (选填)"
        />
        </van-cell-group>

        <div class="register-submit-wrap">
          <van-button round block type="primary" native-type="submit" color="var(--primary-color)" text-color="#1f1f1f" class="register-submit touch-target">
          注册
          </van-button>
        </div>
      </van-form>
    </section>
  </div>
</template>

<script setup>
import { useRegisterPage } from '@/composables/business/useRegisterPage'

const {
  countDown,
  form,
  goBack,
  isSending,
  onRegister,
  sendSmsCode,
} = useRegisterPage()
</script>

<style scoped>
.register-page {
  background:
    radial-gradient(circle at top right, rgba(255, 255, 255, 0.24) 0, rgba(255, 255, 255, 0) 28%),
    linear-gradient(180deg, #ffe27f 0%, #fff6d6 22%, var(--mt-page-bg) 38%);
}

.register-nav {
  background: transparent;
}

.register-content {
  width: 100%;
  padding: 1.2rem 1.6rem calc(1.6rem + env(safe-area-inset-bottom));
}

.register-header {
  margin-bottom: 1.6rem;
  padding: 0.6rem 0 0.8rem;
}

.register-title {
  margin: 0;
  font-size: 2.2rem;
  line-height: 3rem;
  color: var(--mt-strong);
  font-weight: 800;
}

.register-subtitle {
  margin: 0.8rem 0 0;
  font-size: 1.35rem;
  line-height: 2rem;
  color: rgba(31, 31, 31, 0.68);
}

.register-form {
  width: 100%;
}

.register-group {
  border-radius: 1.8rem;
  overflow: hidden;
  border: 1px solid rgba(245, 194, 0, 0.12);
  box-shadow: 0 0.8rem 2rem rgba(245, 194, 0, 0.08);
}

:deep(.register-group .van-field) {
  min-height: 5rem;
  background: linear-gradient(180deg, #ffffff 0%, #fffdf7 100%);
}

:deep(.register-group .van-field__label),
:deep(.register-group .van-field__control),
:deep(.register-group .van-field__control::placeholder) {
  font-size: 1.6rem;
}

:deep(.register-group .van-field__label) {
  color: var(--mt-strong);
  font-weight: 700;
}

:deep(.register-group .van-field__control) {
  color: #1f1f1f;
}

:deep(.register-group .van-field__body) {
  gap: 0.6rem;
}

:deep(.register-group .van-field__button) {
  margin-left: 0.6rem;
  flex-shrink: 0;
}

:deep(.register-group .van-field::after) {
  left: 1.6rem;
  right: 1.6rem;
  border-color: rgba(245, 194, 0, 0.12);
}

.code-btn {
  min-height: 3.6rem;
  min-width: 7.2rem;
  color: #6b5200 !important;
  background: linear-gradient(180deg, #fff8dc 0%, #ffe9a0 100%) !important;
  border: 1px solid rgba(245, 194, 0, 0.28) !important;
  font-size: 1rem;
  padding: 0 0.6rem;
  white-space: nowrap;
}

.code-btn.van-button--disabled {
  color: #b79a4b !important;
  background: #fff5cf !important;
  border-color: rgba(245, 194, 0, 0.18) !important;
}

.register-submit-wrap {
  margin: 1.6rem 0 0;
}

.register-submit {
  font-size: 1.6rem;
  font-weight: 600;
  color: #1f1f1f !important;
  background: linear-gradient(180deg, #ffe27f 0%, #f5c200 100%) !important;
  border: none !important;
  box-shadow: 0 0.8rem 1.6rem rgba(245, 194, 0, 0.2);
}

:deep(.register-submit .van-button__text) {
  color: #1f1f1f !important;
}
</style>
