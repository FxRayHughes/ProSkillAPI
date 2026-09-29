import {
  MultiSelect,
  NumberInput,
  Select,
  Stack,
  Switch,
  Textarea,
  TextInput,
  Tooltip,
  Text,
} from '@mantine/core';
import { useState } from 'react';
import { isOptionAvailable } from '../model/versionOptions';
import {
  isParticleAst,
  isParticleField,
  makeParticleAst,
  particlePreview,
} from '../model/particleCatalog';
import type { FieldDefinition, FieldValue } from '../model/types';
import { MapField } from './MapField';

/** Numeric drafts allow clearing and negative/decimal edits without forcing an immediate reset. */
function NumericField({
  field,
  value,
  onChange,
}: {
  field: Extract<FieldDefinition, { type: 'number' }>;
  value: FieldValue;
  onChange: (value: FieldValue) => void;
}) {
  const [draft, setDraft] = useState<string | number>(
    typeof value === 'number' ? value : Number(value),
  );
  return (
    <NumberInput
      label={field.label}
      description={field.key}
      value={draft}
      allowDecimal={!field.integer}
      onBlur={() => {
        if (draft === '' || !Number.isFinite(Number(draft))) setDraft(Number(value));
      }}
      onChange={(next) => {
        setDraft(next);
        if (typeof next === 'number' && Number.isFinite(next)) onChange(next);
      }}
    />
  );
}

/** Typed controls retain English option payloads and exact list order, including blank lines. */
export function NodeField({
  field,
  value,
  onChange,
  serverVersion = '1.16',
  particleDescription,
  readOnly = false,
}: {
  field: FieldDefinition;
  value: FieldValue;
  onChange: (value: FieldValue) => void;
  /** Filters versioned Minecraft enums while keeping their English wire values. */
  serverVersion?: string;
  /** Config files reuse this selector but use a different persistence description. */
  particleDescription?: string;
  readOnly?: boolean;
}) {
  const label = (
    <Tooltip label={field.tooltip || field.label} multiline w={260} withArrow>
      <Text span size="sm" fw={500}>
        {field.label}
      </Text>
    </Tooltip>
  );
  if (field.type === 'number')
    return (
      <NumericField key={field.id ?? field.key} field={field} value={value} onChange={onChange} />
    );
  if (field.type === 'boolean')
    return (
      <Switch
        label={label}
        checked={value === true}
        onChange={(event) => onChange(event.currentTarget.checked)}
      />
    );
  if (field.type === 'map')
    return <MapField key={field.id ?? field.key} label={field.label} value={value} onChange={onChange} mode={field.key === 'contract' ? 'contract' : 'scalar'} readOnly={readOnly} />;
  if (field.type === 'select') {
    const particle = isParticleField(field.key) && isParticleAst(value) ? value : undefined;
    const current = particle?.value ?? String(value);
    // The selector previews the stored AST variant; it never decides the runtime server version.
    const rendered = particle ? (particlePreview(particle, serverVersion) ?? current) : current;
    const data = field.options.filter((option) => isOptionAvailable(option, serverVersion));
    if (!data.some((option) => option.value === rendered))
      data.unshift({
        value: rendered,
        // Unknown imports stay selectable so editing another field never erases the payload.
        label: current ? `当前值（${serverVersion} 将使用白色 CLOUD）：${current}` : '无',
      });
    return (
      <Select
        label={label}
        description={
          particle && rendered !== current
            ? `AST 原值：${current}；${serverVersion} 服务端使用 ${rendered}`
            : (particleDescription ?? '此项会以英文键写入技能文件')
        }
        searchable
        allowDeselect={false}
        data={data}
        value={rendered}
        onChange={(next) => {
          if (next !== null)
            onChange(
              isParticleField(field.key)
                ? makeParticleAst(next)
                : field.numeric
                  ? Number(next)
                  : next,
            );
        }}
      />
    );
  }
  if (field.type === 'multiselect') {
    const selected = Array.isArray(value) ? value : [];
    const data = (field.options ?? []).filter((option) => isOptionAvailable(option, serverVersion));
    for (const item of selected)
      if (!data.some((option) => option.value === item))
        data.push({ value: item, label: `当前值（${serverVersion} 不支持）：${item}` });
    return (
      <MultiSelect
        label={label}
        description="此项会以英文键写入技能文件"
        searchable
        data={data}
        value={selected}
        onChange={onChange}
      />
    );
  }
  if (field.type === 'string-list')
    return (
      <Textarea
        label={label}
        description="此项会以英文键写入技能文件"
        minRows={3}
        autosize
        value={Array.isArray(value) ? value.join('\n') : ''}
        onChange={(event) =>
          onChange(event.currentTarget.value === '' ? [] : event.currentTarget.value.split('\n'))
        }
      />
    );
  return (
    <Stack gap={0}>
      <TextInput
        label={label}
        description="此项会以英文键写入技能文件"
        value={String(value)}
        onChange={(event) => onChange(event.currentTarget.value)}
      />
    </Stack>
  );
}
