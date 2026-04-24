import axios from 'axios'

let amapSdkPromise = null

/**
 * 高德地图服务封装 (安全脱敏版)
 * 请求由 Vite Proxy 转发，在服务器端自动填充 Key
 */
const amapInstance = axios.create({
  baseURL: '/amap-api'
})

export const loadAmapSdk = async () => {
  if (window.AMap) return window.AMap
  if (amapSdkPromise) return amapSdkPromise

  amapSdkPromise = new Promise((resolve, reject) => {
    const script = document.createElement('script')
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${import.meta.env.VITE_AMAP_KEY}`
    script.async = true
    script.onload = () => {
      if (window.AMap) {
        resolve(window.AMap)
      } else {
        reject(new Error('高德地图加载失败'))
      }
    }
    script.onerror = () => reject(new Error('高德地图脚本加载失败'))
    document.head.appendChild(script)
  })

  return amapSdkPromise
}

/**
 * 地址输入联想
 */
export const inputTips = async (keywords, city = '') => {
  try {
    const res = await amapInstance.get('/v3/assistant/inputtips', {
      params: { 
        keywords, 
        city, 
        citylimit: city ? true : false,
        datatype: 'all' 
      }
    })
    return res.data.tips || []
  } catch (e) {
    return []
  }
}

/**
 * 逆地理编码
 */
export const reverseGeocode = async (lng, lat) => {
  try {
    const res = await amapInstance.get('/v3/geocode/regeo', {
      params: { 
        location: `${lng},${lat}`, 
        extensions: 'all' 
      }
    })
    return res.data.status === '1' ? res.data.regeocode : null
  } catch (e) {
    return null
  }
}

/**
 * 地理编码
 */
export const geocode = async (address) => {
  try {
    const res = await amapInstance.get('/v3/geocode/geo', {
      params: { address }
    })
    return (res.data.status === '1' && res.data.geocodes.length > 0) ? {
      lng: parseFloat(res.data.geocodes[0].location.split(',')[0]),
      lat: parseFloat(loc = res.data.geocodes[0].location.split(',')[1]),
      adcode: res.data.geocodes[0].adcode
    } : null
  } catch (e) {
    return null
  }
}

/**
 * IP 定位
 */
export const ipLocation = async () => {
  try {
    const res = await amapInstance.get('/v3/ip')
    if (res.data.status === '1') {
      return { city: res.data.city, adcode: res.data.adcode }
    }
    return null
  } catch (e) {
    return null
  }
}

/**
 * 获取周边POIs
 */
export const getAroundPois = async (lng, lat, keywords = '') => {
  try {
    const res = await amapInstance.get('/v3/place/around', {
      params: { 
        location: `${lng},${lat}`, 
        keywords, 
        radius: 1000, 
        types: '地名地址信息|住宅区|相关场所', 
        sortrule: 'distance' 
      }
    })
    return res.data.pois || []
  } catch (e) {
    return []
  }
}

/**
 * 静态图 URL (此方法由于涉及外部域名直连，Key 仍会暴露，建议仅在内部系统使用)
 * 若必须在客户端完全隐藏，则需后端协助生成 Base64 图片返回
 */
export const getStaticMapUrl = (lng, lat) => {
  if (!lng || !lat) return ''
  // 静态图 API 必须带 Key 访问，暂无法通过简单 Proxy 隐藏，因为它是给 <img> 标签直接用的
  const AMAP_KEY = import.meta.env.VITE_AMAP_KEY
  return `https://restapi.amap.com/v3/staticmap?location=${lng},${lat}&zoom=15&size=400*200&markers=mid,,A:${lng},${lat}&key=${AMAP_KEY}`
}
