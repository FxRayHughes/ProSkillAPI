import { NumberInput, Switch, Textarea, TextInput } from '@mantine/core';
import { NodeField } from '../../skill-editor/components/NodeField';
import { makeParticleAst, particleOptions } from '../../skill-editor/model/particleCatalog';
import type { ConfigField as Field } from '../model/configDocument';
import type { ConfigValue } from '../model/configDocument';

/** Controls follow the value shape, except particle enums use the shared AST selector. */
export function ConfigField({
  field,
  onChange,
  serverVersion,
}: {
  field: Field;
  onChange: (value: ConfigValue) => void;
  serverVersion: string;
}) {
  const description = field.description || undefined;
  if (field.kind === 'particle')
    return (
      <NodeField
        field={{
          key: field.key,
          label: field.key,
          tooltip: description,
          type: 'select',
          default: makeParticleAst('Flame'),
          options: particleOptions,
        }}
        value={field.value}
        onChange={onChange}
        serverVersion={serverVersion}
        particleDescription="保存时写入跨版本粒子 AST"
      />
    );
  if (field.kind === 'boolean')
    return (
      <Switch
        label={field.key}
        description={description}
        checked={field.value === true}
        onChange={(event) => onChange(event.currentTarget.checked)}
      />
    );
  if (field.kind === 'number')
    return (
      <NumberInput
        label={field.key}
        description={description}
        value={typeof field.value === 'number' ? field.value : 0}
        onChange={(value) => typeof value === 'number' && Number.isFinite(value) && onChange(value)}
      />
    );
  if (field.kind === 'string-list')
    return (
      <Textarea
        label={field.key}
        description={[description, '每行一条'].filter(Boolean).join('\n')}
        autosize
        minRows={2}
        value={Array.isArray(field.value) ? field.value.join('\n') : ''}
        onChange={(event) =>
          onChange(event.currentTarget.value === '' ? [] : event.currentTarget.value.split('\n'))
        }
      />
    );
  return (
    <TextInput
      label={field.key}
      description={description}
      value={String(field.value)}
      onChange={(event) => onChange(event.currentTarget.value)}
    />
  );
}
