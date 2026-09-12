import { nodeRegistry } from '../model/registry';
import { componentName } from './legacyImport';
import { layoutPath } from './legacyLayout';
import type { XYPosition } from '@xyflow/react';
import type { SkillProject, SkillNode } from '../model/types';

/** Legacy uniqueness suffixes cycle a, b, c ... z, aa, ab ... within a single skill file. */
function suffix(index: number): string {
  let value = index;
  let out = '';
  do {
    out = String.fromCharCode(97 + (value % 26)) + out;
    value = Math.floor(value / 26) - 1;
  } while (value >= 0);
  return out;
}

interface ExportResult {
  components: Record<string, unknown>;
  positions: Map<string, XYPosition>;
}

/**
 * Converts the editor graph back into the ordered nested map the server loads.
 * Native components form a forest of triggers, so structures the format cannot express
 * are reported instead of being silently dropped.
 */
export function exportLegacyComponents(project: SkillProject): ExportResult {
  const byId = new Map(project.nodes.map((node) => [node.id, node]));
  const indegree = new Map(project.nodes.map((node) => [node.id, 0]));
  for (const edge of project.edges) {
    if (!byId.has(edge.source) || !byId.has(edge.target)) continue;
    indegree.set(edge.target, (indegree.get(edge.target) ?? 0) + 1);
  }
  for (const [id, count] of indegree) {
    if (count > 1)
      throw new Error(
        `节点“${label(byId.get(id)!)}”被多个父节点连接，原生格式不支持共享子树，请复制一份节点。`,
      );
  }
  for (const node of project.nodes) {
    const definition = nodeRegistry.get(node.data.definitionId);
    if (!definition) throw new Error(`未注册的技能节点：${node.data.definitionId}`);
    if (definition.kind === 'condition' && hasEdge(project, node.id, 'false'))
      throw new Error(
        `节点“${label(node)}”使用了“不满足”分支，原生格式的条件节点没有否定分支，请改用取反条件。`,
      );
    if (indegree.get(node.id) === 0 && definition.kind !== 'entry')
      throw new Error(`节点“${label(node)}”没有上级连接，技能的根节点必须是触发器节点。`);
  }

  const components: Record<string, unknown> = {};
  const positions = new Map<string, XYPosition>();
  const used = new Set<string>();
  const roots = project.nodes.filter((node) => indegree.get(node.id) === 0);
  // A skill with no trigger cannot run; an empty components section is the honest result.
  for (const root of order(roots)) emit(root, components, '', new Set());

  function emit(
    node: SkillNode,
    parent: Record<string, unknown>,
    parentPath: string,
    ancestors: Set<string>,
  ): void {
    if (ancestors.has(node.id))
      throw new Error(`节点“${label(node)}”形成了循环连接，无法写出为嵌套结构。`);
    const definition = nodeRegistry.get(node.data.definitionId)!;
    const key = allocate(node);
    const path = layoutPath(parentPath, key);
    positions.set(path, node.position);
    const children: Record<string, unknown> = {};
    const branch = definition.kind === 'condition' ? 'true' : definition.outputs[0]?.id;
    const targets = branch
      ? order(
          project.edges
            .filter((edge) => edge.source === node.id && edge.sourceHandle === branch)
            .map((edge) => byId.get(edge.target))
            .filter((target): target is SkillNode => Boolean(target)),
        )
      : [];
    const next = new Set(ancestors).add(node.id);
    for (const target of targets) emit(target, children, path, next);
    parent[key] = {
      type: definition.legacy?.category ?? 'mechanic',
      // Kept because the legacy editor writes it and addons may read it.
      indicator: '3D',
      data: { ...node.data.values },
      children,
    };
  }

  /** Reuse the imported key so an untouched skill produces an identical components tree. */
  function allocate(node: SkillNode): string {
    const definition = nodeRegistry.get(node.data.definitionId)!;
    const base = definition.legacy?.name ?? definition.id;
    if (node.data.legacyKey && !used.has(node.data.legacyKey)) {
      used.add(node.data.legacyKey);
      return node.data.legacyKey;
    }
    if (!used.has(base)) {
      used.add(base);
      return base;
    }
    for (let index = 0; ; index++) {
      const candidate = `${base}-${suffix(index)}`;
      if (!used.has(candidate)) {
        used.add(candidate);
        return candidate;
      }
    }
  }

  return { components, positions };
}

/** Sibling order is execution order; vertical position is the visible ordering in the canvas. */
function order(nodes: SkillNode[]): SkillNode[] {
  return [...nodes].sort((a, b) => a.position.y - b.position.y || a.position.x - b.position.x);
}

function hasEdge(project: SkillProject, source: string, handle: string): boolean {
  return project.edges.some((edge) => edge.source === source && edge.sourceHandle === handle);
}

function label(node: SkillNode): string {
  return node.data.legacyKey ? componentName(node.data.legacyKey) : node.data.label;
}
