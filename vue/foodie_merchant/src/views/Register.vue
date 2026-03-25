<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <span>商户入驻注册</span>
          <el-button type="text" @click="$router.push('/login')">已有账号？去登录</el-button>
        </div>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px" status-icon>
        
        <el-divider content-position="left">商户基本信息</el-divider>
        
        <el-form-item label="商户名称" prop="merchantName">
          <el-input v-model="form.merchantName" placeholder="请输入商户名称" />
        </el-form-item>
        
        <el-form-item label="营业执照号" prop="businessLicense">
          <el-input v-model="form.businessLicense" placeholder="请输入营业执照号" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="法人姓名" prop="legalPerson">
              <el-input v-model="form.legalPerson" placeholder="法人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所在城市" prop="cityName">
              <el-input v-model="form.cityName" placeholder="例如：北京市" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="商户地址" prop="address">
          <el-input v-model="form.address" placeholder="详细地址" />
        </el-form-item>

        <el-form-item label="商户描述" prop="description">
           <el-input v-model="form.description" type="textarea" placeholder="简单介绍一下店铺..." />
        </el-form-item>

        <el-form-item label="营业时间" prop="businessHours">
          <el-input v-model="form.businessHours" placeholder="例如：09:00-21:00" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
             <el-form-item label="起送金额" prop="minDeliveryAmount">
              <el-input-number v-model="form.minDeliveryAmount" :precision="2" :step="1" :min="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="配送费" prop="deliveryFee">
              <el-input-number v-model="form.deliveryFee" :precision="2" :step="1" :min="0" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">商户联系人信息</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系人姓名" prop="contactName">
              <el-input v-model="form.contactName" placeholder="商户对接人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="form.contactPhone" placeholder="商户对接电话" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">管理员账号信息</el-divider>

        <el-form-item label="注册用户名" prop="username">
          <el-input v-model="form.username" placeholder="用于登录系统" />
        </el-form-item>

        <el-form-item label="登录密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="设置登录密码" />
        </el-form-item>

        <el-row :gutter="20">
           <el-col :span="12">
            <el-form-item label="管理员姓名" prop="name">
              <el-input v-model="form.name" placeholder="管理员真实姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="管理员电话" prop="phone">
              <el-input v-model="form.phone" placeholder="管理员手机号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="loading" style="width: 100%">立即注册</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';

const router = useRouter();
const formRef = ref(null);
const loading = ref(false);

const form = reactive({
  merchantName: '',
  businessLicense: '',
  legalPerson: '',
  contactName: '',
  contactPhone: '',
  cityName: '',
  address: '',
  description: '',
  businessHours: '09:00-22:00',
  minDeliveryAmount: '',
  deliveryFee: '',
  username: '',
  password: '',
  name: '',
  phone: ''
});

const rules = {
  merchantName: [{ required: true, message: '请输入商户名称', trigger: 'blur' }],
  businessLicense: [{ required: true, message: '请输入营业执照号', trigger: 'blur' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入管理员电话', trigger: 'blur' }]
};

const submitForm = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid, fields) => {
    if (valid) {
      loading.value = true;
      try {
        // Business Rule: If delivery fee or min amount is 0 or unset, send null
        const submitData = { ...form };
        if (!submitData.minDeliveryAmount) submitData.minDeliveryAmount = null;
        if (!submitData.deliveryFee) submitData.deliveryFee = null;

        const response = await axios.post('http://localhost:8082/merchant/admin/register', submitData);
        
        if (response.data && response.status === 200) {
           ElMessage.success('注册成功，请登录');
           router.push('/login');
        } else {
           // Assume API might return error message in data
           ElMessage.warning('注册提交完成'); // Adapt based on real backend response structure
        }
      } catch (error) {
        console.error(error);
        ElMessage.error('注册失败: ' + (error.response?.data?.message || error.message));
      } finally {
        loading.value = false;
      }
    } else {
      console.log('error submit!', fields);
    }
  });
};
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f0f2f5;
  padding: 20px;
}
.register-card {
  width: 100%;
  max-width: 800px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
