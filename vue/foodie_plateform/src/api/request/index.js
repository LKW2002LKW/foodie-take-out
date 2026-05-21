import axios from 'axios'
import { appEnv } from '@/config/env'
import { setupInterceptors } from './interceptors'

// 平台端统一请求实例，集中处理基础地址、超时与拦截逻辑。
const service = axios.create({
  baseURL: appEnv.apiBaseUrl,
  timeout: appEnv.requestTimeout,
})

setupInterceptors(service)

export default service
