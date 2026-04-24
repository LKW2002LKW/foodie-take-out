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
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getNoticeDetail } from '../../api/notice';
import { showToast } from 'vant';

const route = useRoute();
const router = useRouter();
const id = route.params.id;

const detail = ref(null);
const loading = ref(true);

const onClickLeft = () => router.back();

const getTagType = (type) => {
    if (type === 1) return 'primary';
    if (type === 2) return 'warning';
    return 'default';
};

const formattedContent = computed(() => {
    if(!detail.value?.content) return '';
    // Preserve newlines, maybe auto-linkify in future.
    // Replace \n with <br> for HTML rendering if using v-html, OR use white-space: pre-wrap and {{}}
    // Using v-html allows for more flexibility if backend sends rich text later, but implies risk.
    // For now, let's stick to safe text with white-space style, but user might want clickable links? 
    // The previous implementation used standard text. Let's start with standard text + pre-wrap.
    // Actually, let's just return the content and use style to handle newlines.
    return detail.value.content.replace(/\n/g, '<br/>');
});

const loadData = async () => {
    try {
        const res = await getNoticeDetail(id);
        // Accept both 200 and 1
        if (res.code === 200 || res.code === 1) {
            const d = res.data;
            // Check permissions locally as per requirements if backend doesn't enforced it?
            // "Only show target 2 or 3". "Only show status 1".
            // Use loose check (==) to handle string/number mismatch
            if ((d.target == 2 || d.target == 3) && d.status == 1) {
                detail.value = d;
            } else {
               showToast('无权查看或该公告未发布');
               detail.value = null;
            }
        } else {
            showToast(res.msg || '获取详情失败');
        }
    } catch (e) {
        showToast('网络错误');
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    if (id) {
        loadData();
    } else {
        loading.value = false;
    }
});
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