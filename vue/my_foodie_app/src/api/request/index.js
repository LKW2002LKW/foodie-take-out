import axios from 'axios'
import { appEnv } from '@/config/env'
import { setupInterceptors } from './interceptors'

const service = axios.create({
  timeout: appEnv.requestTimeout,
})

setupInterceptors(service)

export default service
