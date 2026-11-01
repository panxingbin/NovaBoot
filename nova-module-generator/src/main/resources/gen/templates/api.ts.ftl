import { del, get, post, put } from './http'
import type { PageResult } from './types'

export interface ${options.className} {
<#list table.columns as col>
  /** ${col.comment!col.javaName} */
  ${col.javaName}<#if col.nullable && !col.pk>?</#if>: <#switch col.javaType><#case "Long">number<#break><#case "Integer">number<#break><#case "Double">number<#break><#case "BigDecimal">number<#break><#case "Boolean">boolean<#break><#default>string</#switch>
</#list>
}

export interface ${options.className}PageParams {
  pageNum: number
  pageSize: number
<#if table.searchColumn??>
  keyword?: string
</#if>
}

export function page${options.className}s(params: ${options.className}PageParams): Promise<PageResult<${options.className}>> {
  return get<PageResult<${options.className}>>('${options.apiPath}', { ...params })
}

export function get${options.className}(id: number): Promise<${options.className}> {
  return get<${options.className}>('${options.apiPath}/' + id)
}

export function create${options.className}(body: ${options.className}): Promise<null> {
  return post<null>('${options.apiPath}', body)
}

export function update${options.className}(id: number, body: ${options.className}): Promise<null> {
  return put<null>('${options.apiPath}/' + id, body)
}

export function delete${options.className}(id: number): Promise<null> {
  return del<null>('${options.apiPath}/' + id)
}
