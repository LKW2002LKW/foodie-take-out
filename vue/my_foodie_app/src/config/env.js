// 统一管理运行时环境变量，避免业务代码直接读取 import.meta.env。
export const appEnv = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL || '/user',
  amapKey: import.meta.env.VITE_AMAP_KEY,
  amapSecurityJsCode: import.meta.env.VITE_AMAP_SECURITY_JS_CODE,
  requestTimeout: 5000,
}
