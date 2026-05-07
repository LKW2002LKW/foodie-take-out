// 将购物车或订单里的口味 JSON 字符串转换成可展示文本。
export const formatFlavorText = (jsonStr, separator = ' ') => {
  try {
    return JSON.parse(jsonStr).map((item) => item.value).join(separator)
  } catch (error) {
    return separator === ',' ? jsonStr || '' : ''
  }
}
