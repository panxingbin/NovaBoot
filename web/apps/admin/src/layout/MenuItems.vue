<script setup lang="ts">
import type { MenuTreeNode } from '@/api/types'
import { getIcon } from '@/utils/icons'

defineProps<{
  nodes: MenuTreeNode[]
}>()
</script>

<template>
  <template v-for="node in nodes" :key="node.id">
    <el-sub-menu v-if="node.menuType === 'D'" :index="`dir-${node.id}`">
      <template #title>
        <el-icon><component :is="getIcon(node.icon)" /></el-icon>
        <span>{{ node.name }}</span>
      </template>
      <MenuItems :nodes="node.children ?? []" />
    </el-sub-menu>
    <el-menu-item v-else :index="node.path">
      <el-icon><component :is="getIcon(node.icon)" /></el-icon>
      <template #title>{{ node.name }}</template>
    </el-menu-item>
  </template>
</template>
