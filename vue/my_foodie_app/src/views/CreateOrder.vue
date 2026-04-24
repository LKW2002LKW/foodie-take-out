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
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useCartStore } from '../store/modules/cart';
import { useUserStore } from '../store/modules/user'; 
import { getMerchantDetail } from '../api/merchant';
import { getDefaultAddress, getAddressDetail } from '../api/address';
import { submitOrder } from '../api/order';
import { showToast, showLoadingToast, closeToast } from 'vant';

const router = useRouter();
const route = useRoute();
const cartStore = useCartStore();
const userStore = useUserStore();

const address = ref(null);
const payMethod = ref(1);
const remark = ref('');
const merchantInfo = ref({});
const submitting = ref(false);

const packAmount = ref(0); // 暂定打包费0，如果有逻辑需后端确认或每个菜品累加

const resolvedMerchantId = computed(() => {
    return route.query.merchantId || cartStore.list[0]?.merchantId || merchantInfo.value.merchantId || merchantInfo.value.id || '';
});

// 计算逻辑
const estimatedTotal = computed(() => {
    // 商品总价
    const goodsTotal = cartStore.totalPrice; // Store already sums (price * num)
    // 配送费
    const delivery = merchantInfo.value.deliveryFee || 0;
    // 打包费 (假设 packAmount 是总打包费)
    const pack = packAmount.value; 
    
    return (goodsTotal + delivery + pack).toFixed(2);
});

const getFlavorText = (jsonStr) => {
    try {
        const arr = JSON.parse(jsonStr);
        return arr.map(a => a.value).join(' ');
    } catch(e) {
        return '';
    }
};

const initData = async () => {
    // 1. 校验购物车
    await cartStore.fetchCartList();
    if (cartStore.list.length === 0) {
        showToast('购物车为空');
        setTimeout(() => router.back(), 1500);
        return;
    }

    // 2. 获取商户信息 (校验单一商户在 Store 中已处理，这里取第一个)
    const mid = resolvedMerchantId.value;
    if (mid) {
        const mRes = await getMerchantDetail(mid);
        if (mRes.code === 1) {
            merchantInfo.value = mRes.data;
        }
    }

    // 3. 获取地址
    // 如果 URL 有 addressId，优先使用
    const qAddrId = route.query.addressId;
    if (qAddrId) {
        const aRes = await getAddressDetail(qAddrId);
        if (aRes.code === 1) address.value = aRes.data;
    } else {
        // 获取默认
        const dRes = await getDefaultAddress();
        if (dRes.code === 1 && dRes.data) {
            address.value = dRes.data;
        }
    }
};

const toAddressList = () => {
    router.push({ path: '/address/list', query: { from: 'createOrder' } });
};

const onSubmit = async () => {
    // 前置校验
    if (!address.value) {
        showToast('请选择收货地址');
        return;
    }
    if (!resolvedMerchantId.value) {
        showToast('商户信息加载中...');
        return;
    }
    if (merchantInfo.value.status !== 1) {
        showToast('店铺已休息，无法下单');
        return;
    }
    // 起送价校验
    const min = merchantInfo.value.minDeliveryAmount || 0; // minAppQuantity?
    if (cartStore.totalPrice < min) {
        showToast(`未达到起送金额￥${min}`);
        return;
    }

    submitting.value = true;
    showLoadingToast({ message: '正在提交...', forbidClick: true });

    try {
        const orderData = {
            merchantId: resolvedMerchantId.value,
            addressBookId: address.value.id,
            payMethod: payMethod.value,
            remark: remark.value,
            amount: Number(estimatedTotal.value), // Optional, backend verify
            packAmount: packAmount.value
        };

        const res = await submitOrder(orderData);
        
        if (res.code === 1) {
            // 提交成功: 
            // 1. 清空购物车
            // 这里我们手动清空 store 本地状态, 后端通常也会清空
            // 但为了保险，我们可以再次 fetchCartList (它会变空)
            // cartStore.clearCartAction() might trigger API delete which is redundant if submit already did it.
            // Usually submit order clears backend cart.
            // Let's just reset frontend state by re-fetching.
            await cartStore.fetchCartList();
            
            closeToast();
            // 2. 跳转到支付页/订单详情页 (Prompt 要求跳转待支付)
            // res.data 应该是 order object, need orderNumber.
            // 假设 res.data 是订单号 string，或者对象
            const orderNo = (typeof res.data === 'string') ? res.data : res.data.orderNumber || res.data.id;
            
            router.replace(`/order/${orderNo}`); 
        } else {
            showToast(res.msg || '下单失败');
        }
    } catch (e) {
        console.error(e);
        showToast('系统异常');
    } finally {
        submitting.value = false;
    }
};

onMounted(() => {
    initData();
});
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
