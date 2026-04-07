<template>
    <div class="order-detail-page mobile-page">
    <van-nav-bar title="订单详情" left-arrow fixed placeholder @click-left="goBack" />

    <div class="status-header">
        <div class="status-text">{{ getStatusText(order.status) }}</div>
        <div class="status-desc" v-if="order.status === 1">请在15分钟内完成支付</div>
        <div class="action-btn" v-if="order.status === 1">
            <van-button round type="primary" size="small" @click="showPayPanel=true">立即支付</van-button>
            <van-button round plain size="small" class="cancel-btn" @click="onCancel">取消订单</van-button>
        </div>
        <!-- 8.5 Cancel available for 1,2,3 -->
        <div class="action-btn" v-if="[2, 3].includes(order.status)">
            <van-button round plain size="small" class="cancel-btn" @click="onCancel">取消订单</van-button>
        </div>
        <!-- 8.6 Review available for 5 -->
        <div class="action-btn" v-if="order.canReview === true">
            <van-button round plain size="small" class="cancel-btn" @click="onReview">去评价</van-button>
        </div>
        <!-- 8.4 Track info display if available -->
        <div class="track-card" v-if="trackInfo && [3,4,5].includes(order.status)" @click="showTrackPopup=true">
            <div class="flex-row">
                 <van-icon name="logistics" size="2rem" />
                 <span style="margin-left:0.8rem; font-weight:bold">订单跟踪</span>
            </div>
            <div style="margin-top:0.8rem; font-size:1.2rem; color:#666">
                {{ trackInfo.trackNodes?.[trackInfo.trackNodes.length-1]?.title || '查看进度' }}
            </div>
        </div>
    </div>

    <!-- 地址信息 -->
    <div class="card address-card">
        <div class="addr-row">
            <span class="addr-tag">收货人</span>
            <span class="addr-val">{{ order.consignee }} {{ order.phone }}</span>
        </div>
        <div class="addr-row">
            <span class="addr-tag">地址</span>
            <span class="addr-val">{{ order.address }}</span>
        </div>
    </div>

    <!-- 商品列表 -->
    <div class="card goods-card">
        <div class="shop-name">{{ order.merchantName || '商户' }}</div>
        <div v-for="item in order.orderDetailList" :key="item.id" class="goods-item">
            <van-image :src="item.image" width="5rem" height="5rem" radius="0.4rem" />
            <div class="g-info">
                <div class="g-name">{{ item.name }}</div>
                <div class="g-spec">{{ item.dishFlavor ? getFlavorText(item.dishFlavor) : '' }}</div>
                <div class="g-count">x{{ item.number }}</div>
            </div>
            <div class="g-price">￥{{ item.amount }}</div>
        </div>
        <div class="fee-row">
            <span>打包费</span>
            <span>￥{{ order.packAmount || 0 }}</span>
        </div>
        <div class="fee-row">
            <span>配送费</span>
            <span>￥{{ order.deliveryFee || 0 }}</span>
        </div>
        <div class="total-row">
            <span>实付 </span>
            <span class="price">￥{{ order.totalAmount }}</span>
        </div>
    </div>

    <!-- 订单信息 -->
    <div class="card info-card">
        <div class="info-row">
            <span>订单号</span>
            <span>{{ order.orderNumber }}</span>
        </div>
        <div class="info-row">
            <span>下单时间</span>
            <span>{{ order.orderTime }}</span>
        </div>
        <div class="info-row" v-if="order.payMethod">
            <span>支付方式</span>
            <span>{{ order.payMethod === 1 ? '微信支付' : '支付宝' }}</span>
        </div>
        <div class="info-row">
            <span>备注</span>
            <span>{{ order.remark || '无' }}</span>
        </div>
    </div>

    <!-- 支付弹窗 (模拟) -->
    <van-action-sheet :show="showPayPanel" @update:show="showPayPanel = $event" title="确认支付">
        <div class="pay-content">
            <div class="pay-amount">￥{{ order.totalAmount }}</div>
            <van-radio-group v-model="payTypeModel">
                <van-cell-group inset>
                    <van-cell title="微信支付" clickable @click="payTypeModel = 1">
                        <template #right-icon>
                            <van-radio :name="1" />
                        </template>
                    </van-cell>
                    <van-cell title="支付宝" clickable @click="payTypeModel = 2">
                        <template #right-icon>
                            <van-radio :name="2" />
                        </template>
                    </van-cell>
                </van-cell-group>
            </van-radio-group>
            <div class="pay-btn-wrapper">
                <van-button type="primary" block round @click="handlePay">确认支付</van-button>
            </div>
        </div>
    </van-action-sheet>

    <!-- 评价弹窗 -->
    <van-action-sheet :show="showReviewPanel" @update:show="showReviewPanel = $event" title="评价订单">
        <div style="padding: 1.6rem;">
            <div style="text-align:center; margin-bottom:1.6rem;">
                <van-rate v-model="reviewRating" :size="25" color="#ffd21e" void-icon="star" void-color="#eee" />
            </div>
            <van-field
                v-model="reviewContent"
                rows="3"
                autosize
                type="textarea"
                placeholder="请输入您的评价..."
                maxlength="100"
                show-word-limit
                style="background:#f5f5f5; border-radius:0.8rem;"
            />
            <div style="margin-top: 1.6rem;">
                <van-uploader v-model="reviewFileList" :after-read="afterRead" multiple :max-count="3" preview-size="8rem" />
            </div>
            <div style="margin-top:2rem;">
                <van-button type="primary" block round @click="submitReview">提交评价</van-button>
            </div>
        </div>
    </van-action-sheet>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
// Import new APIs
import { 
    getOrderDetail, getOrderDetailByNumber, payment, paySuccess, cancelOrder, 
    reviewOrder, getOrderTrack 
} from '../api/order';
import { commonUpload } from '../api/common';
import { 
    showToast, showLoadingToast, closeToast, showSuccessToast, showConfirmDialog 
} from 'vant';

const route = useRoute();
const router = useRouter();
const routeId = route.params.id; // Could be ID or OrderNumber
const order = ref({});
const showPayPanel = ref(false);
const payTypeModel = ref(1);
const trackInfo = ref(null); // Logistics info

// Review related
const showReviewPanel = ref(false);
const reviewContent = ref('');
const reviewRating = ref(5);
const reviewFileList = ref([]);

const loadData = async () => {
    showLoadingToast({ message: '加载中...', forbidClick: true });
    try {
        let res;
        // Simple logic to detect if routeId is numeric ID or string OrderNo
        // Usually IDs are long, OrderNos are also numeric strings but maybe formatted.
        // Let's try getOrderDetail (by ID) first, if fails or if we want to be smarter:
        // If routeId is pure digits and length < 10, likely ID. 
        // If routeId is longer (timestamp based 2026...), likely OrderNo.
        // For now, let's assume routeId is ID if it comes from List/Create success.
        // However, CreateOrder redirects to orderNo.
        // We should robustly check.
        
        // Strategy: 
        // 1. If we suspect it is OrderNo (e.g. length > 10), try query by Number.
        // 2. Otherwise try query by ID.
        // Or just try one and fallback? Mixing types in same route param is tricky.
        // User asked to separate APIs.
        
        if (routeId.length > 10) { 
             // Assume Order Number
             res = await getOrderDetailByNumber(routeId);
        } else {
             // Assume ID
             res = await getOrderDetail(routeId);
        }

        if (res && res.code === 1) {
            order.value = res.data;
            payTypeModel.value = order.value.payMethod || 1;
            
            // If in delivery status, load track
            if ([3, 4].includes(order.value.status)) {
                 // Track usually needs Order ID or Number? 
                 // API says /user/order/track/{id} -> ID.
                 // So we need order.value.id
                 loadTrack(order.value.id);
            }
        } else {
            showToast('订单不存在');
            setTimeout(() => router.back(), 1000);
        }
    } catch(e) {
        showToast('网络错误');
        console.error(e);
    } finally {
        closeToast();
    }
};

const loadTrack = async (id) => {
    try {
        const res = await getOrderTrack(id);
        if (res.code === 1) {
            trackInfo.value = res.data;
        }
    } catch(e) {}
};

const getStatusText = (status) => {
    const map = {
        1: '待付款',
        2: '待接单',
        3: '已接单',
        4: '派送中',
        5: '已完成',
        6: '已取消',
        7: '退款'
    };
    return map[status] || '未知状态';
};

const getFlavorText = (jsonStr) => {
    try {
        const arr = JSON.parse(jsonStr);
        return arr.map(a => a.value).join(' ');
    } catch(e) {
        return '';
    }
};

const goBack = () => {
    if (window.history.length > 1) {
        router.back();
    } else {
        router.push('/order/list');
    }
};

const handlePay = async () => {
    showLoadingToast({ message: '支付中...', forbidClick: true });
    try {
        const orderNo = order.value.orderNumber;
        await payment({ orderNumber: orderNo, payMethod: payTypeModel.value });
        
        setTimeout(async () => {
             await paySuccess(orderNo);
             closeToast();
             showSuccessToast('支付成功');
             showPayPanel.value = false;
             loadData(); 
        }, 1000);

    } catch(e) {
        closeToast();
        console.error(e);
        showToast('支付异常');
    }
};

const onCancel = () => {
    showConfirmDialog({
        title: '提示',
        message: '确定要取消订单吗？'
    }).then(async () => {
        try {
            const res = await cancelOrder({ id: order.value.id, cancelReason: '用户取消' });
            if (res.code === 1) {
                showSuccessToast('订单已取消');
                loadData();
            } else {
                showToast(res.msg || '取消失败');
            }
        } catch(e) {
            showToast('操作失败');
        }
    }).catch(() => {});
};

const onReview = () => {
    if (!order.value.id) {
        showToast('订单不存在');
        return;
    }
    router.push({ path: '/review/create', query: { orderId: order.value.id } });
};

const afterRead = async (file) => {
    file.status = 'uploading';
    file.message = '上传中...';
    try {
        const formData = new FormData();
        formData.append('file', file.file);
        const res = await commonUpload(formData);
        if (res.code === 1) {
            file.status = 'done';
            file.message = '上传成功';
            file.url = res.data; 
        } else {
            file.status = 'failed';
            file.message = '上传失败';
        }
    } catch (e) {
        file.status = 'failed';
        file.message = '上传失败';
    }
};

const submitReview = async () => {
    if (!reviewContent.value.trim()) {
        showToast('请输入评价内容');
        return;
    }
    
    // Filter uploaded images
    const images = reviewFileList.value
        .filter(item => item.status === 'done' && item.url)
        .map(item => item.url)
        .join(','); // Join urls with comma as per DTO

    showLoadingToast({ message: '提交中...', forbidClick: true });
    try {
        const res = await reviewOrder({
            orderId: order.value.id,
            rating: reviewRating.value,
            content: reviewContent.value,
            images: images
        });
        if (res.code === 1) {
            showSuccessToast('评价成功');
            showReviewPanel.value = false;
            // Update UI
            order.value.canReview = false; 
            order.value.status = 5; // ensure completed
            loadData();
        } else {
            showToast(res.msg || '评价失败');
        }
    } catch(e) {
        showToast('提交失败');
    } finally {
        closeToast();
    }
};

onMounted(() => {
    if (routeId) {
        loadData();
    }
});
</script>

<style scoped>
.order-detail-page {
    background: #f7f8fa;
    min-height: 100vh;
    padding-bottom: calc(2rem + env(safe-area-inset-bottom));
}
.status-header {
    background: #FF9800;
    padding: 2.4rem 2rem;
    color: #fff;
}
.status-text {
    font-size: 2rem;
    font-weight: bold;
}
.status-desc {
    margin-top: 0.8rem;
    font-size: 1.4rem;
    opacity: 0.9;
}
.action-btn {
    margin-top: 1.5rem;
}
.cancel-btn {
    margin-left: 1rem;
    color: #fff !important;
    border-color: #fff !important;
    background: transparent; 
}

.card {
    background: #fff;
    margin: 1.2rem;
    padding: 1.6rem;
    border-radius: 0.8rem;
}
.addr-row {
    margin-bottom: 0.8rem;
    line-height: 1.4;
}
.addr-row:last-child {
    margin-bottom: 0;
}
.addr-tag {
    color: #999;
    margin-right: 1.2rem;
    font-size: 1.2rem;
}

.shop-name {
    font-weight: bold;
    font-size: 1.6rem;
    padding-bottom: 1.2rem;
    border-bottom: 1px solid #f9f9f9;
    margin-bottom: 1.2rem;
}
.goods-item {
    display: flex;
    margin-bottom: 1.6rem;
}
.g-info {
    flex: 1;
    margin-left: 1rem;
}
.g-name {
    font-size: 1.4rem;
    color: #333;
}
.g-spec {
    font-size: 1.2rem;
    color: #999;
}
.g-count {
    font-size: 1.2rem;
    color: #999;
    margin-top: 0.4rem;
}
.g-price {
    font-weight: bold;
    font-size: 1.4rem;
}
.fee-row, .total-row, .info-row {
    display: flex;
    justify-content: space-between;
    margin-bottom: 1rem;
    font-size: 1.4rem;
}
.total-row {
    border-top: 1px solid #f9f9f9;
    padding-top: 1.2rem;
    margin-top: 1rem;
    font-weight: bold;
    align-items: center;
}
.total-row .price {
    font-size: 1.8rem;
    color: #e4393c;
}

.pay-content {
    padding: 2.4rem 0;
    text-align: center;
}
.pay-amount {
    font-size: 3.2rem;
    font-weight: bold;
    margin-bottom: 2.4rem;
}
.pay-btn-wrapper {
    padding: 1.6rem;
    margin-top: 1rem;
}
</style>

