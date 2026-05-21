<template>
  <!-- 空白布局用于登录、注册等不需要底部导航的页面。 -->
  <div class="blank-layout">
    <slot />
  </div>
</template>

<style scoped>
.blank-layout {
  min-height: 100vh;
  min-height: 100dvh;
}
</style>
