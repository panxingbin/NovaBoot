import { del, get, post, put } from './http'
import type { PageResult } from './types'

/** 表單字段類型，與後端 FlowFieldType 枚舉一致 */
export type FlowFieldType = 'input' | 'textarea' | 'number' | 'date' | 'select'

export interface FlowFormField {
  key: string
  label: string
  type: FlowFieldType
  required?: boolean
  /** type 為 select 時的下拉選項 */
  options?: string[]
  /** type 為 number 時的取值範圍 */
  min?: number
  max?: number
}

export interface FlowFormDef {
  id: number
  formKey: string
  name: string
  schemaJson: string
  remark: string | null
  createdAt: string
}

export interface FlowFormPageParams {
  pageNum: number
  pageSize: number
  keyword?: string
}

export interface FlowFormSaveBody {
  formKey: string
  name: string
  schemaJson: string
  remark?: string
}

export interface WorkflowApplyBody {
  formKey: string
  data: Record<string, string | number>
}

export interface TodoTask {
  taskId: string
  taskName: string
  createTime: string
  processInstanceId: string
  formKey: string
  formData: Record<string, unknown>
}

export interface TaskCompleteBody {
  approved: boolean
  comment?: string
}

export interface MyProcess {
  processInstanceId: string
  formKey: string
  startTime: string
  endTime: string | null
  running: boolean
  approved: boolean | null
  status: string
  formData: Record<string, unknown>
}

export interface DoneTask {
  taskId: string
  taskName: string
  processInstanceId: string
  startTime: string
  endTime: string | null
  comments: string[]
}

const FIELD_TYPES: readonly FlowFieldType[] = ['input', 'textarea', 'number', 'date', 'select']

/**
 * 解析 schemaJson 為字段列表，容錯處理非法 JSON 與未知類型
 */
export function parseFlowFormSchema(schemaJson: string): FlowFormField[] {
  if (!schemaJson) {
    return []
  }
  let parsed: unknown
  try {
    parsed = JSON.parse(schemaJson)
  } catch {
    return []
  }
  if (!Array.isArray(parsed)) {
    return []
  }
  const fields: FlowFormField[] = []
  for (const item of parsed) {
    if (typeof item !== 'object' || item === null) {
      continue
    }
    const raw = item as Record<string, unknown>
    const key = typeof raw.key === 'string' ? raw.key.trim() : ''
    const type = FIELD_TYPES.find((candidate) => candidate === raw.type)
    if (!key || !type) {
      continue
    }
    const field: FlowFormField = {
      key,
      label: typeof raw.label === 'string' && raw.label.trim() !== '' ? raw.label.trim() : key,
      type,
    }
    if (raw.required === true) {
      field.required = true
    }
    if (Array.isArray(raw.options)) {
      const options = raw.options.filter((option): option is string => typeof option === 'string')
      if (options.length > 0) {
        field.options = options
      }
    }
    if (typeof raw.min === 'number' && Number.isFinite(raw.min)) {
      field.min = raw.min
    }
    if (typeof raw.max === 'number' && Number.isFinite(raw.max)) {
      field.max = raw.max
    }
    fields.push(field)
  }
  return fields
}

// ---------- 表單定義 ----------

export function listAllFlowForms(): Promise<FlowFormDef[]> {
  return get<FlowFormDef[]>('/api/workflow/forms/all')
}

export function getFlowFormByKey(formKey: string): Promise<FlowFormDef> {
  return get<FlowFormDef>(`/api/workflow/forms/key/${encodeURIComponent(formKey)}`)
}

export function pageFlowForms(params: FlowFormPageParams): Promise<PageResult<FlowFormDef>> {
  return get<PageResult<FlowFormDef>>('/api/workflow/forms', { ...params })
}

export function createFlowForm(body: FlowFormSaveBody): Promise<null> {
  return post<null>('/api/workflow/forms', body)
}

export function updateFlowForm(id: number, body: FlowFormSaveBody): Promise<null> {
  return put<null>(`/api/workflow/forms/${id}`, body)
}

export function deleteFlowForm(id: number): Promise<null> {
  return del<null>(`/api/workflow/forms/${id}`)
}

// ---------- 流程 ----------

export function applyWorkflow(body: WorkflowApplyBody): Promise<string> {
  return post<string>('/api/workflow/apply', body)
}

export function listTodoTasks(): Promise<TodoTask[]> {
  return get<TodoTask[]>('/api/workflow/todo')
}

export function completeTask(taskId: string, body: TaskCompleteBody): Promise<null> {
  return post<null>(`/api/workflow/tasks/${encodeURIComponent(taskId)}/complete`, body)
}

export function listMyProcesses(): Promise<MyProcess[]> {
  return get<MyProcess[]>('/api/workflow/mine')
}

export function listDoneTasks(): Promise<DoneTask[]> {
  return get<DoneTask[]>('/api/workflow/done')
}
