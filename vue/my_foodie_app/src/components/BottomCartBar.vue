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
.mt-cart-bar-wrapper { position: fixed; bottom: 24px; left: 0; right: 0; padding: 0 16px; z-index: 2000; pointer-events: none; }
.mt-cart-bar { pointer-events: auto; height: 50px; background-color: #222; border-radius: 25px; display: flex; align-items: center; box-shadow: 0 8px 24px rgba(0,0,0,0.2); transition: background-color 0.3s; }
.mt-cart-active { background-color: #333; }
.mt-cart-left { flex: 1; display: flex; align-items: center; padding-left: 4px; }
.mt-cart-icon-box { width: 54px; height: 54px; background-color: #444; border-radius: 50%; margin-top: -15px; display: flex; align-items: center; justify-content: center; border: 4px solid #222; color: #999; position: relative; transition: all 0.3s; }
.mt-cart-active .mt-cart-icon-box { background-color: #FFD000; color: #222; border-color: #333; }
.mt-cart-badge { position: absolute; top: -2px; right: -2px; background-color: #FF4B33; color: #fff; font-size: 10px; padding: 0 5px; min-width: 16px; height: 16px; line-height: 16px; text-align: center; border-radius: 8px; border: 1px solid #fff; }
.mt-cart-price-info { margin-left: 12px; color: #fff; }
.mt-total-price { display: flex; align-items: baseline; line-height: 1; margin-bottom: 2px; }
.mt-amount { font-size: 22px; font-weight: 800; }
.mt-empty-text { font-size: 14px; color: #999; }
.mt-delivery-text { font-size: 10px; color: #888; }
.mt-cart-right { height: 100%; padding: 0 24px; background-color: #444; color: #999; font-size: 15px; font-weight: 800; display: flex; align-items: center; border-radius: 0 25px 25px 0; }
.mt-can-pay { background-color: #FFD000; color: #222; }
.mt-pop-anim { animation: mt-pop 0.3s cubic-bezier(0.17, 0.67, 0.83, 0.67); }
@keyframes mt-pop { 0% { transform: scale(1); } 50% { transform: scale(1.2); } 100% { transform: scale(1); } }
</style>
