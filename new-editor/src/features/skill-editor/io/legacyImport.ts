import { createNode } from '../model/graph';
import { normalizeValues } from '../model/fieldValues';
import { nodeRegistry } from '../model/registry';
import { layoutPath } from './legacyLayout';
import type { XYPosition } from '@xyflow/react';
import type { FieldValue, NodeDefinition, SkillNode, SkillEdge } from '../model/types';

/** Component keys carry a uniqueness suffix, e.g. "Damage-g" resolves to "Damage". */
export function componentName(key: string): string {
  return key.replace(/-[a-z]+$/i, '');
}

/** Matches by the English constructor name and category, which are the persistence protocol. */
export function findDefinition(key: string, type: unknown): NodeDefinition | undefined {
  const name = componentName(key).toLowerCase();
  const category = String(type).toLowerCase();
  return nodeRegistry
    .list()
    .find(
      (candidate) =>
        candidate.legacy?.category === category && candidate.legacy.name.toLowerCase() === name,
    );
}

function assertFieldValues(key: string, values: unknown): Record<string, FieldValue> {
  if (!values || typeof values !== 'object' || Array.isArray(values))
    throw new Error(`节点参数必须为映射：${key}`);
  for (const [name, value] of Object.entries(values)) {
    const scalar = ['string', 'number', 'boolean'].includes(typeof value);
    const list =
      Array.isArray(value) &&
      value.every((entry) => typeof entry === 'string' || typeof entry === 'number');
    if (!scalar && !list) throw new Error(`节点参数类型不支持：${key}.${name}`);
  }
  return values as Record<string, FieldValue>;
}

/** Old files encode the English component name in the map key and category in type. */
export function importLegacyComponents(
  components: unknown,
  positions = new Map<string, XYPosition>(),
): { nodes: SkillNode[]; edges: SkillEdge[] } {
  const nodes: SkillNode[] = [];
  const edges: SkillEdge[] = [];
  const walk = (tree: unknown, depth: number, parentPath: string, parent?: SkillNode) => {
    if (!tree || typeof tree !== 'object' || Array.isArray(tree))
      throw new Error('旧版节点必须为名称映射');
    if (depth > 100) throw new Error('节点嵌套超过 100 层');
    for (const [key, raw] of Object.entries(tree)) {
      if (!raw || typeof raw !== 'object' || Array.isArray(raw))
        throw new Error(`节点配置无效：${key}`);
      const item = raw as Record<string, unknown>;
      const definition = findDefinition(key, item.type);
      if (!definition) throw new Error(`未知组件：${componentName(key)} (${String(item.type)})`);
      const path = layoutPath(parentPath, key);
      const node = createNode(
        definition,
        positions.get(path) ?? { x: 80 + depth * 300, y: 80 + nodes.length * 150 },
      );
      node.data.legacyKey = key;
      node.data.values = normalizeValues(definition, assertFieldValues(key, item.data ?? {}));
      nodes.push(node);
      if (parent)
        edges.push({
          id: crypto.randomUUID(),
          source: parent.id,
          target: node.id,
          // A native condition only stores the satisfied branch, so children wire to "true".
          sourceHandle: nodeRegistry.get(parent.data.definitionId)!.outputs[0].id,
          targetHandle: 'flow',
        });
      if (item.children) walk(item.children, depth + 1, path, node);
    }
  };
  walk(components, 0, '');
  return { nodes, edges };
}
