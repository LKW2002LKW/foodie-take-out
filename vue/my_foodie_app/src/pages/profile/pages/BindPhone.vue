<template>
  <div class="bind-phone mobile-page">
    <van-nav-bar
      title="绑定手机"
      left-arrow
      fixed
      placeholder
      @click-left="onClickLeft"
    />

    <van-form class="page-form" @submit="onSubmit">
      <van-cell-group inset class="form-group">
        <van-field
          v-model="form.phone"
          name="phone"
          label="手机号"
          placeholder="请输入手机号"
          :rules="[{ required: true, message: '请填写手机号' }, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }]"
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
            <van-button size="small" type="default" class="code-btn" :disabled="isSending" @click="sendCode">
                {{ isSending ? `${countdown}s后重新获取` : '发送验证码' }}
            </van-button>
          </template>
        </van-field>

        <van-field
          v-model="form.password"
          type="password"
          name="password"
          label="当前密码"
          placeholder="请输入当前登录密码"
          :rules="[{ required: true, message: '请填写当前密码' }]"
        />
      </van-cell-group>

      <div class="submit-wrap">
        <van-button round block type="primary" native-type="submit" color="var(--primary-color)" text-color="#1f1f1f" class="submit-btn">
          立即绑定
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { useBindPhonePage } from '@/composables/business/useBindPhonePage'

const {
  countdown,
  form,
  isSending,
  onClickLeft,
  onSubmit,
  sendCode,
} = useBindPhonePage()
</script>

<style scoped>
.bind-phone {
  background: var(--mt-page-bg);
  min-height: 100vh;
  min-height: 100dvh;
}

.page-form {
  padding: 1.2rem;
  padding-bottom: calc(2.4rem + env(safe-area-inset-bottom));
}

.form-group {
  margin-top: 0;
  border-radius: 1.8rem;
  overflow: hidden;
  border: 1px solid rgba(245, 194, 0, 0.12);
  box-shadow: 0 0.8rem 2rem rgba(245, 194, 0, 0.08);
}

.code-btn {
  min-height: 3.6rem;
  min-width: 7.2rem;
  padding: 0 0.6rem;
  color: #6b5200 !important;
  background: linear-gradient(180deg, #fff8dc 0%, #ffe9a0 100%) !important;
  border: 1px solid rgba(245, 194, 0, 0.28) !important;
  font-size: 1rem;
  white-space: nowrap;
}

.submit-wrap {
  margin-top: 1.2rem;
}

.submit-btn {
  min-height: 4.4rem;
  font-size: 1.4rem;
  border: none;
  box-shadow: 0 0.8rem 1.6rem rgba(245, 194, 0, 0.2);
  color: #1f1f1f !important;
}

:deep(.van-cell),
:deep(.van-field) {
  min-height: 5rem;
  background: linear-gradient(180deg, #ffffff 0%, #fffdf7 100%);
}

:deep(.van-cell__title),
:deep(.van-field__label),
:deep(.van-field__control) {
  font-size: 1.6rem;
}

:deep(.van-cell::after),
:deep(.van-field::after) {
  left: 1.6rem;
  right: 1.6rem;
  border-color: rgba(245, 194, 0, 0.12);
}

:deep(.van-field__label) {
  color: var(--mt-strong);
  font-weight: 700;
}

:deep(.van-field__control),
:deep(.van-field__control::placeholder) {
  font-size: 1.6rem;
}

:deep(.van-field__control) {
  color: #1f1f1f;
}

:deep(.van-field__button) {
  margin-left: 0.8rem;
  flex-shrink: 0;
}

:deep(.van-field__body) {
  gap: 0.6rem;
}

:deep(.submit-btn .van-button__text) {
  color: #1f1f1f !important;
}
</style>
