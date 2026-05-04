<template>
  <div class="profile-page mobile-page">
    <div class="user-header" @click="goProfileInfo">
      <div class="user-info">
        <van-image
          round
          width="6.4rem"
          height="6.4rem"
          :src="userInfo.avatar || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'"
          fit="cover"
        />
        <div class="info">
          <h3 class="nickname">
            {{ userInfo.nickname || (userInfo.id ? '用户' + userInfo.id : 'Click to Login/Edit') }}
          </h3>
          <p class="phone">{{ userInfo.phone || '点击编辑资料' }}</p>
        </div>
        <van-icon name="arrow" style="margin-left: auto; color: var(--mt-strong); font-size: 1.6rem;" />
      </div>
    </div>

    <van-cell-group inset class="menu-group">
      <van-cell title="我的订单" is-link to="/order/list" icon="orders-o" />
      <van-cell title="我的评价" is-link to="/review/my" icon="chat-o" />
      <van-cell title="收货地址" is-link to="/address/list" icon="location-o" />
    </van-cell-group>

    <van-cell-group inset class="menu-group">
      <van-cell title="账号安全" icon="shield-o" />
      <van-cell 
        :title="userInfo.hasPassword ? '修改密码' : '设置密码'" 
        is-link 
        to="/profile/password" 
        icon="lock" 
        :value="userInfo.hasPassword ? '已设置' : '未设置'"
      />
      <van-cell 
        :title="userInfo.hasPhone ? '更换手机' : '绑定手机'" 
        is-link 
        to="/profile/phone" 
        icon="phone-o" 
        :value="userInfo.phone || (userInfo.hasPhone ? '已绑定' : '未绑定')"
      />
      <van-cell 
        :title="userInfo.hasWechat ? '已绑定微信' : '未绑定微信'" 
        is-link
        icon="chat-o" 
        :value="userInfo.hasWechat ? '更换绑定' : '绑定微信'" 
        @click="handleBindWeChat"
      />
    </van-cell-group>

    <van-cell-group inset class="menu-group">
      <van-cell title="关于我们" is-link icon="info-o" />
      <van-cell title="系统设置" is-link icon="setting-o" />
    </van-cell-group>

    <div class="logout-btn">
      <van-button round block type="default" class="logout" @click="handleLogout">
        退出登录
      </van-button>
    </div>
  </div>
</template>

<script setup>
import { useProfilePage } from '@/composables/business/useProfilePage'

const {
  goProfileInfo,
  handleBindWeChat,
  handleLogout,
  userInfo,
} = useProfilePage()
</script>

<style scoped>
.profile-page {
  background-color: var(--mt-page-bg);
  min-height: 100vh;
}
.user-header {
  background: linear-gradient(90deg, var(--primary-color) 0%, var(--primary-color-dark) 100%);
  padding: 4rem 2rem 6rem;
  color: var(--mt-strong);
}
.user-info {
  display: flex;
  align-items: center;
  min-height: 6.4rem;
}
.info {
  margin-left: 1.5rem;
}
.nickname {
  margin: 0;
  font-size: 2rem;
  font-weight: 500;
}
.phone {
  margin: 0.5rem 0 0;
  opacity: 0.8;
  font-size: 1.4rem;
}
.menu-group {
  margin-top: 1.2rem;
  transform: translateY(-2rem);
}
.logout-btn {
  margin: 3rem 1.6rem calc(1.6rem + env(safe-area-inset-bottom));
}
.logout {
  color: var(--van-danger-color);
  min-height: 4.4rem;
  font-size: 1.4rem;
}

:deep(.van-cell) {
  min-height: 4.4rem;
}

:deep(.van-cell__title),
:deep(.van-cell__value) {
  font-size: 1.4rem;
}
</style>
