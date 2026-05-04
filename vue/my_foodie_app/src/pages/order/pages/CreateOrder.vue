<template>
    <div class="create-order mobile-page">
    <van-nav-bar title="确认订单" left-arrow fixed placeholder @click-left="$router.back()" />

    <!-- 地址栏 -->
    <div class="address-card" @click="toAddressList">
        <div class="addr-content" v-if="address">
            <div class="addr-info">
                <span class="addr-tag" v-if="address.label">{{ address.label }}</span>
                <span class="addr-detail">{{ address.provinceName }}{{ address.cityName }}{{ address.districtName }}{{ address.detail }}</span>
            </div>
            <div class="addr-user">
                <span class="name">{{ address.consignee }}</span>
                <span class="phone">{{ address.phone }}</span>
            </div>
        </div>
        <div class="addr-empty" v-else>
            请选择收货地址
        </div>
        <van-icon name="arrow" class="addr-arrow" />
    </div>

    <!-- 订单明细 -->
    <div class="order-content">
        <div class="merchant-card">
            <div class="m-header">{{ merchantInfo.name || '商家' }}</div>
            <div class="order-items">
                <div v-for="item in cartStore.list" :key="item.id" class="o-item">
                    <van-image :src="item.image" width="5rem" height="5rem" radius="0.4rem" />
                    <div class="o-info">
                        <div class="o-name">{{ item.name }}</div>
                        <div class="o-spec" v-if="item.dishFlavor">{{ getFlavorText(item.dishFlavor) }}</div>
                    </div>
                    <div class="o-price-row">
                        <div class="o-price">￥{{ item.amount }}</div>
                        <div class="o-num">x{{ item.number }}</div>
                    </div>
                </div>
            </div>
            <!-- 费用明细 -->
            <div class="fee-row">
                <span>打包费</span>
                <span>￥{{ packAmount }}</span>
            </div>
            <div class="fee-row">
                <span>配送费</span>
                <span>￥{{ merchantInfo.deliveryFee || 0 }}</span>
            </div>
            <div class="total-row">
                <div class="total-label">小计</div>
                <div class="total-val">
                    <span class="sym">￥</span>
                    <span class="big">{{ estimatedTotal }}</span>
                </div>
            </div>
        </div>
        
        <!-- 其它信息 -->
        <div class="meta-card">
             <van-cell-group inset>
                <van-cell title="支付方式">
                    <template #right-icon>
                        <van-radio-group v-model="payMethod" direction="horizontal">
                            <van-radio :name="1">微信</van-radio>
                            <van-radio :name="2">支付宝</van-radio>
                        </van-radio-group>
                    </template>
                </van-cell>
                <van-field
                    v-model="remark"
                    label="备注"
                    type="textarea"
                    rows="1"
                    autosize
                    placeholder="口味、偏好等要求"
                    input-align="right"
                />
             </van-cell-group>
        </div>
    </div>

    <!-- 底部提交栏 -->
    <van-submit-bar 
                :price="Number(estimatedTotal) * 100" 
        button-text="去支付" 
        @submit="onSubmit" 
        :loading="submitting"    
    />
    
  </div>
</template>

<script setup>
import { useCreateOrderPage } from '@/composables/business/useCreateOrderPage'

const {
    address,
    cartStore,
    estimatedTotal,
    getFlavorText,
    merchantInfo,
    onSubmit,
    packAmount,
    payMethod,
    remark,
    submitting,
    toAddressList,
} = useCreateOrderPage()
</script>

<style scoped>
.create-order {
    background: var(--mt-page-bg);
    min-height: 100vh;
    padding-bottom: calc(6rem + env(safe-area-inset-bottom));
}
.address-card {
    background: var(--mt-card-bg);
    margin: 1.2rem;
    padding: 1.6rem;
    border-radius: var(--mt-card-radius);
    box-shadow: var(--shadow-sm);
    display: flex;
    align-items: center;
    justify-content: space-between;
    min-height: 4.4rem;
}
.addr-content {
    flex: 1;
}
.addr-info {
    display: flex;
    align-items: center;
    margin-bottom: 0.8rem;
    font-size: 1.6rem;
    font-weight: bold;
    color: var(--mt-strong);
}
.addr-detail {
    line-height: 1.4;
}
.addr-user {
    font-size: 1.4rem;
    color: var(--text-color-secondary);
}
.phone {
    margin-left: 1rem;
}
.addr-tag {
    background: rgba(255, 149, 0, 0.14);
    color: var(--mt-strong);
    font-size: 1rem;
    padding: 0.1rem 0.4rem;
    border-radius: 0.2rem;
    margin-right: 0.6rem;
    font-weight: normal;
    border: 1px solid rgba(255, 149, 0, 0.28);
}
.addr-empty {
    color: var(--mt-warning);
    font-weight: 900;
    font-size: 1.6rem;
}

.order-content {
    margin: 1.2rem;
}
.merchant-card {
    background: var(--mt-card-bg);
    border-radius: var(--mt-card-radius);
    padding: 1.6rem;
    margin-bottom: 1.2rem;
    box-shadow: var(--shadow-sm);
}
.m-header {
    font-size: 1.6rem;
    font-weight: bold;
    color: var(--mt-strong);
    border-bottom: 1px solid var(--mt-divider);
    padding-bottom: 1rem;
    margin-bottom: 1rem;
}
.o-item {
    display: flex;
    margin-bottom: 1.6rem;
}
.o-info {
    flex: 1;
    margin-left: 1rem;
}
.o-name {
    font-size: 1.4rem;
    font-weight: bold;
    color: var(--mt-strong);
}
.o-spec {
    font-size: 1.2rem;
    color: var(--mt-muted);
    margin-top: 0.4rem;
}
.o-price-row {
    text-align: right;
}
.o-price {
    font-weight: bold;
    font-size: 1.4rem;
}
.o-num {
    font-size: 1.2rem;
    color: var(--mt-muted);
    margin-top: 0.4rem;
}
.fee-row {
    display: flex;
    justify-content: space-between;
    font-size: 1.4rem;
    color: var(--text-color-secondary);
    margin-bottom: 1rem;
}
.total-row {
    display: flex;
    justify-content: flex-end;
    align-items: baseline;
    border-top: 1px solid var(--mt-divider);
    padding-top: 1.2rem;
    margin-top: 1rem;
}
.total-label {
    font-size: 1.4rem;
    margin-right: 1rem;
    color: var(--text-color-secondary);
}
.total-val {
    color: var(--mt-strong);
    font-weight: 900;
}
.total-val .big {
    font-size: 2rem;
}

.meta-card {
    border-radius: var(--mt-card-radius);
    overflow: hidden;
    box-shadow: var(--shadow-sm);
}
</style>
