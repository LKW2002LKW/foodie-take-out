<template>
    <div class="order-list-page mobile-page">
    <van-nav-bar title="我的订单" left-arrow fixed placeholder @click-left="$router.back()" />
    
    <van-tabs :active="activeTab" sticky @change="onTabChange">
      <van-tab title="全部" :name="0"></van-tab>
      <van-tab title="待付款" :name="1"></van-tab>
      <van-tab title="待接单" :name="2"></van-tab>
      <van-tab title="已接单" :name="3"></van-tab>
      <van-tab title="已完成" :name="5"></van-tab>
      <!-- "配送中" usually doesn't have a separate tab for users but can be filtered or shown in All/Active -->
    </van-tabs>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
            :loading="loading"
            :finished="finished"
            finished-text="没有更多了"
            @load="onLoad"
            class="list-content"
        >
            <div v-for="item in list" :key="item.id" class="order-card" @click="toDetail(item)">
                <div class="card-header">
                    <span class="m-name">{{ item.merchantName || '店铺' }}</span>
                    <span class="status-tag" :class="getStatusClass(item.status)">{{ item.statusText }}</span>
                </div>
                <!-- Dish Preview (Show first 2 items) -->
                <div class="card-body">
                    <div class="dish-row" v-for="dish in (item.orderDetailList || []).slice(0, 2)" :key="dish.id">
                         <van-image :src="dish.image" width="4rem" height="4rem" radius="0.4rem" fit="cover" />
                         <div class="dish-name">{{ dish.name }}</div>
                         <div class="dish-count">x{{ dish.number }}</div>
                    </div>
                    <div class="more-tip" v-if="(item.orderDetailList || []).length > 2">
                        ... 等 {{ item.orderDetailCount }} 件商品
                    </div>
                </div>
                <div class="card-footer">
                    <div class="time">{{ item.orderTime }}</div>
                    <div class="total">
                        实付 <span class="price">￥{{ item.totalAmount }}</span>
                    </div>
                </div>
                <!-- Actions -->
                <div class="card-actions" v-if="[1,2,3,4,5].includes(item.status)">
                    <van-button 
                        v-if="item.status === 1" 
                        size="small" type="primary" plain round 
                        @click.stop="toPay(item)"
                    >去支付</van-button>
                    <van-button
                        v-if="item.canReview === true"
                        size="small"
                        type="primary"
                        plain
                        round
                        @click.stop="toReview(item)"
                    >去评价</van-button>
                </div>
            </div>
            <van-empty v-if="finished && list.length === 0" description="暂无订单" />
        </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { getOrderPage } from '../api/order';
import { showToast } from 'vant';

const router = useRouter();
const activeTab = ref(0);
const list = ref([]);
const loading = ref(false);
const finished = ref(false);
const refreshing = ref(false);
const page = ref(1);
const pageSize = 10;

const onTabChange = (name) => {
    activeTab.value = name;
    list.value = [];
    page.value = 1;
    finished.value = false;
    loading.value = true;
    onLoad();
};

const onLoad = async () => {
    try {
        if (refreshing.value) {
            list.value = [];
            refreshing.value = false;
        }

        const params = {
            page: page.value,
            pageSize: pageSize,
            status: activeTab.value === 0 ? undefined : activeTab.value
        };

        const res = await getOrderPage(params);
        if (res.code === 1) {
            const records = res.data.records || [];
            list.value.push(...records);
            
            // Check if finished
            if (records.length < pageSize || list.value.length >= res.data.total) {
                finished.value = true;
            } else {
                page.value++;
            }
        } else {
            finished.value = true;
        }
    } catch (e) {
        console.error(e);
        finished.value = true;
    } finally {
        loading.value = false;
    }
};

const onRefresh = () => {
    finished.value = false;
    page.value = 1;
    loading.value = true;
    onLoad();
};

const toDetail = (item) => {
    // Navigate using ID as verified in new API doc requirements
    router.push(`/order/${item.id}`);
};

const toPay = (item) => {
    router.push(`/order/${item.id}`);
};

const toReview = (item) => {
    router.push({ path: '/review/create', query: { orderId: item.id } });
};

const getStatusClass = (s) => {
    if (s === 1) return 's-orange';
    if (s === 5) return 's-gray';
    return 's-black';
};
</script>

<style scoped>
.order-list-page {
    background: #f7f8fa;
    min-height: 100vh;
}
.list-content {
    padding: 1.2rem;
}
.order-card {
    background: #fff;
    border-radius: 0.8rem;
    padding: 1.2rem 1.6rem;
    margin-bottom: 1.2rem;
}
.card-header {
    display: flex;
    justify-content: space-between;
    padding-bottom: 1.2rem;
    border-bottom: 1px solid #f9f9f9;
    margin-bottom: 1.2rem;
    font-size: 1.4rem;
}
.m-name {
    font-weight: bold;
    color: #333;
}
.status-tag {
    font-size: 1.3rem;
}
.s-orange { color: #FF9800; }
.s-gray { color: #999; }
.s-black { color: #333; }

.dish-row {
    display: flex;
    align-items: center;
    margin-bottom: 0.8rem;
}
.dish-name {
    flex: 1;
    margin-left: 0.8rem;
    font-size: 1.4rem;
    color: #333;
}
.dish-count {
    color: #999;
    font-size: 1.2rem;
}
.more-tip {
    font-size: 1.2rem;
    color: #999;
    margin-top: 0.4rem;
    margin-bottom: 0.8rem;
}

.card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 0.8rem;
    font-size: 1.2rem;
    color: #999;
}
.total {
    color: #333;
}
.price {
    font-size: 1.6rem;
    font-weight: bold;
    color: #333;
}
.card-actions {
    margin-top: 1.2rem;
    border-top: 1px solid #f9f9f9;
    padding-top: 1.2rem;
    text-align: right;
}

:deep(.van-button--small) {
    min-height: 4.4rem;
    font-size: 1.2rem;
}
</style>
