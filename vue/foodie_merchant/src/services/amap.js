import axios from 'axios'

let amapSdkPromise = null

const amapInstance = axios.create({
  baseURL: '/amap-api',
  params: {
    key: import.meta.env.VITE_AMAP_KEY,
  },
})

export const loadAmapSdk = async () => {
  if (window.AMap) return window.AMap
  if (amapSdkPromise) return amapSdkPromise

  amapSdkPromise = new Promise((resolve, reject) => {
    const amapKey = import.meta.env.VITE_AMAP_KEY
    if (!amapKey) {
      reject(new Error('缺少高德地图 Key 配置'))
      return
    }

    const script = document.createElement('script')
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${amapKey}`
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
    return (res.data?.tips || []).filter(item => item.location)
  } catch (error) {
    return []
  }
}

export const reverseGeocode = async (lng, lat) => {
  try {
    const res = await amapInstance.get('/v3/geocode/regeo', {
      params: {
        location: `${lng},${lat}`,
        extensions: 'all'
      }
    })
    return res.data?.status === '1' ? res.data.regeocode : null
  } catch (error) {
    return null
  }
}

export const geocode = async (address, city = '') => {
  try {
    const res = await amapInstance.get('/v3/geocode/geo', {
      params: {
        address,
        city
      }
    })

    if (res.data?.status !== '1' || !res.data?.geocodes?.length) {
      return null
    }

    const [lng, lat] = String(res.data.geocodes[0].location || '').split(',')
    return lng && lat ? {
      lng: Number(lng),
      lat: Number(lat),
      adcode: res.data.geocodes[0].adcode || ''
    } : null
  } catch (error) {
    return null
  }
}
