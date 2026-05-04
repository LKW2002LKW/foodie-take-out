import axios from 'axios'

let amapSdkPromise = null

const amapInstance = axios.create({
  baseURL: '/amap-api',
})

export const loadAmapSdk = async () => {
  if (window.AMap) {
    return window.AMap
  }

  if (amapSdkPromise) {
    return amapSdkPromise
  }

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

export const inputTips = async (keywords, city = '') => {
  try {
    const res = await amapInstance.get('/v3/assistant/inputtips', {
      params: {
        city,
        citylimit: Boolean(city),
        datatype: 'all',
        keywords,
      },
    })

    return res.data.tips || []
  } catch (error) {
    return []
  }
}

export const reverseGeocode = async (lng, lat) => {
  try {
    const res = await amapInstance.get('/v3/geocode/regeo', {
      params: {
        extensions: 'all',
        location: `${lng},${lat}`,
      },
    })

    return res.data.status === '1' ? res.data.regeocode : null
  } catch (error) {
    return null
  }
}

export const geocode = async (address) => {
  try {
    const res = await amapInstance.get('/v3/geocode/geo', {
      params: { address },
    })

    if (res.data.status !== '1' || res.data.geocodes.length === 0) {
      return null
    }

    const [lng, lat] = res.data.geocodes[0].location.split(',')

    return {
      adcode: res.data.geocodes[0].adcode,
      lat: parseFloat(lat),
      lng: parseFloat(lng),
    }
  } catch (error) {
    return null
  }
}

export const ipLocation = async () => {
  try {
    const res = await amapInstance.get('/v3/ip')

    if (res.data.status === '1') {
      return {
        adcode: res.data.adcode,
        city: res.data.city,
      }
    }

    return null
  } catch (error) {
    return null
  }
}

export const getAroundPois = async (lng, lat, keywords = '') => {
  try {
    const res = await amapInstance.get('/v3/place/around', {
      params: {
        keywords,
        location: `${lng},${lat}`,
        radius: 1000,
        sortrule: 'distance',
        types: '地名地址信息|住宅区|相关场所',
      },
    })

    return res.data.pois || []
  } catch (error) {
    return []
  }
}

export const getStaticMapUrl = (lng, lat) => {
  if (!lng || !lat) {
    return ''
  }

  return `https://restapi.amap.com/v3/staticmap?location=${lng},${lat}&zoom=15&size=400*200&markers=mid,,A:${lng},${lat}&key=${import.meta.env.VITE_AMAP_KEY}`
}
