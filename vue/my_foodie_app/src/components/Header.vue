<template>
  <header class="app-header">
    <van-nav-bar :title="title" left-arrow @click-left="onClickLeft" v-if="showNav" />
  </header>
</template>

<script setup>
import { defineProps } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  title: String,
  showNav: {
    type: Boolean,
    default: true
  }
})

const router = useRouter()
const onClickLeft = () => router.back()
</script>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
}
</style>
