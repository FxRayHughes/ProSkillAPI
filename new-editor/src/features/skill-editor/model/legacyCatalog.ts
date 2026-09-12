import { nodeRegistry } from './registry';
import catalog from './legacy.generated.json';
import generated from '../../generated/nodes.generated.json';
import { convertLegacyFields } from './legacyFields';
import type { LegacyField } from './legacyFields';
import type { FieldDefinition, NodeDefinition, PortDefinition } from './types';

/** Shape shared by the extracted legacy catalog and the built-in extension catalog. */
export interface LegacyCatalogEntry {
  id: string;
  name: string;
  category: string;
  container: boolean;
  /** 英文说明，注解目录必有；旧抽取目录也带这一项。 */
  description?: string;
  fields: LegacyField[];
  /**
   * 中文名与说明。注解生成的目录里，尚未补译的节点会缺这两项，
   * 因此是可选的——渲染时回退到英文，而不是显示 undefined。
   */
  displayNameZh?: string;
  descriptionZh?: string;
  /** 端口说明。注解目前不表达端口语义，生成的目录没有这一项。 */
  helpZh?: { ports: { id: string; description: string }[] };
  /**
   * 生效前提，由构建期的 exportNodes 从 @SkillNode 注解导出。
   * 旧的抽取式目录没有这一项，因此是可选的。
   */
  requires?: {
    plugins?: string[];
    nodes?: string[];
    capabilities?: string[];
  };
}

/** Each legacy entry becomes an independent node object with structured help. */
export function createLegacyNode(entry: LegacyCatalogEntry): NodeDefinition {
  const kind: NodeDefinition['kind'] = entry.id.startsWith('Trigger')
    ? 'entry'
    : entry.id.startsWith('Condition')
      ? 'condition'
      : 'action';
  const fields: FieldDefinition[] = convertLegacyFields(entry.fields);
  const inputs: PortDefinition[] = kind === 'entry' ? [] : [{ id: 'flow', label: '执行' }];
  const outputs: PortDefinition[] =
    kind === 'condition'
      ? [
          { id: 'true', label: '满足' },
          { id: 'false', label: '不满足' },
        ]
      : [{ id: 'flow', label: '执行' }];
  // 尚未补译的节点回退到英文，而不是显示空白
  const label = entry.displayNameZh ?? entry.name;
  const description = entry.descriptionZh ?? entry.description ?? '';
  return {
    id: entry.id,
    label,
    kind,
    description,
    color:
      kind === 'entry'
        ? '#df6d77'
        : kind === 'condition'
          ? '#7da7eb'
          : entry.id.startsWith('Target')
            ? '#58b779'
            : '#e5b35d',
    inputs,
    outputs,
    fields,
    help: {
      label,
      description,
      behavior: description,
      fields: fields.map((field) => ({
        key: field.key,
        description: field.tooltip ?? `配置${field.label}。`,
      })),
      ports: entry.helpZh?.ports ?? [],
      ...(entry.requires ? { requires: entry.requires } : {}),
    },
    legacy: { name: entry.name, category: entry.category, container: entry.container },
  };
}

/** Registers objects while preserving English IDs as the persistence protocol. */
export function registerLegacyCatalog(): void {
  for (const entry of catalog as LegacyCatalogEntry[])
    if (!nodeRegistry.get(entry.id)) nodeRegistry.register(createLegacyNode(entry));
}

/**
 * 注册由插件源码注解生成的节点目录。
 *
 * 这份目录来自构建期的 exportNodes 任务，是节点契约的权威来源——插件新增节点会自动出现，
 * 且带有生效前提信息。必须在 registerLegacyCatalog 之前调用：后者遇到已注册的 id 会跳过，
 * 从而让抽取式的旧目录只补齐这里没有的条目。
 */
export function registerGeneratedCatalog(): void {
  for (const entry of generated as LegacyCatalogEntry[])
    if (!nodeRegistry.get(entry.id)) nodeRegistry.register(createLegacyNode(entry));
}
