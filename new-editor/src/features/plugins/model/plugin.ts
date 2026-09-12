import type { NodeDefinition } from '../../skill-editor/model/types';
export interface EditorPlugin {
  id: string;
  name: string;
  version: string;
  description?: string;
  nodes: NodeDefinition[];
  overrides?: string[];
}
export function parsePlugin(source: string): EditorPlugin {
  const value = JSON.parse(source) as EditorPlugin;
  if (!value.id || !value.name || !value.version || !Array.isArray(value.nodes))
    throw new Error('插件必须包含 id、name、version 和 nodes');
  if (
    value.nodes.some(
      (node) =>
        !node.id ||
        !node.label ||
        !node.kind ||
        !Array.isArray(node.inputs) ||
        !Array.isArray(node.outputs) ||
        !Array.isArray(node.fields),
    )
  )
    throw new Error('插件节点定义不完整');
  return value;
}

/**
 * 把插件序列化为可再次导入的 JSON。
 *
 * 输出刻意与 {@link parsePlugin} 的输入格式一致，导出的文件能原样导回，
 * 便于在多台机器或多个服主之间搬运节点定义。
 */
export function serializePlugin(plugin: EditorPlugin): string {
  // 字段顺序固定，使版本管理里的 diff 稳定可读
  const ordered: EditorPlugin = {
    id: plugin.id,
    name: plugin.name,
    version: plugin.version,
    ...(plugin.description ? { description: plugin.description } : {}),
    nodes: plugin.nodes,
    ...(plugin.overrides?.length ? { overrides: plugin.overrides } : {}),
  };
  return `${JSON.stringify(ordered, null, 2)}\n`;
}

/** 导出文件名：插件 id 与版本，便于区分同一插件的多个版本。 */
export function pluginFileName(plugin: EditorPlugin): string {
  return `${plugin.id}-v${plugin.version}.json`;
}

/**
 * 用注册表里的当前定义打包一个插件。
 *
 * 从注册表取而不是直接用存储的副本：编辑器可能已加载了同 id 的更新版本，
 * 导出时应反映用户此刻实际在用的定义。取不到的节点回退到插件自带副本。
 */
export function collectPluginNodes(
  plugin: EditorPlugin,
  lookup: (id: string) => NodeDefinition | undefined,
): EditorPlugin {
  return {
    ...plugin,
    nodes: plugin.nodes.map((node) => lookup(node.id) ?? node),
  };
}
