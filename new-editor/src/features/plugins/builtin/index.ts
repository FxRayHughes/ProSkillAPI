import extraNodes from './extra-nodes.generated.json';
import { createLegacyNode } from '../../skill-editor/model/legacyCatalog';
import type { LegacyCatalogEntry } from '../../skill-editor/model/legacyCatalog';
import { nodeRegistry } from '../../skill-editor/model/registry';
import type { EditorPlugin } from '../model/plugin';

/** Components that ship with this repository's server jar but not with the legacy editor. */
export const BUILTIN_PLUGIN_ID = 'proskillapi-extra';

/**
 * Built as a plugin so users can override it, and generated from the same catalog shape as
 * the legacy nodes so imported skills resolve these components through the normal path.
 */
export const builtinPlugin: EditorPlugin = {
  id: BUILTIN_PLUGIN_ID,
  name: 'ProSkillAPI 扩展节点',
  version: '1.0.0',
  description: '本仓库服务端新增的粒子轮廓机制，以及现有技能配置中出现的第三方组件。',
  nodes: (extraNodes as LegacyCatalogEntry[]).map(createLegacyNode),
};

/** Registered before stored plugins so a user-imported plugin of the same ID wins. */
export function registerBuiltinPlugin(): void {
  for (const node of builtinPlugin.nodes) {
    if (!nodeRegistry.get(node.id)) nodeRegistry.register(node);
  }
}
