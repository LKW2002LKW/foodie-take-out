<template>
  <div class="bind-phone">
    <van-nav-bar
      title="绑定手机"
      left-text="返回"
      left-arrow
      @click-left="onClickLeft"
    />

    <van-form @submit="onSubmit">
      <van-cell-group inset style="margin-top: 10px;">
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
          placeholder="请输入验证码"
          :rules="[{ required: true, message: '请填写验证码' }]"
        >
          <template #button>
            <van-button size="small" type="primary" :disabled="isSending" @click="sendCode">
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

      <div style="margin: 16px;">
        <van-button round block type="primary" native-type="submit">
          立即绑定
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showSuccessToast, showLoadingToast } from 'vant'
import { bindPhone, sendBindPhoneCode } from '../../api/user'

const router = useRouter()
const form = ref({
  phone: '',
  code: '',
  password: ''
})

const isSending = ref(false)
const countdown = ref(60)

const onClickLeft = () => {
  router.back()
}

const sendCode = async () => {
    if (!form.value.phone) {
        showToast('请输入手机号')
        return
    }
    if (!/^1[3-9]\d{9}$/.test(form.value.phone)) {
        showToast('手机号格式不正确')
        return
    }

    try {
        const res = await sendBindPhoneCode(form.value.phone)
        if (res.code === 1 || res.code === 200) {
            showSuccessToast('验证码发送成功')
            isSending.value = true
            const timer = setInterval(() => {
                countdown.value--
                if (countdown.value <= 0) {
                    clearInterval(timer)
                    isSending.value = false
                    countdown.value = 60
                }
            }, 1000)
            if (res.data) {
                // Usually dev mode returns code, show it for convenience
                // showToast(`验证码: ${res.data}`) 
            }
        } else {
            showToast(res.msg || '发送失败')
        }
    } catch (error) {
        showToast('发送失败')
    }
}

const onSubmit = async () => {
    showLoadingToast({ message: '绑定中...', forbidClick: true })
    try {
        const res = await bindPhone(form.value)
        if (res.code === 1 || res.code === 200) {
            showSuccessToast('绑定成功')
            router.back()
        } else {
            showToast(res.msg || '绑定失败')
        }
    } catch (error) {
        showToast('绑定失败')
    }
}
</script>

<style scoped>
.bind-phone {
  background-color: #f7f8fa;
  min-height: 100vh;
}
</style>
