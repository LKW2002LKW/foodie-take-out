<template>
  <!-- 统一的“建设中”占位组件，用于尚未补齐业务实现的页面。 -->
  <div class="construction-state">
    <div class="construction-state__card">
      <h1 class="construction-state__title">{{ title }}</h1>
      <p class="construction-state__subtitle">{{ subtitle }}</p>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: '功能建设中',
  },
  subtitle: {
    type: String,
    default: '该模块正在按新架构整理，后续会继续补齐业务实现。',
  },
})
</script>

<style scoped>
.construction-state {
  min-height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.construction-state__card {
  width: min(560px, 100%);
  padding: 48px 32px;
  border-radius: 20px;
  background: linear-gradient(135deg, #fff7ed 0%, #ffffff 100%);
  border: 1px solid #fed7aa;
  box-shadow: 0 20px 45px rgba(249, 115, 22, 0.08);
  text-align: center;
}
.construction-state__title {
  margin: 0 0 12px;
  color: #9a3412;
  font-size: 28px;
  font-weight: 700;
}
.construction-state__subtitle {
  margin: 0;
  color: #7c2d12;
  font-size: 14px;
  line-height: 1.7;
}
</style>
