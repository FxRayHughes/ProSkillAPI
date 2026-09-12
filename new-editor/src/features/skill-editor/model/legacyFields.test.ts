import { registerLegacyCatalog } from './legacyCatalog';
import { registerBuiltinPlugin } from '../../plugins/builtin';
import { describe, expect, it } from 'vitest';
import { load } from 'js-yaml';
import catalog from './legacy.generated.json';
import { nodeRegistry } from './registry';
import { changeField, isFieldVisible, normalizeValues } from './fieldValues';
import { createNode, createProject } from './graph';
import { serializeLegacySkill, parseLegacySkill } from '../io/legacySkill';
import { fieldLabel } from './fieldLabels';

registerLegacyCatalog();
registerBuiltinPlugin();

describe('complete old-node parameter forms', () => {
  it('stores complete Chinese presentation data without changing protocol values', () => {
    for (const entry of catalog) {
      expect(entry.displayNameZh).toMatch(/[\u3400-\u9fff]/);
      expect(entry.displayNameZh).not.toMatch(/[A-Za-z]/);
      expect(entry.descriptionZh).toMatch(/[\u3400-\u9fff]/);
      expect(entry.descriptionZh).not.toMatch(/[A-Za-z]/);
      for (const field of entry.fields) {
        expect(field.labelZh).toMatch(/[\u3400-\u9fff]/);
        expect(field.tooltipZh).toMatch(/[\u3400-\u9fff]/);
        expect(field.tooltipZh).not.toMatch(/[A-Za-z]/);
        field.optionsZh.forEach((option) => {
          expect(option.label).toMatch(/[\u3400-\u9fff]/);
          expect(option.label).not.toMatch(/[A-Za-z]/);
          expect(field.options).toContain(option.value);
        });
      }
    }
  });
  for (const entry of catalog) {
    it(`${entry.id} exposes all original keys and conditional variants`, () => {
      const definition = nodeRegistry.get(entry.id)!;
      const expected = entry.fields.flatMap((field) =>
        field.kind === 'AttributeValue' ? [`${field.key}-base`, `${field.key}-scale`] : [field.key],
      );
      expect(definition.fields.map((field) => field.key)).toEqual(expected);
      expect(definition.fields.length).toBeGreaterThan(0);
      for (const field of entry.fields) expect(fieldLabel(field.label)).toMatch(/[\u3400-\u9fff]/);
    });
  }
  it('switches trigger-specific type options while preserving ordinary parameters', () => {
    const def = nodeRegistry.get('MechanicTrigger')!;
    let values = normalizeValues(def);
    values = changeField(def, values, 'trigger', 'Crouch');
    expect(values.type).toBe('Start Crouching');
    values = changeField(def, values, 'trigger', 'Launch');
    expect(values.type).toBe('Any');
    const visible = def.fields.filter(
      (field) => field.key === 'type' && isFieldVisible(field, values),
    );
    expect(visible).toHaveLength(1);
    expect(
      visible[0].type === 'select' && visible[0].options.some((option) => option.value === 'Arrow'),
    ).toBe(true);
  });
  it('keeps both projectile count growth and packet count independently editable', () => {
    const def = nodeRegistry.get('MechanicParticleProjectile')!;
    let values = normalizeValues(def);
    values = changeField(def, values, 'amount-base', '7');
    values = changeField(def, values, 'amount', 18);
    expect(values['amount-base']).toBe('7');
    expect(values.amount).toBe(18);
  });
  it('respects compound particle requirements and original English options', () => {
    const def = nodeRegistry.get('MechanicItemProjectile')!;
    let values = normalizeValues(def);
    const material = def.fields.find((field) => field.key === '-particle-material')!;
    expect(isFieldVisible(material, values)).toBe(false);
    values = changeField(def, values, 'use-effect', 'True');
    values = changeField(def, values, '-particle-type', 'Block Crack');
    expect(isFieldVisible(material, values)).toBe(true);
    values = changeField(def, values, '-particle-material', 'Stone');
    values = changeField(def, values, 'use-effect', 'False');
    expect(isFieldVisible(material, values)).toBe(false);
    values = changeField(def, values, 'use-effect', 'True');
    expect(values['-particle-material']).toBe('Stone');
  });
  it('round-trips edited growth, signed decimals, lists and enum values', () => {
    const project = createProject();
    const node = createNode(nodeRegistry.get('MechanicParticle')!, { x: 300, y: 100 });
    node.data.values['radius-base'] = 'api-radius';
    node.data.values['radius-scale'] = '-0.5';
    node.data.values.dx = -1.25;
    node.data.values = changeField(
      nodeRegistry.get('MechanicParticle')!,
      node.data.values,
      'particle',
      'Block Crack',
    );
    const messages = createNode(nodeRegistry.get('MechanicArmorStand')!, { x: 300, y: 400 });
    messages.data.values.skills = ['FirstSkill', '', '中文技能'];
    project.nodes.push(node, messages);
    for (const child of [node, messages])
      project.edges.push({
        id: `edge-${child.id}`,
        source: project.nodes[0].id,
        sourceHandle: 'flow',
        target: child.id,
        targetHandle: 'flow',
      });
    const exported = serializeLegacySkill(project);
    const restored = parseLegacySkill(exported);
    expect(restored.nodes[1].data.values).toEqual(node.data.values);
    expect(restored.nodes[2].data.values.skills).toEqual(['FirstSkill', '', '中文技能']);
    // Presentation labels are derived from the registry, never persisted.
    const saved = load(exported) as Record<string, { components: Record<string, unknown> }>;
    expect(JSON.stringify(saved)).not.toContain('definitionId');
  });
  it('imports actual old nested data with English names and fills missing defaults', () => {
    const project = parseLegacySkill(`Fireball:
  max-level: 5
  components:
    Cast-a:
      type: trigger
      data: {icon-key: ''}
      children:
        Damage-b:
          type: mechanic
          data:
            type: Damage
            value-base: 17
            value-scale: 2
            counts: 'False'
`);
    expect(project.nodes[1].data.definitionId).toBe('MechanicDamage');
    expect(project.nodes[1].data.values['value-base']).toBe(17);
    expect(project.nodes[1].data.values.counts).toBe('False');
    expect(project.nodes[1].data.values.classifier).toBe('default');
    expect(project.edges).toHaveLength(1);
    expect(parseLegacySkill(serializeLegacySkill(project)).nodes[1].data.values).toEqual(
      project.nodes[1].data.values,
    );
  });
});
