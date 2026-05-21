<template>
  <div class="address-edit-page mobile-page">
    <van-nav-bar
      :title="isEdit ? '修改收货地址' : '新增收货地址'"
      left-arrow
      fixed
      placeholder
      @click-left="$router.back()"
    />

    <div class="content-box">
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
            @focus="onSearchFocus"
          />
        </div>
      </div>

      <div class="map-preview">
        <div ref="mapRef" class="map-canvas"></div>
        <div v-if="mapLoading" class="map-loading-mask">
          <van-loading size="2.2rem" color="var(--primary-color)" vertical>地图加载中...</van-loading>
        </div>
        <div class="map-center-pin" :class="{ 'is-loading': mapResolving }">
          <div class="map-center-pin__dot"></div>
        </div>
        <div class="map-address-tip van-ellipsis">
          {{ displayAddress || '拖动地图选择准确地址' }}
        </div>
        <button type="button" class="map-locate-btn" @click="relocateMapToCurrent">
          <van-icon name="aim" size="1.9rem" color="var(--mt-strong)" />
        </button>
      </div>

      <!-- 信息表单 -->
      <van-cell-group inset class="form-group">
        <van-field v-model="form.consignee" label="收货人" placeholder="请填写收货人姓名" required />
        <van-cell title="性别">
          <template #value>
            <van-radio-group v-model="form.sex" direction="horizontal">
              <van-radio :name="1" checked-color="#ffe28a">先生</van-radio>
              <van-radio :name="2" checked-color="#ffe28a">女士</van-radio>
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
              <van-radio name="家" checked-color="#ffe28a">家</van-radio>
              <van-radio name="公司" checked-color="#ffe28a">公司</van-radio>
              <van-radio name="学校" checked-color="#ffe28a">学校</van-radio>
            </van-radio-group>
          </template>
        </van-cell>
        <van-cell title="设为默认地址">
          <template #right-icon>
            <van-switch v-model="isDefaultBool" size="2rem" active-color="#ffe28a" />
          </template>
        </van-cell>
      </van-cell-group>

      <div class="action-bar">
        <van-button
          v-if="isEdit"
          round
          plain
          class="action-btn action-btn--delete"
          @click="onDelete"
        >
          删除地址
        </van-button>
        <van-button
          round
          class="action-btn action-btn--save"
          :loading="saving"
          @click="onSave"
        >
          保存地址
        </van-button>
      </div>
    </div>

    <van-popup
      :show="showLocationPicker"
      @update:show="showLocationPicker = $event"
      position="bottom"
      round
      class="location-picker-popup"
      :style="{ height: '56%' }"
    >
      <div class="location-picker-layout">
        <div class="picker-sheet-header">
          <div class="picker-sheet-title">{{ searchKey ? '搜索结果' : '附近地址推荐' }}</div>
          <div class="picker-sheet-close" @click="showLocationPicker = false">
            <van-icon name="cross" />
          </div>
        </div>

        <div class="picker-map-preview">
          <van-image v-if="pickerMapUrl" :src="pickerMapUrl" width="100%" height="16rem" fit="cover" />
          <div v-else class="map-placeholder">
            <van-icon name="map-marked" size="4rem" color="var(--mt-divider)" />
            <p>请选择附近推荐地址</p>
          </div>
        </div>

        <div class="picker-list-wrap">
          <div
            v-for="tip in tips"
            :key="tip.id"
            class="picker-tip-item"
            :class="{ 'picker-tip-item--active': selectedTipId === tip.id }"
            @click="selectLocationTip(tip)"
          >
            <div class="picker-tip-info">
              <div class="tip-main">{{ tip.name }}</div>
              <div class="tip-sub">{{ tip.district }}{{ tip.address }}</div>
            </div>
            <div class="picker-tip-check">
              <div class="mt-checkbox" :class="{ 'checked': selectedTipId === tip.id }">
                <van-icon name="success" class="check-icon" />
              </div>
            </div>
          </div>
          <van-empty v-if="tips.length === 0" image-size="60" description="暂无可选地址" />
        </div>

        <div class="picker-action-bar safe-area-bottom">
          <van-button
            round
            block
            type="primary"
            color="var(--primary-color)"
            text-color="var(--mt-strong)"
            :disabled="!selectedTipId"
            @click="confirmLocationSelection"
          >
            确认所选地址
          </van-button>
        </div>
      </div>
    </van-popup>

    <!-- 城市选择弹窗 -->
    <van-popup :show="showCityPopup" @update:show="showCityPopup = $event" position="right" style="width: 100%; height: 100%">
      <div class="city-picker-layout">
        <van-nav-bar title="选择收货城市" left-arrow @click-left="showCityPopup = false" />
        <div class="city-search-box">
          <van-search v-model="citySearchQuery" placeholder="输入城市名筛选" shape="round" />
        </div>
        <div class="city-list-container">
          <van-index-bar :index-list="filteredIndexList" :highlight-color="'var(--primary-color)'">
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
import { useAddressEditPage } from '@/composables/business/useAddressEditPage'

const {
  confirmLocationSelection,
  currentCity,
  citySearchQuery,
  displayAddress,
  filteredCityData,
  filteredIndexList,
  form,
  isDefaultBool,
  isEdit,
  mapLoading,
  mapRef,
  mapResolving,
  onCitySelect,
  onDelete,
  onSave,
  onSearchFocus,
  onSearchInput,
  pickerMapUrl,
  relocateMapToCurrent,
  saving,
  searchKey,
  selectLocationTip,
  selectedTipId,
  showCityPopup,
  showLocationPicker,
  tips,
  triggerReSearch,
} = useAddressEditPage()
</script>

<style scoped>
.address-edit-page {
  min-height: 100vh;
  background: var(--mt-page-bg);
  padding-bottom: calc(10rem + env(safe-area-inset-bottom));
}

.content-box {
  padding: 1.2rem 1.2rem 0;
}

.mt-search-wrap {
  display: flex;
  align-items: center;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(255, 249, 233, 0.98) 100%);
  padding: 0.8rem 1rem;
  border: 1px solid rgba(245, 194, 0, 0.14);
  border-radius: 1.6rem;
  box-shadow: 0 0.6rem 1.6rem rgba(245, 194, 0, 0.08);
  margin: 1.2rem 1.2rem 0;
}

.city-box {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0 1rem 0 0.2rem;
  border-right: 1px solid rgba(245, 194, 0, 0.16);
  max-width: 8.8rem;
  flex-shrink: 0;
  min-height: 4.4rem;
}

.city-txt {
  font-size: 1.4rem;
  font-weight: 700;
  color: var(--mt-strong);
}

.arrow { font-size: 0.8rem; color: var(--primary-color-dark); }
.search-input-box { flex: 1; }
.custom-search { padding: 0 0 0 1rem; }

:deep(.custom-search .van-search__content) {
  min-height: 4rem;
  border-radius: 999px;
  background: linear-gradient(180deg, #fffef9 0%, #fff7de 100%);
  border: 1px solid rgba(245, 194, 0, 0.14);
  box-shadow: inset 0 0 0 0.1rem rgba(255, 255, 255, 0.7);
}

:deep(.custom-search .van-field__control) {
  font-size: 1.6rem;
}

:deep(.custom-search .van-field__control::placeholder) {
  font-size: 1.6rem;
}

.tip-main { font-size: 1.5rem; font-weight: 700; }
.tip-sub { font-size: 1.2rem; color: var(--mt-muted); margin-top: 0.4rem; }

.map-preview {
  height: 21rem;
  background: linear-gradient(180deg, #fffdf5 0%, #fff3cf 100%);
  position: relative;
  border-radius: 1.8rem;
  overflow: hidden;
  border: 1px solid rgba(245, 194, 0, 0.14);
  box-shadow: 0 0.8rem 1.8rem rgba(245, 194, 0, 0.08);
}

.map-canvas {
  width: 100%;
  height: 100%;
}

.map-loading-mask {
  position: absolute;
  inset: 0;
  z-index: 5;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 253, 245, 0.78);
  backdrop-filter: blur(1px);
}

.map-center-pin {
  position: absolute;
  left: 50%;
  top: 50%;
  z-index: 6;
  width: 2.6rem;
  height: 3.6rem;
  transform: translate(-50%, calc(-100% + 1.2rem));
  pointer-events: none;
}

.map-center-pin::before {
  content: '';
  position: absolute;
  left: 50%;
  top: 0;
  width: 2.6rem;
  height: 2.6rem;
  border-radius: 50% 50% 50% 0;
  transform: translateX(-50%) rotate(-45deg);
  background: linear-gradient(180deg, #ffe37b 0%, var(--primary-color) 100%);
  box-shadow: 0 0.4rem 1rem rgba(245, 194, 0, 0.26);
}

.map-center-pin__dot {
  position: absolute;
  left: 50%;
  top: 0.8rem;
  width: 0.9rem;
  height: 0.9rem;
  border-radius: 50%;
  transform: translateX(-50%);
  background: var(--mt-strong);
  z-index: 1;
}

.map-center-pin.is-loading {
  animation: map-pin-bounce 0.9s ease-in-out infinite;
}

.map-address-tip {
  position: absolute;
  left: 1rem;
  right: 5.8rem;
  bottom: 1rem;
  z-index: 6;
  padding: 0.7rem 1rem;
  border-radius: 999rem;
  background: rgba(255, 255, 255, 0.88);
  color: var(--mt-strong);
  font-size: 1.15rem;
  font-weight: 700;
  box-shadow: 0 0.5rem 1.2rem rgba(0, 0, 0, 0.08);
}

.map-locate-btn {
  position: absolute;
  right: 1rem;
  bottom: 1rem;
  z-index: 6;
  width: 3.8rem;
  height: 3.8rem;
  border: none;
  border-radius: 50%;
  background: linear-gradient(180deg, #fffdf4 0%, #ffe9a0 100%);
  box-shadow: 0 0.6rem 1.2rem rgba(245, 194, 0, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.amap-logo),
:deep(.amap-copyright) {
  opacity: 0.72;
}

.map-placeholder {
  text-align: center;
  font-size: 1.2rem;
  color: var(--mt-muted);
}

@keyframes map-pin-bounce {
  0%, 100% { transform: translate(-50%, calc(-100% + 1.2rem)); }
  50% { transform: translate(-50%, calc(-100% + 0.8rem)); }
}

.form-group {
  margin-top: 1.2rem;
  background: transparent;
}

.action-bar {
  margin-top: 1.2rem;
  margin-bottom: calc(2.4rem + env(safe-area-inset-bottom));
  padding: 1.2rem;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(255, 248, 235, 0.98) 100%);
  border: 1px solid rgba(245, 194, 0, 0.14);
  border-radius: 1.8rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  box-shadow: 0 0.8rem 2rem rgba(0, 0, 0, 0.08);
}

.action-btn {
  min-height: 4.4rem;
  flex: 1;
}

.action-btn--delete {
  border-color: rgba(255, 120, 120, 0.22);
  color: #d64b4b;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96) 0%, rgba(255, 245, 245, 0.98) 100%);
}

.action-btn--save {
  background: linear-gradient(180deg, #ffe27f 0%, #f5c200 100%) !important;
  color: #1f1f1f !important;
  border: none !important;
  box-shadow: 0 0.8rem 1.6rem rgba(245, 194, 0, 0.2);
}

:deep(.action-btn--save .van-button__text) {
  color: #1f1f1f !important;
}

.location-picker-layout {
  height: 100%;
  background: var(--mt-page-bg);
  display: flex;
  flex-direction: column;
}

.location-picker-popup {
  overflow: hidden;
}

.picker-sheet-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.4rem 1.6rem 0.8rem;
}

.picker-sheet-title {
  font-size: 1.5rem;
  font-weight: 800;
  color: var(--mt-strong);
}

.picker-sheet-close {
  width: 3rem;
  height: 3rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--mt-muted);
  background: rgba(255, 255, 255, 0.76);
  border: 1px solid rgba(245, 194, 0, 0.12);
}

.picker-map-preview {
  margin: 0 1.2rem 1.2rem;
  height: 12rem;
  background: linear-gradient(180deg, #fffdf5 0%, #fff3cf 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 1.8rem;
  overflow: hidden;
  border: 1px solid rgba(245, 194, 0, 0.14);
  box-shadow: 0 0.8rem 1.8rem rgba(245, 194, 0, 0.08);
}

.picker-list-wrap {
  flex: 1;
  overflow-y: auto;
  padding: 0 1.2rem 8.8rem;
}

.picker-tip-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.4rem 1.2rem;
  margin-bottom: 1rem;
  background: linear-gradient(180deg, #ffffff 0%, #fffdf7 100%);
  border: 1px solid rgba(245, 194, 0, 0.1);
  border-radius: 1.6rem;
  box-shadow: 0 0.4rem 1rem rgba(245, 194, 0, 0.05);
}

.picker-tip-item--active {
  border-color: rgba(245, 194, 0, 0.34);
  box-shadow: 0 0.6rem 1.4rem rgba(245, 194, 0, 0.12);
}

.picker-tip-info {
  flex: 1;
  min-width: 0;
}

.picker-tip-check {
  flex-shrink: 0;
}

.picker-action-bar {
  position: fixed;
  left: 1.2rem;
  right: 1.2rem;
  bottom: 1rem;
  padding: 1.2rem;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(255, 248, 235, 0.98) 100%);
  border: 1px solid rgba(245, 194, 0, 0.14);
  border-radius: 1.8rem;
  box-shadow: 0 0.8rem 2rem rgba(0, 0, 0, 0.08);
  z-index: 20;
}

.city-picker-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: var(--mt-card-bg);
}

.city-search-box { padding: 0 1.2rem; }

.city-list-container { flex: 1; overflow-y: auto; position: relative; }

:deep(.van-index-bar__sidebar) { right: 0.4rem; z-index: 200; }

:deep(.van-field),
:deep(.van-cell) {
  min-height: 5rem;
}

:deep(.van-field__label),
:deep(.van-field__control),
:deep(.van-cell__title),
:deep(.van-cell__value) {
  font-size: 1.4rem;
}

:deep(.form-group.van-cell-group--inset) {
  margin: 1.2rem 0 0;
  border-radius: 1.8rem;
  overflow: hidden;
  box-shadow: 0 0.8rem 2rem rgba(245, 194, 0, 0.08);
  border: 1px solid rgba(245, 194, 0, 0.12);
}

:deep(.form-group .van-cell) {
  padding: 1.4rem 1.6rem;
  background: linear-gradient(180deg, #ffffff 0%, #fffdf7 100%);
}

:deep(.form-group .van-cell:not(:last-child)::after) {
  left: 1.6rem;
  right: 1.6rem;
  border-color: rgba(245, 194, 0, 0.12);
}

:deep(.form-group .van-field__label),
:deep(.form-group .van-cell__title) {
  color: var(--mt-strong);
  font-weight: 700;
  width: 7.2rem;
  flex: none;
}

:deep(.form-group .van-field__control) {
  color: #1f1f1f;
  font-size: 1.6rem;
  font-weight: 500;
}

:deep(.form-group .van-field__control::placeholder) {
  color: var(--text-color-placeholder);
  font-size: 1.6rem;
}

:deep(.form-group textarea.van-field__control) {
  font-size: 1.6rem;
  line-height: 1.5;
  color: #1f1f1f;
}

:deep(.form-group .van-field__value),
:deep(.form-group .van-cell__value),
:deep(.form-group .van-field__word-limit) {
  color: #2a2a2a;
}

:deep(.form-group .van-radio__label),
:deep(.form-group .van-cell__right-icon) {
  color: #1f1f1f;
}

:deep(.form-group .van-radio-group--horizontal) {
  gap: 1.4rem;
}

:deep(.form-group .van-radio) {
  align-items: center;
}

:deep(.form-group .van-radio__icon .van-badge__wrapper) {
  width: 1.8rem;
  height: 1.8rem;
  border: 1px solid rgba(245, 194, 0, 0.5);
  background: #fffdf7;
  color: transparent;
}

:deep(.form-group .van-radio__icon--checked .van-badge__wrapper) {
  background: #fff1a8;
  border-color: #f5c200;
  color: #b57a00;
}

:deep(.form-group .van-radio__label) {
  color: var(--mt-strong);
}

:deep(.form-group .van-switch) {
  transform: scale(0.96);
}

:deep(.form-group .van-switch--on) {
  background: #ffe7a3;
  border-color: #f2cb4d;
}

:deep(.form-group .van-switch__node) {
  background: #fffdf7;
}

:deep(.location-picker-layout .van-nav-bar) {
  background: var(--mt-card-bg);
}
</style>
