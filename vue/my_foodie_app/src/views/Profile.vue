<template>
  <div class="profile-page">
    <div class="user-header" @click="router.push('/profile/info')">
      <div class="user-info">
        <van-image
          round
          width="64px"
          height="64px"
          :src="userInfo.avatar || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'"
          fit="cover"
        />
        <div class="info">
          <h3 class="nickname">
            {{ userInfo.nickname || (userInfo.id ? '用户' + userInfo.id : 'Click to Login/Edit') }}
          </h3>
          <p class="phone">{{ userInfo.phone || '点击编辑资料' }}</p>
        </div>
        <van-icon name="arrow" style="margin-left: auto; color: white;" />
      </div>
    </div>

    <van-cell-group inset class="menu-group">
      <van-cell title="我的订单" is-link to="/order/list" icon="orders-o" />
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
import { computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../store/modules/user';
import { showConfirmDialog, showSuccessToast, showToast, showLoadingToast, closeToast } from 'vant';
import { logout, getUserProfile, bindWeChat } from '../api/user';

const router = useRouter();
const userStore = useUserStore();
const userInfo = computed(() => userStore.userInfo || {});

const mockWeChatLogin = () => {
    return new Promise((resolve) => {
        showLoadingToast({ message: '正在调起微信授权...', forbidClick: true, duration: 0 });
        setTimeout(() => {
            closeToast();
            // Simulate openid from WeChat
            const randomId = 'wx_openid_' + Math.random().toString(36).substr(2, 9);
            resolve(randomId);
        }, 1500);
    });
};

const handleBindWeChat = async () => {
    // 1. Check current status
    // If bound, maybe confirm before changing? The requirement says "Change Binding", can be direct.
    
    // 2. Simulate User Click -> WeChat Auth
    try {
        const openid = await mockWeChatLogin();
        
        // 3. Call API to bind
        showLoadingToast({ message: '绑定中...', forbidClick: true });
        const res = await bindWeChat({ openid });
        
        if (res.code === 1 || res.code === 200) {
            showSuccessToast('微信绑定成功');
            // 4. Refresh info
            initData();
        } else {
            showToast(res.msg || '绑定失败');
        }
    } catch (error) {
        console.error(error);
        showToast('操作失败，请重试');
    }
};

const initData = async () => {
  try {
    const res = await getUserProfile();
    if (res.code === 1 || res.code === 200) {
      userStore.setUserInfo(res.data);
    }
  } catch (error) {
    console.error('Fetch profile failed', error);
  }
};

onMounted(() => {
  initData();
});

const handleLogout = () => {
  showConfirmDialog({
    title: '提示',
    message: '确认退出登录吗？',
  })
    .then(async () => {
      try {
        await logout();
      } catch (error) {
        console.error(error);
      } finally {
        userStore.clearToken();
        showSuccessToast('已退出');
        router.replace('/login');
      }
    })
    .catch(() => {
    });
};
</script>

<style scoped>
.profile-page {
  background-color: #f7f8fa;
  min-height: 100vh;
}
.user-header {
  background: linear-gradient(to right, #4facfe 0%, #00f2fe 100%);
  padding: 40px 20px 60px;
  color: white;
}
.user-info {
  display: flex;
  align-items: center;
}
.info {
  margin-left: 15px;
}
.nickname {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
}
.phone {
  margin: 5px 0 0;
  opacity: 0.8;
  font-size: 14px;
}
.menu-group {
  margin-top: 12px;
  transform: translateY(-20px);
}
.logout-btn {
  margin: 30px 16px;
}
.logout {
  color: #ee0a24;
}
</style>
