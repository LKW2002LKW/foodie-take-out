<template>
  <div class="mt-login-page">
    <div class="mt-login-header">
      <div class="mt-logo-wrap">
        <div class="mt-logo"><van-icon name="bag" size="2.4rem" color="#222" /></div>
        <h1 class="mt-app-name">吃货联盟</h1>
        <p class="mt-app-slogan">美食，就在身边</p>
      </div>
    </div>
    <div class="mt-login-card">
      <van-tabs :active="activeTab" line-width="2rem" color="#FFD000" @update:active="onTabChange">
        <van-tab title="手机验证码" /><van-tab title="账号密码" />
      </van-tabs>
      <div class="mt-form-box">
        <van-form @submit="onSubmit">
          <van-field v-model="form.phone" placeholder="请输入手机号" class="mt-input"><template #left-icon><van-icon name="phone-o" /></template></van-field>
          <van-field v-if="activeTab === 0" v-model="form.code" placeholder="请输入验证码" class="mt-input">
            <template #left-icon><van-icon name="shield-o" /></template>
            <template #button>
              <van-button
                size="small"
                type="default"
                native-type="button"
                class="mt-code-btn"
                :disabled="isSending"
                @click="sendSmsCode"
              >
                {{ isSending ? countDown + 's后重发' : '获取验证码' }}
              </van-button>
            </template>
          </van-field>
          <van-field v-else v-model="form.password" type="password" placeholder="请输入密码" class="mt-input"><template #left-icon><van-icon name="lock" /></template></van-field>
          <div class="mt-submit-wrap"><van-button round block type="primary" native-type="submit" class="mt-login-btn">登 录</van-button></div>
        </van-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/modules/user'
import { loginByCode, loginByPassword, sendCode } from '../services/user'
import { showToast, showSuccessToast, showFailToast } from 'vant'

const router = useRouter(); const userStore = useUserStore(); const activeTab = ref(0)
const form = reactive({ phone: '', code: '', password: '' })
const isSending = ref(false)
const countDown = ref(60)
let smsTimer = null

const onTabChange = (value) => {
  activeTab.value = value
}

const sendSmsCode = async () => {
  if (isSending.value) return
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    showToast('请输入正确的手机号')
    return
  }

  try {
    isSending.value = true
    const res = await sendCode(form.phone)
    if (res.code === 1) {
      showSuccessToast('验证码已发送')
      smsTimer = setInterval(() => {
        countDown.value -= 1
        if (countDown.value <= 0) {
          clearInterval(smsTimer)
          smsTimer = null
          countDown.value = 60
          isSending.value = false
        }
      }, 1000)
    } else {
      isSending.value = false
      showFailToast(res.msg || '发送失败')
    }
  } catch (error) {
    console.error('Send sms code error:', error)
    isSending.value = false
    showFailToast('发送失败，请稍后再试')
  }
}

const onSubmit = async () => {
  const res = activeTab.value === 0 ? await loginByCode(form.phone, form.code) : await loginByPassword(form.phone, form.password)
  if (res.code === 1) { userStore.setToken(res.data.token); userStore.setUserInfo(res.data); showSuccessToast('登录成功'); router.replace('/merchant/list') }
}

onBeforeUnmount(() => {
  if (smsTimer) {
    clearInterval(smsTimer)
    smsTimer = null
  }
})
</script>

<style scoped>
.mt-login-page {
  --space-8: 0.8rem;
  --space-12: 1.2rem;
  --space-16: 1.6rem;
  width: 100%;
  min-height: 100vh;
  min-height: 100dvh;
  padding-top: env(safe-area-inset-top);
  padding-right: env(safe-area-inset-right);
  padding-bottom: env(safe-area-inset-bottom);
  padding-left: env(safe-area-inset-left);
  background-color: #fff;
  display: flex;
  flex-direction: column;
  overflow-x: hidden;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.mt-login-header {
  width: 100%;
  min-height: 26rem;
  background: linear-gradient(180deg, #ffd000 0%, #ffc107 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 0 0 4rem 4rem;
  padding: var(--space-16) var(--space-16) 6.4rem;
}

.mt-logo-wrap {
  width: 100%;
  text-align: center;
}

.mt-logo {
  width: 8rem;
  height: 8rem;
  background-color: #fff;
  border-radius: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto var(--space-16);
  box-shadow: 0 0.8rem 2.4rem rgba(0, 0, 0, 0.1);
}

.mt-app-name {
  margin: 0;
  font-size: 2.4rem;
  line-height: 3.2rem;
  font-weight: 800;
  color: #222;
}

.mt-app-slogan {
  margin: var(--space-8) 0 0;
  font-size: 1.3rem;
  line-height: 2rem;
  color: #525252;
}

.mt-login-card {
  width: calc(100% - 3.2rem);
  margin: -4rem auto 0;
  background-color: #fff;
  border-radius: 2.4rem;
  box-shadow: 0 1.2rem 3.2rem rgba(0, 0, 0, 0.08);
  padding: 2.4rem 1.6rem 2rem;
}

.mt-form-box {
  width: 100%;
  margin-top: var(--space-12);
}

.mt-input {
  background-color: #f7f7f7;
  border-radius: 1.2rem;
  margin-bottom: var(--space-16);
}

:deep(.mt-input .van-field__control),
:deep(.mt-input .van-field__control::placeholder) {
  font-size: 1.4rem;
}

:deep(.mt-input .van-field__left-icon),
:deep(.mt-input .van-field__right-icon) {
  min-height: 4.4rem;
  display: flex;
  align-items: center;
}

.mt-submit-wrap {
  margin-top: var(--space-16);
}

.mt-code-btn {
  min-width: 9.2rem;
  min-height: 4.4rem;
  padding: 0 var(--space-12);
  color: #4b5563 !important;
  background: #f3f4f6 !important;
  border: 1px solid #d1d5db !important;
  font-size: 1.2rem;
}

.mt-code-btn.van-button--disabled {
  color: #9ca3af !important;
  background: #f9fafb !important;
  border-color: #e5e7eb !important;
}

.mt-login-btn {
  min-height: 4.8rem;
  font-size: 1.7rem;
  font-weight: 600;
  background: linear-gradient(90deg, #ffd000 0%, #ffc107 100%) !important;
  border: none !important;
}
</style>
