import { NumberInput, Switch, Textarea, TextInput } from '@mantine/core';
import type { ConfigField as Field } from '../model/configDocument';

/** Controls are chosen from the value's shape; the YAML comment becomes the help text. */
export function ConfigField({
  field,
  onChange,
}: {
  field: Field;
  onChange: (value: boolean | number | string | string[]) => void;
}) {
  const description = field.description || undefined;
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
