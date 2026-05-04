import { showConfirmDialog, showToast } from 'vant'
import { addAddress, deleteAddress, getAddressDetail, updateAddress } from '@/api/modules/address'

export const useAddressEditActions = ({
  currentCity,
  displayAddress,
  form,
  isDefaultBool,
  isEdit,
  locationConfirmed,
  router,
  saving,
  searchKey,
}) => {
  const loadAddressDetail = async () => {
    if (!isEdit || !form.id) return

    const res = await getAddressDetail(form.id)
    if (res.code !== 1) return

    const data = res.data
    Object.assign(form, data)
    form.sex = Number(data.sex) === 2 ? 2 : 1
    form.isDefault = Number(data.isDefault) === 1 ? 1 : 0
    form.label = typeof data.label === 'string' && data.label.trim()
      ? data.label.trim()
      : '家'

    const rawDetail = data.detail || ''
    const spaceIndex = rawDetail.indexOf(' ')
    if (spaceIndex > -1) {
      displayAddress.value = rawDetail.substring(0, spaceIndex)
      form.detail = rawDetail.substring(spaceIndex + 1)
    } else {
      displayAddress.value = data.address || ''
      form.detail = rawDetail
    }

    searchKey.value = displayAddress.value
    currentCity.value = data.cityName || currentCity.value
    locationConfirmed.value = true
    isDefaultBool.value = Number(form.isDefault) === 1
  }

  const onSave = async () => {
    if (!form.consignee || !form.phone || !locationConfirmed.value || !form.detail) {
      showToast('信息不完整')
      return
    }

    const finalDetail = `${displayAddress.value} ${form.detail.trim()}`
    const payload = { ...form, detail: finalDetail }
    saving.value = true

    try {
      const res = isEdit ? await updateAddress(payload) : await addAddress(payload)
      if (res.code === 1) {
        showToast('保存成功')
        router.back()
      } else {
        showToast(res.msg || '保存失败')
      }
    } catch (error) {
      showToast('提交失败')
    } finally {
      saving.value = false
    }
  }

  const onDelete = async () => {
    try {
      await showConfirmDialog({ title: '提醒', message: '确认删除该地址？' })
    } catch (error) {
      return
    }

    await deleteAddress(form.id)
    router.back()
  }

  return {
    loadAddressDetail,
    onDelete,
    onSave,
  }
}
