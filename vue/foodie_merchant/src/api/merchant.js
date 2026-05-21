import request from '@/utils/request'
import { appEnv } from '@/config/env'

// 商户资料相关原始请求实现，供模块层统一导出复用。
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

// Logo 上传走独立文件接口，这里统一返回上传地址，避免页面拼接环境变量。
export const getMerchantLogoUploadUrl = () => `${appEnv.apiBaseUrl}/merchant/info/upload/logo`
