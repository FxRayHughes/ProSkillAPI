import { Card, Group, Select, Stack, Switch, Text } from '@mantine/core';
import { AUTO_SAVE_INTERVALS, type AutoSavePreferences } from '../lib/preferences';

/**
 * Shared visual settings keep the homepage and the dedicated settings route consistent.
 * Values are structured controls so users never need to edit the persisted preference JSON.
 */
export function AutoSaveSettings({
  value,
  onChange,
}: {
  value: AutoSavePreferences;
  onChange: (next: AutoSavePreferences) => void;
}) {
  return (
    <Card withBorder padding="lg">
      <Stack gap="md">
        <div>
          <Text fw={600}>编辑器自动保存</Text>
          <Text size="sm" c="dimmed">
            开启后，技能编辑器会按周期保存，也可以在停止编辑后自动保存。保存只写回已关联的技能文件。
          </Text>
        </div>
        <Group justify="space-between" align="flex-start" wrap="wrap" gap="lg">
          <Switch
            label="启用自动保存"
            description="编辑器顶部的自动保存按钮也可以快速切换此设置"
            checked={value.enabled}
            onChange={(event) => onChange({ ...value, enabled: event.currentTarget.checked })}
          />
          <Select
            label="周期保存间隔"
            description="关闭周期后仍可使用操作后保存"
            data={AUTO_SAVE_INTERVALS.map((entry) => ({
              value: String(entry.value),
              label: entry.label,
            }))}
            value={String(value.intervalMs)}
            onChange={(next) => {
              const intervalMs = Number(next);
              if (Number.isFinite(intervalMs)) onChange({ ...value, intervalMs });
            }}
            w={220}
          />
          <Switch
            label="编辑操作后保存"
            description="停止连续操作约 800 毫秒后写入"
            checked={value.saveAfterEdit}
            onChange={(event) => onChange({ ...value, saveAfterEdit: event.currentTarget.checked })}
            disabled={!value.enabled}
          />
        </Group>
      </Stack>
    </Card>
  );
}
