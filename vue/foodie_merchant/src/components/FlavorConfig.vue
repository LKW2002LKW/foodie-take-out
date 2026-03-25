<template>
  <div class="flavor-config">
    <div v-for="(flavor, index) in flavorList" :key="index" class="flavor-item">
      <div class="flavor-header">
        <el-form-item 
          label="口味名称" 
          :prop="'flavors.' + index + '.name'"
          :rules="{ required: true, message: '请输入口味名称', trigger: 'blur' }"
          label-width="80px"
          style="margin-bottom: 0; flex: 1;"
        >
          <el-select 
            v-model="flavor.name" 
            filterable 
            allow-create 
            default-first-option
            placeholder="请输入或选择口味 (如：辣度)"
            @change="handleNameChange(flavor)"
          >
            <el-option
              v-for="item in preDefinedFlavors"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-button type="danger" link icon="Delete" @click="removeFlavor(index)">删除口味</el-button>
      </div>

      <el-form-item label="口味选项" label-width="80px" style="margin-top: 10px; margin-bottom: 0;">
        <div class="tag-container">
          <el-tag
            v-for="(tag, tagIndex) in flavor.value"
            :key="tagIndex"
            closable
            :disable-transitions="false"
            @close="removeTag(flavor, tagIndex)"
            class="flavor-tag"
          >
            {{ tag }}
          </el-tag>
          <el-select
            v-if="flavor.inputVisible"
            :ref="(el) => setInputRef(el, index)"
            v-model="flavor.inputValue"
            class="input-new-tag"
            size="small"
            filterable
            allow-create
            default-first-option
            placeholder="选/输"
            @change="handleInputConfirm(flavor)"
            @blur="handleInputConfirm(flavor)"
            @keyup.enter="handleInputConfirm(flavor)"
          >
            <el-option
              v-for="option in (flavorOptions[flavor.name] || [])"
              :key="option"
              :label="option"
              :value="option"
            />
          </el-select>
          <el-button v-else class="button-new-tag" size="small" @click="showInput(flavor, index)">
            + 新增选项
          </el-button>
        </div>
        <div class="form-tip">请输入选项后按回车确认 (如：微辣)</div>
      </el-form-item>
    </div>

    <el-button class="add-btn" type="primary" plain icon="Plus" @click="addFlavor">
      添加口味
    </el-button>
  </div>
</template>

<script setup>
import { ref, reactive, watch, nextTick, computed } from 'vue';
import { Delete, Plus } from '@element-plus/icons-vue';

// Props
const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['update:modelValue']);

// Local state to manage UI-specific flags (like inputVisible)
const flavorList = ref([]);
const inputRefs = ref({}); // Use object map for refs by index

// 预定义口味及其选项
const flavorOptions = reactive({
  '辣度': ['不辣', '微辣', '中辣', '重辣', '特辣'],
  '甜度': ['无糖', '少糖', '半糖', '多糖', '全糖'],
  '忌口': ['不要葱', '不要蒜', '不要香菜', '不要辣'],
  '温度': ['热饮', '常温', '去冰', '少冰', '多冰']
});

// 计算属性：获取所有预定义的口味名称
const preDefinedFlavors = computed(() => Object.keys(flavorOptions));

// Watch prop change to sync local state
watch(
  () => props.modelValue,
  (newVal) => {
    // 1. Convert NEW prop data to a comparable format (ensure values are arrays)
    const newBusinessData = newVal.map(item => ({
        name: item.name,
        value: Array.isArray(item.value) ? item.value : (typeof item.value === 'string' ? JSON.parse(item.value) : [])
    }));

    // 2. Convert CURRENT local business data to comparable format
    const currentBusinessData = flavorList.value.map(item => ({
        name: item.name,
        value: item.value
    }));

    // 3. Compare. If identical, DO NOT update local state.
    // This prevents the "Loop of Death" where local UI state (inputVisible) 
    // is reset because of a redundant round-trip update from the parent.
    if (JSON.stringify(newBusinessData) === JSON.stringify(currentBusinessData)) {
        return;
    }

    // 4. If data actually changed (e.g. initial load, or parent reset), sync it.
    // Try to preserve UI state (inputVisible, inputValue) for existing items by index.
    flavorList.value = newBusinessData.map((item, index) => {
      const oldItem = flavorList.value[index];
      return {
        ...item,
        inputVisible: oldItem ? oldItem.inputVisible : false,
        inputValue: oldItem ? oldItem.inputValue : ''
      };
    });
  },
  { immediate: true, deep: true }
);

// Watch local change to emit update
watch(
  flavorList,
  (newVal) => {
    const cleanList = newVal.map(item => ({
      name: item.name,
      value: item.value
    }));
    
    // Optional: Also check here if we really need to emit 
    // (avoid emitting just because inputVisible changed)
    // But since we fixed the Prop Watcher, the loop is broken anyway. 
    // Emitting redundant data is less harmful than resetting UI state.
    emit('update:modelValue', cleanList);
  },
  { deep: true }
);

// Actions
const addFlavor = () => {
  flavorList.value.push({
    name: '',
    value: [],
    inputVisible: false,
    inputValue: ''
  });
};

const removeFlavor = (index) => {
  flavorList.value.splice(index, 1);
};

const handleNameChange = (flavor) => {
  // 根据选择的口味名称，自动填充预定义的选项
  const options = flavorOptions[flavor.name];
  if (options) {
    // 如果存在预定义选项，则直接使用（复制一份防止引用污染）
    flavor.value = [...options];
  } else {
    // 如果是新输入的口味，先清空值
    flavor.value = [];
    // 立即为该新口味初始化一个空选项列表，确保后续 input focus 时能找到对应的 options（即使为空）
    if (flavor.name) {
       // 使用 Object.assign 或直接赋值触发响应式更新
       if (!flavorOptions[flavor.name]) {
           flavorOptions[flavor.name] = [];
       }
    }
  }
};

const removeTag = (flavor, tagIndex) => {
  flavor.value.splice(tagIndex, 1);
};

const setInputRef = (el, index) => {
    if (el) {
        inputRefs.value[index] = el;
    }
};

const showInput = (flavor, index) => {
  flavor.inputVisible = true;
  nextTick(() => {
    const input = inputRefs.value[index];
    if (input) {
        input.focus();
    }
  });
};

const handleInputConfirm = (flavor) => {
  if (flavor.inputValue) {
    const val = flavor.inputValue;
    if (!flavor.value.includes(val)) {
        flavor.value.push(val);
        
        // 需求：新增选项会同步到 flavorOptions
        if (flavor.name) {
             // 确保数组存在
             if (!flavorOptions[flavor.name]) {
                flavorOptions[flavor.name] = [];
             }
             if (!flavorOptions[flavor.name].includes(val)) {
                 flavorOptions[flavor.name].push(val);
             }
        }
    }
  }
  flavor.inputVisible = false;
  flavor.inputValue = '';
};

</script>

<style scoped>
.flavor-config {
  width: 100%;
}
.flavor-item {
  background-color: #f8f9fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 10px;
  border: 1px solid #e4e7ed;
}
.flavor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.tag-container {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}
.input-new-tag {
  width: 100px;
}
.form-tip {
  font-size: 12px;
  color: #909399;
  line-height: normal;
  margin-top: 5px;
}
.add-btn {
  width: 100%;
  border-style: dashed;
}
</style>
