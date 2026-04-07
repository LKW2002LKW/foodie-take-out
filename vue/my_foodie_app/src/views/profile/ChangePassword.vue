<template>
  <div class="change-password mobile-page">
    <van-nav-bar
      title="修改密码"
      left-text="返回"
      left-arrow
      @click-left="onClickLeft"
    />

    <van-form @submit="onSubmit">
      <van-cell-group inset class="form-group">
        <van-field
          v-model="form.oldPassword"
          type="password"
          name="oldPassword"
          label="旧密码"
          placeholder="请输入旧密码"
          :rules="[{ required: true, message: '请填写旧密码' }]"
        />
        <van-field
          v-model="form.newPassword"
          type="password"
          name="newPassword"
          label="新密码"
          placeholder="请输入新密码"
          :rules="[{ required: true, message: '请填写新密码' }]"
        />
        <van-field
          v-model="form.confirmPassword"
          type="password"
          name="confirmPassword"
          label="确认密码"
          placeholder="请再次输入新密码"
          :rules="[{ required: true, message: '请确认新密码' }, { validator: confirmValidator, message: '两次密码不一致' }]"
        />
      </van-cell-group>

      <div class="submit-wrap">
        <van-button round block type="primary" native-type="submit" class="submit-btn">
          修改密码
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showSuccessToast, showLoadingToast } from 'vant'
import { updatePassword, logout } from '../../api/user'

const router = useRouter()
const form = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const onClickLeft = () => {
  router.back()
}

const confirmValidator = (val) => {
    return val === form.value.newPassword
}

const onSubmit = async () => {
    showLoadingToast({ message: '提交中...', forbidClick: true })
    try {
        const res = await updatePassword(form.value)
        if (res.code === 1 || res.code === 200) {
            showSuccessToast('修改成功，请重新登录')
            // Logout and redirect to login
            await logout()
            router.push('/login')
        } else {
            showToast(res.msg || '修改失败')
        }
    } catch (error) {
        showToast('修改失败')
    }
}
</script>

<style scoped>
.change-password {
  background-color: #f7f8fa;
  min-height: 100vh;
}

.form-group {
  margin-top: 1rem;
}

.submit-wrap {
  margin: 1.6rem;
}

.submit-btn {
  min-height: 4.4rem;
  font-size: 1.4rem;
}

:deep(.van-cell),
:deep(.van-field) {
  min-height: 4.4rem;
}

:deep(.van-cell__title),
:deep(.van-field__label),
:deep(.van-field__control) {
  font-size: 1.4rem;
}
</style>
