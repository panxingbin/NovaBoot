<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="notfound-page">
    <el-result icon="warning" title="404" sub-title="抱歉，您訪問的頁面不存在">
      <template #extra>
        <el-button type="primary" @click="router.replace('/')">返回首頁</el-button>
      </template>
    </el-result>
  </div>
</template>

<style scoped>
.notfound-page {
  display: flex;
  height: 100%;
  align-items: center;
  justify-content: center;
}
</style>
