<template>
  <van-tabbar
    route
    active-color="var(--text-color-placeholder)"
    inactive-color="var(--text-color-placeholder)"
    class="mt-tabbar"
  >
    <van-tabbar-item replace to="/merchant/list">
      <template #icon="props">
        <div class="mt-tabbar-icon" :class="{ 'mt-tabbar-icon-active': props.active }">
          <van-icon name="shop-o" class="mt-tabbar-glyph" :class="{ 'mt-tabbar-glyph-active': props.active }" />
        </div>
      </template>
      首页
    </van-tabbar-item>
    <van-tabbar-item replace to="/cart">
      <template #icon="props">
        <div class="mt-tabbar-icon" :class="{ 'mt-tabbar-icon-active': props.active }">
          <van-icon name="shopping-cart-o" class="mt-tabbar-glyph" :class="{ 'mt-tabbar-glyph-active': props.active }" />
        </div>
      </template>
      购物车
    </van-tabbar-item>
    <van-tabbar-item replace to="/order/list">
      <template #icon="props">
        <div class="mt-tabbar-icon" :class="{ 'mt-tabbar-icon-active': props.active }">
          <van-icon name="orders-o" class="mt-tabbar-glyph" :class="{ 'mt-tabbar-glyph-active': props.active }" />
        </div>
      </template>
      订单
    </van-tabbar-item>
    <van-tabbar-item replace to="/profile">
      <template #icon="props">
        <div class="mt-tabbar-icon" :class="{ 'mt-tabbar-icon-active': props.active }">
          <van-icon name="user-o" class="mt-tabbar-glyph" :class="{ 'mt-tabbar-glyph-active': props.active }" />
        </div>
      </template>
      我的
    </van-tabbar-item>
  </van-tabbar>
</template>

<style scoped>
.mt-tabbar {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96) 0%, rgba(255, 248, 235, 0.98) 100%);
  box-shadow: 0 -0.2rem 1rem rgba(0, 0, 0, 0.05);
}

:deep(.van-tabbar-item) {
  min-height: 4.4rem;
  font-size: 1.2rem;
}

:deep(.van-tabbar-item--active) {
  font-weight: 700;
}

:deep(.van-tabbar-item__icon) {
  color: inherit;
}

.mt-tabbar-icon {
  width: 2.8rem;
  height: 2.8rem;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.18s ease, box-shadow 0.18s ease;
}

.mt-tabbar-icon-active {
  background: linear-gradient(180deg, #ffe27f 0%, #f5c200 100%);
  box-shadow: 0 0.3rem 0.8rem rgba(245, 194, 0, 0.18);
}

.mt-tabbar-glyph {
  color: inherit !important;
  font-size: 1.8rem;
}

.mt-tabbar-glyph-active {
  color: var(--mt-strong) !important;
}
</style>
