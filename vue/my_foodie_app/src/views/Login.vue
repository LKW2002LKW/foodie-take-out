<template>
  <div class="mt-login-page">
    <div class="mt-login-header">
      <div class="mt-logo-wrap">
        <div class="mt-logo"><van-icon name="bag" size="40" color="#222" /></div>
        <h1 class="mt-app-name">吃货联盟</h1>
        <p class="mt-app-slogan">美食，就在身边</p>
      </div>
    </div>
    <div class="mt-login-card">
      <van-tabs v-model:active="activeTab" line-width="20" color="#FFD000">
        <van-tab title="手机验证码" /><van-tab title="账号密码" />
      </van-tabs>
      <div class="mt-form-box">
        <van-form @submit="onSubmit">
          <van-field v-model="form.phone" placeholder="请输入手机号" class="mt-input"><template #left-icon><van-icon name="phone-o" /></template></van-field>
          <van-field v-if="activeTab === 0" v-model="form.code" placeholder="请输入验证码" class="mt-input">
            <template #left-icon><van-icon name="shield-o" /></template>
            <template #button><span class="mt-code-btn">获取验证码</span></template>
          </van-field>
          <van-field v-else v-model="form.password" type="password" placeholder="请输入密码" class="mt-input"><template #left-icon><van-icon name="lock" /></template></van-field>
          <div class="mt-submit-wrap"><van-button round block type="primary" native-type="submit" class="mt-login-btn">登 录</van-button></div>
        </van-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/modules/user'
import { loginByCode, loginByPassword } from '../services/user'
import { showToast, showSuccessToast } from 'vant'

const router = useRouter(); const userStore = useUserStore(); const activeTab = ref(0)
const form = reactive({ phone: '', code: '', password: '' })
const onSubmit = async () => {
  const res = activeTab.value === 0 ? await loginByCode(form.phone, form.code) : await loginByPassword(form.phone, form.password)
  if (res.code === 1) { userStore.setToken(res.data.token); userStore.setUserInfo(res.data); showSuccessToast('登录成功'); router.replace('/merchant/list') }
}
</script>

<style scoped>
.mt-login-page { min-height: 100vh; background-color: #fff; }
.mt-login-header { height: 260px; background: linear-gradient(180deg, #FFD000 0%, #FFC107 100%); display: flex; justify-content: center; align-items: center; border-radius: 0 0 40px 40px; }
.mt-logo { width: 80px; height: 80px; background-color: #fff; border-radius: 20px; display: flex; align-items: center; justify-content: center; margin: 0 auto 16px; box-shadow: 0 8px 24px rgba(0,0,0,0.1); }
.mt-app-name { font-size: 24px; font-weight: 800; color: #222; }
.mt-login-card { margin: -40px 24px 0; background-color: #fff; border-radius: 24px; box-shadow: 0 12px 32px rgba(0,0,0,0.08); padding: 24px 16px; }
.mt-input { background-color: #f7f7f7; border-radius: 12px; margin-bottom: 16px; }
.mt-login-btn { height: 48px; font-size: 17px; background: linear-gradient(90deg, #FFD000 0%, #FFC107 100%) !important; border: none !important; }
</style>
