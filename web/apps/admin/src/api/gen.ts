import { downloadPost, get, post } from './http'

export interface GenTableVO {
  tableName: string
  tableComment: string | null
}

export interface GenColumnVO {
  columnName: string
  javaName: string
  javaType: string
  columnType: string
  comment: string | null
  pk: boolean
  nullable: boolean
  formField: boolean
  searchCandidate: boolean
}

export interface GenTableMetaVO {
  tableName: string
  tableComment: string | null
  columns: GenColumnVO[]
  searchColumn: GenColumnVO | null
}

export interface GenOptionsBody {
  tableName: string
  moduleName: string
  businessName: string
  functionName: string
  author: string
  menuStartId: number
  menuIcon?: string
}

export function listGenTables(): Promise<GenTableVO[]> {
  return get<GenTableVO[]>('/api/gen/tables')
}

export function getGenMeta(tableName: string): Promise<GenTableMetaVO> {
  return get<GenTableMetaVO>('/api/gen/meta/' + encodeURIComponent(tableName))
}

export function previewGen(body: GenOptionsBody): Promise<Record<string, string>> {
  return post<Record<string, string>>('/api/gen/preview', body)
}

export function downloadGenZip(body: GenOptionsBody): Promise<void> {
  return downloadPost('/api/gen/download', body, body.businessName + '-gen.zip')
}
