import { createI18n } from 'vue-i18n'

export type Lang = 'zh-CN' | 'zh-TW' | 'en'

export const LANG_KEY = 'nova_lang'

export const langs: Array<{ id: Lang; label: string; short: string }> = [
  { id: 'zh-CN', label: '简体中文', short: '简' },
  { id: 'zh-TW', label: '繁體中文', short: '繁' },
  { id: 'en', label: 'English', short: 'EN' },
]

const allLangs: Lang[] = ['zh-CN', 'zh-TW', 'en']

function readInitial(): Lang {
  // URL 查詢參數優先（?lang=en），與主題的深鏈接方式一致
  try {
    const fromQuery = new URLSearchParams(window.location.search).get('lang') as Lang | null
    if (fromQuery && allLangs.includes(fromQuery)) {
      localStorage.setItem(LANG_KEY, fromQuery)
      return fromQuery
    }
    const stored = localStorage.getItem(LANG_KEY)
    if (stored && allLangs.includes(stored as Lang)) {
      return stored as Lang
    }
  } catch {
    // localStorage 不可用時回退簡體中文
  }
  return 'zh-CN'
}

// 頁面模塊片段：頂層命名空間 = 頁面名（user/role/...）；base 為外殼文案（common/login/...）
import base from './modules/base'
import user from './modules/user'
import role from './modules/role'
import menu from './modules/menu'
import dept from './modules/dept'
import post from './modules/post'
import dict from './modules/dict'
import config from './modules/config'
import notice from './modules/notice'
import file from './modules/file'
import operlog from './modules/operlog'
import loginlog from './modules/loginlog'
import online from './modules/online'
import notfound from './modules/notfound'
import gen from './modules/gen'

const pageFragments: Record<string, Record<'zhCN' | 'zhTW' | 'en', object>> = {
  user,
  role,
  menu,
  dept,
  post,
  dict,
  config,
  notice,
  file,
  operlog,
  loginlog,
  online,
  notfound,
  gen,
}

type LangKey = 'zhCN' | 'zhTW' | 'en'

function pack(lang: LangKey): Record<string, object> {
  return {
    // base 的子命名空間（common/login/layout/dashboard/http/router）提升到頂層
    ...base[lang],
    ...Object.fromEntries(
      Object.entries(pageFragments).map(([namespace, module]) => [namespace, module[lang]]),
    ),
  }
}

export const i18n = createI18n({
  legacy: false,
  globalInjection: true,
  locale: readInitial(),
  fallbackLocale: 'zh-CN',
  missingWarn: false,
  fallbackWarn: false,
  messages: {
    'zh-CN': pack('zhCN'),
    'zh-TW': pack('zhTW'),
    en: pack('en'),
  },
})

// 同步 <html lang>，保證無障礙與字體渲染策略正確
document.documentElement.lang = i18n.global.locale.value

export function setLang(lang: Lang): void {
  i18n.global.locale.value = lang
  document.documentElement.lang = lang
  try {
    localStorage.setItem(LANG_KEY, lang)
  } catch {
    // 忽略持久化失敗
  }
}

export default i18n
