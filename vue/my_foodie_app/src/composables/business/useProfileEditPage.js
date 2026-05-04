import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { closeToast, showLoadingToast, showSuccessToast, showToast } from 'vant'
import { commonUpload } from '@/api/modules/common'
import { profileSexColumns } from '@/constants/profile'
import { getUserProfile, updateUserProfile } from '@/api/modules/user'
import { useUserStore } from '@/stores/modules/userStore'

const DEFAULT_DATE = ['1990', '01', '01']

export const useProfileEditPage = () => {
  const router = useRouter()
  const userStore = useUserStore()

  const userInfo = ref({
    nickname: '',
    avatar: '',
    sex: '0',
    birthday: '',
  })

  const showSexPicker = ref(false)
  const showBirthdayPicker = ref(false)
  const minDate = new Date(1950, 0, 1)
  const maxDate = new Date()
  const currentDate = ref([...DEFAULT_DATE])

  const sexText = computed(() => {
    const option = profileSexColumns.find((column) => column.value === userInfo.value.sex)
    return option ? option.text : '保密'
  })

  const initData = async () => {
    showLoadingToast({ message: '加载中...', forbidClick: true, duration: 0 })
    try {
      const res = await getUserProfile()
      if (res.code === 1 || res.code === 200) {
        const data = res.data || {}
        userInfo.value = {
          nickname: data.nickname || '',
          avatar: data.avatar || '',
          sex: String(data.sex || '0'),
          birthday: data.birthday || '',
        }
        userStore.setUserInfo(data)

        if (data.birthday) {
          currentDate.value = data.birthday.split('-')
        }
      }
    } catch (error) {
      showToast('获取用户信息失败')
    } finally {
      closeToast()
    }
  }

  const onClickLeft = () => {
    router.back()
  }

  const afterRead = async (file) => {
    file.status = 'uploading'
    file.message = '上传中...'

    try {
      const formData = new FormData()
      formData.append('file', file.file)
      const res = await commonUpload(formData)

      if (res.code === 1 || res.code === 200) {
        userInfo.value.avatar = res.data
        file.status = 'done'
        file.message = '上传成功'
        showSuccessToast('头像上传成功')
        return
      }

      file.status = 'failed'
      file.message = '上传失败'
      showToast(res.msg || '上传失败')
    } catch (error) {
      file.status = 'failed'
      file.message = '上传失败'
      showToast('上传失败')
    }
  }

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
        userStore.setUserInfo({
          ...userStore.userInfo,
          ...userInfo.value,
        })
        showSuccessToast('保存成功')
        return
      }

      showToast(res.msg || '保存失败')
    } catch (error) {
      showToast('保存失败')
    } finally {
      closeToast()
    }
  }

  onMounted(() => {
    initData()
  })

  return {
    afterRead,
    currentDate,
    maxDate,
    minDate,
    onClickLeft,
    onConfirmBirthday,
    onConfirmSex,
    onSubmit,
    sexColumns: profileSexColumns,
    sexText,
    showBirthdayPicker,
    showSexPicker,
    userInfo,
  }
}
