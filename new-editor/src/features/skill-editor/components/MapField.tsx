import { ActionIcon, Group, NumberInput, Select, Stack, TextInput, Button, Text } from '@mantine/core';
import { Plus, Trash2 } from 'lucide-react';
import { useMemo, useState } from 'react';
import type { FieldValue } from '../model/types';

type Scalar = string | number | boolean;

/**
 * Edits a native map as rows. Users choose a value type and never need to know
 * that the legacy YAML codec stores the result as a JSON-compatible object.
 */
export function MapField({
  label,
  value,
  onChange,
  mode = 'scalar',
  readOnly = false,
}: {
  label: string;
  value: FieldValue;
  onChange: (value: FieldValue) => void;
  mode?: 'scalar' | 'contract';
  /** Derived maps are displayed for auditability but cannot drift from their source rows. */
  readOnly?: boolean;
}) {
  const [keyErrors, setKeyErrors] = useState<Record<string, string>>({});
  const entries = useMemo(() => {
    if (!value || typeof value !== 'object' || Array.isArray(value)) return [];
    return Object.entries(value as Record<string, Scalar>);
  }, [value]);
  const update = (next: Record<string, Scalar>) => onChange(next);
  const changeEntry = (oldKey: string, key: string, next: Scalar) => {
    const normalizedKey = key.trim();
    if (!normalizedKey) {
      setKeyErrors((errors) => ({ ...errors, [oldKey]: '参数名不能为空' }));
      return;
    }
    if (normalizedKey !== oldKey && entries.some(([entryKey]) => entryKey !== oldKey && entryKey === normalizedKey)) {
      setKeyErrors((errors) => ({ ...errors, [oldKey]: '参数名不能重复' }));
      return;
    }
    setKeyErrors((errors) => {
      const nextErrors = { ...errors };
      delete nextErrors[oldKey];
      return nextErrors;
    });
    const nextMap: Record<string, Scalar> = {};
    for (const [entryKey, entryValue] of entries) nextMap[entryKey === oldKey ? normalizedKey : entryKey] = entryValue;
    nextMap[normalizedKey] = next;
    update(nextMap);
  };
  const removeEntry = (key: string) => {
    const nextMap: Record<string, Scalar> = {};
    for (const [entryKey, entryValue] of entries) if (entryKey !== key) nextMap[entryKey] = entryValue;
    update(nextMap);
  };
  const addEntry = () => {
    if (readOnly) return;
    const nextMap: Record<string, Scalar> = {};
    for (const [entryKey, entryValue] of entries) nextMap[entryKey] = entryValue;
    let key = 'parameter';
    let index = 1;
    while (key in nextMap) key = `parameter-${index++}`;
    nextMap[key] = '';
    update(nextMap);
  };
  return (
    <Stack gap="xs">
      <Group justify="space-between">
        <Text size="sm" fw={500}>{label}</Text>
        {!readOnly && <Button size="compact-xs" variant="light" leftSection={<Plus size={13} />} onClick={addEntry}>添加参数</Button>}
      </Group>
      {!entries.length && <Text size="xs" c="dimmed">暂无参数。点击“添加参数”创建第一项。</Text>}
      {entries.map(([entryKey, entryValue]) => {
        const type = typeof entryValue === 'boolean' ? 'boolean' : typeof entryValue === 'number' ? 'number' : 'text';
        return (
          <Group key={entryKey} align="end" wrap="nowrap">
            <TextInput label="参数名" value={entryKey} error={keyErrors[entryKey]} disabled={readOnly} onChange={(event) => changeEntry(entryKey, event.currentTarget.value, entryValue)} />
            <Select label="参数类型" data={[{ value: 'text', label: '文本参数' }, { value: 'number', label: '数字参数' }, { value: 'boolean', label: '布尔参数' }]} value={mode === 'contract' ? String(entryValue) : type} disabled={readOnly} onChange={(next) => {
              if (mode === 'contract') changeEntry(entryKey, entryKey, next ?? 'text');
              else if (next === 'number') changeEntry(entryKey, entryKey, typeof entryValue === 'number' ? entryValue : Number(entryValue) || 0);
              else if (next === 'boolean') changeEntry(entryKey, entryKey, Boolean(entryValue));
              else changeEntry(entryKey, entryKey, String(entryValue));
            }} />
            {mode === 'contract' ? <TextInput label="来源" value={readOnly ? '由发送参数自动推导' : '接收端声明'} disabled /> : type === 'number' ? <NumberInput label="值" value={entryValue as number} disabled={readOnly} onChange={(next) => typeof next === 'number' && Number.isFinite(next) && changeEntry(entryKey, entryKey, next)} /> : type === 'boolean' ? <Select label="值" data={[{ value: 'true', label: '是' }, { value: 'false', label: '否' }]} value={String(entryValue)} disabled={readOnly} onChange={(next) => changeEntry(entryKey, entryKey, next === 'true')} /> : <TextInput label="值" value={String(entryValue)} disabled={readOnly} onChange={(event) => changeEntry(entryKey, entryKey, event.currentTarget.value)} />}
            {!readOnly && <ActionIcon aria-label="删除参数" color="red" variant="subtle" onClick={() => removeEntry(entryKey)}><Trash2 size={15} /></ActionIcon>}
          </Group>
        );
      })}
    </Stack>
  );
}
