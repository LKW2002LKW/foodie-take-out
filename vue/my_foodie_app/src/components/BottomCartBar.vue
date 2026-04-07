<template>
  <div class="mt-cart-bar-wrapper">
    <div class="mt-cart-bar" :class="{ 'mt-cart-active': cartStore.totalNum > 0 }">
      <div class="mt-cart-left" @click="emit('toggle-cart-popup')">
        <div class="mt-cart-icon-box" :class="{ 'mt-pop-anim': cartStore.totalNum > 0 }">
          <van-icon name="shopping-cart" size="28" />
          <div v-if="cartStore.totalNum > 0" class="mt-cart-badge">{{ cartStore.totalNum }}</div>
        </div>
        <div class="mt-cart-price-info">
          <div v-if="cartStore.totalNum > 0" class="mt-total-price">
            <span class="mt-currency">￥</span><span class="mt-amount">{{ cartStore.totalPrice }}</span>
          </div>
          <div v-else class="mt-empty-text">未选购商品</div>
          <div class="mt-delivery-text">预估另需配送费￥{{ merchantInfo?.deliveryFee || 0 }}</div>
        </div>
      </div>
      <div class="mt-cart-right" :class="{ 'mt-can-pay': canCheckout }" @click="onCheckout">{{ checkoutBtnText }}</div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useCartStore } from '../store/modules/cart'
const props = defineProps({ merchantInfo: Object })
const emit = defineEmits(['checkout', 'toggle-cart-popup'])
const cartStore = useCartStore()
const checkoutBtnText = computed(() => {
  if (!cartStore.totalNum) return '￥0起送'
  const min = props.merchantInfo?.minDeliveryAmount || 0
  if (cartStore.totalPrice < min) return `还差￥${(min - cartStore.totalPrice).toFixed(1)}起送`
  return '去结算'
})
const canCheckout = computed(() => {
  const min = props.merchantInfo?.minDeliveryAmount || 0
  return cartStore.totalPrice >= min && cartStore.totalNum > 0
})
const onCheckout = () => { if (canCheckout.value) emit('checkout') }
</script>

<style scoped>
.mt-cart-bar-wrapper { position: fixed; bottom: 2.4rem; left: 0; right: 0; padding: 0 1.6rem; z-index: 2000; pointer-events: none; }
.mt-cart-bar { pointer-events: auto; height: 5rem; background-color: #222; border-radius: 2.5rem; display: flex; align-items: center; box-shadow: 0 0.8rem 2.4rem rgba(0,0,0,0.2); transition: background-color 0.3s; }
.mt-cart-active { background-color: #333; }
.mt-cart-left { flex: 1; display: flex; align-items: center; padding-left: 0.4rem; }
.mt-cart-icon-box { width: 5.4rem; height: 5.4rem; background-color: #444; border-radius: 50%; margin-top: -1.5rem; display: flex; align-items: center; justify-content: center; border: 0.4rem solid #222; color: #999; position: relative; transition: all 0.3s; }
.mt-cart-active .mt-cart-icon-box { background-color: #FFD000; color: #222; border-color: #333; }
.mt-cart-badge { position: absolute; top: -0.2rem; right: -0.2rem; background-color: #FF4B33; color: #fff; font-size: 1rem; padding: 0 0.5rem; min-width: 1.6rem; height: 1.6rem; line-height: 1.6rem; text-align: center; border-radius: 0.8rem; border: 0.1rem solid #fff; }
.mt-cart-price-info { margin-left: 1.2rem; color: #fff; }
.mt-total-price { display: flex; align-items: baseline; line-height: 1; margin-bottom: 0.2rem; }
.mt-amount { font-size: 2.2rem; font-weight: 800; }
.mt-empty-text { font-size: 1.4rem; color: #999; }
.mt-delivery-text { font-size: 1rem; color: #888; }
.mt-cart-right { height: 100%; padding: 0 2.4rem; background-color: #444; color: #999; font-size: 1.5rem; font-weight: 800; display: flex; align-items: center; border-radius: 0 2.5rem 2.5rem 0; }
.mt-can-pay { background-color: #FFD000; color: #222; }
.mt-pop-anim { animation: mt-pop 0.3s cubic-bezier(0.17, 0.67, 0.83, 0.67); }
@keyframes mt-pop { 0% { transform: scale(1); } 50% { transform: scale(1.2); } 100% { transform: scale(1); } }
</style>
