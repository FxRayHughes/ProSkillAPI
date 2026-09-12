import type { EditorPlugin } from './plugin';
import { nodeRegistry } from '../../skill-editor/model/registry';
import { registerBuiltinPlugin } from '../builtin';
const KEY = 'proskillapi.plugins.v1';
export function readPlugins(): EditorPlugin[] {
  try {
    return JSON.parse(localStorage.getItem(KEY) || '[]') as EditorPlugin[];
  } catch {
    return [];
  }
}
export function writePlugins(plugins: EditorPlugin[]): void {
  localStorage.setItem(KEY, JSON.stringify(plugins));
}
export function applyStoredPlugins(): void {
  // Built-in nodes come first so a stored plugin can still override them by ID.
  registerBuiltinPlugin();
  for (const plugin of readPlugins())
    for (const node of plugin.nodes) {
      try {
        if (plugin.overrides?.includes(node.id)) {
          nodeRegistry.override(node);
        } else {
          nodeRegistry.register(node);
        }
      } catch {
        /* stale plugin remains editable in manager */
      }
    }
}
