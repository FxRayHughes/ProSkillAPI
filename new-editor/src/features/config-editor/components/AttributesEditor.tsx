import {
  ActionIcon,
  Autocomplete,
  Button,
  Card,
  Group,
  NavLink,
  Stack,
  Text,
  Textarea,
  TextInput,
} from '@mantine/core';
import { Plus, Trash2 } from 'lucide-react';
import { useState } from 'react';
import { FORMULA_PRESETS, STAT_KEYS } from '../model/attributes';
import type { AttributeEntry } from '../model/attributes';

const STAT_OPTIONS = STAT_KEYS.map((entry) => ({
  value: entry.value,
  label: `${entry.value} — ${entry.label}`,
}));

/** Attribute names are referenced by classes and MythicMobs configs, so renaming is explicit. */
export function AttributesEditor({
  entries,
  selected,
  onSelect,
  onChange,
  onRename,
  onAdd,
  onRemove,
}: {
  entries: AttributeEntry[];
  selected?: string;
  onSelect: (key: string) => void;
  onChange: (entry: AttributeEntry) => void;
  onRename: (from: string, to: string) => void;
  onAdd: (key: string) => void;
  onRemove: (key: string) => void;
}) {
  const [newName, setNewName] = useState('');
  const [rename, setRename] = useState('');
  const active = entries.find((entry) => entry.key === selected);
  return (
    <Group align="flex-start" wrap="wrap" gap="lg">
      <Stack gap={2} w={{ base: '100%', md: 220 }}>
        <Text fw={600} size="sm" mb="xs">
          属性（{entries.length}）
        </Text>
        {entries.map((entry) => (
          <NavLink
            key={entry.key}
            label={entry.key}
            description={entry.stats.map((stat) => stat.stat).join(', ') || '未绑定 stat-key'}
            active={entry.key === selected}
            onClick={() => {
              onSelect(entry.key);
              setRename(entry.key);
            }}
          />
        ))}
        <Group align="flex-end" wrap="nowrap" gap="xs" mt="xs">
          <TextInput
            placeholder="新属性名"
            value={newName}
            onChange={(event) => setNewName(event.currentTarget.value)}
            style={{ flex: 1 }}
          />
          <ActionIcon
            variant="light"
            aria-label="新增属性"
            disabled={!newName.trim() || entries.some((entry) => entry.key === newName.trim())}
            onClick={() => {
              onAdd(newName.trim());
              setNewName('');
            }}
          >
            <Plus size={16} />
          </ActionIcon>
        </Group>
      </Stack>

      {active && (
        <Card withBorder style={{ flex: 1, minWidth: 320 }} maw={620}>
          <Stack gap="sm">
            <Group justify="space-between">
              <Text fw={600}>{active.key}</Text>
              <Button
                size="compact-xs"
                color="red"
                variant="subtle"
                leftSection={<Trash2 size={14} />}
                onClick={() => onRemove(active.key)}
              >
                删除属性
              </Button>
            </Group>
            <Group align="flex-end" wrap="nowrap" gap="xs">
              <TextInput
                label="属性名（配置键）"
                description="职业配置与 MythicMobs 怪物字段都用这个名字引用"
                value={rename}
                onChange={(event) => setRename(event.currentTarget.value)}
                style={{ flex: 1 }}
              />
              <Button
                variant="default"
                disabled={!rename.trim() || rename === active.key}
                onClick={() => onRename(active.key, rename.trim())}
              >
                重命名
              </Button>
            </Group>
            <TextInput
              label="显示名"
              value={active.display}
              onChange={(event) => onChange({ ...active, display: event.currentTarget.value })}
            />
            <Group grow>
              <TextInput
                label="最大可投点数"
                value={active.max}
                onChange={(event) => onChange({ ...active, max: event.currentTarget.value })}
              />
              <TextInput
                label="图标材质"
                value={active.icon}
                onChange={(event) => onChange({ ...active, icon: event.currentTarget.value })}
              />
              <TextInput
                label="图标数据值"
                value={active.iconData}
                onChange={(event) => onChange({ ...active, iconData: event.currentTarget.value })}
              />
            </Group>
            <Textarea
              label="图标 Lore"
              description="每行一条，支持 &amp; 颜色码与 {amount} 占位符"
              autosize
              minRows={1}
              value={active.iconLore.join('\n')}
              onChange={(event) =>
                onChange({
                  ...active,
                  iconLore:
                    event.currentTarget.value === '' ? [] : event.currentTarget.value.split('\n'),
                })
              }
            />

            <Text fw={600} size="sm" mt="xs">
              绑定效果
            </Text>
            <Text size="xs" c="dimmed">
              公式中 a = 当前基础值，v = 玩家投入的点数。
            </Text>
            {active.stats.map((stat, index) => (
              <Group key={index} align="flex-end" wrap="nowrap" gap="xs">
                <Autocomplete
                  label="stat-key"
                  data={STAT_OPTIONS.map((option) => option.value)}
                  value={stat.stat}
                  onChange={(value) =>
                    onChange({
                      ...active,
                      stats: active.stats.map((entry, i) =>
                        i === index ? { ...entry, stat: value } : entry,
                      ),
                    })
                  }
                  style={{ flex: 1 }}
                />
                <Autocomplete
                  label="公式"
                  data={FORMULA_PRESETS.map((preset) => preset.value)}
                  value={stat.formula}
                  onChange={(value) =>
                    onChange({
                      ...active,
                      stats: active.stats.map((entry, i) =>
                        i === index ? { ...entry, formula: value } : entry,
                      ),
                    })
                  }
                  style={{ flex: 1 }}
                />
                <ActionIcon
                  variant="subtle"
                  color="red"
                  aria-label={`删除绑定 ${stat.stat}`}
                  onClick={() =>
                    onChange({ ...active, stats: active.stats.filter((_, i) => i !== index) })
                  }
                >
                  <Trash2 size={16} />
                </ActionIcon>
              </Group>
            ))}
            <Button
              variant="light"
              size="xs"
              leftSection={<Plus size={14} />}
              onClick={() =>
                onChange({ ...active, stats: [...active.stats, { stat: '', formula: 'a+v' }] })
              }
            >
              新增绑定
            </Button>
          </Stack>
        </Card>
      )}
    </Group>
  );
}
