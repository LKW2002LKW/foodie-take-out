<template>
  <div class="profile-edit mobile-page">
    <van-nav-bar
      title="个人资料"
      left-text="返回"
      left-arrow
      @click-left="onClickLeft"
    />

    <van-form @submit="onSubmit">
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
        <van-button round block type="primary" native-type="submit" class="submit-btn">
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showSuccessToast, showLoadingToast, closeToast } from 'vant'
import { getUserProfile, updateUserProfile } from '../../api/user'
import { commonUpload } from '../../api/common'

const router = useRouter()
const userInfo = ref({
  nickname: '',
  avatar: '',
  sex: '0', 
  birthday: ''
})

const showSexPicker = ref(false)
const showBirthdayPicker = ref(false)
const minDate = new Date(1950, 0, 1)
const maxDate = new Date()
const currentDate = ref(['1990', '01', '01'])

const sexColumns = [
  { text: '保密', value: '0' },
  { text: '男', value: '1' },
  { text: '女', value: '2' }
]

const sexText = computed(() => {
    const option = sexColumns.find(col => col.value === userInfo.value.sex)
    return option ? option.text : '保密'
})

const initData = async () => {
    showLoadingToast({ message: '加载中...', forbidClick: true, duration: 0 })
    try {
        const res = await getUserProfile()
        closeToast()
        if (res.code === 1 || res.code === 200) {
            const data = res.data
            userInfo.value = {
                nickname: data.nickname || '',
                avatar: data.avatar || '',
                sex: String(data.sex || '0'),
                birthday: data.birthday || ''
            }
            if(data.birthday){
                currentDate.value = data.birthday.split('-')
            }
        }
    } catch (error) {
        closeToast()
        showToast('获取用户信息失败')
    }
}

onMounted(() => {
    initData()
})

const onClickLeft = () => {
    router.back()
}

const afterRead = async (file) => {
    file.status = 'uploading';
    file.message = '上传中...';
    try {
        const formData = new FormData();
        formData.append('file', file.file);
        const res = await commonUpload(formData);
        if (res.code === 1 || res.code === 200) {
            userInfo.value.avatar = res.data;
            file.status = 'done';
            file.message = '上传成功';
            showSuccessToast('头像上传成功');
        } else {
            file.status = 'failed';
            file.message = '上传失败';
            showToast(res.msg || '上传失败');
        }
    } catch (error) {
        file.status = 'failed';
        file.message = '上传失败';
        showToast('上传失败');
    }
};

const onConfirmSex = ({ selectedOptions }) => {
    userInfo.value.sex = selectedOptions[0].value
    showSexPicker.value = false
}

const onConfirmBirthday = ({ selectedValues }) => {
    userInfo.value.birthday = selectedValues.join('-')
    showBirthdayPicker.value = false
}

const onSubmit = async () => {
    showLoadingToast({ message: '保存中...', forbidClick: true })
    try {
        const res = await updateUserProfile(userInfo.value)
        if (res.code === 1 || res.code === 200) {
            showSuccessToast('保存成功')
            // Refresh user info or just stay
        } else {
            showToast(res.msg || '保存失败')
        }
    } catch (error) {
        showToast('保存失败')
    }
}
</script>

<style scoped>
.profile-edit {
  background-color: #f7f8fa;
  min-height: 100vh;
}

.form-group {
  margin-top: 1rem;
}

.submit-wrap {
  margin: 1.6rem;
}

.submit-btn {
  min-height: 4.4rem;
  font-size: 1.4rem;
}

:deep(.van-cell),
:deep(.van-field) {
  min-height: 4.4rem;
}

:deep(.van-cell__title),
:deep(.van-field__label),
:deep(.van-field__control) {
  font-size: 1.4rem;
}
</style>
