// 简单的 localStorage 封装工具
export function setLocal(key, value) {
  try {
    localStorage.setItem(key, String(value))
  } catch (e) {
    // 本地环境或私密模式下可能失败，静默失败以保持兼容
  }
}

export function getLocal(key, defaultValue = null) {
  try {
    const v = localStorage.getItem(key)
    return v === null ? defaultValue : v
  } catch (e) {
    return defaultValue
  }
}

export function removeLocal(key) {
  try {
    localStorage.removeItem(key)
  } catch (e) {
    // ignore
  }
}

export function setLocalJson(key, obj) {
  try {
    localStorage.setItem(key, JSON.stringify(obj))
  } catch (e) {
    // ignore
  }
}

export function getLocalJson(key, defaultValue = null) {
  try {
    const v = localStorage.getItem(key)
    if (v === null) return defaultValue
    try {
      return JSON.parse(v)
    } catch (e) {
      return defaultValue
    }
  } catch (e) {
    return defaultValue
  }
}

export default {
  setLocal,
  getLocal,
  removeLocal,
  setLocalJson,
  getLocalJson,
}
