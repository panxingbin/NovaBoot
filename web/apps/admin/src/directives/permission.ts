import type { Directive, DirectiveBinding } from 'vue'
import { useUserStore } from '@/stores/user'

function hasPermission(required: string | string[]): boolean {
  const values = Array.isArray(required) ? required : [required]
  if (values.length === 0) {
    return true
  }
  const { permissions } = useUserStore()
  if (permissions.includes('*:*:*')) {
    return true
  }
  return values.some((item) => permissions.includes(item))
}

export const permission: Directive<HTMLElement, string | string[]> = {
  mounted(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
    if (!binding.value) {
      return
    }
    if (!hasPermission(binding.value)) {
      el.parentNode?.removeChild(el)
    }
  },
}

export default permission
