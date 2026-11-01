import axios from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiEnvelope } from './types'
import { i18n } from '@/locales'

const t = i18n.global.t

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
      ElMessage.error(envelope.message || t('http.sessionExpired'))
      void redirectToLogin()
      return Promise.reject(new Error(envelope.message || t('http.sessionExpired')))
    }
    ElMessage.error(envelope.message || t('http.requestFailed'))
    return Promise.reject(new Error(envelope.message || t('http.requestFailed')))
  },
  (error: unknown) => {
    let status: number | undefined
    let message = t('http.networkError')
    let payload: unknown
    if (axios.isAxiosError(error)) {
      status = error.response?.status
      payload = error.response?.data
      if (error.code === 'ECONNABORTED') {
        message = t('http.timeout')
      }
    }
    if (status === 401) {
      ElMessage.error(t('http.sessionExpired'))
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

/**
 * 文件下載：攜帶 Token 以 blob 方式拉取並觸發瀏覽器保存。
 * 後端業務錯誤以 HTTP 200 + JSON envelope 返回，需檢測 content-type 後轉為異常。
 */
export async function download(url: string, filename: string): Promise<void> {
  const response = await instance.get(url, { responseType: 'blob' })
  const blob = response.data as Blob
  if (blob.type.includes('application/json')) {
    const envelope = JSON.parse(await blob.text()) as ApiEnvelope<unknown>
    throw new Error(envelope.message || '下載失敗')
  }
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = filename
  link.click()
  URL.revokeObjectURL(link.href)
}
