<template>
  <div class="address-edit-page">
    <van-nav-bar fixed :title="isEdit ? '修改地址' : '新增地址'" left-arrow @click-left="$router.back()" placeholder />

    <van-form @submit="onSave">
        <!-- Vant AddressEdit 组件功能强大，但为了完全控制字段，我们手写表单或使用 Vant AddressEdit 并自定义 -->
        <!-- 这里为了严格匹配接口字段要求，采用 Vant 只有 UI 组件组合的方式 -->
        
        <van-cell-group inset class="form-group">
            <van-field
                v-model="form.consignee"
                name="consignee"
                label="收货人"
                placeholder="收货人姓名"
                :rules="[{ required: true, message: '请填写收货人' }]"
            />
            <van-field name="sex" label="性别">
                <template #input>
                    <van-radio-group v-model="form.sex" direction="horizontal">
                        <van-radio :name="1">先生</van-radio>
                        <van-radio :name="0">女士</van-radio>
                    </van-radio-group>
                </template>
            </van-field>
            <van-field
                v-model="form.phone"
                name="phone"
                label="手机号"
                placeholder="收货人手机号"
                type="tel"
                :rules="[{ required: true, message: '请填写手机号' }, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式错误' }]"
            />
            <van-field
                v-model="areaText"
                is-link
                readonly
                name="area"
                label="地区"
                placeholder="点击选择省市区"
                @click="showArea = true"
                :rules="[{ required: true, message: '请选择地区' }]"
            />
            <van-popup v-model:show="showArea" position="bottom">
                <van-area
                    :area-list="areaList"
                    @confirm="onAreaConfirm"
                    @cancel="showArea = false"
                />
            </van-popup>
            
            <van-field
                v-model="form.detail"
                name="detail"
                label="详细地址"
                placeholder="街道门牌、楼层房间号等信息"
                rows="2"
                autosize
                type="textarea"
                :rules="[{ required: true, message: '请填写详细地址' }]"
            />
        </van-cell-group>

        <van-cell-group inset class="form-group">
            <van-field name="label" label="标签">
                <template #input>
                    <div class="tags-row">
                        <van-tag 
                            round size="medium" 
                            :type="form.label === '家' ? 'primary' : 'default'"
                            @click="form.label = '家'"
                        >家</van-tag>
                        <van-tag 
                            round size="medium" 
                            :type="form.label === '公司' ? 'primary' : 'default'"
                            @click="form.label = '公司'"
                        >公司</van-tag>
                        <van-tag 
                            round size="medium" 
                            :type="form.label === '学校' ? 'primary' : 'default'"
                            @click="form.label = '学校'"
                        >学校</van-tag>
                         <!-- 允许自定义输入标签 -->
                         <input v-if="!['家','公司','学校'].includes(form.label) && form.label" v-model="form.label" class="custom-tag-input" />
                    </div>
                </template>
            </van-field>
        </van-cell-group>

        <van-cell-group inset class="form-group">
            <van-cell center title="设为默认收货地址">
                <template #right-icon>
                    <van-switch v-model="isDefaultBool" size="24" :disabled="isFirstAddress" />
                </template>
            </van-cell>
            <div v-if="isFirstAddress" class="tip-text">首个地址将自动设为默认地址</div>
        </van-cell-group>

        <div style="margin: 16px;">
            <van-button round block type="primary" native-type="submit">
                保存
            </van-button>
            <van-button 
                v-if="isEdit" 
                round block type="danger" 
                class="delete-btn" 
                @click="onDelete"
                native-type="button"
            >
                删除
            </van-button>
        </div>
    </van-form>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showToast, showSuccessToast, showFailToast, showConfirmDialog } from 'vant';
import { areaList } from '@vant/area-data'; // 需要安装 @vant/area-data
import { addAddress, updateAddress, getAddressDetail, deleteAddress, setDefaultAddress } from '../api/address';

const route = useRoute();
const router = useRouter();

const isEdit = computed(() => route.query.type === 'edit');
const addressId = route.query.id;
const showArea = ref(false);
const areaText = ref('');
const isFirstAddress = ref(false); // 需要判断是否是第一个地址，如果是，默认打开switch且不可关

const form = reactive({
    id: undefined,
    consignee: '',
    sex: 1,
    phone: '',
    provinceCode: '',
    provinceName: '',
    cityCode: '',
    cityName: '',
    districtCode: '',
    districtName: '',
    detail: '',
    label: '家',
    isDefault: 0,
    // 经纬度通常通过地图选点获取，这里暂且模拟或留空
    longitude: 116.480881,
    latitude: 39.908098
});

const isDefaultBool = computed({
    get: () => form.isDefault === 1,
    set: (val) => { form.isDefault = val ? 1 : 0; }
});

const onAreaConfirm = ({ selectedOptions }) => {
    showArea.value = false;
    areaText.value = selectedOptions.map(item => item.text).join('/');
    form.provinceCode = selectedOptions[0]?.value;
    form.provinceName = selectedOptions[0]?.text;
    form.cityCode = selectedOptions[1]?.value;
    form.cityName = selectedOptions[1]?.text;
    form.districtCode = selectedOptions[2]?.value;
    form.districtName = selectedOptions[2]?.text;
};

const initData = async () => {
    if (isEdit.value && addressId) {
        try {
            const res = await getAddressDetail(addressId);
            if (res.code === 1) {
                Object.assign(form, res.data);
                areaText.value = `${form.provinceName}/${form.cityName}/${form.districtName}`;
            }
        } catch (e) {
            showFailToast('加载失败');
        }
    } else {
        // 新增模式，如果是第一个地址，这里其实需要在前端或者后端判断。
        // 根据需求："新增地址：当用户新增地址时，第一个地址自动被设为默认地址。"
        // 既然后端也会处理，前端这里只是为了UI展示。我们假设后端处理了，前端如果不确定是否第一个，默认关。
    }
};

const onSave = async () => {
    const data = { ...form };
    
    try {
        let res;
        if (isEdit.value) {
            res = await updateAddress(data);
            // 这里还有一个逻辑：修改地址时，如果用户勾选了默认地址，需要调用 set default 接口吗？
            // 需求说："修改地址...若需要设置为默认地址，需通过PUT ...接口来修改"
            // 但是修改接口传参里有 isDefault。
            // 按照需求描述："修改地址...保持默认地址状态不变...除非显式设置isDefault=1"
            // 我们可以在 updateAddress 完成后，如果 isDefault 变了且为1，再调一次 setDefaultAddress，
            // 或者假设后端在 update 接口如果接收到 isDefault=1 会自动处理。
            // 为了保险，如果用户显式把非默认改为默认，我们额外调用 setDefault
            
            if (res.code === 1 && form.isDefault === 1) {
                 await setDefaultAddress(form.id);
            }
        } else {
            res = await addAddress(data);
             // 如果是新增，且用户选了默认 (通常新增时也可以选默认)，或者它是第一个(后端处理)
             // 如果接口返回的 id，且用户选了默认，也可以补充调用一次 setDefault
             if (res.code === 1 && form.isDefault === 1 && res.data && res.data.id) {
                 await setDefaultAddress(res.data.id);
             }
        }
        
        if (res.code === 1) {
            showSuccessToast('保存成功');
            router.back();
        } else {
            showFailToast(res.msg || '保存失败');
        }
    } catch (e) {
        showFailToast('保存失败');
    }
};

const onDelete = () => {
    showConfirmDialog({
        title: '确认删除',
        message: '确认删除该地址吗？'
    }).then(async () => {
        try {
            const res = await deleteAddress(form.id);
            if (res.code === 1) {
                showSuccessToast('删除成功');
                router.back();
            } else {
                showFailToast(res.msg || '删除失败');
            }
        } catch (e) {
            showFailToast('删除失败');
        }
    }).catch(() => {});
};

onMounted(() => {
    initData();
});
</script>

<style scoped>
.address-edit-page {
    background: #f7f8fa;
    min-height: 100vh;
    padding-top: 10px;
}
.form-group {
    margin-bottom: 12px;
}
.tags-row {
    display: flex;
    gap: 10px;
}
.delete-btn {
    margin-top: 12px;
}
.tip-text {
    font-size: 12px;
    color: #999;
    padding: 0 16px 10px;
}
.custom-tag-input {
    width: 60px;
    border: 1px solid #ddd;
    border-radius: 4px;
    padding: 0 4px;
    font-size: 12px;
}
</style>