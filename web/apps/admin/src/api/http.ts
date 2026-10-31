import axios from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiEnvelope } from './types'

export const TOKEN_KEY = 'nova_token'

const instance = axios.create({
  timeout: 15000,
})

instance.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

async function redirectToLogin(): Promise<void> {
  try {
    const [{ default: router }, { useUserStore }] = await Promise.all([
      import('@/router'),
      import('@/stores/user'),
    ])
    useUserStore().reset()
    if (router.currentRoute.value.path !== '/login') {
      await router.replace('/login')
    }
  } catch {
    window.location.href = '/login'
  }
}

instance.interceptors.response.use(
  (response) => {
    const envelope = response.data as ApiEnvelope<unknown> | undefined
    if (!envelope || typeof envelope.code !== 'number') {
      return response
    }
    if (envelope.code === 0) {
      return response
    }
    if (envelope.code === 401) {
      ElMessage.error(envelope.message || '登錄已失效，請重新登入')
      void redirectToLogin()
      return Promise.reject(new Error(envelope.message || '未登錄'))
    }
    ElMessage.error(envelope.message || '請求失敗')
    return Promise.reject(new Error(envelope.message || '請求失敗'))
  },
  (error: unknown) => {
    let status: number | undefined
    let message = '網絡異常，請稍後重試'
    let payload: unknown
    if (axios.isAxiosError(error)) {
      status = error.response?.status
      payload = error.response?.data
      if (error.code === 'ECONNABORTED') {
        message = '請求超時，請稍後重試'
      }
    }
    if (status === 401) {
      ElMessage.error('登錄已失效，請重新登入')
      void redirectToLogin()
    } else {
      const dataMessage = (payload as { message?: string } | undefined)?.message
      ElMessage.error(dataMessage || message)
    }
    return Promise.reject(error instanceof Error ? error : new Error(String(error)))
  },
)

export async function get<T>(url: string, params?: Record<string, unknown>): Promise<T> {
  const response = await instance.get<ApiEnvelope<T>>(url, { params })
  return response.data.data
}

export async function post<T>(url: string, data?: unknown): Promise<T> {
  const response = await instance.post<ApiEnvelope<T>>(url, data)
  return response.data.data
}

export async function put<T>(url: string, data?: unknown): Promise<T> {
  const response = await instance.put<ApiEnvelope<T>>(url, data)
  return response.data.data
}

export async function del<T>(url: string): Promise<T> {
  const response = await instance.delete<ApiEnvelope<T>>(url)
  return response.data.data
}
