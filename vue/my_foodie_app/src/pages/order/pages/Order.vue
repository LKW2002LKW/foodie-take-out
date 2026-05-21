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
            <div style="margin-top:0.8rem; font-size:1.2rem; color:var(--text-color-secondary)">
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
                <van-rate v-model="reviewRating" :size="25" color="var(--primary-color)" void-icon="star" void-color="rgba(245, 194, 0, 0.22)" />
            </div>
            <van-field
                v-model="reviewContent"
                rows="3"
                autosize
                type="textarea"
                placeholder="请输入您的评价..."
                maxlength="100"
                show-word-limit
                style="background:rgba(255, 248, 235, 0.96); border-radius:0.8rem; border:1px solid rgba(245, 194, 0, 0.16);"
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
import { useOrderDetailPage } from '@/composables/business/useOrderDetailPage'

const {
    afterRead,
    getFlavorText,
    getStatusText,
    goBack,
    handlePay,
    onCancel,
    onReview,
    order,
    payTypeModel,
    reviewContent,
    reviewFileList,
    reviewRating,
    showPayPanel,
    showReviewPanel,
    showTrackPopup,
    submitReview,
    trackInfo,
} = useOrderDetailPage()
</script>

<style scoped>
.order-detail-page {
    background: var(--mt-page-bg);
    min-height: 100vh;
    padding-bottom: calc(2rem + env(safe-area-inset-bottom));
}
.status-header {
    background: var(--mt-warning);
    padding: 2.4rem 2rem;
    color: var(--mt-card-bg);
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
    color: var(--mt-card-bg) !important;
    border-color: var(--mt-card-bg) !important;
    background: transparent; 
}

.card {
    background: var(--mt-card-bg);
    margin: 1.2rem;
    padding: 1.6rem;
    border-radius: var(--mt-card-radius);
    box-shadow: var(--shadow-sm);
}
.addr-row {
    margin-bottom: 0.8rem;
    line-height: 1.4;
}
.addr-row:last-child {
    margin-bottom: 0;
}
.addr-tag {
    color: var(--mt-muted);
    margin-right: 1.2rem;
    font-size: 1.2rem;
}

.shop-name {
    font-weight: bold;
    font-size: 1.6rem;
    padding-bottom: 1.2rem;
    border-bottom: 1px solid var(--mt-divider);
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
    color: var(--mt-strong);
}
.g-spec {
    font-size: 1.2rem;
    color: var(--mt-muted);
}
.g-count {
    font-size: 1.2rem;
    color: var(--mt-muted);
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
    border-top: 1px solid var(--mt-divider);
    padding-top: 1.2rem;
    margin-top: 1rem;
    font-weight: bold;
    align-items: center;
}
.total-row .price {
    font-size: 1.8rem;
    color: var(--van-danger-color);
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
