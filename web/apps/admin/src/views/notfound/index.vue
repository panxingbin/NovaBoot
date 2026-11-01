<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const router = useRouter()
</script>

<template>
  <div class="notfound-page">
    <el-result icon="warning" :title="t('notfound.code')" :sub-title="t('notfound.description')">
      <template #extra>
        <el-button type="primary" @click="router.replace('/')">
          {{ t('notfound.backHome') }}
        </el-button>
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
