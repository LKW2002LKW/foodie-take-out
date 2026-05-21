import axios from 'axios'
import { appEnv } from '@/config/env'
import { registerRequestInterceptors } from './interceptors'

// 商家端统一请求实例，封装基础地址、超时与鉴权拦截。
const service = axios.create({
  baseURL: appEnv.apiBaseUrl,
  timeout: appEnv.requestTimeout,
})

registerRequestInterceptors(service)

export default service
