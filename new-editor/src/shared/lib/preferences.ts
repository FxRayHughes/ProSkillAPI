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
 * 目标服务端版本只用于过滤枚举选项（粒子、材质、音效等），不写入技能文件，
 * 因此它属于编辑器的全局偏好，而不是某个技能的属性。
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
