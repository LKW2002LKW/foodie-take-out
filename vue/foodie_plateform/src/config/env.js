// 平台端运行时环境配置统一收口，避免业务代码直接散读环境变量。
export const appEnv = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081/platform',
  requestTimeout: 5000,
}
