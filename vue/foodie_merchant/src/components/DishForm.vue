<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑菜品详情' : '新增美味菜品'"
    width="860px"
    class="dish-dialog"
    destroy-on-close
    append-to-body
    :close-on-click-modal="false"
    @close="$emit('update:visible', false)"
  >
    <el-form 
      ref="formRef" 
      :model="form" 
      :rules="rules" 
      label-width="100px" 
      label-position="top"
      class="dish-form"
    >
      <div class="dish-form__container">
        <!-- 左侧：图片上传 -->
        <div class="dish-form__aside">
          <el-form-item prop="image" label="菜品封面">
            <el-upload
              class="dish-form__uploader"
              :action="uploadUrl"
              :show-file-list="false"
              :headers="headers"
              :on-success="handleUploadSuccess"
              :before-upload="beforeUpload"
              drag
            >
              <img v-if="form.image" :src="form.image" class="dish-form__preview" />
              <div v-else class="dish-form__placeholder">
                <el-icon class="dish-form__upload-icon"><Plus /></el-icon>
                <span>点击或拖拽上传</span>
              </div>
            </el-upload>
            <p class="dish-form__tip">推荐尺寸 800x800，大小不超过 2MB</p>
          </el-form-item>
        </div>

        <!-- 右侧：详情表单 -->
        <div class="dish-form__main">
          <el-row :gutter="20">
            <el-col :span="14">
              <el-form-item label="菜品名称" prop="name">
                <el-input v-model="form.name" placeholder="请输入菜品名称" maxlength="20" show-word-limit />
              </el-form-item>
            </el-col>
            <el-col :span="10">
              <el-form-item label="所属分类" prop="categoryId">
                <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
                  <el-option
                    v-for="item in categoryList"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="售卖价格 (元)" prop="price">
                <el-input-number 
                  v-model="form.price" 
                  :precision="2" 
                  :step="1" 
                  :min="0" 
                  style="width: 100%"
                  controls-position="right"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="售卖状态" prop="status">
                <el-radio-group v-model="form.status" class="dish-form__radio-group">
                  <el-radio-button :label="1">立即起售</el-radio-button>
                  <el-radio-button :label="0">暂不上架</el-radio-button>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="菜品描述" prop="description">
            <el-input 
              v-model="form.description" 
              type="textarea" 
              :rows="3" 
              placeholder="请简单介绍菜品的特色、食材等信息..."
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </div>
      </div>

      <!-- 底部：口味配置 -->
      <div class="dish-form__flavors">
        <div class="dish-form__flavors-header">
          <h3 class="dish-form__section-title">口味定制选项</h3>
          <p class="dish-form__section-subtitle">顾客在下单时可以选择的个性化口味</p>
        </div>
        <el-form-item prop="flavors">
          <FlavorConfig v-model="form.flavors" />
        </el-form-item>
      </div>
    </el-form>

    <template #footer>
      <div class="dish-dialog__footer">
        <el-button @click="$emit('update:visible', false)">取消</el-button>
        <el-button 
          type="primary" 
          size="large"
          class="dish-dialog__submit"
          :loading="loading" 
          @click="onSubmit"
        >
          {{ isEdit ? '确认修改' : '立即创建' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import FlavorConfig from '@/components/FlavorConfig.vue'
import dishApi from '@/api/dish'
import categoryApi from '@/api/category'

const props = defineProps({
  visible: Boolean,
  isEdit: Boolean,
  dishId: [Number, String]
})

const emit = defineEmits(['update:visible', 'success'])

// 1. 基础状态
const loading = ref(false)
const formRef = ref(null)
const categoryList = ref([])
const uploadUrl = 'http://localhost:8082/merchant/common/upload' // 生产环境应从配置文件读取

const headers = computed(() => ({
  Authorization: 'Bearer ' + localStorage.getItem('merchant_token')
}))

const form = reactive({
  id: undefined,
  name: '',
  categoryId: undefined,
  price: undefined,
  image: '',
  description: '',
  status: 1,
  flavors: []
})

const rules = {
  name: [{ required: true, message: '菜品名称不能为空', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择所属分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入售卖价格', trigger: 'blur' }],
  image: [{ required: true, message: '请上传菜品封面', trigger: 'change' }]
}

// 2. 监听弹窗开启获取数据
watch(() => props.visible, async (val) => {
  if (val) {
    fetchCategories()
    if (props.isEdit && props.dishId) {
      fetchDetail(props.dishId)
    } else {
      resetForm()
    }
  }
})

// 3. 方法
const resetForm = () => {
  Object.assign(form, {
    id: undefined,
    name: '',
    categoryId: undefined,
    price: undefined,
    image: '',
    description: '',
    status: 1,
    flavors: []
  })
  formRef.value?.clearValidate()
}

const fetchCategories = async () => {
  const { data } = await categoryApi.getCategoryList(1)
  categoryList.value = data || []
}

const fetchDetail = async (id) => {
  loading.value = true
  try {
    const { data } = await dishApi.getDishById(id)
    if (data) {
      // 处理口味数据，确保 value 是数组
      if (data.flavors) {
        data.flavors = data.flavors.map(f => ({
          ...f,
          value: typeof f.value === 'string' ? JSON.parse(f.value) : f.value
        }))
      }
      Object.assign(form, data)
    }
  } catch (e) {
    ElMessage.error('加载菜品详情失败')
  } finally {
    loading.value = false
  }
}

const handleUploadSuccess = (res) => {
  if (res.code === 1) {
    form.image = res.data
    formRef.value?.validateField('image')
  } else {
    ElMessage.error('图片上传失败')
  }
}

const beforeUpload = (file) => {
  const isImg = ['image/jpeg', 'image/png'].includes(file.type)
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImg) ElMessage.error('只支持 JPG/PNG 格式')
  if (!isLt2M) ElMessage.error('图片大小不能超过 2MB')
  return isImg && isLt2M
}

const onSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const payload = JSON.parse(JSON.stringify(form))
        // 转换口味数据格式为后端要求的字符串
        payload.flavors?.forEach(f => {
          if (Array.isArray(f.value)) f.value = JSON.stringify(f.value)
        })
        
        if (props.isEdit) {
          await dishApi.updateDish(payload)
          ElMessage.success('更新成功')
        } else {
          await dishApi.addDish(payload)
          ElMessage.success('添加成功')
        }
        emit('success')
        emit('update:visible', false)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.dish-dialog :deep(.el-dialog__header) {
  padding: 24px;
  margin-right: 0;
  border-bottom: 1px solid #f1f5f9;
}

.dish-dialog :deep(.el-dialog__title) {
  font-weight: 700;
  color: #1e293b;
}

.dish-form {
  padding: 8px;
}

.dish-form__container {
  display: flex;
  gap: 32px;
  margin-bottom: 32px;
}

.dish-form__aside {
  width: 240px;
}

.dish-form__main {
  flex: 1;
}

.dish-form__uploader {
  width: 240px;
  height: 240px;
}

:deep(.el-upload-dragger) {
  padding: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px dashed #e2e8f0;
  border-radius: 12px;
  transition: all 0.3s;
}

:deep(.el-upload-dragger:hover) {
  border-color: #2563eb;
  background: #f8fafc;
}

.dish-form__preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 10px;
}

.dish-form__placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: #94a3b8;
}

.dish-form__upload-icon {
  font-size: 32px;
}

.dish-form__tip {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 12px;
  text-align: center;
}

.dish-form__section-title {
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 4px;
}

.dish-form__section-subtitle {
  font-size: 13px;
  color: #64748b;
  margin: 0 0 20px;
}

.dish-form__flavors {
  background: #f8fafc;
  padding: 24px;
  border-radius: 12px;
  border: 1px solid #f1f5f9;
}

.dish-dialog__footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 8px 12px;
}

.dish-dialog__submit {
  padding-left: 40px;
  padding-right: 40px;
  border-radius: 10px;
  font-weight: 600;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: #475569;
}
</style>
