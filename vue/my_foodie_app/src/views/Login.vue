<template>
  <div class="mt-login-page mobile-page">
    <div class="mt-login-header">
      <div class="mt-logo-wrap">
        <div class="mt-logo"><van-icon name="bag" size="2.4rem" color="var(--mt-strong)" /></div>
        <h1 class="mt-app-name">吃货联盟</h1>
        <p class="mt-app-slogan">美食，就在身边</p>
      </div>
    </div>
    <div class="mt-login-card">
      <van-tabs :active="activeTab" line-width="2rem" color="var(--primary-color)" class="mt-login-tabs" @update:active="onTabChange">
        <van-tab title="手机验证码" /><van-tab title="账号密码" />
      </van-tabs>
      <div class="mt-form-box">
        <van-form @submit="onSubmit">
          <van-field v-model="form.phone" placeholder="请输入手机号" class="mt-input"><template #left-icon><van-icon name="phone-o" /></template></van-field>
          <van-field v-if="activeTab === 0" v-model="form.code" placeholder="验证码" class="mt-input">
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
          <div class="mt-submit-wrap">
            <van-button round block type="primary" native-type="submit" class="mt-login-btn">登 录</van-button>
            <van-button round block plain native-type="button" class="mt-register-btn" @click="router.push('/register')">注 册</van-button>
          </div>
        </van-form>
        <div class="mt-third-party">
          <div class="mt-third-party-divider">
            <span>第三方登录</span>
          </div>
          <van-button
            round
            block
            native-type="button"
            class="mt-wechat-btn"
            :loading="isWechatSubmitting"
            @click="onWechatLogin"
          >
            <svg class="svgfont mt-wechat-icon" aria-hidden="true" viewBox="0 0 1024 1024">
              <use xlink:href="#icon-weixin" />
            </svg>
            <span>微信一键登录</span>
          </van-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/modules/user'
import { loginByCode, loginByPassword, loginByWechat, sendCode } from '../services/user'
import { showToast, showSuccessToast, showFailToast } from 'vant'

const router = useRouter(); const userStore = useUserStore(); const activeTab = ref(0)
const form = reactive({ phone: '', code: '', password: '' })
const isSending = ref(false)
const isWechatSubmitting = ref(false)
const countDown = ref(60)
let smsTimer = null

const onTabChange = (value) => {
  activeTab.value = value
}

const handleLoginSuccess = (res, successText = '登录成功') => {
  if (res.code !== 1 || !res.data?.token) {
    showFailToast(res.msg || '登录失败')
    return false
  }

  userStore.setToken(res.data.token)
  userStore.setUserInfo(res.data)
  showSuccessToast(successText)
  router.replace('/merchant/list')
  return true
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
  try {
    const res = activeTab.value === 0
      ? await loginByCode(form.phone, form.code)
      : await loginByPassword(form.phone, form.password)

    handleLoginSuccess(res)
  } catch (error) {
    console.error('Login error:', error)
  }
}

const onWechatLogin = async () => {
  if (isWechatSubmitting.value) return

  try {
    isWechatSubmitting.value = true
    const mockCode = `mock_code_${Date.now()}`
    const res = await loginByWechat(mockCode)
    handleLoginSuccess(res, '微信登录成功')
  } catch (error) {
    console.error('Wechat login error:', error)
    showFailToast('微信登录失败，请稍后再试')
  } finally {
    isWechatSubmitting.value = false
  }
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
  background-color: var(--mt-page-bg);
  display: flex;
  flex-direction: column;
  overflow-x: hidden;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.mt-login-header {
  width: 100%;
  min-height: 24rem;
  background:
    radial-gradient(circle at top right, rgba(255, 255, 255, 0.24) 0, rgba(255, 255, 255, 0) 30%),
    linear-gradient(180deg, #ffe27f 0%, #f5c200 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 0 0 4rem 4rem;
  padding: 2rem var(--space-16) 5.6rem;
}

.mt-logo-wrap {
  width: 100%;
  text-align: center;
}

.mt-logo {
  width: 8rem;
  height: 8rem;
  background: linear-gradient(180deg, rgba(255,255,255,0.96) 0%, rgba(255,250,236,0.96) 100%);
  border-radius: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto var(--space-16);
  box-shadow: 0 0.8rem 1.8rem rgba(0, 0, 0, 0.08);
}

.mt-app-name {
  margin: 0;
  font-size: 2.4rem;
  line-height: 3.2rem;
  font-weight: 800;
  color: var(--mt-strong);
}

.mt-app-slogan {
  margin: var(--space-8) 0 0;
  font-size: 1.3rem;
  line-height: 2rem;
  color: rgba(31, 31, 31, 0.72);
}

.mt-login-card {
  width: calc(100% - 3.2rem);
  margin: -4rem auto 0;
  background: linear-gradient(180deg, rgba(255,255,255,0.98) 0%, rgba(255,252,244,0.98) 100%);
  border-radius: 2.4rem;
  box-shadow: 0 1rem 2.2rem rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(245, 194, 0, 0.14);
  padding: 2rem 1.6rem 2rem;
}

.mt-form-box {
  width: 100%;
  margin-top: var(--space-12);
}

.mt-login-tabs {
  overflow: hidden;
  border-radius: 1.6rem;
}

:deep(.mt-login-tabs .van-tabs__nav) {
  background: transparent;
}

.mt-input {
  background: linear-gradient(180deg, #fffefb 0%, #fff9ea 100%);
  border-radius: 1.4rem;
  margin-bottom: var(--space-16);
  border: 1px solid rgba(245, 194, 0, 0.12);
  box-shadow: inset 0 0 0 0.1rem rgba(255, 255, 255, 0.72);
}

:deep(.mt-input .van-field__control),
:deep(.mt-input .van-field__control::placeholder) {
  font-size: 1.6rem;
  line-height: 2.2rem;
}

:deep(.mt-input .van-field__left-icon),
:deep(.mt-input .van-field__right-icon) {
  min-height: 4.4rem;
  display: flex;
  align-items: center;
}

:deep(.mt-input .van-field) {
  min-height: 5rem;
  background: transparent;
  display: flex;
  align-items: center;
  padding-top: 0;
  padding-bottom: 0;
}

:deep(.mt-input .van-field__body) {
  min-height: 5rem;
  display: flex;
  align-items: center;
  gap: 0.6rem;
}

:deep(.mt-input .van-field__control) {
  color: #1f1f1f;
  min-height: 2.2rem;
}

:deep(.mt-input .van-field__button) {
  margin-left: 0.6rem;
  flex-shrink: 0;
}

.mt-submit-wrap {
  margin-top: var(--space-16);
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.mt-third-party {
  margin-top: 1.6rem;
}

.mt-third-party-divider {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1.2rem;
  font-size: 1.2rem;
  color: rgba(31, 31, 31, 0.52);
}

.mt-third-party-divider::before,
.mt-third-party-divider::after {
  content: '';
  flex: 1;
  height: 0.1rem;
  background: rgba(31, 31, 31, 0.1);
}

.mt-third-party-divider span {
  padding: 0 1.2rem;
}

.mt-code-btn {
  min-width: 7.2rem;
  min-height: 3.6rem;
  padding: 0 0.6rem;
  color: #6b5200 !important;
  background: linear-gradient(180deg, #fff8dc 0%, #ffe9a0 100%) !important;
  border: 1px solid rgba(245, 194, 0, 0.28) !important;
  font-size: 1rem;
  white-space: nowrap;
}

.mt-code-btn.van-button--disabled {
  color: #b79a4b !important;
  background: #fff5cf !important;
  border-color: rgba(245, 194, 0, 0.18) !important;
}

.mt-login-btn {
  min-height: 4.8rem;
  font-size: 1.7rem;
  font-weight: 600;
  color: #1f1f1f !important;
  background: linear-gradient(180deg, #ffe27f 0%, #f5c200 100%) !important;
  border: none !important;
  box-shadow: 0 0.8rem 1.6rem rgba(245, 194, 0, 0.22);
}

:deep(.mt-login-btn .van-button__text) {
  color: #1f1f1f !important;
}

.mt-register-btn {
  min-height: 4.4rem;
  color: #6b5200 !important;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96) 0%, rgba(255, 248, 220, 0.98) 100%) !important;
  border: 1px solid rgba(245, 194, 0, 0.22) !important;
}

:deep(.mt-register-btn .van-button__text) {
  color: #6b5200 !important;
}

.mt-wechat-btn {
  min-height: 4.4rem;
  color: #1f1f1f !important;
  background: linear-gradient(180deg, #ffffff 0%, #f7fff9 100%) !important;
  border: 1px solid rgba(7, 193, 96, 0.22) !important;
  box-shadow: 0 0.6rem 1.4rem rgba(7, 193, 96, 0.08);
}

:deep(.mt-wechat-btn .van-button__content) {
  display: inline-flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 0.8rem;
}

:deep(.mt-wechat-btn .van-button__text) {
  color: #1f1f1f !important;
}

.mt-wechat-icon {
  width: 2rem;
  height: 2rem;
  flex: 0 0 2rem;
  display: inline-block;
  font-size: 2rem;
  vertical-align: middle;
}
</style>
