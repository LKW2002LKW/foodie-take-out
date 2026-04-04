<template>
  <div class="address-edit-page">
    <van-nav-bar
      :title="isEdit ? '修改收货地址' : '新增收货地址'"
      left-arrow
      fixed
      placeholder
      @click-left="$router.back()"
    />

    <div class="content-box">
      <!-- 顶部搜索组合 -->
      <div class="mt-search-wrap">
        <div class="city-box" @click="showCityPopup = true">
          <span class="city-txt van-ellipsis">{{ currentCity }}</span>
          <van-icon name="arrow-down" class="arrow" />
        </div>
        <div class="search-input-box">
          <van-search
            v-model="searchKey"
            placeholder="搜索收货地址"
            background="transparent"
            class="custom-search"
            @update:model-value="onSearchInput"
            @focus="isSearching = true"
          />
        </div>
      </div>

      <!-- 搜索候选列表 -->
      <div v-if="isSearching && tips.length > 0" class="search-overlay">
        <div v-for="tip in tips" :key="tip.id" class="tip-item" @click="onSelectTip(tip)">
          <div class="tip-main">{{ tip.name }}</div>
          <div class="tip-sub">{{ tip.district }}{{ tip.address }}</div>
        </div>
      </div>

      <!-- 地图预览区 -->
      <div class="map-preview">
        <van-image v-if="form.longitude" :src="mapUrl" width="100%" height="160" fit="cover" />
        <div v-else class="map-placeholder">
          <van-icon name="map-marked" size="40" color="#ddd" />
          <p>请选择准确的收货地址</p>
        </div>
      </div>

      <!-- 信息表单 -->
      <van-cell-group inset class="form-group">
        <van-field v-model="form.consignee" label="收货人" placeholder="请填写收货人姓名" required />
        <van-cell title="性别">
          <template #value>
            <van-radio-group v-model="form.sex" direction="horizontal">
              <van-radio :name="1" checked-color="#FFD100">先生</van-radio>
              <van-radio :name="2" checked-color="#FFD100">女士</van-radio>
            </van-radio-group>
          </template>
        </van-cell>
        <van-field v-model="form.phone" label="手机号" type="tel" placeholder="请填写收货人手机号" required />
        
        <!-- 核心优化：点击定位地址触发重新搜索 -->
        <van-field 
          v-model="displayAddress" 
          label="定位地址" 
          readonly 
          placeholder="点击选择地址" 
          required 
          is-link
          @click="triggerReSearch"
        />
        
        <van-field v-model="form.detail" rows="2" autosize label="门牌号" type="textarea" placeholder="例: 16号楼501室" required />
        
        <van-cell title="标签">
          <template #value>
            <van-radio-group v-model="form.label" direction="horizontal">
              <van-radio name="家" checked-color="#FFD100">家</van-radio>
              <van-radio name="公司" checked-color="#FFD100">公司</van-radio>
              <van-radio name="学校" checked-color="#FFD100">学校</van-radio>
            </van-radio-group>
          </template>
        </van-cell>
        <van-cell title="设为默认地址">
          <template #right-icon>
            <van-switch v-model="isDefaultBool" size="20" active-color="#FFD100" />
          </template>
        </van-cell>
      </van-cell-group>

      <div class="action-bar">
        <van-button round block type="primary" color="#FFD100" text-color="#222" :loading="saving" @click="onSave">保存地址</van-button>
        <van-button v-if="isEdit" round block plain class="mt-10" @click="onDelete">删除地址</van-button>
      </div>
    </div>

    <!-- 城市选择弹窗 -->
    <van-popup v-model:show="showCityPopup" position="right" style="width: 100%; height: 100%">
      <div class="city-picker-layout">
        <van-nav-bar title="选择收货城市" left-arrow @click-left="showCityPicker = false" />
        <div class="city-search-box">
          <van-search v-model="citySearchQuery" placeholder="输入城市名筛选" shape="round" />
        </div>
        <div class="city-list-container">
          <van-index-bar :index-list="filteredIndexList" highlight-color="#FFD100">
            <div v-for="group in filteredCityData" :key="group.initial">
              <van-index-anchor :index="group.initial" />
              <van-cell v-for="city in group.list" :key="city" :title="city" @click="onCitySelect(city)" />
            </div>
          </van-index-bar>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { inputTips, getStaticMapUrl, reverseGeocode } from '../services/amap'
import { addAddress, updateAddress, getAddressDetail, deleteAddress } from '../services/address'
import { useLocationStore } from '../store/modules/location'

const route = useRoute(); const router = useRouter(); const locationStore = useLocationStore()
const isEdit = !!route.query.id; const saving = ref(false); const isSearching = ref(false)
const isDefaultBool = ref(false); const locationConfirmed = ref(false)
const searchKey = ref(''); const tips = ref([]); const displayAddress = ref('')
const showCityPopup = ref(false); const citySearchQuery = ref('')
const currentCity = ref(locationStore.city || '北京市')

const cityData = [{ initial: 'A', list: ['阿坝', '安康', '安庆', '鞍山'] }, { initial: 'B', list: ['北京', '保定', '宝鸡', '包头', '蚌埠'] }, { initial: 'C', list: ['成都', '重庆', '长沙', '常州', '沧州'] }, { initial: 'D', list: ['大连', '东莞', '大理', '德州', '大庆'] }, { initial: 'F', list: ['佛山', '福州', '抚顺', '阜阳'] }, { initial: 'G', list: ['广州', '深圳', '贵阳', '桂林', '赣州'] }, { initial: 'H', list: ['杭州', '合肥', '哈尔滨', '海口', '邯郸', '惠州'] }, { initial: 'J', list: ['济南', '嘉兴', '金华', '九江', '吉林'] }, { initial: 'K', list: ['昆明', '开封'] }, { initial: 'L', list: ['兰州', '洛阳', '临沂', '拉萨', '廊坊'] }, { initial: 'M', list: ['马鞍山', '茂名', '绵阳'] }, { initial: 'N', list: ['南京', '南昌', '南宁', '宁波', '南通'] }, { initial: 'Q', list: ['青岛', '泉州', '秦皇岛', '齐齐哈尔'] }, { initial: 'S', list: ['上海', '苏州', '沈阳', '石家庄', '三亚', '汕头'] }, { initial: 'T', list: ['天津', '太原', '唐山', '泰州', '台州'] }, { initial: 'W', list: ['武汉', '无锡', '温州', '潍坊', '威海'] }, { initial: 'X', list: ['西安', '厦门', '西宁', '徐州', '邢台', '咸阳'] }, { initial: 'Y', list: ['扬州', '烟台', '银川', '宜昌', '延安'] }, { initial: 'Z', list: ['郑州', '珠海', '中山', '淄博', '湛江', '枣庄'] }]
const filteredCityData = computed(() => {
  if (!citySearchQuery.value) return cityData
  return cityData.map(g => ({ ...g, list: g.list.filter(c => c.includes(citySearchQuery.value.trim())) })).filter(g => g.list.length > 0)
})
const filteredIndexList = computed(() => filteredCityData.value.map(g => g.initial))

const form = reactive({
  id: route.query.id || null, consignee: '', sex: 1, phone: '', detail: '', label: '家',
  longitude: null, latitude: null, isDefault: 0,
  provinceName: '', cityName: '', districtName: '',
  provinceCode: '', cityCode: '', districtCode: ''
})

const mapUrl = computed(() => getStaticMapUrl(form.longitude, form.latitude))
watch(isDefaultBool, (val) => { form.isDefault = val ? 1 : 0 })

const onSearchInput = (val) => {
  if (!val) { tips.value = []; return }
  clearTimeout(searchTimer)
  searchTimer = setTimeout(async () => {
    const results = await inputTips(val, currentCity.value)
    tips.value = results.filter(t => t.location && typeof t.location === 'string')
  }, 300)
}
let searchTimer = null

// 点击“定位地址”触发逻辑
const triggerReSearch = () => {
  isSearching.value = true
  searchKey.value = displayAddress.value
  onSearchInput(searchKey.value)
}

const onSelectTip = async (tip) => {
  const [lng, lat] = tip.location.split(',')
  form.longitude = parseFloat(lng); form.latitude = parseFloat(lat); 
  displayAddress.value = tip.name // 空格前面的部分
  
  const regeo = await reverseGeocode(lng, lat)
  if (regeo && regeo.addressComponent) {
    const comp = regeo.addressComponent
    form.provinceName = comp.province || ''; form.cityName = (comp.city && comp.city.length > 0) ? comp.city : comp.province
    form.districtName = comp.district || ''; form.districtCode = String(comp.adcode)
    form.provinceCode = form.districtCode.substring(0, 2) + '0000'; form.cityCode = form.districtCode.substring(0, 4) + '00'
    locationConfirmed.value = true
  }
  isSearching.value = false; searchKey.value = tip.name; tips.value = []
}

const onSave = async () => {
  if (!form.consignee || !form.phone || !locationConfirmed.value || !form.detail) return showToast('信息不完整')
  // 合并保存：定位地址 + 空格 + 门牌号
  const finalDetail = `${displayAddress.value} ${form.detail.trim()}`
  const payload = { ...form, detail: finalDetail }
  saving.value = true
  try {
    const res = isEdit ? await updateAddress(payload) : await addAddress(payload)
    if (res.code === 1) { showToast('保存成功'); router.back() } else showToast(res.msg || '保存失败')
  } catch (e) { showToast('提交失败') } finally { saving.value = false }
}

const onDelete = () => {
  showConfirmDialog({ title: '提醒', message: '确认删除该地址？' }).then(async () => {
    await deleteAddress(form.id); router.back()
  })
}

const onCitySelect = (city) => { currentCity.value = city; showCityPopup.value = false; searchKey.value = ''; tips.value = [] }

onMounted(async () => {
  if (isEdit) {
    const res = await getAddressDetail(form.id)
    if (res.code === 1) {
      const data = res.data
      Object.assign(form, data)
      
      // 【核心逻辑】拆分 detail 为：定位地址 + 门牌号
      const rawDetail = data.detail || ''
      const spaceIndex = rawDetail.indexOf(' ')
      if (spaceIndex > -1) {
        displayAddress.value = rawDetail.substring(0, spaceIndex)
        form.detail = rawDetail.substring(spaceIndex + 1)
      } else {
        displayAddress.value = data.address || '' // 兼容无空格老数据
        form.detail = rawDetail
      }
      
      searchKey.value = displayAddress.value
      locationConfirmed.value = true
      isDefaultBool.value = form.isDefault === 1
    }
  }
})
</script>

<style scoped>
.address-edit-page { min-height: 100vh; background: #f7f7f7; padding-bottom: 100px; }
.mt-search-wrap { display: flex; align-items: center; background: #fff; padding: 8px 12px; position: sticky; top: 46px; z-index: 110; }
.city-box { display: flex; align-items: center; gap: 4px; padding-right: 12px; border-right: 1px solid #eee; max-width: 80px; flex-shrink: 0; }
.city-txt { font-size: 14px; font-weight: bold; color: #222; }
.arrow { font-size: 8px; color: #999; }
.search-input-box { flex: 1; }
.custom-search { padding: 0 0 0 12px; }
.search-overlay { position: fixed; top: 96px; left: 0; right: 0; bottom: 0; background: #fff; z-index: 120; overflow-y: auto; padding: 0 16px; }
.tip-item { padding: 16px 0; border-bottom: 1px solid #f5f5f5; }
.tip-main { font-size: 15px; font-weight: bold; }
.tip-sub { font-size: 12px; color: #999; margin-top: 4px; }
.map-preview { height: 160px; background: #f0f0f0; display: flex; align-items: center; justify-content: center; }
.form-group { margin-top: 12px; }
.action-bar { position: fixed; bottom: 0; left: 0; right: 0; padding: 16px; background: #fff; z-index: 100; display: flex; flex-direction: column; gap: 10px; }
.city-picker-layout { display: flex; flex-direction: column; height: 100vh; background: #fff; }
.city-list-container { flex: 1; overflow-y: auto; position: relative; }
:deep(.van-index-bar__sidebar) { right: 4px; z-index: 200; }
</style>
