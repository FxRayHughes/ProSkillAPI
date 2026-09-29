import { Stack, Text, Title } from '@mantine/core';
import { AutoSaveSettings } from '../../../shared/components/AutoSaveSettings';
import { ServerVersionSelect } from '../../../shared/components/ServerVersionSelect';
import type { AutoSavePreferences } from '../../../shared/lib/preferences';

/** Centralizes editor preferences so operational settings are not mixed into file workflows. */
export function SettingsPage({
  serverVersion,
  onVersionChange,
  autoSave,
  onAutoSaveChange,
}: {
  serverVersion: string;
  onVersionChange: (version: string) => void;
  autoSave: AutoSavePreferences;
  onAutoSaveChange: (value: AutoSavePreferences) => void;
}) {
  return (
    <Stack p={{ base: 'md', sm: 'xl' }} maw={900} mx="auto" gap="lg">
      <div>
        <Title order={2}>编辑器设置</Title>
        <Text c="dimmed" size="sm">
          统一管理预览版本和编辑文件的自动保存行为。
        </Text>
      </div>
      <AutoSaveSettings value={autoSave} onChange={onAutoSaveChange} />
      <div>
        <Text fw={600}>预览服务端版本</Text>
        <Text size="sm" c="dimmed" mb="sm">
          仅用于编辑器粒子列表与兼容性提示，导出的技能不会绑定这个版本。
        </Text>
        <ServerVersionSelect value={serverVersion} onChange={onVersionChange} />
      </div>
    </Stack>
  );
}
