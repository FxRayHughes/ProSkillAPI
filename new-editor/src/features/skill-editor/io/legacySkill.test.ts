import { registerLegacyCatalog } from '../model/legacyCatalog';
import { registerBuiltinPlugin } from '../../plugins/builtin';
import { describe, expect, it } from 'vitest';
import { load } from 'js-yaml';
import { readFileSync } from 'node:fs';
import { nodeRegistry } from '../model/registry';
import { findDefinition } from './legacyImport';
import { parseLegacySkill, serializeLegacySkill } from './legacySkill';
import { LAYOUT_KEY } from './legacyLayout';
import { particleFallbackIssues } from './particleDiagnostics';
import { isParticleAst } from '../model/particleCatalog';

registerLegacyCatalog();
registerBuiltinPlugin();

const FIXTURES = ['冲锋', '圣光术', '迪菲亚匪徒背刺', '被动攻击速度', '爆炎阵', '闪现'];

function fixture(name: string): string {
  return readFileSync(new URL(`./__fixtures__/${name}.yml`, import.meta.url), 'utf8');
}
function section(yaml: string): Record<string, never> {
  const raw = load(yaml) as Record<string, Record<string, never>>;
  return raw[Object.keys(raw).find((key) => key !== 'loaded')!];
}

interface Component {
  type: string;
  data?: Record<string, unknown>;
  children?: Record<string, Component>;
}

/** Old scalar particles become AST nodes; the semantic source value must survive. */
function sameValue(before: unknown, after: unknown): boolean {
  if (isParticleAst(after))
    return isParticleAst(before)
      ? JSON.stringify(before) === JSON.stringify(after)
      : String(before).toLowerCase() === after.value.toLowerCase();
  return String(before).toLowerCase() === String(after).toLowerCase();
}

/**
 * Compares what the server actually loads: component keys, order, types and values.
 * Keys the editor adds must be registered defaults, never invented data.
 */
function expectSameTree(before: Record<string, Component>, after: Record<string, Component>): void {
  expect(Object.keys(after)).toEqual(Object.keys(before));
  for (const key of Object.keys(before)) {
    expect(after[key].type).toBe(before[key].type);
    const source = before[key].data ?? {};
    const written = after[key].data ?? {};
    for (const [name, value] of Object.entries(source)) {
      expect(name in written, `${key}.${name} 在写出后丢失`).toBe(true);
      expect(sameValue(value, written[name]), `${key}.${name} 值被改写`).toBe(true);
    }
    const definition = nodeRegistry.get(findDefinition(key, before[key].type)!.id)!;
    for (const name of Object.keys(written)) {
      if (name in source) continue;
      const field = definition.fields.find((entry) => entry.key === name);
      expect(field, `${key}.${name} 不是该节点的已注册字段`).toBeDefined();
      expect(sameValue(field!.default, written[name]), `${key}.${name} 新增值不是默认值`).toBe(
        true,
      );
    }
    expectSameTree(before[key].children ?? {}, after[key].children ?? {});
  }
}

describe('native skill round-trip', () => {
  it('imports old particle scalars into a portable AST and exports that AST unchanged', () => {
    const source = `Particles:
  components:
    Cast-a:
      type: trigger
      data: {}
      children:
        Particle-b:
          type: mechanic
          data:
            particle: Block Crack
`;
    const ast = parseLegacySkill(source);
    const particle = ast.nodes[1].data.values.particle;
    expect(isParticleAst(particle)).toBe(true);
    if (!isParticleAst(particle)) throw new Error('particle AST missing');
    expect(particle.value).toBe('Block Crack');
    expect(particle.versions.v1_12).toBe('BLOCK_CRACK');
    expect(particle.versions.v1_20).toBe('BLOCK');
    const written = section(serializeLegacySkill(ast)).components as unknown as Record<
      string,
      Component
    >;
    expect(written['Cast-a'].children!['Particle-b'].data!.particle).toEqual(particle);
    expect(parseLegacySkill(serializeLegacySkill(ast)).nodes[1].data.values.particle).toEqual(
      particle,
    );
  });

  it('exports unknown particles as ASTs with no variants so runtime can use white CLOUD', () => {
    const ast = parseLegacySkill(`Particles:
  components:
    Cast-a:
      type: trigger
      data: {}
      children:
        Particle-a:
          type: mechanic
          data:
            particle: UNKNOWN_PARTICLE
`);
    expect(particleFallbackIssues(ast, '26.2')).toContain(
      'Particle-a.particle：UNKNOWN_PARTICLE 在 26.2 将使用白色 CLOUD',
    );
    const particle = ast.nodes[1].data.values.particle;
    expect(particle).toEqual({ kind: 'particle', value: 'UNKNOWN_PARTICLE', versions: {} });
    expect(serializeLegacySkill(ast)).toContain('UNKNOWN_PARTICLE');
  });

  it('preserves imported AST variants supplied by another editor', () => {
    const source = `Particles:
  components:
    Cast-a:
      type: trigger
      data: {}
      children:
        Particle-b:
          type: mechanic
          data:
            particle:
              kind: particle
              value: Custom
              versions:
                v1_20: FLAME
`;
    const ast = parseLegacySkill(source);
    expect(ast.nodes[1].data.values.particle).toEqual({
      kind: 'particle',
      value: 'Custom',
      versions: { v1_20: 'FLAME' },
    });
    expect(parseLegacySkill(serializeLegacySkill(ast)).nodes[1].data.values.particle).toEqual(
      ast.nodes[1].data.values.particle,
    );
  });
  for (const name of FIXTURES) {
    it(`${name} keeps its component tree, order and values`, () => {
      const source = fixture(name);
      const project = parseLegacySkill(source);
      const written = serializeLegacySkill(project);
      expectSameTree(
        section(source).components as unknown as Record<string, Component>,
        section(written).components as unknown as Record<string, Component>,
      );
      expect(parseLegacySkill(written).nodes).toHaveLength(project.nodes.length);
    });

    it(`${name} preserves skill metadata`, () => {
      const before = section(fixture(name)) as unknown as Record<string, unknown>;
      const after = section(
        serializeLegacySkill(parseLegacySkill(fixture(name))),
      ) as unknown as Record<string, unknown>;
      for (const key of ['name', 'type', 'max-level', 'skill-req-lvl', 'icon', 'msg']) {
        if (before[key] === undefined) continue;
        expect(String(after[key])).toBe(String(before[key]));
      }
      // Settings.load reads every attribute key, so list settings must survive as lists.
      expect(after.attributes).toEqual(
        Object.fromEntries(
          Object.entries(before.attributes as Record<string, unknown>).map(([key, value]) => [
            key,
            Array.isArray(value) ? value.map(String) : String(value),
          ]),
        ),
      );
      expect(after['icon-lore']).toEqual(before['icon-lore'] ?? []);
    });
  }

  it('restores node positions from the editor layout section', () => {
    const project = parseLegacySkill(fixture('冲锋'));
    project.nodes[1].position = { x: 1234, y: 567 };
    const written = serializeLegacySkill(project);
    expect((section(written) as unknown as Record<string, unknown>)[LAYOUT_KEY]).toBeDefined();
    const restored = parseLegacySkill(written);
    expect(restored.nodes[1].position).toEqual({ x: 1234, y: 567 });
  });

  it('reuses original component keys so an untouched skill produces no key churn', () => {
    const source = fixture('冲锋');
    const written = serializeLegacySkill(parseLegacySkill(source));
    const keys = (tree: Record<string, Component>): string[] =>
      Object.entries(tree).flatMap(([key, value]) => [key, ...keys(value.children ?? {})]);
    expect(keys(section(written).components as unknown as Record<string, Component>)).toEqual(
      keys(section(source).components as unknown as Record<string, Component>),
    );
  });

  it('rejects files that are not in the native format', () => {
    expect(() => parseLegacySkill('schemaVersion: 1\nnodes: []\nedges: []\n')).toThrow('原生格式');
  });

  it('reports unknown components by name instead of failing silently', () => {
    expect(() =>
      parseLegacySkill(`Test:
  components:
    Nonexistent-a:
      type: 'mechanic'
      data: {}
`),
    ).toThrow('未知组件：Nonexistent (mechanic)');
  });
});
