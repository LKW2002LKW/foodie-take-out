<template>
  <div class="login-container">
    <!-- 沉浸式头部背景 -->
    <div class="login-header">
      <div class="app-logo">
        <van-icon name="bag-o" size="48" color="#fff" />
        <h1 class="app-name">吃货联盟</h1>
      </div>
    </div>

    <!-- 登录卡片 -->
    <div class="login-card shadow-lg">
      <van-tabs v-model:active="activeTab" shrink background="transparent" line-width="20px">
        <van-tab title="验证码登录">
          <div class="form-content">
            <van-form @submit="onCodeLogin">
              <div class="input-group">
                <van-field
                  v-model="codeForm.phone"
                  name="phone"
                  placeholder="请输入手机号"
                  :rules="[{ required: true, message: '' }]"
                  class="custom-input"
                >
                  <template #left-icon>
                    <van-icon name="phone-o" class="input-icon" />
                  </template>
                </van-field>
              </div>
              
              <div class="input-group">
                <van-field
                  v-model="codeForm.code"
                  name="code"
                  placeholder="请输入验证码"
                  :rules="[{ required: true, message: '' }]"
                  class="custom-input"
                >
                  <template #left-icon>
                    <van-icon name="shield-o" class="input-icon" />
                  </template>
                  <template #button>
                    <span 
                      class="code-btn" 
                      :class="{ disabled: isSending }"
                      @click="sendSmsCode"
                    >
                      {{ isSending ? `${countDown}s` : '获取验证码' }}
                    </span>
                  </template>
                </van-field>
              </div>

              <div class="submit-btn-area">
                <van-button round block type="primary" native-type="submit" class="submit-btn shadow-md">
                  登录
                </van-button>
              </div>
            </van-form>
          </div>
        </van-tab>
        
        <van-tab title="密码登录">
          <div class="form-content">
            <van-form @submit="onPwdLogin">
              <div class="input-group">
                <van-field
                  v-model="pwdForm.phone"
                  name="phone"
                  placeholder="请输入手机号"
                  :rules="[{ required: true, message: '' }]"
                  class="custom-input"
                >
                  <template #left-icon>
                    <van-icon name="phone-o" class="input-icon" />
                  </template>
                </van-field>
              </div>
              
              <div class="input-group">
                <van-field
                  v-model="pwdForm.password"
                  type="password"
                  name="password"
                  placeholder="请输入密码"
                  :rules="[{ required: true, message: '' }]"
                  class="custom-input"
                >
                  <template #left-icon>
                    <van-icon name="lock" class="input-icon" />
                  </template>
                </van-field>
              </div>

              <div class="submit-btn-area">
                <van-button round block type="primary" native-type="submit" class="submit-btn shadow-md">
                  登录
                </van-button>
              </div>
            </van-form>
          </div>
        </van-tab>
      </van-tabs>

      <div class="extra-actions">
        <span @click="$router.push('/register')">注册新账号</span>
        <span>忘记密码</span>
      </div>
    </div>

    <!-- 第三方登录 -->
    <div class="social-login">
        <div class="divider">
            <span>或者使用以下方式登录</span>
        </div>
        <div class="social-icons">
            <div class="social-btn wechat" @click="onWeChatLogin">
                <van-icon name="wechat" />
            </div>
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../store/modules/user';
import { showToast, showSuccessToast, showFailToast } from 'vant';
import { sendCode, loginByCode, loginByPassword, loginByWeChat } from '../api/user';

const router = useRouter();
const userStore = useUserStore();
const activeTab = ref(0);
const isSending = ref(false);
const countDown = ref(60);

const codeForm = reactive({ phone: '', code: '' });
const pwdForm = reactive({ phone: '', password: '' });

const sendSmsCode = async () => {
    if (!codeForm.phone) return showToast('请输入手机号');
    if (isSending.value) return;
    try {
        isSending.value = true;
        const res = await sendCode(codeForm.phone);
        if (res.code === 1) {
            showSuccessToast('已发送');
            let timer = setInterval(() => {
                countDown.value--;
                if (countDown.value <= 0) {
                    clearInterval(timer);
                    isSending.value = false;
                    countDown.value = 60;
                }
            }, 1000);
        } else {
             showFailToast(res.msg || '发送失败');
             isSending.value = false;
        }
    } catch (e) {
        isSending.value = false;
        showToast('发送失败');
    }
};

const handleLoginSuccess = (res) => {
    if (res.code === 1) {
        userStore.setToken(res.data.token);
        userStore.setUserInfo(res.data);
        showSuccessToast('登录成功');
        router.replace('/merchant/list');
    } else {
        showFailToast(res.msg || '登录失败');
    }
};

const onCodeLogin = async () => {
    if(!codeForm.phone || !codeForm.code) return showToast('请填写完整');
    try {
        const res = await loginByCode(codeForm.phone, codeForm.code);
        handleLoginSuccess(res);
    } catch (e) { showFailToast('系统繁忙'); }
};

const onPwdLogin = async () => {
    if(!pwdForm.phone || !pwdForm.password) return showToast('请填写完整');
    try {
        const res = await loginByPassword(pwdForm.phone, pwdForm.password);
        handleLoginSuccess(res);
    } catch (e) { showFailToast('系统繁忙'); }
};

const onWeChatLogin = async () => {
     try {
        const res = await loginByWeChat('mock-code');
        handleLoginSuccess(res);
    } catch (e) { showToast('微信登录暂不可用'); }
};
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background-color: #f7f8fa;
  display: flex;
  flex-direction: column;
  position: relative;
}
.login-header {
  height: 240px;
  background: linear-gradient(135deg, #FFC107 0%, #FF9800 100%);
  display: flex;
  justify-content: center;
  padding-top: 60px;
  box-sizing: border-box;
}
.app-logo {
  text-align: center;
  color: white;
}
.app-name {
  font-size: 24px;
  margin-top: 10px;
  font-weight: 600;
  letter-spacing: 2px;
}
.login-card {
  margin: -80px 20px 0;
  background: white;
  border-radius: 16px;
  padding: 20px 10px;
  min-height: 300px;
  z-index: 10;
}
.form-content {
  padding: 20px 10px 0;
}
.input-group {
  margin-bottom: 16px;
  border-radius: 8px;
  background-color: #f5f6fa;
  overflow: hidden;
}
.custom-input {
  background-color: transparent;
  padding: 12px 16px;
}
.input-icon {
  font-size: 18px;
  color: #999;
  margin-right: 8px;
}
.code-btn {
  color: #FF9800;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}
.code-btn.disabled {
  color: #ccc;
}
.submit-btn-area {
  margin-top: 30px;
}
.submit-btn {
  height: 44px;
  background: linear-gradient(to right, #FFC107, #FF9800);
  border: none;
  font-size: 16px;
  font-weight: 500;
}
.extra-actions {
  display: flex;
  justify-content: space-between;
  padding: 20px 30px;
  font-size: 14px;
  color: #666;
}
.social-login {
  margin-top: 40px;
  text-align: center;
}
.divider {
  position: relative;
  height: 20px;
  margin-bottom: 20px;
}
.divider span {
  background-color: #f7f8fa;
  padding: 0 10px;
  color: #ccc;
  font-size: 12px;
}
.social-btn {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: white;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  font-size: 24px;
  color: #07c160;
}
.shadow-lg {
  box-shadow: 0 8px 16px rgba(0,0,0,0.08);
}
.shadow-md {
  box-shadow: 0 4px 6px rgba(255, 152, 0, 0.2);
}
</style>
