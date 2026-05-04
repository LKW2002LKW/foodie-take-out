export const getLocal = (key, defaultValue = null) => {
  const value = localStorage.getItem(key)
  return value == null ? defaultValue : value
}

export const setLocal = (key, value) => {
  localStorage.setItem(key, value)
}

export const removeLocal = (key) => {
  localStorage.removeItem(key)
}

export const getLocalJson = (key, defaultValue = {}) => {
  const raw = localStorage.getItem(key)

  if (!raw) {
    return defaultValue
  }

  try {
    return JSON.parse(raw)
  } catch (error) {
    return defaultValue
  }
}

export const setLocalJson = (key, value) => {
  localStorage.setItem(key, JSON.stringify(value))
}
