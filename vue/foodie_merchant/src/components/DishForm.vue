<template>
  <el-dialog
    :model-value="visible"
    :title="title"
    width="800px"
    @close="handleClose"
    destroy-on-close
    append-to-body
    :close-on-click-modal="false"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="dish-form">
      <!-- 基本信息 -->
      <h3 class="section-title">基本信息</h3>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="菜品名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入菜品名称" maxlength="20" show-word-limit />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="菜品分类" prop="categoryId">
            <el-select v-model="form.categoryId" placeholder="请选择菜品分类" style="width: 100%">
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
          <el-form-item label="菜品价格" prop="price">
            <el-input-number 
              v-model="form.price" 
              :precision="2" 
              :step="0.1" 
              :min="0.01" 
              :max="10000" 
              style="width: 100%"
              placeholder="请输入价格"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="售卖状态" prop="status">
             <el-radio-group v-model="form.status">
                <el-radio :label="1">起售</el-radio>
                <el-radio :label="0">停售</el-radio>
             </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 图片上传 -->
       <el-form-item label="菜品图片" prop="image">
         <div class="upload-container">
            <el-upload
              class="avatar-uploader"
              :action="uploadUrl"
              :show-file-list="false"
              :headers="headers"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
              drag
            >
              <img v-if="form.image" :src="form.image" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              
              <template #tip>
                  <div class="el-upload__tip">
                    只能上传 jpg/png 文件，且不超过 2MB。建议尺寸 800x800
                  </div>
              </template>
            </el-upload>
         </div>
      </el-form-item>

      <el-form-item label="菜品描述" prop="description">
        <el-input 
          v-model="form.description" 
          type="textarea" 
          rows="3" 
          placeholder="请输入菜品描述，让顾客更了解这道菜"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <!-- 口味配置 -->
      <h3 class="section-title">口味配置</h3>
      <div class="flavor-wrapper">
        <el-form-item prop="flavors" label-width="0">
           <FlavorConfig v-model="form.flavors" />
        </el-form-item>
      </div>

    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="loading">保存</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import FlavorConfig from '@/components/FlavorConfig.vue';
import dishApi from '@/api/dish';
import categoryApi from '@/api/category';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  isEdit: {
    type: Boolean,
    default: false
  },
  dishId: {
    type: [Number, String],
    default: undefined
  }
});

const emit = defineEmits(['update:visible', 'success']);

// --- State ---
const loading = ref(false);
const formRef = ref(null);
const categoryList = ref([]);
const uploadUrl = 'http://localhost:8082/merchant/common/upload'; // 假设通用上传接口
const headers = computed(() => {
  return {
    Authorization: 'Bearer ' + localStorage.getItem('merchant_token')
  }
});

const form = reactive({
  id: undefined,
  name: '',
  categoryId: undefined,
  price: undefined,
  image: '',
  description: '',
  status: 1, // 默认为起售
  flavors: []
});

const rules = {
  name: [
    { required: true, message: '请输入菜品名称', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择菜品分类', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' }
  ],
  image: [
    { required: true, message: '请上传菜品图片', trigger: 'change' }
  ]
};

const title = computed(() => props.isEdit ? '编辑菜品' : '新增菜品');

// --- Watchers ---
watch(
  () => props.visible,
  async (val) => {
    if (val) {
      await getCategoryList();
      if (props.isEdit && props.dishId) {
        await getDishDetail(props.dishId);
      } else {
        resetForm();
      }
    }
  }
);

// --- Methods ---

const resetForm = () => {
  form.id = undefined;
  form.name = '';
  form.categoryId = undefined;
  form.price = undefined;
  form.image = '';
  form.description = '';
  form.status = 1;
  form.flavors = [];
  if (formRef.value) formRef.value.clearValidate();
};

const getCategoryList = async () => {
  try {
    const res = await categoryApi.getCategoryList(1); // 1 for Dish type
    categoryList.value = res.data || [];
  } catch (error) {
    console.error('Fetch categories failed', error);
  }
};

const getDishDetail = async (id) => {
  loading.value = true;
  try {
    const res = await dishApi.getDishById(id);
    if (res.data) {
      const data = res.data;
      Object.assign(form, data);
      
      // Ensure flavors is parsed if backend returns string in value, 
      // but usually backend for detail returns objects. 
      // Assuming backend returns: flavors: [{name: '...', value: '["a","b"]'}, ...]
      // We need to consistency for FlavorConfig which expects value as Array.
      // But FlavorConfig handles string parse in watch prop. 
      // Let's check FlavorConfig implementation. 
      // Yes, FlavorConfig parses string to array in watch.
      // However, if the backend returns the "flavors" list, we should make sure it is assigned to form.flavors correctly.
      
    }
  } catch (error) {
    ElMessage.error('获取详情失败');
  } finally {
    loading.value = false;
  }
};

const handleClose = () => {
  emit('update:visible', false);
};

const handleAvatarSuccess = (response, uploadFile) => {
  // Assuming response structure: { code: 1, data: 'url...', msg: '' }
  // or { code: 200, data: 'url...' }
  if (response.code === 1 || response.code === 200) {
     form.image = response.data;
     if (formRef.value) formRef.value.validateField('image');
  } else {
     ElMessage.error('上传失败: ' + response.msg);
  }
};

const beforeAvatarUpload = (rawFile) => {
  const isImage = rawFile.type === 'image/jpeg' || rawFile.type === 'image/png';
  const isLt2M = rawFile.size / 1024 / 1024 < 2;

  if (!isImage) {
    ElMessage.error('上传头像图片只能是 JPG/PNG 格式!');
    return false;
  }
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!');
    return false;
  }
  return true;
};

const submitForm = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        // Deep copy to avoid modifying reactive form state used by UI
        const payload = JSON.parse(JSON.stringify(form));
        
        // Transform flavors value (Array -> JSON String) for backend
        if (payload.flavors && payload.flavors.length > 0) {
            payload.flavors.forEach(f => {
                if (Array.isArray(f.value)) {
                    f.value = JSON.stringify(f.value);
                }
            });
        }
        
        if (props.isEdit) {
            await dishApi.updateDish(payload);
            ElMessage.success('更新成功');
        } else {
            await dishApi.addDish(payload);
            ElMessage.success('新增成功');
        }
        emit('success');
        handleClose();
      } catch (error) {
        console.error(error);
      } finally {
        loading.value = false;
      }
    }
  });
};
</script>

<style scoped>
.section-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 20px;
    padding-bottom: 10px;
    border-bottom: 1px solid #ebeef5;
}
.dish-form {
    max-height: 600px;
    overflow-y: auto;
    padding-right: 10px;
}
/* Scrollbar styling */
.dish-form::-webkit-scrollbar {
  width: 6px;
}
.dish-form::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 4px;
}

.upload-container {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    width: 200px; 
    height: 200px; /* Force square for consistency */
    display: flex;
    justify-content: center;
    align-items: center;
    transition: var(--el-transition-duration-fast);
}
.upload-container:hover {
    border-color: var(--el-color-primary);
}
.avatar-uploader .el-upload {
  width: 100%;
  height: 100%;
}
.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover; /* Cover mode to look good */
  display: block;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100%;
  height: 100%;
  text-align: center;
  display: flex !important;
  align-items: center;
  justify-content: center;
}
:deep(.el-upload-dragger) {
    width: 100%;
    height: 100%;
    border: none;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    padding: 0;
}
</style>
