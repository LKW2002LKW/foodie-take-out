<template>
  <div class="notice-list-page">
    <van-nav-bar title="消息通知" left-arrow fixed placeholder @click-left="onClickLeft" />
    
    <van-tabs v-model:active="activeTab" sticky @change="onTabChange" color="#FF9800" title-active-color="#FF9800">
      <van-tab title="全部" :name="0"></van-tab>
      <van-tab title="系统公告" :name="1"></van-tab>
      <van-tab title="活动公告" :name="2"></van-tab>
    </van-tabs>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
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
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { getNoticePage } from '../../api/notice';
import { showToast } from 'vant';

const router = useRouter();

const activeTab = ref(0); // 0=All, 1=System, 2=Activity
const list = ref([]);
const loading = ref(false);
const finished = ref(false);
const refreshing = ref(false);
const page = ref(1);
const pageSize = 10;

const onClickLeft = () => router.back();

const filteredList = computed(() => {
    // Client-side filtering as requested:
    // 1. Target: 2 (User) or 3 (All). 
    // 2. Status: 1 (Published).
    return list.value.filter(item => {
        const targetMatch = (item.target === 2 || item.target === 3);
        const statusMatch = (item.status === 1);
        return targetMatch && statusMatch;
    });
});

const getTagType = (type) => {
    // 1-System (Blue), 2-Activity (Orange)
    if (type === 1) return 'primary';
    if (type === 2) return 'warning';
    return 'default';
};

const getBrief = (content) => {
    if (!content) return '';
    // Strip newlines for brief
    const plain = content.replace(/\n/g, ' ');
    return plain.length > 40 ? plain.substring(0, 40) + '...' : plain;
};

const formatTime = (timeStr) => {
    if (!timeStr) return '';
    // "2026-01-20 10:00:00" -> "01-20 10:00"
    try {
        return timeStr.substring(5, 16);
    } catch(e) {
        return timeStr;
    }
};

const onLoad = async () => {
    if (refreshing.value) {
        list.value = [];
        refreshing.value = false;
    }

    try {
        const params = {
            page: page.value,
            pageSize: pageSize
        };
        // activeTab: 0 means no type filter (server might expect null for 'All')
        if (activeTab.value !== 0) {
            params.type = activeTab.value;
        }

        const res = await getNoticePage(params);
        // Accept both 200 and 1
        if (res.code === 200 || res.code === 1) {
            const rows = res.data.list || res.data.records || [];
            if (page.value === 1) {
                list.value = rows;
            } else {
                list.value = [...list.value, ...rows];
            }
            
            // Check if finished by comparing total or list size
            // If rows returned is less than pageSize, we are done
            const total = res.data.total || 0;
            if (rows.length < pageSize || list.value.length >= total) {
                finished.value = true;
            } else {
                page.value++;
            }
        } else {
            // Error code handling?
            finished.value = true;
        }
    } catch (e) {
        finished.value = true; // Stop infinite loading on error
    } finally {
        loading.value = false;
    }
};

const onRefresh = () => {
    finished.value = false;
    loading.value = true;
    page.value = 1;
    onLoad();
};

const onTabChange = () => {
    list.value = [];
    page.value = 1;
    finished.value = false;
    loading.value = true;
    onLoad();
};

const toDetail = (id) => {
    router.push(`/notice/${id}`);
};
</script>

<style scoped>
.notice-list-page {
    min-height: 100vh;
    background: #f7f8fa;
}
.notice-item {
    background: #fff;
    margin: 12px 12px 0;
    padding: 16px;
    border-radius: 8px;
    display: flex;
    flex-direction: column;
    box-shadow: 0 1px 4px rgba(0,0,0,0.02);
}
.notice-item:last-child {
    margin-bottom: 12px;
}
.notice-header {
    margin-bottom: 8px;
}
.notice-title-row {
    display: flex;
    align-items: center;
    margin-bottom: 4px;
}
.notice-tag {
    margin-right: 8px;
    flex-shrink: 0;
}
.notice-title {
    font-size: 16px;
    font-weight: bold;
    color: #333;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    flex: 1;
}
.notice-time {
    font-size: 12px;
    color: #999;
    margin-top: 4px;
}

.notice-brief {
    font-size: 13px;
    color: #666;
    line-height: 1.5;
    margin-top: 4px;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
}
</style>