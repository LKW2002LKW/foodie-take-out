<template>
    <div class="notice-detail-page mobile-page">
    <van-nav-bar title="公告详情" left-arrow fixed placeholder @click-left="onClickLeft" />

    <div v-if="loading" class="loading-box">
        <van-loading vertical>加载中...</van-loading>
    </div>

    <div v-else-if="detail" class="detail-container">
        <h1 class="n-title">{{ detail.title }}</h1>
        <div class="n-meta">
            <van-tag :type="getTagType(detail.type)" plain class="n-tag">{{ detail.typeName }}</van-tag>
            <span class="n-time">{{ detail.publishTime }}</span>
        </div>
        <van-divider />
        <div class="n-content" v-html="formattedContent"></div>
    </div>
    
    <van-empty v-else description="公告加载失败或不存在" />
  </div>
</template>

<script setup>
import { useNoticeDetailPage } from '@/composables/business/useNoticeDetailPage'

const {
  detail,
  formattedContent,
  getTagType,
  loading,
  onClickLeft,
} = useNoticeDetailPage()
</script>

<style scoped>
.notice-detail-page {
    min-height: 100vh;
    background: var(--mt-page-bg);
}
.loading-box {
    padding-top: 10rem;
    text-align: center;
}
.detail-container {
    padding: 2rem;
}
.n-title {
    font-size: 2rem;
    font-weight: bold;
    color: var(--mt-strong);
    line-height: 1.4;
    margin-bottom: 1.2rem;
}
.n-meta {
    display: flex;
    align-items: center;
    margin-bottom: 1.6rem;
    color: var(--mt-muted);
    font-size: 1.2rem;
}
.n-tag {
    margin-right: 1rem;
}
.n-content {
    font-size: 1.5rem;
    color: var(--text-color);
    line-height: 1.8;
    text-align: justify;
    word-break: break-all;
}
</style>
