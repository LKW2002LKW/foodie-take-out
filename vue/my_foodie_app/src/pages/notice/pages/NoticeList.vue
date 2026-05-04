<template>
    <div class="notice-list-page mobile-page">
    <van-nav-bar title="消息通知" left-arrow fixed placeholder @click-left="onClickLeft" />
    
        <van-tabs :active="activeTab" @update:active="activeTab = $event" sticky @change="onTabChange" color="var(--primary-color)" title-active-color="var(--mt-strong)">
      <van-tab title="全部" :name="0"></van-tab>
      <van-tab title="系统公告" :name="1"></van-tab>
      <van-tab title="活动公告" :name="2"></van-tab>
    </van-tabs>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        :loading="loading"
        @update:loading="loading = $event"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
        <div v-if="filteredList.length > 0">
             <div 
                v-for="item in filteredList" 
                :key="item.id" 
                class="notice-item"
                @click="toDetail(item.id)"
            >
                <div class="notice-header">
                    <div class="notice-title-row">
                        <van-tag :type="getTagType(item.type)" mark class="notice-tag">
                            {{ item.typeName }}
                        </van-tag>
                        <span class="notice-title">{{ item.title }}</span>
                    </div>
                    <div class="notice-time">{{ formatTime(item.publishTime) }}</div>
                </div>
                <div class="notice-brief">
                    {{ getBrief(item.content) }}
                </div>
            </div>
        </div>
        <van-empty v-else-if="!loading" description="暂无公告" />
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { useNoticeListPage } from '@/composables/business/useNoticeListPage'

const {
  activeTab,
  filteredList,
  finished,
  formatTime,
  getBrief,
  getTagType,
  loading,
  onClickLeft,
  onLoad,
  onRefresh,
  onTabChange,
  refreshing,
  toDetail,
} = useNoticeListPage()
</script>

<style scoped>
.notice-list-page {
    min-height: 100vh;
    background: var(--mt-page-bg);
}
.notice-item {
    background: var(--mt-card-bg);
    margin: 1.2rem 1.2rem 0;
    padding: 1.6rem;
    border-radius: var(--mt-card-radius);
    display: flex;
    flex-direction: column;
    box-shadow: var(--shadow-sm);
}
.notice-item:last-child {
    margin-bottom: calc(1.2rem + env(safe-area-inset-bottom));
}
.notice-header {
    margin-bottom: 0.8rem;
}
.notice-title-row {
    display: flex;
    align-items: center;
    margin-bottom: 0.4rem;
}
.notice-tag {
    margin-right: 0.8rem;
    flex-shrink: 0;
}
.notice-title {
    font-size: 1.6rem;
    font-weight: bold;
    color: var(--mt-strong);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    flex: 1;
}
.notice-time {
    font-size: 1.2rem;
    color: var(--mt-muted);
    margin-top: 0.4rem;
}

.notice-brief {
    font-size: 1.3rem;
    color: var(--text-color-secondary);
    line-height: 1.5;
    margin-top: 0.4rem;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
}
</style>
