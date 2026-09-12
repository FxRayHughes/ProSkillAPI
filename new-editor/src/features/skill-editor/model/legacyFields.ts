import { fieldLabel, optionLabel } from './fieldLabels';
import type { FieldDefinition } from './types';

/** Snapshot of the old input instances, including helper-generated fields and visibility rules. */
export interface LegacyField {
  kind: string;
  key: string;
  label: string;
  value?: string | number | (string | number)[];
  base?: number | string;
  scale?: number | string;
  index?: number;
  values?: string[];
  options?: string[];
  tooltip?: string;
  labelZh?: string;
  tooltipZh?: string;
  optionsZh?: { value: string; label: string; since?: string; until?: string }[];
  requirements?: { key: string; values: (string | number)[] }[];
}

/** Attributes use the original flat -base/-scale keys, including expression strings. */
export function convertLegacyFields(fields: readonly LegacyField[]): FieldDefinition[] {
  return fields.flatMap((field, index): FieldDefinition[] => {
    const common = {
      key: field.key,
      id: `${field.key}:${index}`,
      label: field.labelZh ?? fieldLabel(field.label),
      // Tooltip text is presentation-only; preserve the English protocol text in the field key/value.
      tooltip: field.tooltipZh ?? `用于配置“${fieldLabel(field.label)}”。`,
      requirements: field.requirements,
    };
    const options =
      field.optionsZh ??
      (field.options ?? []).map((value) => ({ value, label: optionLabel(value) }));
    switch (field.kind) {
      case 'StringValue':
        return [{ ...common, type: 'text', default: String(field.value ?? '') }];
      case 'IntValue':
      case 'DoubleValue':
        return [
          {
            ...common,
            type: 'number',
            integer: field.kind === 'IntValue',
            default: Number(field.value ?? 0),
          },
        ];
      case 'ListValue':
        return [{ ...common, type: 'select', default: String(field.value ?? ''), options }];
      case 'IndexListValue':
        return [
          {
            ...common,
            type: 'select',
            numeric: true,
            default: field.index ?? 0,
            options: options.map((option, i) => ({ ...option, value: String(i) })),
          },
        ];
      case 'MultiListValue':
        return [{ ...common, type: 'multiselect', default: field.values ?? [], options }];
      case 'StringListValue':
        return [
          {
            ...common,
            type: 'string-list',
            default: Array.isArray(field.value) ? field.value.map(String) : [],
          },
        ];
      case 'AttributeValue':
        return (['base', 'scale'] as const).map((part) => ({
          ...common,
          id: `${common.id}:${part}`,
          key: `${field.key}-${part}`,
          label: `${common.label} · ${part === 'base' ? '基础值' : '每级成长'}`,
          // Text controls deliberately accept legacy named values/formulas, not just numbers.
          type: 'text',
          default: String(field[part] ?? 0),
        }));
      default:
        throw new Error(`Unsupported legacy field: ${field.kind}`);
    }
  });
}
