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
  createdAt: string
}

export interface RoleVO {
  id: number
  name: string
  code: string
  sortNo: number
  status: number
  remark: string | null
  createdAt: string
}
