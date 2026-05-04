import axios from 'axios'
import { appEnv } from '@/config/env'
import { registerRequestInterceptors } from './interceptors'

const service = axios.create({
  baseURL: appEnv.apiBaseUrl,
  timeout: appEnv.requestTimeout,
})

registerRequestInterceptors(service)

export default service
