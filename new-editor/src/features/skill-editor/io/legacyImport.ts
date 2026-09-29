import { createNode } from '../model/graph';
import { normalizeValues } from '../model/fieldValues';
import { nodeRegistry } from '../model/registry';
import { isParticleAst, isParticleField, makeParticleAst } from '../model/particleCatalog';
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
  const normalized: Record<string, FieldValue> = {};
  for (const [name, value] of Object.entries(values)) {
    if (isParticleField(name)) {
      // Old scalar configs become portable ASTs at import; an existing AST stays intact.
      if (typeof value === 'string') normalized[name] = makeParticleAst(value);
      else if (isParticleAst(value)) normalized[name] = value;
      else throw new Error(`粒子 AST 无效：${key}.${name}`);
      continue;
    }
    const scalar = ['string', 'number', 'boolean'].includes(typeof value);
    const list =
      Array.isArray(value) &&
      value.every((entry) => typeof entry === 'string' || typeof entry === 'number');
    const map =
      value !== null &&
      typeof value === 'object' &&
      !Array.isArray(value) &&
      Object.values(value).every((entry) =>
        ['string', 'number', 'boolean'].includes(typeof entry),
      );
    if (!scalar && !list && !map) throw new Error(`节点参数类型不支持：${key}.${name}`);
    normalized[name] = value as FieldValue;
  }
  return normalized;
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
      // Keep source tokens in the AST; only the server selects a Bukkit enum at runtime.
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
  // A run-group call with no continuation can be rendered as a direct wire. Synthetic
  // shared groups collapse back into the original multi-parent graph.
  const groups = new Map<string, SkillNode>();
  for (const node of nodes) if (nodeRegistry.get(node.data.definitionId)?.legacy?.name === 'GROUP') {
    const name = String(node.data.values.group ?? '');
    if (name) groups.set(name, node);
  }
  const removed = new Set<string>();
  const sharedBody = new Map<string, SkillNode>();
  for (const [name, group] of groups) {
    if (!name.startsWith('shared/')) continue;
    const children = edges.filter((edge) => edge.source === group.id);
    if (children.length !== 1) continue;
    const body = nodes.find((node) => node.id === children[0].target);
    if (!body) continue;
    body.data.sharedGroupKey = name;
    sharedBody.set(name, body);
    removed.add(group.id);
    removed.add(children[0].id);
  }
  for (const node of nodes) {
    if (nodeRegistry.get(node.data.definitionId)?.legacy?.name !== 'Run Group') continue;
    if (edges.some((edge) => edge.source === node.id)) continue;
    const name = String(node.data.values.group ?? '');
    const target = sharedBody.get(name) ?? groups.get(name);
    const incoming = edges.filter((edge) => edge.target === node.id);
    if (!target || incoming.length !== 1) continue;
    const duplicate = edges.some((edge) => edge.source === incoming[0].source && edge.target === target.id);
    if (duplicate) continue;
    incoming[0].target = target.id;
    incoming[0].targetHandle = sharedBody.has(name) ? 'flow' : 'invoke';
    removed.add(node.id);
  }
  return {
    nodes: nodes.filter((node) => !removed.has(node.id)),
    edges: edges.filter((edge) => !removed.has(edge.id) && !removed.has(edge.source) && !removed.has(edge.target)),
  };
}
