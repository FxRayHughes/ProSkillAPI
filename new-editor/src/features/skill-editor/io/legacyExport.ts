import { nodeRegistry } from '../model/registry';
import { componentName } from './legacyImport';
import { layoutPath } from './legacyLayout';
import { isParticleField, makeParticleAst } from '../model/particleCatalog';
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
 * Shared graph branches become named GROUP roots plus run group references.
 * The server still receives an ordinary v1 components forest.
 */
export function exportLegacyComponents(project: SkillProject): ExportResult {
  const byId = new Map(project.nodes.map((node) => [node.id, node]));
  const indegree = new Map(project.nodes.map((node) => [node.id, 0]));
  for (const edge of project.edges) {
    if (!byId.has(edge.source) || !byId.has(edge.target)) continue;
    // A wire into GROUP is a call edge; the declaration remains a root.
    if (isGroup(byId.get(edge.target)!)) continue;
    indegree.set(edge.target, (indegree.get(edge.target) ?? 0) + 1);
  }
  const shared = new Map(
    [...indegree].filter(([, count]) => count > 1).map(([id]) => {
      const node = byId.get(id)!;
      return [id, node.data.sharedGroupKey ?? `shared/${id}`];
    }),
  );
  const namedGroups = new Set<string>();
  for (const node of project.nodes) {
    const definition = nodeRegistry.get(node.data.definitionId);
    if (!definition) throw new Error(`未注册的技能节点：${node.data.definitionId}`);
    if (isGroup(node)) {
      const name = String(node.data.values.group ?? '').trim();
      if (!name || namedGroups.has(name)) throw new Error(`共享组键不能为空或重复：${name}`);
      namedGroups.add(name);
    }
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
  for (const root of order(roots)) emit(root, components, '', new Set(), isGroup(root));
  // A shared node has one body. Every incoming wire instead writes a small goto.
  for (const [id, name] of shared) {
    if (namedGroups.has(name)) throw new Error(`共享组键冲突：${name}`);
    const rootKey = allocateKey('GROUP');
    const rootPath = layoutPath('', rootKey);
    const body: Record<string, unknown> = {};
    const node = byId.get(id)!;
    positions.set(rootPath, { x: node.position.x - 280, y: node.position.y });
    emit(node, body, rootPath, new Set(), true);
    components[rootKey] = { type: 'trigger', indicator: '3D', data: { group: name }, children: body };
  }

  function emit(
    node: SkillNode,
    parent: Record<string, unknown>,
    parentPath: string,
    ancestors: Set<string>,
    body = false,
  ): void {
    if (ancestors.has(node.id))
      throw new Error(`节点“${label(node)}”形成了循环连接，无法写出为嵌套结构。`);
    const definition = nodeRegistry.get(node.data.definitionId)!;
    const reference = !body && (shared.get(node.id) ?? (isGroup(node) ? String(node.data.values.group) : undefined));
    if (reference) {
      const key = allocateKey('Run Group');
      const path = layoutPath(parentPath, key);
      positions.set(path, node.position);
      parent[key] = { type: 'mechanic', indicator: '3D', data: { group: reference }, children: {} };
      return;
    }
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
    for (const target of targets) {
      if (isGroup(target)) {
        // A direct wire to a GROUP is a call edge. Persist it as the same
        // runtime mechanic used by the explicit "run group" node so both
        // editor forms retain identical execution semantics in v1 YAML.
        const group = String(target.data.values.group ?? '').trim();
        if (!group) throw new Error(`共享组“${label(target)}”缺少组键。`);
        const key = allocateKey('Run Group');
        const callPath = layoutPath(path, key);
        positions.set(callPath, target.position);
        children[key] = { type: 'mechanic', indicator: '3D', data: { group }, children: {} };
      } else {
        emit(target, children, path, next);
      }
    }
    parent[key] = {
      type: definition.legacy?.category ?? 'mechanic',
      // Kept because the legacy editor writes it and addons may read it.
      indicator: '3D',
      // Even programmatic scalar updates are wrapped, so exports always persist the AST.
      data: Object.fromEntries(
        Object.entries(node.data.values).map(([field, value]) => [
          field,
          isParticleField(field) && typeof value === 'string' ? makeParticleAst(value) : value,
        ]),
      ),
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

  /** Synthetic references use the same sibling-key uniqueness contract. */
  function allocateKey(base: string): string {
    if (!used.has(base)) { used.add(base); return base; }
    for (let index = 0; ; index++) {
      const candidate = `${base}-${suffix(index)}`;
      if (!used.has(candidate)) { used.add(candidate); return candidate; }
    }
  }

  return { components, positions };
}

function isGroup(node: SkillNode): boolean {
  return nodeRegistry.get(node.data.definitionId)?.legacy?.name === 'GROUP';
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
