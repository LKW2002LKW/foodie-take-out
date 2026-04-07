<template>
  <div class="home-page mobile-page">
    <header class="home-header">
      <h1 class="home-title">吃货联盟</h1>
      <p class="home-subtitle">发现附近好店和热门美食</p>
    </header>

    <div class="home-search">
      <van-search v-model="value" placeholder="请输入搜索关键词" />
    </div>

    <section class="home-banner">
      <van-swipe class="my-swipe" :autoplay="3000" indicator-color="white">
      <van-swipe-item>1</van-swipe-item>
      <van-swipe-item>2</van-swipe-item>
      <van-swipe-item>3</van-swipe-item>
      <van-swipe-item>4</van-swipe-item>
    </van-swipe>
    </section>

    <section class="home-grid">
      <van-grid>
      <van-grid-item icon="photo-o" text="文字" />
      <van-grid-item icon="photo-o" text="文字" />
      <van-grid-item icon="photo-o" text="文字" />
      <van-grid-item icon="photo-o" text="文字" />
    </van-grid>
    </section>

    <Footer />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import Footer from '../components/Footer.vue'

const value = ref('')
</script>
<style scoped>
.home-page {
  background: #f6f7fb;
  padding-bottom: calc(1.6rem + env(safe-area-inset-bottom));
}

.home-header {
  width: 100%;
  padding: 1.6rem 1.6rem 1.2rem;
}

.home-title {
  margin: 0;
  font-size: 2rem;
  line-height: 2.8rem;
  font-weight: 700;
  color: #222;
}

.home-subtitle {
  margin: 0.8rem 0 0;
  font-size: 1.3rem;
  line-height: 2rem;
  color: #636363;
}

.home-search,
.home-banner,
.home-grid {
  width: 100%;
  padding: 0 1.6rem;
}

.home-banner,
.home-grid {
  margin-top: 1.2rem;
}

:deep(.van-search) {
  padding: 0;
  background: transparent;
}

:deep(.van-search__content) {
  min-height: 4.4rem;
  border-radius: 1.2rem;
}

.my-swipe .van-swipe-item {
  color: #fff;
  font-size: 1.6rem;
  line-height: 15rem;
  text-align: center;
  background: linear-gradient(135deg, #5b8cff 0%, #4f46e5 100%);
  border-radius: 1.6rem;
}

:deep(.van-grid) {
  border-radius: 1.6rem;
  overflow: hidden;
}

:deep(.van-grid-item__content) {
  min-height: 8.8rem;
}

:deep(.van-grid-item__text) {
  font-size: 1.2rem;
}
</style>
