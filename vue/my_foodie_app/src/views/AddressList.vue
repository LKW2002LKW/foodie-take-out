<template>
  <div class="address-list-page">
    <van-nav-bar fixed title="选择收货地址" left-arrow @click-left="$router.back()" placeholder />
    
    <div class="list-content">
        <!-- Current Location Option (Only in Select Mode) -->
        <div v-if="isSelectMode" class="current-loc-bar" @click="onRelocate">
            <div class="loc-left">
                <van-icon name="aim" class="loc-icon" />
                <span class="loc-text">{{ locationStore.isLocated ? '当前定位' : '点击定位' }}</span>
                <span class="loc-addr">{{ locationStore.address }}</span>
            </div>
            <span class="loc-tip" v-if="!locationStore.isLocated">重新定位</span>
        </div>

        <van-address-list
          v-model="chosenAddressId"
          :list="list"
          default-tag-text="默认"
          @add="onAdd"
          @edit="onEdit"
          @select="onSelect"
          @select-default="onSetDefault"
        />
        
        <div v-if="loading" class="loading-wrapper">
             <van-loading type="spinner" />
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { showToast, showSuccessToast, showFailToast, showLoadingToast, closeToast } from 'vant';
import { getAddressList } from '../api/address';
import { useLocationStore } from '../store/modules/location';

const router = useRouter();
const route = useRoute();
const locationStore = useLocationStore();
const chosenAddressId = ref('');
const list = ref([]);
const loading = ref(false);

// 判断是来选择地址的还是管理的
const isSelectMode = route.query.mode === 'select';

const onRelocate = () => {
    showLoadingToast({ message: '定位中...', forbidClick: true, duration: 5000 });
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition((pos) => {
            closeToast();
            const longitude = pos.coords.longitude;
            const latitude = pos.coords.latitude;
            // 简单显示经纬度，实际项目应调用逆地理编码接口(如百度/高德Web API)获取路名
            const address = `当前位置 (经度:${longitude.toFixed(2)}, 纬度:${latitude.toFixed(2)})`;
            
            locationStore.setLocation({ address, longitude, latitude });
            showSuccessToast('定位成功');
            setTimeout(() => {
                router.back();
            }, 500);
        }, (err) => {
            closeToast();
            let msg = '定位失败';
            switch(err.code) {
                case err.PERMISSION_DENIED: msg = '定位权限被拒绝'; break;
                case err.POSITION_UNAVAILABLE: msg = '位置不可用'; break;
                case err.TIMEOUT: msg = '定位超时'; break;
            }
            showFailToast(msg);
        });
    } else {
        closeToast();
        showFailToast('当前环境不支持定位');
    }
};

const loadList = async () => {
    loading.value = true;
    try {
        const res = await getAddressList();
        if (res.code === 1) {
            // 映射后端数据到 Vant AddressList 组件需要的格式
            // Vant 需要: id, name, tel, address, isDefault
            list.value = (res.data || []).map(item => ({
                id: item.id,
                name: item.consignee,
                tel: item.phone,
                address: item.fullAddress || (item.provinceName + item.cityName + item.districtName + item.detail),
                isDefault: item.isDefault === 1,
                // 保留原始数据以便编辑使用
                original: item
            }));
            
            // 如果是选择模式，可以回显选中的地址
            if (isSelectMode && list.value.length > 0) {
                 // 优先显示 store 中的选中状态? 此处简化为如果有默认选中默认
                 // 也可以根据 UserStore/LocationStore 之前的选择 ID 来反选
                 const def = list.value.find(i => i.isDefault);
                 if (def) chosenAddressId.value = def.id;
            }
        }
    } catch (e) {
        showFailToast('获取地址列表失败');
    } finally {
        loading.value = false;
    }
};

const onAdd = () => {
    router.push('/address/edit?type=add');
};

const onEdit = (item) => {
    router.push(`/address/edit?type=edit&id=${item.id}`);
};

const onSelect = (item) => {
    // 来源：下单确认页 (选择收货地址)
    if (route.query.from === 'createOrder') {
        router.replace({
            path: '/order/create',
            query: { addressId: item.id }
        });
        return;
    }

    // 来源：首页 (选择定位地址)
    if (isSelectMode) {
        // 更新 Store
        const addr = item.address; 
        const lng = item.original.longitude || 116.40; // Fallback if backend null
        const lat = item.original.latitude || 39.90;
        
        locationStore.setLocation({
            address: addr,
            longitude: lng,
            latitude: lat
        });

        showToast('已切换地址');
        router.back();
    }
};

onMounted(() => {
    loadList();
});
</script>

<style scoped>
.address-list-page {
    background: #f7f8fa;
    min-height: 100vh;
}
.loading-wrapper {
    text-align: center;
    padding-top: 20px;
}
.current-loc-bar {
    background: #fff;
    padding: 12px 16px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
    border-bottom: 1px solid #eee;
}
.loc-left {
    display: flex;
    align-items: center;
    flex: 1;
    overflow: hidden;
}
.loc-icon {
    font-size: 18px;
    margin-right: 6px;
    color: #333;
}
.loc-text {
    font-weight: bold;
    margin-right: 10px;
    min-width: 60px;
}
.loc-addr {
    font-size: 14px;
    color: #666;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}
.loc-tip {
    font-size: 12px;
    color: #FF9800;
    margin-left: 10px;
}
</style>
