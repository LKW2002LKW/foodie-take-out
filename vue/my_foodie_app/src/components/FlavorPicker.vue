<template>
  <van-popup
    :show="show"
    @update:show="$emit('update:show', $event)"
    position="bottom"
    round
    class="flavor-picker"
    @close="onClose"
  >
    <div class="picker-container" v-if="food">
      <div class="food-header">
        <van-image :src="food.image" width="8.4rem" height="8.4rem" radius="0.8rem" fit="cover" />
        <div class="food-info">
          <div class="name">{{ food.name }}</div>
          <div class="selected-text" v-if="selectedText">已选：{{ selectedText }}</div>
          <div class="price">
             <span class="symbol">￥</span>
             <span class="val">{{ food.price }}</span>
          </div>
        </div>
      </div>

      <div class="flavor-list">
        <div v-for="flavor in food.flavors" :key="flavor.id" class="flavor-section">
          <div class="flavor-title">{{ flavor.name }}</div>
          <div class="flavor-options">
            <div
              v-for="opt in parseFlavorValue(flavor.value)"
              :key="opt"
              class="flavor-option"
              :class="{ active: selections[flavor.name] === opt }"
              @click="onSelect(flavor.name, opt)"
            >
              {{ opt }}
            </div>
          </div>
        </div>
      </div>

      <div class="picker-footer safe-area-bottom">
        <div class="footer-left">
          <span class="symbol">￥</span>
          <span class="total">{{ food.price }}</span>
        </div>
        <van-button 
          round 
          type="primary" 
          class="add-btn" 
          @click="onConfirm"
          :disabled="!isAllSelected"
        >
          加入购物车
        </van-button>
      </div>
    </div>
  </van-popup>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  show: Boolean,
  food: Object
})

const emit = defineEmits(['update:show', 'confirm'])

const selections = ref({})

const parseFlavorValue = (val) => {
  if (Array.isArray(val)) return val
  try {
    return JSON.parse(val)
  } catch (e) {
    return []
  }
}

const selectedText = computed(() => {
  return Object.values(selections.value).filter(Boolean).join('/')
})

const isAllSelected = computed(() => {
  if (!props.food?.flavors) return true
  return props.food.flavors.every(f => selections.value[f.name])
})

const onSelect = (name, val) => {
  selections.value[name] = val
}

const onConfirm = () => {
  const flavorRes = Object.entries(selections.value).map(([name, value]) => ({
    name,
    value
  }))
  emit('confirm', flavorRes)
  emit('update:show', false)
}

const onClose = () => {
  selections.value = {}
}

// Default select first options
watch(() => props.food, (newFood) => {
  if (newFood?.flavors) {
    newFood.flavors.forEach(f => {
      const opts = parseFlavorValue(f.value)
      if (opts.length > 0) {
        selections.value[f.name] = opts[0]
      }
    })
  }
}, { immediate: true })

</script>

<style scoped>
.flavor-picker {
  max-height: 80vh;
}
.picker-container {
  background: linear-gradient(180deg, #fffdf7 0%, #fff6df 100%);
  padding: 2rem 1.6rem;
  display: flex;
  flex-direction: column;
}
.food-header {
  display: flex;
  gap: 1.2rem;
  margin-bottom: 2rem;
  padding: 0.4rem;
  border-radius: 1.6rem;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.92) 0%, rgba(255, 248, 228, 0.96) 100%);
  box-shadow: 0 0.6rem 1.6rem rgba(245, 194, 0, 0.08);
}
.food-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.food-info .name {
  font-size: 1.8rem;
  font-weight: 800;
  color: var(--mt-strong);
}
.selected-text {
  font-size: 1.2rem;
  color: var(--mt-muted);
  margin-top: 0.4rem;
}
.price {
  color: var(--van-danger-color);
  font-weight: 800;
  margin-top: auto;
}
.price .symbol {
  font-size: 1.2rem;
}
.price .val {
  font-size: 2.2rem;
}

.flavor-list {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 2.4rem;
}
.flavor-section {
  margin-bottom: 2rem;
  padding: 1.4rem;
  border-radius: 1.4rem;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(245, 194, 0, 0.12);
}
.flavor-title {
  font-size: 1.4rem;
  font-weight: 700;
  color: var(--mt-strong);
  margin-bottom: 1rem;
}
.flavor-options {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
}
.flavor-option {
  padding: 0.7rem 1.6rem;
  background: linear-gradient(180deg, #fffefb 0%, #fff7df 100%);
  border-radius: 999px;
  font-size: 1.3rem;
  color: var(--mt-strong);
  transition: all 0.2s;
  border: 1px solid rgba(245, 194, 0, 0.14);
}
.flavor-option.active {
  background: linear-gradient(180deg, #ffe593 0%, #ffd457 100%);
  color: var(--mt-strong);
  border-color: #f5c200;
  font-weight: 700;
  box-shadow: 0 0.4rem 0.9rem rgba(245, 194, 0, 0.18);
}

.picker-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid rgba(245, 194, 0, 0.14);
  padding-top: 1.6rem;
}
.footer-left {
  color: var(--van-danger-color);
  font-weight: 800;
}
.footer-left .symbol { font-size: 1.4rem; }
.footer-left .total { font-size: 2.4rem; }

.add-btn {
  padding: 0 3.2rem;
  height: 4.4rem;
  font-weight: 800;
  font-size: 1.5rem;
  color: var(--mt-strong);
  border: 1px solid rgba(212, 156, 16, 0.16);
  box-shadow: inset 0 0.1rem 0 rgba(255, 255, 255, 0.4), 0 0.5rem 1.1rem rgba(245, 194, 0, 0.2);
  --van-button-primary-background: linear-gradient(180deg, #ffe27f 0%, #f5c200 100%);
  --van-button-primary-border-color: rgba(212, 156, 16, 0.16);
  --van-button-primary-color: var(--mt-strong);
}

.add-btn :deep(.van-button__text) {
  color: var(--mt-strong);
}

.add-btn.van-button--disabled {
  opacity: 0.55;
}
</style>
