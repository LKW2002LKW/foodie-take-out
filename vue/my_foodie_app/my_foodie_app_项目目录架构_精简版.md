# my_foodie_app 项目目录（精简版）

## 根目录

```text
my_foodie_app/
├── .env.development        # 开发环境变量（高德 Key、后端地址）
├── package.json            # 项目依赖与脚本
├── vite.config.js          # Vite 配置
├── index.html              # Vue 挂载入口
├── README.md               # 项目说明
├── public/                 # 公共静态资源
├── src/                    # 项目源码
├── dist/                   # 打包产物（自动生成）
└── node_modules/           # npm依赖（自动生成）
```

---

# src 核心架构

```text
src/
├── main.js                         # Vue 启动入口
├── App.vue                         # 根组件

├── api/                            # 接口层
│   ├── address.js                  # 地址接口
│   ├── cart.js                     # 购物车接口
│   ├── common.js                   # 上传/公共接口
│   ├── dish.js                     # 菜品接口
│   ├── merchant.js                 # 商家接口
│   ├── notice.js                   # 公告接口
│   ├── order.js                    # 订单接口
│   ├── review.js                   # 评价接口
│   ├── setmeal.js                  # 套餐接口
│   ├── user.js                     # 用户接口
│   │
│   ├── modules/                    # 统一导出层
│   │   ├── address.js
│   │   ├── cart.js
│   │   ├── merchant.js
│   │   ├── order.js
│   │   └── ...
│   │
│   └── request/
│       ├── index.js                # Axios 实例
│       └── interceptors.js         # 请求/响应拦截器

├── assets/                         # 静态资源
│   ├── inconfont/                  # 图标字体
│   └── styles/
│       └── global.css              # 全局样式

├── components/                     # 全局公共组件
│   └── business/
│       └── ReviewCard.vue          # 评价卡片

├── composables/                    # 组合式业务逻辑
│   ├── business/                   # 页面业务逻辑
│   │   ├── useLoginPage.js
│   │   ├── useRegisterPage.js
│   │   ├── useMerchantListPage.js
│   │   ├── useMerchantDetailPage.js
│   │   ├── useCartPage.js
│   │   ├── useCreateOrderPage.js
│   │   ├── useOrderListPage.js
│   │   ├── useProfilePage.js
│   │   ├── useReviewCreatePage.js
│   │   └── ...
│   │
│   └── common/
│       └── useSmsCountdown.js      # 验证码倒计时

├── config/                         # 全局配置
│   ├── env.js                      # 环境变量读取
│   ├── pinia.js                    # Pinia 配置
│   ├── router.js                   # Router 配置
│   └── vant.js                     # Vant 按需引入

├── constants/                      # 常量配置
│   ├── location.js
│   ├── merchant.js
│   ├── order.js
│   ├── review.js
│   └── storageKeys.js

├── layouts/                        # 布局层
│   ├── BlankLayout.vue             # 空白布局
│   ├── MobileLayout.vue            # 主移动端布局
│   └── components/
│       └── AppTabbar.vue           # 底部导航栏

├── pages/                          # 页面层
│   ├── address/
│   │   └── pages/
│   │       ├── AddressList.vue
│   │       └── AddressEdit.vue
│   │
│   ├── auth/
│   │   └── pages/
│   │       ├── Login.vue
│   │       └── Register.vue
│   │
│   ├── cart/
│   │   └── pages/
│   │       └── Cart.vue
│   │
│   ├── merchant/
│   │   ├── components/
│   │   │   ├── MerchantCard.vue
│   │   │   ├── DishList.vue
│   │   │   ├── FlavorPicker.vue
│   │   │   └── BottomCartBar.vue
│   │   │
│   │   └── pages/
│   │       ├── MerchantList.vue
│   │       └── MerchantDetail.vue
│   │
│   ├── notice/
│   │   └── pages/
│   │       ├── NoticeList.vue
│   │       └── NoticeDetail.vue
│   │
│   ├── order/
│   │   └── pages/
│   │       ├── CreateOrder.vue
│   │       ├── Order.vue
│   │       └── OrderList.vue
│   │
│   ├── profile/
│   │   └── pages/
│   │       ├── Profile.vue
│   │       ├── ProfileEdit.vue
│   │       ├── BindPhone.vue
│   │       └── ChangePassword.vue
│   │
│   └── review/
│       └── pages/
│           ├── MyReview.vue
│           └── ReviewCreate.vue

├── router/                         # 路由层
│   ├── index.js                    # 路由入口
│   ├── guards/
│   │   ├── beforeEach.js           # 登录鉴权
│   │   └── afterEach.js            # 页面标题
│   │
│   └── modules/
│       ├── auth.js
│       ├── merchant.js
│       ├── order.js
│       └── ...

├── stores/                         # Pinia 状态管理
│   ├── index.js
│   └── modules/
│       ├── userStore.js            # 用户状态
│       ├── cartStore.js            # 购物车状态
│       └── locationStore.js        # 定位状态

├── styles/
│   └── variables.css               # 全局CSS变量

├── utils/                          # 工具层
│   ├── location.js                 # 浏览器定位
│   │
│   ├── business/
│   │   ├── flavor.js
│   │   ├── merchant.js
│   │   └── notice.js
│   │
│   ├── common/
│   │   └── storage.js              # localStorage工具
│   │
│   └── location/
│       └── amapLoader.js           # 高德地图SDK
```

---

# 当前项目架构特点

## 1. 页面层（pages）
只负责：
- 页面结构
- 页面样式
- 调用 composables

不直接写复杂业务。

---

## 2. composables 业务层
核心业务逻辑全部下沉：
- 请求
- 状态
- 交互
- 页面行为

实现：
- 页面更轻
- 逻辑复用
- 易维护

---

## 3. api 接口层
统一管理：
- Axios
- 请求拦截
- Token
- 接口定义

业务代码：
```js
import { loginApi } from '@/api/modules/user'
```

不直接操作 axios。

---

## 4. stores 状态层
Pinia 仅存：
- 用户状态
- 购物车状态
- 定位状态

避免把页面逻辑塞进 store。

---

## 5. router 路由层
模块化拆分：
- auth
- merchant
- order
- review

支持：
- 登录鉴权
- 页面标题
- 权限控制

---

# 项目整体分层

```text
pages（页面）
    ↓
composables（业务逻辑）
    ↓
api/modules（接口导出）
    ↓
api/request（axios封装）
    ↓
backend API
```

---

# 一眼定位文件建议

## 登录相关
```text
pages/auth/pages/Login.vue
composables/business/useLoginPage.js
api/user.js
stores/modules/userStore.js
```

## 商家列表
```text
pages/merchant/pages/MerchantList.vue
composables/business/useMerchantListPage.js
api/merchant.js
```

## 商家详情
```text
pages/merchant/pages/MerchantDetail.vue
composables/business/useMerchantDetailPage.js
```

## 购物车
```text
pages/cart/pages/Cart.vue
stores/modules/cartStore.js
composables/business/useCartPage.js
```

## 下单
```text
pages/order/pages/CreateOrder.vue
composables/business/useCreateOrderPage.js
api/order.js
```

## 用户中心
```text
pages/profile/pages/Profile.vue
composables/business/useProfilePage.js
stores/modules/userStore.js
```
