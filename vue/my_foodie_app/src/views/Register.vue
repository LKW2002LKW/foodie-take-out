<template>
  <div class="register-page">
    <van-nav-bar title="注册" left-text="返回" left-arrow @click-left="$router.back()" />
    
    <van-form @submit="onRegister">
      <van-cell-group inset>
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
            <van-button size="small" type="primary" native-type="button" :disabled="isSending" @click="sendSmsCode">
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
      <div style="margin: 16px;">
        <van-button round block type="primary" native-type="submit">
          注册
        </van-button>
      </div>
    </van-form>
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
  padding-top: 20px;
}
</style>
