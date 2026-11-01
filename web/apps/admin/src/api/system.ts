import { del, download, get, post, put } from './http'
import type {
  ConfigVO,
  DeptVO,
  DictDataVO,
  DictTypeVO,
  FileVO,
  MenuTreeNode,
  MenuType,
  MenuVO,
  NoticeVO,
  PageResult,
  PostVO,
  RoleVO,
  UserVO,
} from './types'

export interface UserPageParams {
  pageNum: number
  pageSize: number
  keyword?: string
  deptId?: number
}

export interface UserSaveBody {
  nickname: string
  deptId?: number
  email?: string
  phone?: string
  status: number
  remark?: string
  roleIds: number[]
  postIds: number[]
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
  dataScope: number
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

export interface UserImportResult {
  successCount: number
  failureCount: number
  errors: string[]
}

export function exportUsers(params: { keyword?: string; deptId?: number }): Promise<void> {
  const search = new URLSearchParams()
  if (params.keyword) {
    search.set('keyword', params.keyword)
  }
  if (params.deptId != null) {
    search.set('deptId', String(params.deptId))
  }
  const qs = search.toString()
  return download(`/api/system/users/export${qs ? `?${qs}` : ''}`, 'users.xlsx')
}

export function importUsers(file: File): Promise<UserImportResult> {
  const formData = new FormData()
  formData.append('file', file)
  return post<UserImportResult>('/api/system/users/import', formData)
}

// ---------- 角色 ----------

export function pageRoles(params: RolePageParams): Promise<PageResult<RoleVO>> {
  return get<PageResult<RoleVO>>('/api/system/roles', { ...params })
}

export function createRole(body: RoleSaveBody): Promise<number | null> {
  return post<number | null>('/api/system/roles', body)
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

export function getRoleDeptIds(id: number): Promise<number[]> {
  return get<number[]>(`/api/system/roles/${id}/deptIds`)
}

export function assignRoleDepts(id: number, deptIds: number[]): Promise<null> {
  return put<null>(`/api/system/roles/${id}/depts`, { deptIds })
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

// ---------- 部門 ----------

export interface DeptSaveBody {
  parentId: number
  name: string
  leader?: string
  phone?: string
  sortNo: number
  status: number
}

export function getDeptTree(): Promise<DeptVO[]> {
  return get<DeptVO[]>('/api/system/depts/tree')
}

export function listDepts(keyword?: string): Promise<DeptVO[]> {
  return get<DeptVO[]>('/api/system/depts', keyword ? { keyword } : undefined)
}

export function createDept(body: DeptSaveBody): Promise<null> {
  return post<null>('/api/system/depts', body)
}

export function updateDept(id: number, body: DeptSaveBody): Promise<null> {
  return put<null>(`/api/system/depts/${id}`, body)
}

export function deleteDept(id: number): Promise<null> {
  return del<null>(`/api/system/depts/${id}`)
}

// ---------- 崗位 ----------

export interface PostSaveBody {
  code: string
  name: string
  sortNo: number
  status: number
  remark?: string
}

export function pagePosts(params: {
  pageNum: number
  pageSize: number
  keyword?: string
}): Promise<PageResult<PostVO>> {
  return get<PageResult<PostVO>>('/api/system/posts', { ...params })
}

export function listAllPosts(): Promise<PostVO[]> {
  return get<PostVO[]>('/api/system/posts/all')
}

export function createPost(body: PostSaveBody): Promise<null> {
  return post<null>('/api/system/posts', body)
}

export function updatePost(id: number, body: PostSaveBody): Promise<null> {
  return put<null>(`/api/system/posts/${id}`, body)
}

export function deletePost(id: number): Promise<null> {
  return del<null>(`/api/system/posts/${id}`)
}

// ---------- 字典 ----------

export interface DictTypeSaveBody {
  name: string
  type: string
  remark?: string
}

export function pageDictTypes(params: {
  pageNum: number
  pageSize: number
  keyword?: string
}): Promise<PageResult<DictTypeVO>> {
  return get<PageResult<DictTypeVO>>('/api/system/dicts/types', { ...params })
}

export function createDictType(body: DictTypeSaveBody): Promise<null> {
  return post<null>('/api/system/dicts/types', body)
}

export function updateDictType(id: number, body: DictTypeSaveBody): Promise<null> {
  return put<null>(`/api/system/dicts/types/${id}`, body)
}

export function deleteDictType(id: number): Promise<null> {
  return del<null>(`/api/system/dicts/types/${id}`)
}

export interface DictDataSaveBody {
  dictType: string
  label: string
  value: string
  sortNo: number
  status: number
  remark?: string
}

export function pageDictData(params: {
  pageNum: number
  pageSize: number
  dictType: string
  keyword?: string
}): Promise<PageResult<DictDataVO>> {
  return get<PageResult<DictDataVO>>('/api/system/dicts/data', { ...params })
}

export function createDictData(body: DictDataSaveBody): Promise<null> {
  return post<null>('/api/system/dicts/data', body)
}

export function updateDictData(id: number, body: DictDataSaveBody): Promise<null> {
  return put<null>(`/api/system/dicts/data/${id}`, body)
}

export function deleteDictData(id: number): Promise<null> {
  return del<null>(`/api/system/dicts/data/${id}`)
}

export function listDictDataByType(type: string): Promise<DictDataVO[]> {
  return get<DictDataVO[]>(`/api/system/dicts/data/type/${encodeURIComponent(type)}`)
}

// ---------- 參數 ----------

export interface ConfigSaveBody {
  name: string
  key: string
  value: string
  remark?: string
}

export function pageConfigs(params: {
  pageNum: number
  pageSize: number
  keyword?: string
}): Promise<PageResult<ConfigVO>> {
  return get<PageResult<ConfigVO>>('/api/system/configs', { ...params })
}

export function createConfig(body: ConfigSaveBody): Promise<null> {
  return post<null>('/api/system/configs', body)
}

export function updateConfig(id: number, body: ConfigSaveBody): Promise<null> {
  return put<null>(`/api/system/configs/${id}`, body)
}

export function deleteConfig(id: number): Promise<null> {
  return del<null>(`/api/system/configs/${id}`)
}

export function getConfigByKey(key: string): Promise<ConfigVO> {
  return get<ConfigVO>(`/api/system/configs/key/${encodeURIComponent(key)}`)
}

// ---------- 公告 ----------

export interface NoticeSaveBody {
  title: string
  type: number
  content: string
  status: number
  remark?: string
}

export function pageNotices(params: {
  pageNum: number
  pageSize: number
  keyword?: string
}): Promise<PageResult<NoticeVO>> {
  return get<PageResult<NoticeVO>>('/api/system/notices', { ...params })
}

export function getNotice(id: number): Promise<NoticeVO> {
  return get<NoticeVO>(`/api/system/notices/${id}`)
}

export function createNotice(body: NoticeSaveBody): Promise<null> {
  return post<null>('/api/system/notices', body)
}

export function updateNotice(id: number, body: NoticeSaveBody): Promise<null> {
  return put<null>(`/api/system/notices/${id}`, body)
}

export function deleteNotice(id: number): Promise<null> {
  return del<null>(`/api/system/notices/${id}`)
}

// ---------- 檔案 ----------

export function uploadFile(formData: FormData): Promise<FileVO> {
  return post<FileVO>('/api/system/files/upload', formData)
}

export function pageFiles(params: {
  pageNum: number
  pageSize: number
  keyword?: string
}): Promise<PageResult<FileVO>> {
  return get<PageResult<FileVO>>('/api/system/files', { ...params })
}

export function downloadFile(id: number, originalName: string): Promise<void> {
  return download(`/api/system/files/download/${id}`, originalName)
}

export function deleteFile(id: number): Promise<null> {
  return del<null>(`/api/system/files/${id}`)
}
