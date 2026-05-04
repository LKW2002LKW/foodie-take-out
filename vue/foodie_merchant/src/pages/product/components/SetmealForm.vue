<template>
  <el-dialog
    :model-value="visible"
    :title="title"
    width="900px"
    top="5vh"
    destroy-on-close
    append-to-body
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="setmeal-form">
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
        <el-button type="primary" :loading="loading" @click="submitForm">保存</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { Plus } from '@element-plus/icons-vue'
import DishSelector from '@/components/DishSelector.vue'
import { useSetmealForm } from '@/composables/business/useSetmealForm'

const props = defineProps({
  visible: { type: Boolean, default: false },
  isEdit: { type: Boolean, default: false },
  setmealId: { type: [Number, String], default: undefined },
})

const emit = defineEmits(['update:visible', 'success'])

const {
  beforeAvatarUpload,
  categoryList,
  form,
  formRef,
  handleAvatarSuccess,
  handleClose,
  headers,
  loading,
  originTotalPrice,
  rules,
  submitForm,
  title,
  uploadUrl,
} = useSetmealForm(props, emit)
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
