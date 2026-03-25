<template>
  <div class="login-container">
    <el-card class="login-card">
      <div class="login-header">
        <h2>商户管理后台登录</h2>
      </div>
      
      <el-form :model="form" :rules="rules" ref="formRef" size="large">
        <el-form-item prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="请输入用户名" 
            prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="请输入密码" 
            prefix-icon="Lock" 
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" class="login-btn" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
        
        <div class="register-link">
          <el-button link type="primary" @click="$router.push('/register')">还没有账号？立即注册</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { User, Lock } from '@element-plus/icons-vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { useMerchantStore } from '../stores/merchant';

const router = useRouter();
const merchantStore = useMerchantStore();
const formRef = ref(null);
const loading = ref(false);

const form = reactive({
  username: '',
  password: ''
});

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
};

const handleLogin = async () => {
  if (!formRef.value) return;

  await formRef.value.validate(async (valid) => {
    if (!valid) return;

    loading.value = true;
    try {
      const res = await axios.post(
        'http://localhost:8082/merchant/admin/login',
        form
      );

      const { code, msg, data } = res.data;

      // ❗ 核心：先判断业务 code
      if (code !== 1) {
        ElMessage.error(msg || '登录失败');
        return;
      }

      // ✅ 登录成功
      const token = data?.token;
      if (!token) {
        ElMessage.error('登录失败：Token 不存在');
        return;
      }

      localStorage.setItem('merchant_token', token);
      ElMessage.success('登录成功');

      // 跳转
      router.push('/');

    } catch (error) {
      console.error(error);
      ElMessage.error('网络异常，请稍后再试');
    } finally {
      loading.value = false;
    }
  });
};

</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #2d3a4b;
  background-image: url('https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'); /* Placeholder background */
  background-size: cover;
}
.login-card {
  width: 400px;
  padding: 20px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.95);
}
.login-header {
  text-align: center;
  margin-bottom: 30px;
}
.login-header h2 {
  color: #303133;
  font-weight: 600;
}
.login-btn {
  width: 100%;
}
.register-link {
  text-align: right;
  margin-top: -10px;
}
</style>
