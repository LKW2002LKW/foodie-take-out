import axios from 'axios'
import { appEnv } from '@/config/env'
import { setupInterceptors } from './interceptors'

// 项目统一请求实例，负责承接超时、鉴权与异常处理。
const service = axios.create({
  baseURL: appEnv.apiBaseUrl,
  timeout: appEnv.requestTimeout,
})

setupInterceptors(service)

export default service
