export interface ApiEnvelope<T> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  total: number
  records: T[]
}

export interface CaptchaVO {
  captchaId: string
  imageBase64: string
}

export interface LoginBody {
  username: string
  password: string
  captchaId: string
  captchaCode: string
}

export interface LoginVO {
  token: string
}

export interface ProfileUser {
  id: number
  username: string
  nickname: string
}

export interface ProfileVO {
  user: ProfileUser
  roles: string[]
  permissions: string[]
}

export type MenuType = 'D' | 'M' | 'B'

export interface MenuTreeNode {
  id: number
  parentId: number
  name: string
  menuType: MenuType
  path: string
  component: string
  perm: string
  icon: string
  sortNo: number
  visible: number
  status: number
  children: MenuTreeNode[]
}

export type MenuVO = Omit<MenuTreeNode, 'children'>

export interface UserVO {
  id: number
  username: string
  nickname: string
  deptId: number | null
  email: string | null
  phone: string | null
  status: number
  remark: string | null
  roleIds: number[]
  postIds: number[]
  createdAt: string
}

export interface RoleVO {
  id: number
  name: string
  code: string
  sortNo: number
  status: number
  dataScope: number
  remark: string | null
  createdAt: string
}

export interface DeptVO {
  id: number
  parentId: number
  name: string
  sortNo: number
  leader: string | null
  phone: string | null
  status: number
  createdAt: string
  children?: DeptVO[]
}

export interface PostVO {
  id: number
  code: string
  name: string
  sortNo: number
  status: number
  remark: string | null
  createdAt: string
}

export interface DictTypeVO {
  id: number
  name: string
  type: string
  remark: string | null
  createdAt: string
}

export interface DictDataVO {
  id: number
  dictType: string
  label: string
  value: string
  sortNo: number
  status: number
  remark: string | null
}

export interface ConfigVO {
  id: number
  name: string
  key: string
  value: string
  builtin: number
  remark: string | null
}

export interface NoticeVO {
  id: number
  title: string
  type: number
  content: string
  status: number
  remark: string | null
  createdAt: string
}

export interface FileVO {
  id: number
  name: string
  originalName: string
  size: number
  contentType: string | null
  createdAt: string
}

export interface OperLogVO {
  id: number
  title: string
  businessType: number
  method: string
  requestMethod: string
  url: string
  ip: string
  param: string
  errorMsg: string
  status: number
  costMs: number
  operName: string
  createdAt: string
}

export interface LoginLogVO {
  id: number
  username: string
  ip: string
  status: number
  msg: string
  createdAt: string
}

export interface OnlineUserVO {
  token: string
  username: string
  nickname: string
  device: string
  loginTime: string
}
