import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showLoadingToast, showSuccessToast, showToast } from 'vant'
import { logout, updatePassword } from '@/api/modules/user'

// 修改密码页组合式，完成表单校验、提交与退出重登流程。
export const useChangePasswordPage = () => {
  const router = useRouter()

  const form = ref({
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
  })

  const onClickLeft = () => {
    router.back()
  }

  const confirmValidator = (val) => val === form.value.newPassword

  const onSubmit = async () => {
    showLoadingToast({ message: '提交中...', forbidClick: true })
    try {
      const res = await updatePassword(form.value)
      if (res.code === 1 || res.code === 200) {
        showSuccessToast('修改成功，请重新登录')
        await logout()
        router.push('/login')
        return
      }

      showToast(res.msg || '修改失败')
    } catch (error) {
      showToast('修改失败')
    }
  }

  return {
    confirmValidator,
    form,
    onClickLeft,
    onSubmit,
  }
}
