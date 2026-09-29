import type { FieldDefinition, FieldValue, NodeDefinition } from './types';
import { isParticleAst, isParticleField, makeParticleAst } from './particleCatalog';

/**
 * Signal senders persist ordinary scalar rows. Their contract is a projection of
 * those rows, so the editor and runtime agree without asking authors to maintain
 * two independent maps. References remain text until the runtime resolves them.
 */
export function deriveSignalContract(value: FieldValue): Record<string, string> {
  if (!value || typeof value !== 'object' || Array.isArray(value)) return {};
  const contract: Record<string, string> = {};
  for (const [key, entry] of Object.entries(value)) {
    if (typeof entry === 'number' && Number.isFinite(entry)) contract[key] = 'number';
    else if (typeof entry === 'boolean') contract[key] = 'boolean';
    else if (typeof entry === 'string') contract[key] = 'text';
  }
  return contract;
}

/** Legacy option names are case-insensitive on import, but saved spelling stays canonical. */
export const normalizeOption = (value: unknown): string =>
  String(isParticleAst(value) ? value.value : value)
    .toLowerCase()
    .replaceAll('_', ' ');

/** Multiple requirements are ANDed: optional particle fields need both switches enabled. */
export function isFieldVisible(
  field: FieldDefinition,
  values: Record<string, FieldValue>,
): boolean {
  return (
    !field.requirements ||
    field.requirements.every((rule) =>
      rule.values.some((value) => normalizeOption(value) === normalizeOption(values[rule.key])),
    )
  );
}

/** Resolve hidden variants in declaration order, keeping unrelated imported values intact. */
export function normalizeValues(
  definition: NodeDefinition,
  saved: Record<string, FieldValue> = {},
): Record<string, FieldValue> {
  const values = structuredClone(saved);
  for (const field of definition.fields) {
    if (!isFieldVisible(field, values)) continue;
    if (values[field.key] === undefined) values[field.key] = structuredClone(field.default);
    if (field.type === 'select') {
      // Particle tokens belong to the portable AST; only the runtime lowers them to a
      // Bukkit enum. Old scalar values are wrapped, while imported AST variants stay intact.
      if (isParticleField(field.key)) {
        if (typeof values[field.key] === 'string')
          values[field.key] = makeParticleAst(values[field.key] as string);
        continue;
      }
      const match = field.options.find(
        (option) => normalizeOption(option.value) === normalizeOption(values[field.key]),
      );
      // Preserve unrecognized imported enum values; UI offers them alongside current options.
      if (match) values[field.key] = field.numeric ? Number(match.value) : match.value;
    }
  }
  return values;
}

/** Switching a discriminator resets only newly visible variants, never unrelated fields. */
export function changeField(
  definition: NodeDefinition,
  saved: Record<string, FieldValue>,
  key: string,
  value: FieldValue,
): Record<string, FieldValue> {
  const before = normalizeValues(definition, saved);
  const after = { ...before, [key]: value };
  for (const field of definition.fields) {
    if (field.key !== key && !isFieldVisible(field, before) && isFieldVisible(field, after)) {
      const replacesVariant = definition.fields.some(
        (previous) =>
          previous !== field && previous.key === field.key && isFieldVisible(previous, before),
      );
      if (after[field.key] === undefined || replacesVariant)
        after[field.key] = structuredClone(field.default);
    }
  }
  return normalizeValues(definition, after);
}
