import { del, get, post, put } from './http'
import type { MenuTreeNode, MenuType, MenuVO, PageResult, RoleVO, UserVO } from './types'

export interface UserPageParams {
  pageNum: number
  pageSize: number
  keyword?: string
}

export interface UserSaveBody {
  nickname: string
  deptId?: number
  email?: string
  phone?: string
  status: number
  remark?: string
  roleIds: number[]
}

export interface UserCreateBody extends UserSaveBody {
  username: string
  password: string
}

export interface RolePageParams {
  pageNum: number
  pageSize: number
  keyword?: string
}

export interface RoleSaveBody {
  name: string
  code: string
  sortNo: number
  status: number
  remark?: string
}

export interface MenuSaveBody {
  parentId: number
  name: string
  menuType: MenuType
  path?: string
  component?: string
  perm?: string
  icon?: string
  sortNo: number
  visible: number
  status: number
}

// ---------- 用戶 ----------

export function pageUsers(params: UserPageParams): Promise<PageResult<UserVO>> {
  return get<PageResult<UserVO>>('/api/system/users', { ...params })
}

export function createUser(body: UserCreateBody): Promise<null> {
  return post<null>('/api/system/users', body)
}

export function updateUser(id: number, body: UserSaveBody): Promise<null> {
  return put<null>(`/api/system/users/${id}`, body)
}

export function updateUserPassword(id: number, password: string): Promise<null> {
  return put<null>(`/api/system/users/${id}/password`, { password })
}

export function deleteUser(id: number): Promise<null> {
  return del<null>(`/api/system/users/${id}`)
}

// ---------- 角色 ----------

export function pageRoles(params: RolePageParams): Promise<PageResult<RoleVO>> {
  return get<PageResult<RoleVO>>('/api/system/roles', { ...params })
}

export function createRole(body: RoleSaveBody): Promise<null> {
  return post<null>('/api/system/roles', body)
}

export function updateRole(id: number, body: RoleSaveBody): Promise<null> {
  return put<null>(`/api/system/roles/${id}`, body)
}

export function deleteRole(id: number): Promise<null> {
  return del<null>(`/api/system/roles/${id}`)
}

export function getRoleMenuIds(id: number): Promise<number[]> {
  return get<number[]>(`/api/system/roles/${id}/menuIds`)
}

export function assignRoleMenus(id: number, menuIds: number[]): Promise<null> {
  return put<null>(`/api/system/roles/${id}/menus`, { menuIds })
}

// ---------- 選單 ----------

export function getMenuTree(): Promise<MenuTreeNode[]> {
  return get<MenuTreeNode[]>('/api/system/menus/tree')
}

export function listMenus(keyword?: string): Promise<MenuVO[]> {
  return get<MenuVO[]>('/api/system/menus', keyword ? { keyword } : undefined)
}

export function createMenu(body: MenuSaveBody): Promise<null> {
  return post<null>('/api/system/menus', body)
}

export function updateMenu(id: number, body: MenuSaveBody): Promise<null> {
  return put<null>(`/api/system/menus/${id}`, body)
}

export function deleteMenu(id: number): Promise<null> {
  return del<null>(`/api/system/menus/${id}`)
}
