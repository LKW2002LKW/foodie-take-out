<template>
  <div class="mt-cart-bar-wrapper">
    <div class="mt-cart-bar" :class="{ 'mt-cart-active': displayTotalNum > 0 }">
      <div class="mt-cart-left" @click="emit('toggle-cart-popup')">
        <div class="mt-cart-icon-box" :class="{ 'mt-pop-anim': displayTotalNum > 0 }">
          <van-icon name="shopping-cart" size="28" />
          <div v-if="displayTotalNum > 0" class="mt-cart-badge">{{ displayTotalNum }}</div>
        </div>
        <div class="mt-cart-price-info">
          <div v-if="displayTotalNum > 0" class="mt-total-price">
            <span class="mt-currency">￥</span><span class="mt-amount">{{ displayTotalPrice.toFixed(1) }}</span>
          </div>
          <div v-else class="mt-empty-text">未选购商品</div>
          <div class="mt-delivery-text">预估另需配送费￥{{ props.merchantInfo?.deliveryFee || 0 }}</div>
        </div>
      </div>
      <div class="mt-cart-right" :class="{ 'mt-can-pay': canCheckout }" @click="onCheckout">{{ checkoutBtnText }}</div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps({
  merchantInfo: Object,
  totalNum: {
    type: Number,
    default: 0,
  },
  totalPrice: {
    type: [Number, String],
    default: 0,
  },
})
const emit = defineEmits(['checkout', 'toggle-cart-popup'])
const displayTotalNum = computed(() => Number(props.totalNum || 0))
const displayTotalPrice = computed(() => Number(props.totalPrice || 0))
const checkoutBtnText = computed(() => {
  if (!displayTotalNum.value) return '￥0起送'
  const min = props.merchantInfo?.minDeliveryAmount || 0
  if (displayTotalPrice.value < min) return `还差￥${(min - displayTotalPrice.value).toFixed(1)}起送`
  return '去结算'
})
const canCheckout = computed(() => {
  const min = props.merchantInfo?.minDeliveryAmount || 0
  return displayTotalPrice.value >= min && displayTotalNum.value > 0
})
const onCheckout = () => { if (canCheckout.value) emit('checkout') }
</script>

<style scoped>
.mt-cart-bar-wrapper { position: fixed; bottom: 0; left: 0; right: 0; z-index: 2000; }
.mt-cart-bar {
  height: calc(5rem + env(safe-area-inset-bottom));
  padding-bottom: env(safe-area-inset-bottom);
  background: linear-gradient(180deg, #FFF6D7 0%, #FFEAB1 100%);
  border-radius: 0;
  display: flex;
  align-items: center;
  box-shadow: 0 -0.3rem 1.2rem rgba(245, 194, 0, 0.12);
  transition: background-color 0.2s;
  border-top: 1px solid rgba(245, 194, 0, 0.2);
}

.mt-cart-active {
  background: linear-gradient(180deg, #FFEFBF 0%, #FFE29A 100%);
}

.mt-cart-left {
  flex: 1;
  display: flex;
  align-items: center;
  padding-left: 1.2rem;
}

.mt-cart-icon-box {
  width: 4.4rem;
  height: 4.4rem;
  background-color: rgba(255, 255, 255, 0.76);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #846211;
  position: relative;
  transition: all 0.2s;
  border: 1px solid rgba(245, 194, 0, 0.28);
}

.mt-cart-active .mt-cart-icon-box {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-dark) 100%);
  color: var(--mt-strong);
}

.mt-cart-badge {
  position: absolute;
  top: -0.2rem;
  right: -0.2rem;
  background: linear-gradient(135deg, #FF6A3D 0%, #FF4B33 100%);
  color: var(--mt-card-bg);
  font-size: 1rem;
  padding: 0 0.5rem;
  min-width: 1.6rem;
  height: 1.6rem;
  line-height: 1.6rem;
  text-align: center;
  border-radius: 0.8rem;
  border: 0.1rem solid var(--mt-card-bg);
}

.mt-cart-price-info {
  margin-left: 1.2rem;
  color: var(--mt-strong);
}

.mt-total-price {
  display: flex;
  align-items: baseline;
  line-height: 1;
  margin-bottom: 0.2rem;
}

.mt-amount {
  font-size: 2.2rem;
  font-weight: 900;
}

.mt-empty-text {
  font-size: 1.4rem;
  color: var(--text-color-secondary);
}

.mt-delivery-text {
  font-size: 1rem;
  color: #8f7532;
}

.mt-cart-right {
  height: 3.6rem;
  margin-right: 1rem;
  padding: 0 2rem;
  background: linear-gradient(180deg, #ffe593 0%, #ffd96b 100%);
  color: var(--mt-strong);
  font-size: 1.5rem;
  font-weight: 900;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  border: 1px solid rgba(212, 156, 16, 0.16);
  box-shadow: inset 0 0.1rem 0 rgba(255, 255, 255, 0.45), 0 0.4rem 1rem rgba(245, 194, 0, 0.16);
  min-width: 10.8rem;
}

.mt-can-pay {
  background: linear-gradient(180deg, #ffd95f 0%, #f5c200 100%);
  color: var(--mt-strong);
}
.mt-pop-anim { animation: mt-pop 0.3s cubic-bezier(0.17, 0.67, 0.83, 0.67); }
@keyframes mt-pop { 0% { transform: scale(1); } 50% { transform: scale(1.2); } 100% { transform: scale(1); } }
</style>
