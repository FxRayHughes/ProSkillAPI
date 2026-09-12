import { Alert, Button, Card, Code, Group, SimpleGrid, Stack, Text, Title } from '@mantine/core';
import { BriefcaseBusiness, FolderOpen, Sliders, Swords } from 'lucide-react';
import { useWorkspace } from '../model/workspaceContext';
import type { WorkspacePage } from './WorkspaceShell';

const CARDS: { id: WorkspacePage; label: string; hint: string; icon: React.ReactNode }[] = [
  {
    id: 'skills',
    label: '技能管理',
    hint: '浏览 dynamic/skill，用蓝图编辑技能。',
    icon: <Swords size={28} />,
  },
  {
    id: 'classes',
    label: '职业管理',
    hint: '编辑 dynamic/class 里的职业属性与技能列表。',
    icon: <BriefcaseBusiness size={28} />,
  },
  {
    id: 'config',
    label: '配置管理',
    hint: 'config.yml、attributes.yml 等配置的可视化编辑。',
    icon: <Sliders size={28} />,
  },
];

/** The single place a plugin folder is chosen; every page then reads the same directory. */
export function HomePage({ onOpen }: { onOpen: (page: WorkspacePage) => void }) {
  const { root, error, openFolder, clearError } = useWorkspace();
  return (
    <Stack p="xl" maw={900} mx="auto">
      <Text size="xs" c="dimmed" tt="uppercase">
        ProSkillAPI / 技能工坊
      </Text>
      <Title order={1}>欢迎回来</Title>
      <Text c="dimmed">技能、职业和插件配置都在同一个插件目录里编辑，保存后服务器直接可用。</Text>

      {error && (
        <Alert color="red" title="无法打开插件目录" withCloseButton onClose={clearError}>
          {error}
        </Alert>
      )}

      <Card withBorder padding="lg" mt="md">
        <Group justify="space-between" wrap="wrap" gap="md">
          <div>
            <Text fw={600}>{root ? '当前插件目录' : '尚未选择插件目录'}</Text>
            <Text size="sm" c="dimmed">
              {root ? (
                root.handle.name
              ) : (
                <>
                  选择包含 <Code>dynamic</Code> 文件夹和 <Code>config.yml</Code> 的那一层目录。
                </>
              )}
            </Text>
          </div>
          <Button
            variant={root ? 'default' : 'filled'}
            leftSection={<FolderOpen size={16} />}
            onClick={openFolder}
          >
            {root ? '切换目录' : '选择插件目录'}
          </Button>
        </Group>
      </Card>

      <SimpleGrid cols={{ base: 1, sm: 3 }} mt="md">
        {CARDS.map((card) => (
          <Card
            key={card.id}
            withBorder
            padding="lg"
            onClick={() => onOpen(card.id)}
            style={{ cursor: 'pointer' }}
          >
            {card.icon}
            <Title order={3} size="h4" mt="md">
              {card.label}
            </Title>
            <Text c="dimmed" size="sm" mt="xs">
              {card.hint}
            </Text>
          </Card>
        ))}
      </SimpleGrid>
    </Stack>
  );
}
