<template>
  <div class="profile-edit mobile-page">
    <van-nav-bar
      title="个人资料"
      left-arrow
      fixed
      placeholder
      @click-left="onClickLeft"
    />

    <van-form class="page-form" @submit="onSubmit">
      <van-cell-group inset class="form-group">
        <van-cell title="头像" center>
          <template #right-icon>
            <van-uploader :after-read="afterRead" :max-count="1">
                <van-image
                    round
                    width="5rem"
                    height="5rem"
                    :src="userInfo.avatar || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'"
                    fit="cover"
                />
            </van-uploader>
          </template>
        </van-cell>
        
        <van-field
          v-model="userInfo.nickname"
          name="nickname"
          label="昵称"
          placeholder="请输入昵称"
          input-align="right"
        />

        <van-field
          v-model="sexText"
          is-link
          readonly
          name="sex"
          label="性别"
          placeholder="点击选择性别"
          input-align="right"
          @click="showSexPicker = true"
        />

        <van-field
          v-model="userInfo.birthday"
          is-link
          readonly
          name="birthday"
          label="生日"
          placeholder="点击选择生日"
          input-align="right"
          @click="showBirthdayPicker = true"
        />

      </van-cell-group>

      <div class="submit-wrap">
        <van-button round block type="primary" native-type="submit" color="var(--primary-color)" text-color="#1f1f1f" class="submit-btn">
          保存
        </van-button>
      </div>
    </van-form>

    <!-- Popups moved outside form -->
    <van-popup :show="showSexPicker" @update:show="showSexPicker = $event" position="bottom">
        <van-picker
        :columns="sexColumns"
        @confirm="onConfirmSex"
        @cancel="showSexPicker = false"
        />
    </van-popup>

    <van-popup :show="showBirthdayPicker" @update:show="showBirthdayPicker = $event" position="bottom">
        <van-date-picker
        v-model="currentDate"
        title="选择日期"
        :min-date="minDate"
        :max-date="maxDate"
        @confirm="onConfirmBirthday"
        @cancel="showBirthdayPicker = false"
        />
    </van-popup>
  </div>
</template>

<script setup>
import { useProfileEditPage } from '@/composables/business/useProfileEditPage'

const {
  afterRead,
  currentDate,
  maxDate,
  minDate,
  onClickLeft,
  onConfirmBirthday,
  onConfirmSex,
  onSubmit,
  sexColumns,
  sexText,
  showBirthdayPicker,
  showSexPicker,
  userInfo,
} = useProfileEditPage()
</script>

<style scoped>
.profile-edit {
  background: var(--mt-page-bg);
  min-height: 100vh;
  min-height: 100dvh;
}

.page-form {
  padding: 1.2rem;
  padding-bottom: calc(2.4rem + env(safe-area-inset-bottom));
}

.form-group {
  margin-top: 0;
  border-radius: 1.8rem;
  overflow: hidden;
  border: 1px solid rgba(245, 194, 0, 0.12);
  box-shadow: 0 0.8rem 2rem rgba(245, 194, 0, 0.08);
}

.submit-wrap {
  margin-top: 1.2rem;
}

.submit-btn {
  min-height: 4.4rem;
  font-size: 1.4rem;
  border: none;
  box-shadow: 0 0.8rem 1.6rem rgba(245, 194, 0, 0.2);
  color: #1f1f1f !important;
}

:deep(.van-cell),
:deep(.van-field) {
  min-height: 5rem;
  background: linear-gradient(180deg, #ffffff 0%, #fffdf7 100%);
}

:deep(.van-cell__title),
:deep(.van-field__label),
:deep(.van-field__control) {
  font-size: 1.6rem;
}

:deep(.van-cell::after),
:deep(.van-field::after) {
  left: 1.6rem;
  right: 1.6rem;
  border-color: rgba(245, 194, 0, 0.12);
}

:deep(.van-field__label),
:deep(.van-cell__title) {
  color: var(--mt-strong);
  font-weight: 700;
}

:deep(.van-field__control),
:deep(.van-field__control::placeholder) {
  font-size: 1.6rem;
}

:deep(.van-field__control) {
  color: #1f1f1f;
}

:deep(.van-cell__value) {
  color: #1f1f1f;
  font-size: 1.5rem;
}

:deep(.submit-btn .van-button__text) {
  color: #1f1f1f !important;
}
</style>
