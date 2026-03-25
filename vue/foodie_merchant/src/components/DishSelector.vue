<template>
  <div class="dish-selector">
    <el-row :gutter="20" style="height: 100%;">
      <!-- 左侧：菜品选择 -->
      <el-col :span="14" style="height: 100%;">
        <el-card shadow="never" class="selector-card left-card">
          <template #header>
            <div class="card-header">
              <span>可选菜品</span>
              <el-input
                v-model="searchQuery"
                placeholder="搜索菜品"
                prefix-icon="Search"
                clearable
                style="width: 150px;"
                size="small"
              />
            </div>
          </template>
          
          <div class="filter-bar">
             <el-scrollbar>
               <div class="category-tags">
                 <el-check-tag 
                    :checked="activeCategoryId === 0" 
                    @change="handleCategoryChange(0)"
                    class="filter-tag"
                 >全部</el-check-tag>
                 <el-check-tag 
                    v-for="cat in categoryList" 
                    :key="cat.id" 
                    :checked="activeCategoryId === cat.id"
                    @change="handleCategoryChange(cat.id)"
                    class="filter-tag"
                 >{{ cat.name }}</el-check-tag>
               </div>
             </el-scrollbar>
          </div>

          <el-scrollbar wrap-class="dish-list-scroll">
            <div v-if="filteredDishList.length === 0" class="empty-text">暂无数据</div>
            <div 
                v-for="dish in filteredDishList" 
                :key="dish.id" 
                class="dish-item"
                @click="addDish(dish)"
            >
                <el-image :src="dish.image" class="dish-img" fit="cover" loading="lazy" />
                <div class="dish-info">
                    <div class="dish-name">{{ dish.name }}</div>
                    <div class="dish-meta">
                        <span class="dish-price">￥{{ Number(dish.price).toFixed(2) }}</span>
                        <el-tag size="small" type="info">{{ dish.categoryName }}</el-tag>
                    </div>
                    <div class="dish-desc" :title="dish.description">{{ dish.description }}</div>
                </div>
                <div class="dish-action">
                    <el-icon><Plus /></el-icon>
                </div>
            </div>
          </el-scrollbar>
        </el-card>
      </el-col>

      <!-- 右侧：已选列表 -->
      <el-col :span="10" style="height: 100%;">
        <el-card shadow="never" class="selector-card right-card">
           <template #header>
            <div class="card-header">
              <span>已选菜品 ({{ modelValue.length }})</span>
              <el-button type="danger" link size="small" @click="clearAll">清空</el-button>
            </div>
          </template>
          
          <el-scrollbar>
             <div v-if="modelValue.length === 0" class="empty-text">请从左侧添加菜品</div>
             <div v-for="(item, index) in modelValue" :key="item.dishId" class="selected-item">
                <div class="selected-info">
                   <div class="name">{{ item.name }}</div>
                   <div class="price">￥{{ Number(item.price).toFixed(2) }} / 份</div>
                </div>
                <div class="selected-action">
                   <el-input-number 
                      v-model="item.copies" 
                      :min="1" 
                      :max="99" 
                      size="small" 
                      style="width: 90px;" 
                   />
                   <el-button type="danger" link icon="Delete" @click="removeDish(index)"></el-button>
                </div>
             </div>
          </el-scrollbar>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { Search, Plus, Delete } from '@element-plus/icons-vue';
import dishApi from '@/api/dish';
import categoryApi from '@/api/category';

// Props
const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['update:modelValue']);

// Local State
const searchQuery = ref('');
const activeCategoryId = ref(0);
const categoryList = ref([]);
const rawDishList = ref([]);

// Lifecycle
onMounted(async () => {
   await loadCategories();
   await loadDishes();
});

// Logic
const loadCategories = async () => {
    try {
        const res = await categoryApi.getCategoryList(1); // 1 for Dish type
        categoryList.value = res.data || [];
    } catch (e) { console.error(e); }
};

const loadDishes = async () => {
    try {
        // Fetch all dishes roughly. Or page based?
        // Usually for selector, fetch all available dishes or implement infinite scroll.
        // Assuming fetch list by params.
        const res = await dishApi.getDishPage({ page: 1, pageSize: 1000, status: 1 }); // Only active dishes
        if (res.data) {
            rawDishList.value = res.data.records || [];
        }
    } catch (e) { console.error(e); }
};

const processedDishList = computed(() => {
    // 1. Map category names if needed, or already in row
    return rawDishList.value;
});

const filteredDishList = computed(() => {
    return processedDishList.value.filter(dish => {
        const matchName = dish.name.toLowerCase().includes(searchQuery.value.toLowerCase());
        const matchCat = activeCategoryId.value === 0 || dish.categoryId === activeCategoryId.value;
        return matchName && matchCat;
    });
});

const handleCategoryChange = (id) => {
    activeCategoryId.value = id;
};

const addDish = (dish) => {
    const exists = props.modelValue.find(i => i.dishId === dish.id);
    if (exists) {
        exists.copies++;
    } else {
        // Create new setmeal dish object
        const newSetmealDish = {
            dishId: dish.id,
            name: dish.name,   // Redundant but required
            price: dish.price, // Redundant but required
            copies: 1,
            image: dish.image // Optional for display
        };
        // Emit update
        emit('update:modelValue', [...props.modelValue, newSetmealDish]);
    }
};

const removeDish = (index) => {
    const list = [...props.modelValue];
    list.splice(index, 1);
    emit('update:modelValue', list);
};

const clearAll = () => {
    emit('update:modelValue', []);
};

</script>

<style scoped>
.dish-selector {
  height: 400px; /* Fixed height for scroll content */
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #f5f7fa;
  padding: 10px;
}
.selector-card {
  height: 100%;
  border: none;
  display: flex;
  flex-direction: column;
}
:deep(.el-card__body) {
    padding: 0;
    flex: 1;
    overflow: hidden;
    position: relative;
    height: 100%; /* Important for internal scrollbar */
}
:deep(.el-card__header) {
    padding: 10px 15px;
    border-bottom: 1px solid #ebeef5;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  font-weight: 500;
}
.filter-bar {
  padding: 8px 10px;
  border-bottom: 1px dashed #ebeef5;
  background: #fff;
}
.category-tags {
  display: flex;
  gap: 8px;
  white-space: nowrap;
}
.filter-tag {
  cursor: pointer;
}
/* Dish List Left */
.dish-item {
    display: flex;
    padding: 10px;
    border-bottom: 1px solid #f0f2f5;
    cursor: pointer;
    transition: background 0.2s;
}
.dish-item:hover {
    background-color: #ecf5ff;
}
.dish-img {
    width: 60px;
    height: 60px;
    border-radius: 4px;
    flex-shrink: 0;
    margin-right: 10px;
    background: #f5f7fa;
}
.dish-info {
    flex: 1;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
}
.dish-name {
    font-size: 14px;
    font-weight: bold;
    color: #303133;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
.dish-meta {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 4px;
}
.dish-price {
    color: #f56c6c;
    font-weight: bold;
}
.dish-desc {
    font-size: 12px;
    color: #909399;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    margin-top: 4px;
}
.dish-action {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 30px;
    color: var(--el-color-primary);
}

/* Selected List Right */
.selected-item {
    padding: 10px 15px;
    border-bottom: 1px solid #f0f2f5;
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: #fff;
}
.selected-info {
    flex: 1;
}
.selected-info .name {
    font-size: 14px;
    color: #303133;
}
.selected-info .price {
    font-size: 12px;
    color: #909399;
    margin-top: 2px;
}
.selected-action {
    display: flex;
    align-items: center;
    gap: 10px;
}
.empty-text {
    padding: 20px;
    text-align: center;
    color: #909399;
    font-size: 13px;
}
</style>
