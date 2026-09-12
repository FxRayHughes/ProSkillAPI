import type { Connection, XYPosition } from '@xyflow/react';
import { nodeRegistry } from './registry';
import { normalizeValues } from './fieldValues';
import type { NodeDefinition, SkillEdge, SkillNode, SkillProject } from './types';

/** Initialize typed defaults once, not on every inspector render. */
export function createNode(definition: NodeDefinition, position: XYPosition): SkillNode {
  return {
    id: crypto.randomUUID(),
    type: 'skill',
    position,
    data: {
      definitionId: definition.id,
      label: definition.label,
      values: normalizeValues(definition),
    },
  };
}

/** Enforce declared pins and an acyclic execution graph before accepting a wire. */
export function canConnect(connection: Connection | SkillEdge, project: SkillProject): boolean {
  const source = project.nodes.find((node) => node.id === connection.source);
  const target = project.nodes.find((node) => node.id === connection.target);
  const from = source && nodeRegistry.get(source.data.definitionId);
  const to = target && nodeRegistry.get(target.data.definitionId);
  if (
    !from?.outputs.some((port) => port.id === connection.sourceHandle) ||
    !to?.inputs.some((port) => port.id === connection.targetHandle)
  )
    return false;
  if (
    project.edges.some(
      (edge) =>
        edge.source === connection.source &&
        edge.target === connection.target &&
        edge.sourceHandle === connection.sourceHandle &&
        edge.targetHandle === connection.targetHandle,
    )
  )
    return false;
  const pending = [connection.target];
  const visited = new Set<string>();
  while (pending.length) {
    const id = pending.pop()!;
    if (id === connection.source) return false;
    if (visited.has(id)) continue;
    visited.add(id);
    pending.push(...project.edges.filter((edge) => edge.source === id).map((edge) => edge.target));
  }
  return true;
}

/** A new document always starts with a valid registered entry point. */
export function createProject(name = '新技能'): SkillProject {
  return {
    schemaVersion: 1,
    source: {},
    meta: {
      name,
      type: '主动技能',
      maxLevel: 1,
      skillReq: '',
      skillReqLevel: 0,
      needsPermission: false,
      msg: '',
      combo: '',
      icon: 'DIAMOND_SWORD',
      iconData: 0,
      iconDurability: 0,
      iconLore: [],
      description: [],
      attributes: {
        'level-base': '1',
        'level-scale': '0',
        'cost-base': '1',
        'cost-scale': '0',
        'cooldown-base': '0',
        'cooldown-scale': '0',
        'mana-base': '0',
        'mana-scale': '0',
        'points-spent-req-base': '0',
        'points-spent-req-scale': '0',
      },
      serverVersion: '1.16',
    },
    nodes: [createNode(nodeRegistry.get('TriggerCast')!, { x: 100, y: 160 })],
    edges: [],
  };
}
