import { del, get } from './http'
import type { LoginLogVO, OnlineUserVO, OperLogVO, PageResult } from './types'

export interface OperLogPageParams {
  pageNum: number
  pageSize: number
  title?: string
  username?: string
  status?: number
}

export function pageOperLogs(params: OperLogPageParams): Promise<PageResult<OperLogVO>> {
  return get<PageResult<OperLogVO>>('/api/monitor/operlogs', { ...params })
}

export function deleteOperLog(id: number): Promise<null> {
  return del<null>(`/api/monitor/operlogs/${id}`)
}

export interface LoginLogPageParams {
  pageNum: number
  pageSize: number
  username?: string
  status?: number
}

export function pageLoginLogs(params: LoginLogPageParams): Promise<PageResult<LoginLogVO>> {
  return get<PageResult<LoginLogVO>>('/api/monitor/loginlogs', { ...params })
}

export function pageOnlineUsers(params: {
  pageNum: number
  pageSize: number
  keyword?: string
}): Promise<PageResult<OnlineUserVO>> {
  return get<PageResult<OnlineUserVO>>('/api/monitor/online', { ...params })
}

export function forceLogout(token: string): Promise<null> {
  return del<null>(`/api/monitor/online/${encodeURIComponent(token)}`)
}
