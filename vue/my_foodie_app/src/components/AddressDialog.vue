<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑地址' : '新增地址'"
    width="600px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
         :model="form"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item label="收货人" prop="consignee">
        <el-input v-model="form.consignee" placeholder="请输入收货人姓名" />
      </el-form-item>

      <el-form-item label="性别" prop="sex">
        <el-radio-group v-model="form.sex">
          <el-radio :label="1">先生</el-radio>
          <el-radio :label="2">女士</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="手机号" prop="phone">
        <el-input v-model="form.phone" placeholder="请输入手机号" />
      </el-form-item>

      <el-form-item label="所在地区" prop="region">
        <el-cascader
          v-model="form.region"
          :options="regionOptions"
          :props="{ expandTrigger: 'hover' }"
          placeholder="请选择省市区"
          @change="handleRegionChange"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="详细地址" prop="detail">
        <el-input
          v-model="form.detail"
          type="textarea"
          :rows="3"
          placeholder="请输入详细地址（街道、门牌号等）"
        />
      </el-form-item>

      <el-form-item label="地址标签">
        <el-radio-group v-model="form.label">
          <el-radio label="家">家</el-radio>
          <el-radio label="公司">公司</el-radio>
          <el-radio label="学校">学校</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="loading">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { addAddress, updateAddress } from '@/api/address'
import { regionData } from 'element-china-area-data' // 需要安装：npm install element-china-area-data

const props = defineProps({
  modelValue: Boolean,
  address: Object
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const isEdit = computed(() => !!props.address)
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  id: null,
  consignee: '',
  sex: 1,
  phone: '',
  region: [],
  provinceCode: '',
  provinceName: '',
  cityCode: '',
  cityName: '',
  districtCode: '',
  districtName: '',
  detail: '',
  label: '家',
  longitude: null,
  latitude: null
})

const rules = {
  consignee: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  region: [
    { required: true, message: '请选择所在地区', trigger: 'change' }
  ],
  detail: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ]
}

const regionOptions = regionData

// 监听地址变化
watch(() => props.address, (newVal) => {
  if (newVal) {
    Object.assign(form, {
      id: newVal.id,
      consignee: newVal.consignee,
      sex: newVal.sex,
      phone: newVal.phone,
      region: [newVal.provinceCode, newVal.cityCode, newVal.districtCode],
      provinceCode: newVal.provinceCode,
      provinceName: newVal.provinceName,
      cityCode: newVal.cityCode,
      cityName: newVal.cityName,
      districtCode: newVal.districtCode,
      districtName: newVal.districtName,
      detail: newVal.detail,
      label: newVal.label || '家',
      longitude: newVal.longitude,
      latitude: newVal.latitude
    })
  }
}, { immediate: true })

// 地区选择变化
const handleRegionChange = (value) => {
  if (value && value.length === 3) {
    // 从 regionData 中获取名称
    const province = regionOptions.find(p => p.value === value[0])
    const city = province?.children?.find(c => c.value === value[1])
    const district = city?.children?.find(d => d.value === value[2])

    form.provinceCode = value[0]
    form.provinceName = province?.label
    form.cityCode = value[1]
    form.cityName = city?.label
    form.districtCode = value[2]
    form.districtName = district?.label
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    loading.value = true
    
    const data = {
      id: form.id,
      consignee: form.consignee,
      sex: form.sex,
      phone: form.phone,
      provinceCode: form.provinceCode,
      provinceName: form.provinceName,
      cityCode: form.cityCode,
      cityName: form.cityName,
      districtCode: form.districtCode,
      districtName: form.districtName,
      detail: form.detail,
      label: form.label,
      longitude: form.longitude,
      latitude: form.latitude
    }

    if (isEdit.value) {
      await updateAddress(data)
      ElMessage.success('修改成功')
    } else {
      await addAddress(data)
      ElMessage.success('添加成功')
    }

    emit('success')
    handleClose()
  } catch (error) {
    console.error('保存地址失败：', error)
  } finally {
    loading.value = false
  }
}

// 关闭弹窗
const handleClose = () => {
  formRef.value?.resetFields()
  visible.value = false
}
</script>
