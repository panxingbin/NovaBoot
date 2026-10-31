import type { MenuTreeNode } from '@/api/types'

export function filterVisibleMenus(nodes: MenuTreeNode[]): MenuTreeNode[] {
  const result: MenuTreeNode[] = []
  for (const node of nodes) {
    if (node.menuType === 'B') {
      continue
    }
    const children = filterVisibleMenus(node.children ?? [])
    if (node.menuType === 'M') {
      if (node.visible !== 0) {
        result.push({ ...node, children })
      }
    } else if (children.length > 0) {
      result.push({ ...node, children })
    }
  }
  return result
}

interface IdNode {
  id: number
  children?: IdNode[]
}

export function collectLeafIds(nodes: IdNode[], acc: Set<number> = new Set()): Set<number> {
  for (const node of nodes) {
    if (node.children && node.children.length > 0) {
      collectLeafIds(node.children, acc)
    } else {
      acc.add(node.id)
    }
  }
  return acc
}

export type TreeOf<T> = T & { children: TreeOf<T>[] }

export function buildTree<T extends { id: number; parentId: number; sortNo: number }>(
  list: T[],
): TreeOf<T>[] {
  const nodeMap = new Map<number, TreeOf<T>>()
  for (const item of list) {
    nodeMap.set(item.id, { ...item, children: [] })
  }
  const roots: TreeOf<T>[] = []
  for (const item of list) {
    const node = nodeMap.get(item.id)
    if (!node) {
      continue
    }
    const parent = item.parentId ? nodeMap.get(item.parentId) : undefined
    if (parent) {
      parent.children.push(node)
    } else {
      roots.push(node)
    }
  }
  const sortNodes = (nodes: TreeOf<T>[]): void => {
    nodes.sort((a, b) => a.sortNo - b.sortNo)
    for (const node of nodes) {
      sortNodes(node.children)
    }
  }
  sortNodes(roots)
  return roots
}
