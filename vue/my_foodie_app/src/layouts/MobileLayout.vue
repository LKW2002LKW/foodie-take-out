<script setup>
import { useRoute } from 'vue-router'
import AppTabbar from './components/AppTabbar.vue'

const route = useRoute()
</script>

<template>
  <div class="app-container" :class="{ 'has-tabbar': route.meta.showTabbar }">
    <slot />
    <AppTabbar v-if="route.meta.showTabbar" />
  </div>
</template>

<style scoped>
.app-container {
  width: 100%;
  min-height: 100vh;
  min-height: 100dvh;
  display: flex;
  flex-direction: column;
  overflow-x: hidden;
}

.has-tabbar {
  padding-bottom: calc(5rem + env(safe-area-inset-bottom));
}
</style>
