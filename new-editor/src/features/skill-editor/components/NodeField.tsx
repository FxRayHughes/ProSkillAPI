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
import type { FieldDefinition, FieldValue } from '../model/types';

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
}: {
  field: FieldDefinition;
  value: FieldValue;
  onChange: (value: FieldValue) => void;
  /** Filters versioned Minecraft enums while keeping their English wire values. */
  serverVersion?: string;
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
  if (field.type === 'select') {
    const current = String(value);
    const version = Number.parseFloat(serverVersion);
    const data = field.options.filter(
      (option) =>
        (!option.since || version >= Number.parseFloat(option.since)) &&
        (!option.until || version <= Number.parseFloat(option.until)),
    );
    if (!data.some((option) => option.value === current))
      data.unshift({ value: current, label: current ? '当前导入值' : '无' });
    return (
      <Select
        label={label}
        description="此项会以英文键写入技能文件"
        searchable
        allowDeselect={false}
        data={data}
        value={current}
        onChange={(next) => {
          if (next !== null) onChange(field.numeric ? Number(next) : next);
        }}
      />
    );
  }
  if (field.type === 'multiselect') {
    const selected = Array.isArray(value) ? value : [];
    const version = Number.parseFloat(serverVersion);
    const data = (field.options ?? []).filter(
      (option) =>
        (!option.since || version >= Number.parseFloat(option.since)) &&
        (!option.until || version <= Number.parseFloat(option.until)),
    );
    for (const item of selected)
      if (!data.some((option) => option.value === item))
        data.push({ value: item, label: `当前导入值 ${data.length + 1}` });
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
