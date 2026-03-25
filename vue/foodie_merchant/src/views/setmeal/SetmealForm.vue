<template>
  <el-dialog
    :model-value="visible"
    :title="title"
    width="900px"
    @close="handleClose"
    destroy-on-close
    append-to-body
    :close-on-click-modal="false"
    top="5vh"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="setmeal-form">
      <!-- 基本信息 -->
      <h3 class="section-title">基本信息</h3>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="套餐名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入套餐名称 (不可重复)" maxlength="20" show-word-limit />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="套餐分类" prop="categoryId">
            <el-select v-model="form.categoryId" placeholder="请选择套餐分类" style="width: 100%">
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
          <el-form-item label="套餐价格" prop="price">
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
          <el-form-item label="套餐图片" prop="image">
            <!-- Reuse simplistic upload logic or component -->
             <div class="upload-container">
                <el-upload
                  class="avatar-uploader"
                  :action="uploadUrl"
                  :show-file-list="false"
                  :headers="headers"
                  :on-success="handleAvatarSuccess"
                  :before-upload="beforeAvatarUpload"
                >
                  <img v-if="form.image" :src="form.image" class="avatar" />
                  <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                </el-upload>
             </div>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="套餐描述" prop="description">
        <el-input 
          v-model="form.description" 
          type="textarea" 
          rows="2" 
          maxlength="200"
          show-word-limit
          placeholder="请输入套餐描述"
        />
      </el-form-item>

      <!-- 菜品选择 -->
      <h3 class="section-title">
          <span>套餐菜品</span>
          <span class="price-summary">
             ( 原价总计: <span class="origin-price">￥{{ originTotalPrice }}</span>
               <span class="divider">/</span>
               套餐价: <span class="current-price">￥{{ form.price || 0 }}</span>
               <span class="divider">/</span>
               优惠: <span class="discount-price">￥{{ (originTotalPrice - (form.price || 0)).toFixed(2) }}</span> )
          </span>
      </h3>
      
      <el-form-item prop="setmealDishes" label-width="0">
        <DishSelector v-model="form.setmealDishes" />
      </el-form-item>

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
import DishSelector from '@/components/DishSelector.vue';
import setmealApi from '@/api/setmeal';
import categoryApi from '@/api/category';

const props = defineProps({
  visible: { type: Boolean, default: false },
  isEdit: { type: Boolean, default: false },
  setmealId: { type: [Number, String], default: undefined }
});

const emit = defineEmits(['update:visible', 'success']);

// --- State ---
const loading = ref(false);
const formRef = ref(null);
const categoryList = ref([]);
const uploadUrl = 'http://localhost:8082/merchant/common/upload';
const headers = computed(() => ({
    Authorization: 'Bearer ' + localStorage.getItem('merchant_token')
}));

const form = reactive({
  id: undefined,
  name: '',
  categoryId: undefined,
  price: undefined,
  image: '',
  description: '',
  status: 1,
  setmealDishes: [] // [{dishId, name, price, copies, image?}]
});

// 计算原价
const originTotalPrice = computed(() => {
    if (!form.setmealDishes || form.setmealDishes.length === 0) return '0.00';
    const total = form.setmealDishes.reduce((acc, item) => {
        return acc + (item.price * item.copies);
    }, 0);
    return total.toFixed(2);
});

const rules = {
  name: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  image: [{ required: true, message: '请上传图片', trigger: 'change' }],
  setmealDishes: [
      { required: true, message: '请添加至少一个菜品', trigger: 'change' },
      { 
          validator: (rule, value, callback) => {
              if (!value || value.length === 0) {
                  callback(new Error('请添加至少一个菜品'));
              } else {
                  callback();
              }
          }, 
          trigger: 'change' 
      }
  ]
};

const title = computed(() => props.isEdit ? '编辑套餐' : '新增套餐');

// --- Watchers ---
watch(
  () => props.visible,
  async (val) => {
    if (val) {
      await getCategoryList();
      if (props.isEdit && props.setmealId) {
        await getDetail(props.setmealId);
      } else {
        resetForm();
      }
    }
  }
);

// --- Methods ---
const resetForm = () => {
    Object.assign(form, {
        id: undefined,
        name: '',
        categoryId: undefined,
        price: undefined,
        image: '',
        description: '',
        status: 1,
        setmealDishes: []
    });
    if (formRef.value) formRef.value.clearValidate();
};

const getCategoryList = async () => {
    try {
        const res = await categoryApi.getCategoryList(2); // 2 for Setmeal type
        categoryList.value = res.data || [];
    } catch (e) { console.error(e); }
};

const getDetail = async (id) => {
    loading.value = true;
    try {
        const res = await setmealApi.getSetmealById(id);
        if (res.data) {
            Object.assign(form, res.data);
            // Ensure data types if needed
        }
    } catch (e) {
        ElMessage.error('加载失败');
    } finally {
        loading.value = false;
    }
};

const handleAvatarSuccess = (res) => {
    if (res.code === 1 || res.code === 200) {
        form.image = res.data;
        if (formRef.value) formRef.value.validateField('image');
    } else {
        ElMessage.error(res.msg || '上传失败');
    }
};

const beforeAvatarUpload = (file) => {
    const isImg = file.type === 'image/jpeg' || file.type === 'image/png';
    const isLt2M = file.size / 1024 / 1024 < 2;
    if (!isImg) ElMessage.error('格式错误');
    if (!isLt2M) ElMessage.error('大小超过2MB');
    return isImg && isLt2M;
};

const handleClose = () => {
    emit('update:visible', false);
};

const submitForm = async () => {
    if (!formRef.value) return;
    await formRef.value.validate(async (valid) => {
        if (valid) {
            loading.value = true;
            try {
                const payload = { ...form };
                // Ensure dishes have required fields only if needed, but backend needs basic info
                // Payload structure matches requirement
                if (props.isEdit) {
                    await setmealApi.updateSetmeal(payload);
                    ElMessage.success('更新成功');
                } else {
                    await setmealApi.addSetmeal(payload);
                    ElMessage.success('新增成功');
                }
                emit('success');
                handleClose();
            } catch (e) {
                console.error(e);
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
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.setmeal-form {
    max-height: 650px;
    overflow-y: auto;
    padding-right: 15px;
}
.price-summary {
    font-size: 13px;
    font-weight: normal;
    color: #909399;
}
.origin-price {
    text-decoration: line-through;
    color: #909399;
}
.current-price {
    font-size: 16px;
    font-weight: bold;
    color: #f56c6c;
}
.discount-price {
    color: #67c23a;
    font-weight: bold;
}
.divider {
    margin: 0 5px;
    color: #dcdfe6;
}

/* Upload Style Reuse */
.upload-container {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    width: 150px; 
    height: 150px;
    display: flex;
    justify-content: center;
    align-items: center;
}
.avatar { width: 100%; height: 100%; object-fit: cover; }
.avatar-uploader-icon { font-size: 28px; color: #8c939d; }
</style>
