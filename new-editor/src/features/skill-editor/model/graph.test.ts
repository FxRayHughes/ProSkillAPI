import { registerLegacyCatalog } from './legacyCatalog';
import { registerBuiltinPlugin } from '../../plugins/builtin';
import { describe, expect, it } from 'vitest';
import { canConnect, createNode, createProject } from './graph';
import { NodeRegistry, nodeRegistry } from './registry';
import { serializeLegacySkill, parseLegacySkill } from '../io/legacySkill';

registerLegacyCatalog();
registerBuiltinPlugin();

describe('node registry contract', () => {
  it('registers every legacy component constructor', () => {
    expect(nodeRegistry.list().length).toBeGreaterThanOrEqual(100);
    expect(nodeRegistry.get('MechanicDamage')).toBeDefined();
    expect(nodeRegistry.get('TriggerBlockBreak')).toBeDefined();
    expect(nodeRegistry.get('ConditionHealth')).toBeDefined();
  });
  it('registers the components this repository adds on top of the legacy editor', () => {
    for (const id of [
      'MechanicParticleRing',
      'MechanicParticlePolygon',
      'MechanicParticlePointLine',
      'MechanicDash',
      'TargetThreatLowest',
    ])
      expect(nodeRegistry.get(id), id).toBeDefined();
    expect(nodeRegistry.get('MechanicParticleRing')!.legacy).toEqual({
      name: 'Particle Ring',
      category: 'mechanic',
      container: false,
    });
  });
  it('rejects duplicate definitions and entry input ports', () => {
    const registry = new NodeRegistry();
    const entry = nodeRegistry.get('TriggerCast')!;
    registry.register(entry);
    expect(() => registry.register(entry)).toThrow('duplicate');
    expect(() =>
      registry.register({ ...entry, id: 'bad', inputs: [{ id: 'in', label: 'in' }] }),
    ).toThrow('Entry');
  });
  it('rejects duplicate pin IDs', () => {
    const registry = new NodeRegistry();
    const entry = nodeRegistry.get('TriggerCast')!;
    expect(() =>
      registry.register({ ...entry, outputs: [entry.outputs[0], entry.outputs[0]] }),
    ).toThrow('duplicate');
  });
});

describe('blueprint connections', () => {
  it('accepts real output pins and rejects missing pins, duplicates, entry targets and cycles', () => {
    const project = createProject();
    const entry = project.nodes[0];
    const damage = createNode(nodeRegistry.get('MechanicDamage')!, { x: 300, y: 100 });
    const second = createNode(nodeRegistry.get('MechanicParticle')!, { x: 600, y: 100 });
    project.nodes.push(damage, second);
    const wire = {
      source: entry.id,
      target: damage.id,
      sourceHandle: 'flow',
      targetHandle: 'flow',
    };
    expect(canConnect(wire, project)).toBe(true);
    expect(canConnect({ ...wire, sourceHandle: 'missing' }, project)).toBe(false);
    expect(canConnect({ ...wire, source: damage.id, target: entry.id }, project)).toBe(false);
    project.edges.push({ id: 'one', ...wire });
    expect(canConnect(wire, project)).toBe(false);
    project.edges.push({ id: 'two', ...wire, source: damage.id, target: second.id });
    expect(canConnect({ ...wire, source: second.id, target: damage.id }, project)).toBe(false);
  });
});

describe('structures the native format cannot express', () => {
  function wire(
    project: ReturnType<typeof createProject>,
    from: string,
    to: string,
    handle: string,
  ) {
    project.edges.push({
      id: `${from}-${to}`,
      source: from,
      sourceHandle: handle,
      target: to,
      targetHandle: 'flow',
    });
  }

  it('rejects a condition using its negative branch', () => {
    const project = createProject();
    const condition = createNode(nodeRegistry.get('ConditionHealth')!, { x: 300, y: 100 });
    const damage = createNode(nodeRegistry.get('MechanicDamage')!, { x: 600, y: 100 });
    project.nodes.push(condition, damage);
    wire(project, project.nodes[0].id, condition.id, 'flow');
    wire(project, condition.id, damage.id, 'false');
    expect(() => serializeLegacySkill(project)).toThrow('否定分支');
  });

  it('rejects a node reached from two parents', () => {
    const project = createProject();
    const first = createNode(nodeRegistry.get('MechanicDamage')!, { x: 300, y: 100 });
    const second = createNode(nodeRegistry.get('MechanicDamage')!, { x: 300, y: 300 });
    const shared = createNode(nodeRegistry.get('MechanicParticle')!, { x: 600, y: 200 });
    project.nodes.push(first, second, shared);
    wire(project, project.nodes[0].id, first.id, 'flow');
    wire(project, project.nodes[0].id, second.id, 'flow');
    wire(project, first.id, shared.id, 'flow');
    wire(project, second.id, shared.id, 'flow');
    expect(() => serializeLegacySkill(project)).toThrow('共享子树');
  });

  it('rejects a non-trigger node left without a parent', () => {
    const project = createProject();
    project.nodes.push(createNode(nodeRegistry.get('MechanicDamage')!, { x: 300, y: 100 }));
    expect(() => serializeLegacySkill(project)).toThrow('触发器');
  });

  it('orders siblings by vertical position so the canvas matches execution order', () => {
    const project = createProject();
    const lower = createNode(nodeRegistry.get('MechanicDamage')!, { x: 300, y: 400 });
    const upper = createNode(nodeRegistry.get('MechanicParticle')!, { x: 300, y: 100 });
    project.nodes.push(lower, upper);
    wire(project, project.nodes[0].id, lower.id, 'flow');
    wire(project, project.nodes[0].id, upper.id, 'flow');
    const restored = parseLegacySkill(serializeLegacySkill(project));
    expect(restored.nodes.map((node) => node.data.definitionId)).toEqual([
      'TriggerCast',
      'MechanicParticle',
      'MechanicDamage',
    ]);
  });
});
