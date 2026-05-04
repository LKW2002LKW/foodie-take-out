export const formatFlavorText = (jsonStr, separator = ' ') => {
  try {
    return JSON.parse(jsonStr).map((item) => item.value).join(separator)
  } catch (error) {
    return separator === ',' ? jsonStr || '' : ''
  }
}
