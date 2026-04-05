<template>
  <div class="order-list-page">
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
                         <van-image :src="dish.image" width="40" height="40" radius="4" fit="cover" />
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
    padding: 12px;
}
.order-card {
    background: #fff;
    border-radius: 8px;
    padding: 12px 16px;
    margin-bottom: 12px;
}
.card-header {
    display: flex;
    justify-content: space-between;
    padding-bottom: 12px;
    border-bottom: 1px solid #f9f9f9;
    margin-bottom: 12px;
    font-size: 14px;
}
.m-name {
    font-weight: bold;
    color: #333;
}
.status-tag {
    font-size: 13px;
}
.s-orange { color: #FF9800; }
.s-gray { color: #999; }
.s-black { color: #333; }

.dish-row {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
}
.dish-name {
    flex: 1;
    margin-left: 8px;
    font-size: 14px;
    color: #333;
}
.dish-count {
    color: #999;
}
.more-tip {
    font-size: 12px;
    color: #999;
    margin-top: 4px;
    margin-bottom: 8px;
}

.card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 8px;
    font-size: 12px;
    color: #999;
}
.total {
    color: #333;
}
.price {
    font-size: 16px;
    font-weight: bold;
    color: #333;
}
.card-actions {
    margin-top: 12px;
    border-top: 1px solid #f9f9f9;
    padding-top: 12px;
    text-align: right;
}
</style>
