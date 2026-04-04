/**
 * 获取当前位置 (增强版)
 */
export const getCurrentLocation = () => {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error('您的浏览器不支持地理定位'))
      return
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        resolve({
          lat: position.coords.latitude,
          lng: position.coords.longitude,
          accuracy: position.coords.accuracy
        })
      },
      (error) => {
        let msg = '定位失败'
        switch (error.code) {
          case 1: msg = '用户拒绝了位置请求'; break
          case 2: msg = '无法获取当前位置信息'; break
          case 3: msg = '获取位置超时'; break
        }
        reject(new Error(msg))
      },
      {
        enableHighAccuracy: true, // 核心：开启高精度模式
        timeout: 5000,           // 5秒超时
        maximumAge: 0            // 不读取缓存位置
      }
    )
  })
}
