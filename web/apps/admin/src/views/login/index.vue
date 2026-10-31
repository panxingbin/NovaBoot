<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Key, Lock, User } from '@element-plus/icons-vue'
import { getCaptcha, login } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const captchaInfo = ref({ captchaId: '', imageBase64: '' })

const form = reactive({
  username: '',
  password: '',
  captchaCode: '',
})

const rules: FormRules = {
  username: [{ required: true, message: '請輸入用戶名', trigger: 'blur' }],
  password: [{ required: true, message: '請輸入密碼', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '請輸入驗證碼', trigger: 'blur' }],
}

async function refreshCaptcha(): Promise<void> {
  try {
    captchaInfo.value = await getCaptcha()
    form.captchaCode = ''
  } catch {
    // 錯誤提示由 http 層統一彈出，保留舊驗證碼即可
  }
}

onMounted(() => {
  void refreshCaptcha()
})

async function handleLogin(): Promise<void> {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    userStore.reset()
    const { token } = await login({
      username: form.username,
      password: form.password,
      captchaId: captchaInfo.value.captchaId,
      captchaCode: form.captchaCode,
    })
    userStore.setToken(token)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    const target = redirect.startsWith('/') && !redirect.startsWith('//') ? redirect : '/'
    await router.replace(target)
    ElMessage.success('登入成功')
  } catch {
    await refreshCaptcha()
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <el-card class="login-card" shadow="always">
      <div class="login-title">NovaBoot 管理後台</div>
      <div class="login-subtitle">開源後台框架 · M0</div>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用戶名"
            :prefix-icon="User"
            autocomplete="username"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="密碼"
            :prefix-icon="Lock"
            autocomplete="current-password"
          />
        </el-form-item>
        <el-form-item prop="captchaCode">
          <div class="captcha-row">
            <el-input
              v-model="form.captchaCode"
              class="captcha-input"
              placeholder="驗證碼"
              :prefix-icon="Key"
            />
            <img
              v-if="captchaInfo.imageBase64"
              class="captcha-img"
              :src="captchaInfo.imageBase64"
              alt="驗證碼"
              title="點擊刷新驗證碼"
              @click="refreshCaptcha"
            />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button class="login-btn" type="primary" :loading="loading" @click="handleLogin">
            登 入
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.login-page {
  display: flex;
  height: 100%;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #dbeafe 0%, #ede9fe 100%);
}
.login-card {
  width: 400px;
}
.login-title {
  color: #1f2937;
  font-size: 22px;
  font-weight: 700;
  text-align: center;
}
.login-subtitle {
  margin: 4px 0 20px;
  color: #9ca3af;
  font-size: 13px;
  text-align: center;
}
.captcha-row {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}
.captcha-input {
  flex: 1;
}
.captcha-img {
  height: 40px;
  cursor: pointer;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}
.login-btn {
  width: 100%;
}
</style>
