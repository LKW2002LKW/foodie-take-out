<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import BlankLayout from '@/layouts/BlankLayout.vue'
import MobileLayout from '@/layouts/MobileLayout.vue'

const route = useRoute()

const layoutComponent = computed(() => {
  if (route.meta.layout === 'blank') {
    return BlankLayout
  }

  return MobileLayout
})
</script>

<template>
  <router-view v-slot="{ Component }">
    <component :is="layoutComponent">
      <transition name="mt-fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </component>
  </router-view>
</template>

<style scoped>
.mt-fade-enter-active,
.mt-fade-leave-active {
  transition: opacity 0.2s ease;
}

.mt-fade-enter-from,
.mt-fade-leave-to {
  opacity: 0;
}
</style>
