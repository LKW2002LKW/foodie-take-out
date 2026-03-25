<template>
  <div class="create-order">
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
                    <van-image :src="item.image" width="50" height="50" radius="4" />
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
        :price="estimatedTotal * 100" 
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
    const mid = cartStore.list[0].merchantId;
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
    if (!merchantInfo.value.id) {
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
            merchantId: merchantInfo.value.id,
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
    background: #f3f3f3;
    min-height: 100vh;
    padding-bottom: 60px;
}
.address-card {
    background: #fff;
    margin: 12px;
    padding: 16px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: space-between;
}
.addr-content {
    flex: 1;
}
.addr-info {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
    font-size: 16px;
    font-weight: bold;
    color: #333;
}
.addr-detail {
    line-height: 1.4;
}
.addr-user {
    font-size: 14px;
    color: #666;
}
.phone {
    margin-left: 10px;
}
.addr-tag {
    background: #FF9800;
    color: #fff;
    font-size: 10px;
    padding: 1px 4px;
    border-radius: 2px;
    margin-right: 6px;
    font-weight: normal;
}
.addr-empty {
    color: #FF9800;
    font-weight: bold;
    font-size: 16px;
}

.order-content {
    margin: 12px;
}
.merchant-card {
    background: #fff;
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 12px;
}
.m-header {
    font-size: 16px;
    font-weight: bold;
    border-bottom: 1px solid #f9f9f9;
    padding-bottom: 10px;
    margin-bottom: 10px;
}
.o-item {
    display: flex;
    margin-bottom: 16px;
}
.o-info {
    flex: 1;
    margin-left: 10px;
}
.o-name {
    font-size: 14px;
    font-weight: bold;
    color: #333;
}
.o-spec {
    font-size: 12px;
    color: #999;
    margin-top: 4px;
}
.o-price-row {
    text-align: right;
}
.o-price {
    font-weight: bold;
    font-size: 14px;
}
.o-num {
    font-size: 12px;
    color: #999;
    margin-top: 4px;
}
.fee-row {
    display: flex;
    justify-content: space-between;
    font-size: 14px;
    color: #333;
    margin-bottom: 10px;
}
.total-row {
    display: flex;
    justify-content: flex-end;
    align-items: baseline;
    border-top: 1px solid #f5f5f5;
    padding-top: 12px;
    margin-top: 10px;
}
.total-label {
    font-size: 14px;
    margin-right: 10px;
}
.total-val {
    color: #333;
    font-weight: bold;
}
.total-val .big {
    font-size: 20px;
}

.meta-card {
    border-radius: 8px;
    overflow: hidden;
}
</style>
