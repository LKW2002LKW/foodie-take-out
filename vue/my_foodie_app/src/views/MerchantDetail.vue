<template>
  <div class="merchant-detail">
    <!-- 顶部导航 -->
    <van-nav-bar
      left-arrow
      title="店铺详情"
      fixed
      placeholder
      @click-left="$router.back()"
    />

    <!-- 商家头部信息 -->
    <div class="merchant-header-wrap" v-if="merchantInfo">
      <div class="merchant-header">
        <div class="m-info">
            <van-image :src="merchantInfo.logo || merchantInfo.image" width="76" height="76" radius="6" class="m-logo" fit="cover">
                <template #error>
                    <van-icon name="shop-o" size="30" color="#ccc" />
                </template>
            </van-image>
            <div class="m-text">
                <div class="m-row-1">
                    <div class="m-name">{{ merchantInfo.name || merchantInfo.merchantName }}</div>
                </div>
                <!-- Business Hours & Status -->
                <div class="m-row-time">
                     <div class="status-tag" :class="{ 'off': merchantInfo.status !== 1 }">
                        {{ merchantInfo.status === 1 ? '营业中' : '休息中' }}
                     </div>
                     <span class="m-time-text">营业时间: {{ merchantInfo.beginTime || '09:00' }}-{{ merchantInfo.endTime || '21:00' }}</span>
                </div>
                
                <!-- Description / Notice -->
                <div class="m-notice van-ellipsis" @click="showMerchantInfo = true">
                    公告: {{ merchantInfo.description || '暂无简介' }}
                    <van-icon name="arrow" size="10" />
                </div>
                
                <!-- Delivery Tags/Coupons (Placeholder for future) -->
                <div class="m-tags">
                   <span class="m-tag-item">起送￥{{ merchantInfo.minDeliveryAmount || 0 }}</span>
                   <span class="m-tag-item">配送￥{{ merchantInfo.deliveryFee || 0 }}</span>
                   <span class="m-tag-item" v-if="merchantInfo.deliveryDto">{{ merchantInfo.deliveryDto.description }}</span>
                </div>
            </div>
        </div>
      </div>
    </div>
    <div v-else class="loading-header">
        <van-skeleton title avatar :row="2" />
    </div>

    <!-- 顶部Tab切换 -->
    <van-tabs v-model:active="activeTab" sticky offset-top="46" shrink >
       <van-tab title="点餐">
          <!-- 菜单区域 -->
          <div class="menu-container">
            <!-- 左侧分类边栏 -->
            <div class="menu-sidebar">
              <van-sidebar v-model="activeCategoryIndex" @change="onCategoryChange" v-if="categories.length > 0">
                  <van-sidebar-item 
                    v-for="(cat, index) in categories" 
                    :key="cat.id" 
                    :title="cat.name"
                    :badge="cat.dishCount > 0 ? cat.dishCount : ''"
                  />
              </van-sidebar>
              <div v-else class="empty-cats" style="padding-top: 50px; text-align: center; color: #999; font-size: 12px;">
                  <div style="margin-bottom:10px">暂无分类</div>
              </div>
            </div>

            <!-- 右侧商品列表 -->
            <div class="menu-content" ref="scrollContainer">
              <div v-if="loading" class="loading-box">
                   <van-loading type="spinner" />
              </div>
              <div v-else class="food-list">
                  <!-- 分类标题 -->
                  <div class="cat-title" v-if="currentCategory">{{ currentCategory.name }}</div>
                  
                  <div v-for="item in currentDishes" :key="item.id" class="food-item">
                      <van-image :src="item.image" width="80" height="80" radius="4" @click="showFoodDetail(item)">
                          <template #error>
                              <van-icon name="photo-o" size="24" color="#ccc" />
                          </template>
                      </van-image>
                      <div class="food-info">
                          <div class="food-name">{{ item.name }}</div>
                          <div class="food-desc van-ellipsis" v-if="item.description">{{ item.description }}</div>
                          <div class="food-sales">月售 {{ item.sales || item.salesVolume || 0 }}</div>
                          <div class="food-price-row">
                              <span class="price">￥<span class="num">{{ item.price }}</span></span>
                              
                              <!-- 加减排 UI Logic -->
                              <div class="cart-control">
                                  <div v-if="getDishCount(item) > 0" class="btn-sub" @click.stop="onSubItem(item)">
                                      <van-icon name="minus" />
                                  </div>
                                  <div v-if="getDishCount(item) > 0" class="count-num">{{ getDishCount(item) }}</div>
                                  
                                  <div class="btn-add" @click.stop="onAddItem(item)">
                                      <van-icon name="plus" />
                                      <span v-if="hasSpecs(item) && getDishCount(item) === 0" class="spec-text">选规格</span>
                                  </div>
                              </div>
                          </div>
                      </div>
                  </div>
                  
                  <van-empty v-if="!loading && currentDishes.length === 0" description="该分类暂无商品" />
              </div>
            </div>
          </div>
       </van-tab>
       
       <van-tab title="评价">
          <div class="review-container">
               <div class="review-header" v-if="reviewStats">
                   <div class="rh-left">
                       <div class="rh-score">{{ reviewStats.avgRating || 0 }}</div>
                       <div class="rh-text">商家评分</div>
                   </div>
                   <div class="rh-right">
                       <div class="rh-tags">
                           <div class="rh-row">
                               <span class="label">好评率</span>
                               <span class="val">{{ (reviewStats.goodRate * 100).toFixed(0) }}%</span>
                           </div>
                       </div>
                   </div>
               </div>
               
               <div class="review-filter">
                   <div class="filter-wrap">
                       <span class="r-tag" :class="{active: !filterParams.ratingFilter && !filterParams.hasImage}" @click="setFilter({})">全部 {{ reviewStats?.totalCount || 0 }}</span>
                       <span class="r-tag" :class="{active: filterParams.ratingFilter===5}" @click="setFilter({ratingFilter:5})">好评 {{ reviewStats?.fiveStarCount || 0 }}</span>
                       <span class="r-tag" :class="{active: filterParams.ratingFilter===3}" @click="setFilter({ratingFilter:3})">中评 {{ reviewStats?.mediumCount || 0 }}</span>
                       <span class="r-tag" :class="{active: filterParams.ratingFilter===1}" @click="setFilter({ratingFilter:1})">差评 {{ reviewStats?.badCount || 0 }}</span>
                   </div>
                   <van-checkbox v-model="filterParams.hasImage" shape="square" @change="onFilterChange" class="img-check">只看有图 ({{ reviewStats?.hasImageCount || 0 }})</van-checkbox>
               </div>
               
               <van-list
                  v-model:loading="reviewLoading"
                  :finished="reviewFinished"
                  finished-text="没有更多了"
                  @load="onLoadReviews"
               >
                  <div class="review-item" v-for="item in reviewList" :key="item.id">
                      <div class="user-row">
                          <van-image :src="item.userAvatar" width="32" height="32" round class="u-avatar">
                              <template #error><van-icon name="contact" color="#ccc" size="20" style="background:#f5f5f5;padding:6px;border-radius:50%"/></template>
                          </van-image>
                          <div class="u-info">
                              <div class="u-name">
                                  <span>{{ item.userNickname || '匿名用户' }}</span>
                                  <span class="u-date">{{ item.createTime?.substring(0,10) }}</span>
                              </div>
                              <van-rate :model-value="item.rating" readonly size="10" color="#ffd21e" />
                          </div>
                      </div>
                      <div class="r-content">{{ item.content }}</div>
                      
                      <div class="r-imgs" v-if="item.imageList && item.imageList.length > 0">
                          <van-image
                             v-for="(img, idx) in item.imageList"
                             :key="idx"
                             :src="img"
                             width="80"
                             height="80"
                             radius="4"
                             fit="cover"
                             style="margin-right:6px; margin-bottom:6px;"
                             @click="previewImage(item.imageList, idx)"
                          />
                      </div>
                      
                      <div class="m-reply" v-if="item.reply">
                          <div class="reply-label">商家回复：</div>
                          <div class="reply-content">{{ item.reply }}</div>
                      </div>
                  </div>
                  <van-empty v-if="reviewList.length===0 && reviewFinished" description="暂无评价" />
               </van-list>
          </div>
       </van-tab>
    </van-tabs>

    <!-- 底部购物车栏 -->
    <div class="cart-bar safe-area-bottom">
        <div class="cart-icon-wrapper" @click="toggleCartPopup">
            <div class="cart-circle" :class="{ 'has-goods': cartStore.totalNum > 0 }">
                <van-icon name="shopping-cart" size="24" :color="cartStore.totalNum > 0 ? '#fff' : '#999'" />
                <div class="badge" v-if="cartStore.totalNum > 0">{{ cartStore.totalNum }}</div>
            </div>
        </div>
        <div class="cart-info">
            <div class="total-price" v-if="cartStore.totalNum > 0">
                ￥<span class="big">{{ cartStore.totalPrice }}</span>
            </div>
            <div class="empty-text" v-else>未选购商品</div>
            <div class="delivery-tip">另需配送费￥{{ merchantInfo?.deliveryFee || 0 }}</div>
        </div>
        <div class="cart-btn" :class="{ 'can-pay': canCheckout }" @click="onCheckout">
            {{ checkoutBtnText }}
        </div>
    </div>

    <!-- 购物车详情弹窗 -->
    <van-popup v-model:show="showCartPopup" position="bottom" round class="cart-popup safe-area-bottom" :style="{ maxHeight: '50%' }">
        <div class="cart-header">
            <div class="cart-title">购物车</div>
            <div class="cart-clear" @click="onClearCart">
                <van-icon name="delete-o" />
                <span>清空</span>
            </div>
        </div>
        <div class="cart-list">
            <div v-for="(item, idx) in cartStore.list" :key="idx" class="cart-item-row">
                 <div class="ci-info">
                     <div class="ci-name">{{ item.name }}</div>
                     <div class="ci-spec" v-if="item.dishFlavor">{{ JSON.parse(item.dishFlavor).map(f => f.value).join(',') }}</div>
                 </div>
                 <div class="ci-price">￥{{ item.amount * item.number }}</div>
                 <div class="ci-control">
                     <div class="btn-sub" @click="cartStore.subFromCart(item)"><van-icon name="minus" /></div>
                     <div class="num">{{ item.number }}</div>
                     <div class="btn-add" @click="cartStore.addToCart(item)"><van-icon name="plus" /></div>
                 </div>
            </div>
            <van-empty v-if="cartStore.list.length === 0" description="购物车为空" />
        </div>
    </van-popup>

    <!-- 规格选择弹窗 -->
    <van-popup v-model:show="showSpecPopup" closeable round position="bottom" class="spec-popup">
        <div class="spec-content" v-if="selectedDish">
            <div class="spec-header">
                <van-image :src="selectedDish.image" width="60" height="60" radius="4" />
                <div class="spec-h-info">
                    <div class="spec-name">{{ selectedDish.name }}</div>
                </div>
            </div>
            <div class="spec-scroll">
                <div class="spec-group" v-for="flavor in selectedDish.flavors" :key="flavor.name">
                    <div class="g-title">{{ flavor.name }}</div>
                    <div class="g-opts">
                        <span 
                            class="opt-item" 
                            :class="{ active: isFlavorSelected(flavor.name, val) }"
                            v-for="val in JSON.parse(flavor.value)" 
                            :key="val"
                            @click="selectFlavor(flavor.name, val)"
                        >
                            {{ val }}
                        </span>
                    </div>
                </div>
            </div>
            <div class="spec-footer">
                <div class="ft-price">￥{{ selectedDish.price }}</div>
                <van-button type="primary" round block @click="confirmAddWithSpec">加入购物车</van-button>
            </div>
        </div>
    </van-popup>

    <!-- 商品详情弹窗 (New) -->
    <van-popup v-model:show="showDetailPopup" closeable round position="bottom" class="food-detail-popup" :style="{ height: '80%' }">
        <div v-if="detailLoading" class="loading-box" style="padding-top: 50px;">
             <van-loading vertical>加载中...</van-loading>
        </div>
        <div v-else-if="detailData" class="detail-container">
             <van-image :src="detailData.image" width="100%" height="240" fit="cover" />
             <div class="detail-content">
                 <div class="d-name">{{ detailData.name }}</div>
                 <div class="d-sales">月售 {{ detailData.sales || detailData.salesVolume || 0 }}</div>
                 
                 <div class="d-price-row">
                     <span class="d-price">￥{{ detailData.price }}</span>
                     <van-button size="small" type="primary" round @click="onAddFromDetail">加入购物车</van-button>
                 </div>
                 
                 <van-divider />
                 
                 <div class="d-desc-title">商品简介</div>
                 <div class="d-desc">{{ detailData.desc }}</div>

                 <!-- 套餐内容展示 -->
                 <div v-if="detailData.isSetmeal && detailData.setmealDishes && detailData.setmealDishes.length > 0">
                     <van-divider>套餐包含</van-divider>
                     <div class="setmeal-list">
                         <div v-for="(sd, i) in detailData.setmealDishes" :key="i" class="sm-item">
                             <span class="sm-name">{{ sd.name || sd.dishName }}</span>
                             <span class="sm-count">x{{ sd.copies }}</span>
                             <span class="sm-price">￥{{ sd.price }}</span>
                         </div>
                     </div>
                 </div>
             </div>
        </div>
    </van-popup>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showToast, showConfirmDialog, showImagePreview } from 'vant';
import { useCartStore } from '../store/modules/cart';
import { getMerchantDetail, getMerchantCategories } from '../api/merchant';
import { getDishList, getDishDetail } from '../api/dish';
import { getSetmealList, getSetmealDetail } from '../api/setmeal';
import { getReviewStats, getReviewPage } from '../api/review';

const route = useRoute();
const router = useRouter();
const cartStore = useCartStore();
// Ensure merchantId is valid number if possible, or string. 
// Backend IDs as string "13" are common, but let's be consistent.
const merchantId = route.params.id;

// --- Review State ---
const activeTab = ref(0);
const reviewStats = ref(null);
const reviewList = ref([]);
const reviewLoading = ref(false);
const reviewFinished = ref(false);
const reviewPage = ref(1);
const reviewPageSize = 10;
const filterParams = ref({
    ratingFilter: null,
    hasImage: false
});

const fetchReviewStats = async () => {
    try {
        const res = await getReviewStats(merchantId);
        if (res.code === 1) {
            reviewStats.value = res.data;
        }
    } catch (error) {
        console.error('Fetch review stats failed', error);
    }
};

const onLoadReviews = async () => {
    if (reviewFinished.value) return;
    reviewLoading.value = true;
    try {
        const res = await getReviewPage({
            page: reviewPage.value,
            pageSize: reviewPageSize,
            merchantId: merchantId,
            ratingFilter: filterParams.value.ratingFilter,
            hasImage: filterParams.value.hasImage
        });
        
        if (res.code === 1) {
            const rawList = res.data.list || [];
            // Map data correctly
            const list = rawList.map(item => {
                let imgs = [];
                if (item.imageList) {
                    // Split by comma if it's a string
                    imgs = typeof item.imageList === 'string' 
                        ? item.imageList.split(',') 
                        : item.imageList;
                }
                return {
                    ...item,
                    imageList: imgs,
                    reply: item.merchantReply
                };
            });

            if (reviewPage.value === 1) {
                reviewList.value = list;
            } else {
                reviewList.value = [...reviewList.value, ...list];
            }
            
            // Fix infinite loading: finish if list is empty or smaller than page size
            if (list.length < reviewPageSize || reviewList.value.length >= res.data.total) {
                reviewFinished.value = true;
            } else {
                reviewPage.value++;
            }
        } else {
            reviewFinished.value = true;
        }
    } catch (error) {
        reviewFinished.value = true;
    } finally {
        reviewLoading.value = false;
    }
};

const setFilter = (params) => {
    // 切换筛选条件，重置列表
    if (params.ratingFilter === undefined) filterParams.value.ratingFilter = null;
    else filterParams.value.ratingFilter = params.ratingFilter;
    
    if (params.hasImage !== undefined) filterParams.value.hasImage = params.hasImage;
    
    reviewPage.value = 1;
    reviewFinished.value = false;
    reviewList.value = [];
    onLoadReviews();
};

const onFilterChange = (val) => {
    setFilter({ hasImage: val });
};

const previewImage = (images, index) => {
    showImagePreview({
        images,
        startPosition: index,
        closeable: true,
    });
};

watch(activeTab, (newVal) => {
   if (newVal === 1 && !reviewStats.value) {
       fetchReviewStats();
       // List will load automatically by van-list or we trigger it if list is empty
       if (reviewList.value.length === 0) {
           onLoadReviews();
       }
   } 
});

const merchantInfo = ref(null);
const categories = ref([]);
const activeCategoryIndex = ref(0);
const currentDishes = ref([]);
const loading = ref(false);

const showSpecPopup = ref(false);
const showCartPopup = ref(false);
// New: Detail Popup
const showDetailPopup = ref(false);
const detailLoading = ref(false);
const detailData = ref(null);

const selectedDish = ref(null);
// 当前选中的规格状态： { "辣度": "微辣", "温度": "常温" }
const currentSpecs = ref({});

const showFoodDetail = async (item) => {
    detailLoading.value = true;
    showDetailPopup.value = true;
    detailData.value = null; // Clear old data
    
    try {
        let res;
        if (item.setmealId) { 
            // It's a setmeal
            res = await getSetmealDetail(item.id); // Or setmealId depending on list API mapping
        } else {
            // It's a dish
            res = await getDishDetail(item.id);
        }
        
        if (res.code === 1) {
            const data = res.data;
            // Unify structure for template
            detailData.value = {
                ...data,
                isSetmeal: !!item.setmealId,
                // Handle different image fields
                image: data.image || data.pic || item.image,
                name: data.name || data.setmealName,
                desc: data.description || data.remark || '暂无描述',
                // Ensure flavors is parsed if it's a dish coming from detail
                // Backend usually usually returns objects for detail API, but let's be safe
                flavors: data.flavors || [], 
                // Setmeal dishes
                setmealDishes: data.setmealDishes || [] 
            };
        } else {
            showToast('获取详情失败');
            showDetailPopup.value = false;
        }
    } catch (e) {
        showToast('加载失败');
        showDetailPopup.value = false;
    } finally {
        detailLoading.value = false;
    }
};

// Add to cart from detail popup
const onAddFromDetail = () => {
    if (!detailData.value) return;
    
    // Construct item object compatible with cart store
    const itemToAdd = {
        ...detailData.value,
        id: detailData.value.id,
        merchantId: parseInt(merchantId), // ensure ID
        // mapping for cart logic
        setmealId: detailData.value.isSetmeal ? detailData.value.id : undefined,
    };
    
    if (hasSpecs(itemToAdd)) {
        // If it has specs, open spec popup (close detail first or overlay?)
        // Standard UX: Detail page has "Select Spec" button which opens spec sheet
        // Or Detail page *is* the spec sheet.
        // Let's close detail and open spec to reuse logic
        showDetailPopup.value = false;
        openSpecPopup(itemToAdd);
    } else {
        cartStore.addToCart(itemToAdd);
        showToast('已加入购物车');
        showDetailPopup.value = false;
    }
};

// 获取商家信息
const loadMerchant = async () => {
    try {
        const res = await getMerchantDetail(merchantId);
        if (res.code === 1) {
            merchantInfo.value = res.data;
        } else {
            showToast('获取商家信息失败: ' + res.msg);
        }
    } catch (e) {
        console.error(e);
        showToast('获取商家信息异常');
    }
};

// 获取分类
const loadCategories = async () => {
    try {
        console.log('Loading categories for merchant:', merchantId);
        // 尝试获取分类
        const res = await getMerchantCategories(merchantId);
        console.log('Cat Response:', res);
        
        // Relaxed check: Accept if data is array even if code is missing/weird
        // or if code is 1/"1"
        const isSuccess = (res && res.code == 1) || (Array.isArray(res) && res.length > 0) || (res.data && Array.isArray(res.data));
        
        if (isSuccess) {
            let list = [];
            // Strategy 1: res.data is the array
            if (Array.isArray(res.data)) {
                list = res.data;
            } 
            // Strategy 2: res is the array (interceptor unwrapped it too much?)
            else if (Array.isArray(res)) {
                list = res;
            }
            // Strategy 3: res.data.data (Page wrapper)
            else if (res.data && Array.isArray(res.data.data)) {
                list = res.data.data;
            }
            // Strategy 4: res is object but data is in res.list?
            else if (res.list && Array.isArray(res.list)) {
                list = res.list;
            }

            console.log('Final Category List:', list);
            
            // Assign
            categories.value = list;
            
            // 如果分类加载成功，默认选中第一个加载商品
            if (categories.value.length > 0) {
                // Ensure active index is 0
                activeCategoryIndex.value = 0;
                loadDishes(categories.value[0]);
            } else {
                 console.warn('Categories loaded but list is empty');
            }
        } else {
            console.warn('Category fetch failed/bad format:', res);
            // showToast('分类加载失败'); 
            // Don't show toast to avoid spamming if it's just empty
        }
    } catch (e) {
        console.error('Cat Load Error:', e);
    }
};

// 切换分类
const onCategoryChange = (index) => {
    loadDishes(categories.value[index]);
};

const currentCategory = computed(() => categories.value[activeCategoryIndex.value]);

// 获取菜品数据
const loadDishes = async (category) => {
    if (!category) return;
    loading.value = true;
    currentDishes.value = [];
    try {
        console.log('Fetching dishes/setmeals for cat:', category.name, 'Type:', category.type);
        let res;
        // 判断分类类型: 1 菜品 2 套餐 
        // 使用 loose equality (==) 以防类型不匹配 (string "2" vs number 2)
        if (category.type == 2) {
             // 查询套餐
             res = await getSetmealList({ categoryId: category.id, merchantId: merchantId, status: 1 });
             if (res.code === 1) {
                 // 套餐通常没有 flavor，但为了统一处理，我们标记 setmealId
                 // 注意：套餐数据结构可能与菜品略有不同，需要做适配
                 currentDishes.value = (res.data || []).map(item => ({ 
                    ...item, 
                    setmealId: item.id,
                    // 套餐图片字段可能是 image 或 pic
                    image: item.image || item.pic,
                    // 确保有 price
                    price: item.price
                 }));
             } else {
                 console.warn('Setmeal load error:', res.msg);
             }
        } else {
             // 查询菜品
             res = await getDishList({ categoryId: category.id, merchantId: merchantId, status: 1 });
             if (res.code === 1) {
                 currentDishes.value = res.data || [];
             } else {
                 console.warn('Dish load error:', res.msg);
             }
        }
    } catch(e) {
        console.error("Load dishes error", e);
        // showFailToast('加载商品失败'); // Optional: quiet failure is better for UX sometimes
    } finally {
        loading.value = false;
    }
};

// --- 购物车逻辑 ---

// 判断是否有规格 (菜品有 flavors 数组)
const hasSpecs = (item) => {
    // 套餐如果不选规格，则不算有specs。菜品如果有 flavors 且长度>0
    if (item.setmealId) return false; 
    return item.flavors && item.flavors.length > 0;
};

// 获得显示数量 (如果是多规格，这里显示的总数)
const getDishCount = (item) => {
    const id = item.setmealId || item.id;
    const isSetmeal = !!item.setmealId;
    return cartStore.getCartItemCount(isSetmeal ? null : id, isSetmeal ? id : null);
};

// 点击加号
const onAddItem = (item) => {
    if (hasSpecs(item)) {
        // 打开规格选择
        openSpecPopup(item);
    } else {
        // 直接加入
        // 确保传入 merchantId (数据源可能缺这个字段，需补齐)
        item.merchantId = merchantId;
        cartStore.addToCart(item);
    }
};

// 点击减号
const onSubItem = (item) => {
    if (hasSpecs(item)) {
        showToast('多规格商品请在购物车中减少');
        return;
    }
    item.merchantId = merchantId;
    cartStore.subFromCart(item);
};

// --- 规格选择 ---
const openSpecPopup = (item) => {
    selectedDish.value = item;
    currentSpecs.value = {};
    // 初始化选中第一个选项
    if (item.flavors) {
        item.flavors.forEach(f => {
            const vals = JSON.parse(f.value);
            if (vals.length > 0) currentSpecs.value[f.name] = vals[0];
        });
    }
    showSpecPopup.value = true;
};

const isFlavorSelected = (name, val) => currentSpecs.value[name] === val;
const selectFlavor = (name, val) => currentSpecs.value[name] = val;

const confirmAddWithSpec = () => {
    // 构造选中的 flavor 数组 [{name: "辣度", value: "微辣"}]
    // 之前 dishFlavor 是 JSON 字符串 [{"name":"辣度","value":"微辣"}]
    const flavorArr = Object.keys(currentSpecs.value).map(k => ({
        name: k,
        value: currentSpecs.value[k]
    }));
    
    const item = { ...selectedDish.value, merchantId };
    cartStore.addToCart(item, flavorArr);
    showSpecPopup.value = false;
};

// --- 底部购物车 ---
const toggleCartPopup = () => {
    if (cartStore.totalNum === 0) return;
    showCartPopup.value = !showCartPopup.value;
};

const checkoutBtnText = computed(() => {
    if (!cartStore.totalNum) return '未选购';
    const min = merchantInfo.value?.minDeliveryAmount || 0;
    if (cartStore.totalPrice < min) return `差￥${(min - cartStore.totalPrice).toFixed(1)}起送`;
    return '去结算';
});

const canCheckout = computed(() => {
    const min = merchantInfo.value?.minDeliveryAmount || 0;
    return cartStore.totalPrice >= min && cartStore.totalNum > 0;
});

const onClearCart = () => {
    showConfirmDialog({ title: '确认清空购物车吗？' })
        .then(() => {
            cartStore.clearCartAction(merchantId);
            showCartPopup.value = false;
        });
};

const onCheckout = () => {
    if (!canCheckout.value) return;
    // showToast('跳转去结算...');
    // router.push('/order/create')
    // Must pass merchantId via State or Query, but since cart is global and single-merchant enforced via business logic,
    // we can just go to create page. Ideally we verify cart merchant status there.
    router.push({ path: '/order/create', query: { merchantId: merchantId } });
};

onMounted(() => {
    loadMerchant();
    loadCategories();
    // 每次进入必须刷新购物车
    cartStore.fetchCartList();
});
</script>

<style scoped>
.merchant-detail {
    padding-bottom: 50px;
    height: 100vh;
    display: flex;
    flex-direction: column;
    background: #fff;
    font-family: -apple-system, BlinkMacSystemFont, 'Helvetica Neue', Helvetica, Segoe UI, Arial, Roboto, 'PingFang SC', 'miui', 'Hiragino Sans GB', 'Microsoft Yahei', sans-serif;
}
.merchant-header-wrap {
    background: #fff;
    /* Navbar has placeholder prop, so we don't need top margin here */
    box-shadow: 0 1px 6px rgba(0,0,0,0.04);
    z-index: 10;
    position: relative;
}
.merchant-header {
    padding: 16px;
    background-color: #fff;
}
.loading-header {
    padding: 16px;
    background: #fff;
}
.m-info {
    display: flex;
    align-items: flex-start;
}
.m-logo {
    box-shadow: 0 4px 12px rgba(0,0,0,0.08); /* Stronger shadow for logo */
    flex-shrink: 0;
    border: 1px solid #f2f2f2;
}
.m-text {
    margin-left: 12px;
    flex: 1;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    min-height: 76px; /* Match logo height */
}
.m-row-1 {
    display: flex;
    align-items: center;
    margin-bottom: 4px;
}
.m-name {
    font-size: 19px;
    font-weight: 800;
    color: #222;
    margin-right: 8px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    line-height: 1.2;
}

.m-row-time {
    display: flex;
    align-items: center;
    margin-bottom: 6px;
    font-size: 11px;
}
.status-tag {
    background-color: #e8f5e9; /* Light green bg */
    color: #4caf50; /* Green text */
    padding: 1px 6px;
    border-radius: 4px;
    margin-right: 8px;
    font-weight: 500;
    display: inline-block;
}
.status-tag.off {
    background-color: #f5f5f5;
    color: #999;
}
.m-time-text {
    color: #666;
}

.m-notice {
    font-size: 11px;
    color: #999;
    margin-bottom: 6px;
    line-height: 1.4;
    display: flex;
    align-items: center;
}
.m-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
}
.m-tag-item {
    font-size: 10px;
    color: #ff4b33;
    background: #fff0ec;
    padding: 1px 4px;
    border-radius: 2px;
}

/* Menu Layout */
.menu-container {
    flex: 1;
    display: flex;
    overflow: hidden;
    background: #fff;
}
.menu-sidebar {
    width: 90px;
    background: #f7f8fa;
    overflow-y: auto;
    flex-shrink: 0;
}
/* Override Vant sidebar style to look more like Meituan */
:deep(.van-sidebar-item) {
    background-color: #f7f8fa;
    color: #666;
    padding: 14px 12px;
    line-height: 1.4;
    font-size: 13px;
    user-select: none;
}
:deep(.van-sidebar-item--select) {
    background-color: #fff;
    color: #333;
    font-weight: 700;
}
:deep(.van-sidebar-item--select)::before {
    background-color: #FF9800; /* Orange highlight */
    height: 18px;
    width: 3px;
    border-radius: 2px;
}

.menu-content {
    flex: 1;
    overflow-y: auto;
    background: #fff;
    padding-bottom: 80px; /* Spacer for cart bar */
}
.food-list {
    padding: 0;
}
.cat-title {
    padding: 12px 16px;
    font-weight: 600;
    font-size: 13px;
    color: #666;
    background: #fff;
    position: sticky;
    top: 0;
    z-index: 9; /* Ensure strictly above items but below other overlays */
}
.food-item {
    display: flex;
    padding: 12px 16px;
    margin-bottom: 2px;
}
.food-info {
    flex: 1;
    margin-left: 10px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    position: relative;
    padding-bottom: 4px;
    min-height: 80px; /* Ensure height matches image roughly */
}
.food-name {
    font-size: 16px;
    font-weight: 700;
    color: #333;
    margin-bottom: 4px;
    line-height: 1.3;
}
.food-desc {
    font-size: 11px;
    color: #999;
    margin-bottom: 4px;
    line-height: 1.4;
    /* Limit desc lines */
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}
.food-sales {
    font-size: 10px;
    color: #999;
    margin-bottom: 4px;
}
.food-price-row {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    margin-top: auto;
}
.price {
    color: #ff4b33; /* Meituan red/orange */
    font-weight: 700;
    font-size: 15px;
    display: flex;
    align-items: baseline;
}
.price .num {
    font-size: 20px;
    margin-left: 1px;
    letter-spacing: -0.5px;
}

/* Cart Control */
.cart-control {
    display: flex;
    align-items: center;
}
.btn-sub, .btn-add {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    cursor: pointer;
}
.btn-sub {
    border: 1px solid #ddd;
    color: #666;
    background: #fff;
}
.btn-add {
    background: #ffb000; /* Meituan yellow/orange */
    color: #333; /* Dark text on yellow */
    font-weight: bold;
}
.count-num {
    margin: 0 10px;
    font-size: 14px;
    color: #333;
    min-width: 14px;
    text-align: center;
}
.spec-text {
    font-size: 12px;
    background: #ffb000;
    color: #333;
    padding: 4px 10px;
    border-radius: 20px;
    font-weight: bold;
    /* Hide icon if we show text usually, but here we used icon in template. 
       Let's use a pill shape for spec wrapper if detected */
}
/* Pill button for specs override */
.btn-add:has(.spec-text) {
    width: auto;
    height: auto;
    border-radius: 12px;
    padding: 0;
    background: transparent;
}
.btn-add:has(.spec-text) .van-icon {
    display: none;
}
.spec-btn-pill {
    background: #ffb000;
    color: #333;
    padding: 6px 14px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: bold;
}

/* Cart Bar */
.cart-bar {
    position: fixed;
    bottom: 20px;
    left: 16px;
    right: 16px;
    height: 50px;
    background: #222;
    border-radius: 25px;
    display: flex;
    align-items: center;
    z-index: 2000;
    box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    padding-right: 4px;
}
.cart-icon-wrapper {
    position: relative;
    width: 50px;
    height: 50px;
    margin-left: 4px;
}
.cart-circle {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    background: #333;
    position: absolute;
    top: -10px;
    left: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 4px solid #fff; /* White border to float */
    box-shadow: 0 -2px 4px rgba(0,0,0,0.05);
}
.cart-circle.has-goods {
    background: #ffb000;
    color: #333;
}
.badge {
    position: absolute;
    top: -2px;
    right: -2px;
    background: #ff4b33;
    color: #fff;
    font-size: 10px;
    padding: 0 5px;
    min-width: 16px;
    height: 16px;
    line-height: 16px;
    text-align: center;
    border-radius: 8px;
    border: 1px solid #fff;
}
.cart-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    padding-left: 12px;
}
.total-price {
    color: #fff;
    font-size: 18px;
    line-height: 1.2;
}
.total-price .big {
    font-size: 22px;
    font-weight: bold;
}
.empty-text {
    color: #999;
    font-size: 12px;
}
.delivery-tip {
    color: #888;
    font-size: 10px;
    margin-top: 2px;
}
.cart-btn {
    height: 42px;
    padding: 0 24px;
    background: #444;
    color: #999;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: bold;
    border-radius: 0 21px 21px 0; /* Right rounded inside bar */
    border-radius: 21px; /* Floating pill button style inside bar */
    margin-left: 10px;
}
.cart-btn.can-pay {
    background: #ffb000;
    color: #333;
    background-image: linear-gradient(135deg, #ffb000 0%, #ffc040 100%);
}

/* Popup */
.cart-popup {
    padding-bottom: 80px; /* Space for floating bar */
}
.cart-header {
    display: flex;
    justify-content: space-between;
    padding: 12px 16px;
    background: #fcfcfc;
    border-bottom: 1px solid #eee;
    align-items: center;
    border-radius: 12px 12px 0 0;
}
.cart-title {
    font-size: 16px;
    font-weight: bold;
    color: #333;
}
.cart-clear {
    color: #666;
    font-size: 12px;
    display: flex;
    align-items: center;
}
.cart-item-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    border-bottom: 1px solid #f9f9f9;
}
.ci-info {
    flex: 1;
}
.ci-name {
    font-size: 16px;
    color: #333;
    font-weight: 500;
}
.ci-spec {
    font-size: 11px;
    color: #999;
    margin-top: 4px;
}
.ci-price {
    width: 70px;
    text-align: right;
    font-weight: bold;
    color: #333;
    font-size: 16px;
}
.ci-control {
    display: flex;
    align-items: center;
    margin-left: 16px;
}
.ci-control .num {
    margin: 0 10px;
    min-width: 16px;
    text-align: center;
}

/* Spec Popup */
.spec-popup {
    /* Height handled by content */
    max-height: 80%;
    overflow: hidden;
    display: flex;
    flex-direction: column;
}
.spec-content {
    padding: 16px;
    position: relative;
    display: flex;
    flex-direction: column;
    height: 100%;
    overflow: hidden;
}
.spec-header {
    display: flex;
    margin-bottom: 20px;
    flex-shrink: 0;
}
.spec-h-info {
    margin-left: 12px;
    padding-top: 4px;
}
.spec-name {
    font-weight: 700;
    font-size: 18px;
    color: #333;
}
.spec-scroll {
    flex: 1;
    overflow-y: auto;
    padding-bottom: 20px;
}
.spec-group {
    margin-bottom: 20px;
}
.g-title {
    font-size: 14px;
    margin-bottom: 10px;
    color: #333;
    font-weight: 500;
}
.g-opts {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
}
.opt-item {
    border: 1px solid #f5f5f5;
    background: #f5f5f5;
    padding: 6px 16px;
    border-radius: 8px;
    font-size: 13px;
    color: #333;
    transition: all 0.2s;
}
.opt-item.active {
    background: #fff8e1; /* Pale yellow */
    color: #ffb000;
    border-color: #ffb000;
    font-weight: 500;
}
.spec-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: auto;
    padding-top: 10px;
    border-top: 1px solid #f5f5f5;
    flex-shrink: 0;
    background: #fff;
}
.ft-price {
    font-size: 24px;
    color: #ff4b33;
    font-weight: bold;
}
.ft-price::before {
    content: '￥';
    font-size: 14px;
}

/* Detail Popup Styles */
.food-detail-popup {
    background: #fff;
    display: flex;
    flex-direction: column;
}
.detail-container {
    flex: 1;
    overflow-y: auto;
    padding-bottom: 60px;
}
.detail-content {
    padding: 16px;
}
.d-name {
    font-size: 22px;
    font-weight: 800;
    margin-bottom: 8px;
    color: #333;
}
.d-sales {
    color: #999;
    font-size: 12px;
    margin-bottom: 16px;
}
.d-price-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
}
.d-price {
    font-size: 28px;
    color: #ff4b33;
    font-weight: bold;
}
.d-desc-title {
    font-weight: 700;
    margin-bottom: 8px;
    font-size: 15px;
    color: #333;
}
.d-desc {
    color: #666;
    font-size: 14px;
    line-height: 1.6;
    margin-bottom: 20px;
}
.sm-item {
    display: flex;
    justify-content: space-between;
    padding: 12px 0;
    color: #333;
    font-size: 14px;
    border-bottom: 1px dashed #eee;
}
.sm-name {
    flex: 1;
}
.sm-count {
    margin: 0 16px;
    color: #999;
}
.sm-price {
    font-weight: 500;
}

/* Reviews */
.review-container {
    background: #f5f5f5;
    min-height: calc(100vh - 100px); /* Adjust based on header/tabs */
    padding-bottom: 60px; /* Space for cart bar if visible */
}
.review-header {
    background: #fff;
    padding: 20px 16px;
    display: flex;
    align-items: center;
    margin-bottom: 10px;
}
.rh-left {
    text-align: center;
    padding-right: 24px;
    border-right: 1px solid #eee;
}
.rh-score {
    font-size: 28px;
    color: #ffb000;
    font-weight: bold;
    line-height: 1;
    margin-bottom: 4px;
}
.rh-text {
    font-size: 12px;
    color: #666;
}
.rh-right {
    flex: 1;
    padding-left: 24px;
}
.rh-row {
   display: flex;
   align-items: center;
   font-size: 13px;
   color: #666;
}
.rh-row .val {
    font-size: 18px;
    color: #333;
    margin-left: 8px;
}

.review-filter {
    background: #fff;
    padding: 12px 16px;
    border-bottom: 1px solid #f9f9f9;
}
.filter-wrap {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    margin-bottom: 10px;
}
.r-tag {
    padding: 6px 12px;
    background: #f5f5f5;
    color: #666;
    font-size: 12px;
    border-radius: 4px;
    transition: all 0.2s;
}
.r-tag.active {
    background: #fff8e1;
    color: #ffb000;
    font-weight: 500;
}
.img-check {
    font-size: 12px;
    color: #666;
}

.review-item {
    background: #fff;
    padding: 16px;
    margin-bottom: 1px;
}
.user-row {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;
}
.u-avatar {
    margin-right: 10px;
    flex-shrink: 0;
}
.u-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
}
.u-name {
    display: flex;
    justify-content: space-between;
    font-size: 12px;
    color: #333;
    margin-bottom: 2px;
}
.u-date {
    color: #999;
}

.r-content {
    font-size: 14px;
    color: #333;
    line-height: 1.5;
    margin-bottom: 8px;
    white-space: pre-wrap;
}
.r-imgs {
    display: flex;
    flex-wrap: wrap;
    margin-bottom: 8px;
}
.r-order-info {
    background: #f9f9f9;
    padding: 6px 10px;
    border-radius: 4px;
    font-size: 11px;
    color: #999;
    display: inline-flex;
    align-items: center;
    margin-bottom: 8px;
}
.m-reply {
    background: #f5f5f5;
    padding: 10px;
    border-radius: 4px;
    font-size: 12px;
    color: #666;
    margin-top: 8px;
}
.reply-label {
    font-weight: bold;
    margin-bottom: 4px;
}

/* Fix Tabs Layout to fill screen */
.merchant-detail :deep(.van-tabs) {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
}
.merchant-detail :deep(.van-tabs__content) {
    flex: 1;
    overflow: hidden; 
    display: flex;
    flex-direction: column;
}
/* Ensure tab pane fills height so menu-container can scroll internally */
.merchant-detail :deep(.van-tab__pane) {
    height: 100%;
    display: flex;
    flex-direction: column;
    overflow-y: auto; /* Enable scrolling for the tab content (important for Reviews) */
    -webkit-overflow-scrolling: touch;
}
</style>
