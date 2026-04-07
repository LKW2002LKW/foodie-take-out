<template>
  <div class="register-page mobile-page">
    <van-nav-bar class="register-nav" title="注册" left-text="返回" left-arrow @click-left="$router.back()" />

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
          placeholder="请输入验证码"
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
          <van-button round block type="primary" native-type="submit" class="register-submit touch-target">
          注册
          </van-button>
        </div>
      </van-form>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showToast, showSuccessToast, showFailToast } from 'vant';
import { register, sendCode } from '../api/user';
import { useUserStore } from '../store/modules/user';

const router = useRouter();
const userStore = useUserStore();

const form = reactive({
  phone: '',
  code: '',
  password: '',
  nickname: ''
});

const isSending = ref(false);
const countDown = ref(60);

const sendSmsCode = async () => {
    if (!/^1[3-9]\d{9}$/.test(form.phone)) {
        showToast('请输入正确的手机号');
        return;
    }
    try {
        isSending.value = true;
        const res = await sendCode(form.phone);
        if (res.code === 1) {
            showSuccessToast('验证码已发送');
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
    } catch (error) {
        console.error(error);
        showFailToast('发送失败');
        isSending.value = false;
    }
};

const onRegister = async () => {
    try {
        const res = await register(form.phone, form.code, form.password, form.nickname);
        if (res.code === 1) {
             showSuccessToast('注册成功');
             // Auto Login or Redirect? Doc response includes token, so auto login.
             if (res.data?.token) {
                 userStore.setToken(res.data.token);
                 userStore.setUserInfo(res.data);
                 router.push('/profile');
             } else {
                 router.push('/login');
             }
        } else {
             showFailToast(res.msg || '注册失败');
        }
    } catch (error) {
        console.error(error);
        showFailToast('系统错误');
    }
};
</script>

<style scoped>
.register-page {
  background: linear-gradient(180deg, #fff9de 0%, #f7f8fa 35%);
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
}

.register-title {
  margin: 0;
  font-size: 2rem;
  line-height: 2.8rem;
  color: #222;
  font-weight: 700;
}

.register-subtitle {
  margin: 0.8rem 0 0;
  font-size: 1.3rem;
  line-height: 2rem;
  color: #636363;
}

.register-form {
  width: 100%;
}

.register-group {
  border-radius: 1.6rem;
  overflow: hidden;
}

:deep(.register-group .van-field) {
  min-height: 4.4rem;
}

:deep(.register-group .van-field__label),
:deep(.register-group .van-field__control),
:deep(.register-group .van-field__control::placeholder) {
  font-size: 1.4rem;
}

.code-btn {
  min-height: 4.4rem;
  min-width: 9.8rem;
  color: #4b5563 !important;
  background: #f3f4f6 !important;
  border: 1px solid #d1d5db !important;
  font-size: 1.2rem;
  padding: 0 1.2rem;
}

.code-btn.van-button--disabled {
  color: #9ca3af !important;
  background: #f9fafb !important;
  border-color: #e5e7eb !important;
}

.register-submit-wrap {
  margin: 1.6rem 0 0;
}

.register-submit {
  font-size: 1.6rem;
  font-weight: 600;
  background: linear-gradient(90deg, #ffd000 0%, #ffc107 100%) !important;
  border: none !important;
}
</style>
