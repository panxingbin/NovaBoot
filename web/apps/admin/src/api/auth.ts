import { get, post } from './http'
import type { CaptchaVO, LoginBody, LoginVO, ProfileVO } from './types'

export function getCaptcha(): Promise<CaptchaVO> {
  return post<CaptchaVO>('/api/auth/captcha')
}

export function login(body: LoginBody): Promise<LoginVO> {
  return post<LoginVO>('/api/auth/login', body)
}

export function logout(): Promise<null> {
  return post<null>('/api/auth/logout')
}

export function getProfile(): Promise<ProfileVO> {
  return get<ProfileVO>('/api/auth/profile')
}
