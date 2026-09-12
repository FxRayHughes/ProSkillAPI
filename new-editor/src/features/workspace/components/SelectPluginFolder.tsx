import { Alert, Button, Card, Loader, Stack, Text, Title } from '@mantine/core';
import { FolderOpen } from 'lucide-react';
import { useWorkspace } from '../model/workspaceContext';

/** Shown by any page reached before a plugin folder has been chosen. */
export function SelectPluginFolder({
  restored,
  title,
  hint,
}: {
  restored: boolean;
  title: string;
  hint: string;
}) {
  const { openFolder, error, clearError } = useWorkspace();
  if (!restored)
    return (
      <Stack p="xl" align="center">
        <Loader size="sm" />
      </Stack>
    );
  return (
    <Stack p={{ base: 'md', sm: 'xl' }} maw={620}>
      <Title order={2}>{title}</Title>
      <Text c="dimmed" size="sm">
        {hint}
      </Text>
      {error && (
        <Alert color="red" title="无法打开插件目录" withCloseButton onClose={clearError}>
          {error}
        </Alert>
      )}
      <Card withBorder padding="lg">
        <Stack align="flex-start">
          <Text size="sm">
            选择服务器上的 SkillAPI 插件目录，也就是包含 <code>dynamic</code> 文件夹和{' '}
            <code>config.yml</code> 的那一层。选择一次，技能、职业和配置三个页面都会使用它。
          </Text>
          <Button leftSection={<FolderOpen size={16} />} onClick={openFolder}>
            选择插件目录
          </Button>
        </Stack>
      </Card>
    </Stack>
  );
}
