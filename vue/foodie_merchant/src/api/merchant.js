import request from '@/utils/request'
import { appEnv } from '@/config/env'

export const getCurrentMerchantInfo = () => request({
  url: '/merchant/info/current',
  method: 'get',
})

export const updateMerchantInfo = (data) => request({
  url: '/merchant/info',
  method: 'put',
  data,
})

export const updateMerchantStatus = (status) => request({
  url: '/merchant/info/status',
  method: 'put',
  data: { status },
})

export const updateMerchantBusinessHours = (businessHours) => request({
  url: '/merchant/info/businessHours',
  method: 'put',
  data: { businessHours },
})

export const getMerchantLogoUploadUrl = () => `${appEnv.apiBaseUrl}/merchant/info/upload/logo`
