<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { Key, Lock, User } from '@element-plus/icons-vue'
import { getCaptcha, login } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import { useTheme, type ThemeId } from '@/theme/useTheme'
import LangSwitch from '@/components/LangSwitch.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { t } = useI18n()
const { theme, setTheme, themes } = useTheme()

const formRef = ref<FormInstance>()
const loading = ref(false)
const captchaInfo = ref({ captchaId: '', imageBase64: '' })

const form = reactive({
  username: '',
  password: '',
  captchaCode: '',
})

const rules = {
  username: [{ required: true, message: () => t('login.usernameRequired'), trigger: 'blur' }],
  password: [{ required: true, message: () => t('login.passwordRequired'), trigger: 'blur' }],
  captchaCode: [{ required: true, message: () => t('login.captchaRequired'), trigger: 'blur' }],
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

function handleThemeCommand(command: string | number | object): void {
  setTheme(command as ThemeId)
}

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
    ElMessage.success(t('login.success'))
  } catch {
    await refreshCaptcha()
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-tools">
      <LangSwitch />
      <el-dropdown trigger="click" @command="handleThemeCommand">
        <el-icon class="theme-btn" :size="20" title="Theme">
          <component :is="themes.find((item) => item.id === theme)?.icon" />
        </el-icon>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item
              v-for="item in themes"
              :key="item.id"
              :command="item.id"
              :icon="item.icon"
              :class="{ 'is-current': item.id === theme }"
            >
              {{ item.label }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    <el-card class="login-card" shadow="always">
      <div class="login-title">NovaBoot {{ t('login.title') }}</div>
      <div class="login-subtitle">{{ t('login.subtitle') }}</div>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            :placeholder="t('login.username')"
            :prefix-icon="User"
            autocomplete="username"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            :placeholder="t('login.password')"
            :prefix-icon="Lock"
            autocomplete="current-password"
          />
        </el-form-item>
        <el-form-item prop="captchaCode">
          <div class="captcha-row">
            <el-input
              v-model="form.captchaCode"
              class="captcha-input"
              :placeholder="t('login.captcha')"
              :prefix-icon="Key"
            />
            <img
              v-if="captchaInfo.imageBase64"
              class="captcha-img"
              :src="captchaInfo.imageBase64"
              alt="captcha"
              title="captcha"
              @click="refreshCaptcha"
            />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button class="login-btn" type="primary" :loading="loading" @click="handleLogin">
            {{ t('login.loginBtn') }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.login-page {
  position: relative;
  display: flex;
  height: 100%;
  align-items: center;
  justify-content: center;
  background: var(--nova-login-bg);
}
.login-tools {
  position: absolute;
  top: 20px;
  right: 24px;
  display: flex;
  align-items: center;
  gap: 14px;
}
.theme-btn {
  color: var(--el-text-color-regular);
  cursor: pointer;
  outline: none;
}
.theme-btn:hover {
  color: var(--el-color-primary);
}
.login-card {
  width: 400px;
}
.login-title {
  color: var(--el-text-color-primary);
  font-size: 22px;
  font-weight: 700;
  text-align: center;
}
.login-subtitle {
  margin: 4px 0 20px;
  color: var(--el-text-color-secondary);
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
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
}
.login-btn {
  width: 100%;
}
</style>
