export const appEnv = {
  amapSecurityJsCode: import.meta.env.VITE_AMAP_SECURITY_JS_CODE,
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8082',
  requestTimeout: 5000,
}
