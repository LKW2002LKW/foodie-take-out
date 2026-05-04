import request from './request'

/**
 * 文件上传
 * @param {FormData} data - 包含 file 的 FormData
 */
export const commonUpload = (data) => {
  return request({
    url: '/user/common/upload',
    method: 'post',
    data,
    headers: {
        'Content-Type': 'multipart/form-data'
    }
  })
}
