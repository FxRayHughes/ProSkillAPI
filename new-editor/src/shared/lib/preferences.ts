const THEME_KEY = 'proskillapi.theme';
export type ThemeMode = 'light' | 'dark';
export function readTheme(): ThemeMode {
  return localStorage.getItem(THEME_KEY) === 'light' ? 'light' : 'dark';
}
export function writeTheme(mode: ThemeMode): void {
  localStorage.setItem(THEME_KEY, mode);
}

const SERVER_VERSION_KEY = 'proskillapi.serverVersion';

/**
 * 自动保存只保存编辑器当前打开的文件，不改变服务端运行期状态。
 * 周期为 0 表示不启用定时保存；操作后保存使用编辑完成后的短暂防抖窗口。
 */
export interface AutoSavePreferences {
  enabled: boolean;
  intervalMs: number;
  saveAfterEdit: boolean;
}

const AUTO_SAVE_KEY = 'proskillapi.autoSave';
export const AUTO_SAVE_INTERVALS = [
  { value: 0, label: '关闭周期保存' },
  { value: 10_000, label: '每 10 秒' },
  { value: 30_000, label: '每 30 秒' },
  { value: 60_000, label: '每 1 分钟' },
  { value: 300_000, label: '每 5 分钟' },
] as const;

export const DEFAULT_AUTO_SAVE_PREFERENCES: AutoSavePreferences = {
  enabled: false,
  intervalMs: 30_000,
  saveAfterEdit: true,
};

function isSupportedInterval(value: unknown): value is number {
  return AUTO_SAVE_INTERVALS.some((entry) => entry.value === value);
}

export function readAutoSavePreferences(): AutoSavePreferences {
  try {
    const raw = localStorage.getItem(AUTO_SAVE_KEY);
    if (!raw) return DEFAULT_AUTO_SAVE_PREFERENCES;
    const parsed = JSON.parse(raw) as Partial<AutoSavePreferences>;
    return {
      enabled: parsed.enabled === true,
      intervalMs: isSupportedInterval(parsed.intervalMs)
        ? parsed.intervalMs
        : DEFAULT_AUTO_SAVE_PREFERENCES.intervalMs,
      saveAfterEdit: parsed.saveAfterEdit !== false,
    };
  } catch {
    // 浏览器隐私模式可能禁用 localStorage；编辑器仍应使用安全默认值运行。
    return DEFAULT_AUTO_SAVE_PREFERENCES;
  }
}

export function writeAutoSavePreferences(value: AutoSavePreferences): void {
  try {
    localStorage.setItem(AUTO_SAVE_KEY, JSON.stringify(value));
  } catch {
    // A blocked storage backend should not prevent editing; the current session still uses value.
  }
}

/**
 * 目标版本只用于编辑器枚举预览，不写入粒子 AST；仅在主页修改并全局复用。
 */
export const SERVER_VERSIONS = [
  '1.8',
  '1.9',
  '1.10',
  '1.11',
  '1.12',
  '1.13',
  '1.14',
  '1.15',
  '1.16',
  '1.17',
  '1.18',
  '1.19',
  '1.20',
  '1.21',
  '26.1',
  '26.2',
] as const;

export const DEFAULT_SERVER_VERSION = '1.21';

export function readServerVersion(): string {
  const stored = localStorage.getItem(SERVER_VERSION_KEY);
  // 旧偏好可能存了已下线的版本号，回退到默认值而不是让过滤逻辑拿到无效值
  return stored && (SERVER_VERSIONS as readonly string[]).includes(stored)
    ? stored
    : DEFAULT_SERVER_VERSION;
}

export function writeServerVersion(version: string): void {
  localStorage.setItem(SERVER_VERSION_KEY, version);
}
