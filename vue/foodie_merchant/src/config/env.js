// 运行时环境配置统一收口，避免业务代码直接散读环境变量。
export const appEnv = {
  amapSecurityJsCode: import.meta.env.VITE_AMAP_SECURITY_JS_CODE,
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8082',
  requestTimeout: 5000,
}
