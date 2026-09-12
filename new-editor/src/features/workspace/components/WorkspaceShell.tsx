import {
  ActionIcon,
  AppShell,
  Group,
  NavLink,
  ScrollArea,
  Text,
  Title,
  Tooltip,
} from '@mantine/core';
import { BriefcaseBusiness, FolderOpen, Home, Menu, PlugZap, Sliders, Swords } from 'lucide-react';
import { useState } from 'react';
import { useWorkspace } from '../model/workspaceContext';

export type WorkspacePage = 'home' | 'skills' | 'classes' | 'config' | 'plugins';

const PAGES: { id: WorkspacePage; label: string; description: string; icon: React.ReactNode }[] = [
  { id: 'home', label: '主页', description: '工程总览', icon: <Home size={17} /> },
  { id: 'skills', label: '技能管理', description: '蓝图与文件', icon: <Swords size={17} /> },
  {
    id: 'classes',
    label: '职业管理',
    description: '职业属性',
    icon: <BriefcaseBusiness size={17} />,
  },
  { id: 'config', label: '配置管理', description: '插件配置项', icon: <Sliders size={17} /> },
  { id: 'plugins', label: '插件管理', description: '扩展节点', icon: <PlugZap size={17} /> },
];

export function WorkspaceShell({
  page,
  onNavigate,
  children,
  headerActions,
}: {
  page: WorkspacePage;
  onNavigate: (page: WorkspacePage) => void;
  children: React.ReactNode;
  /** Header actions participate in layout to avoid covering page controls. */
  headerActions?: React.ReactNode;
}) {
  const [collapsed, setCollapsed] = useState(false);
  const { root, openFolder } = useWorkspace();
  return (
    <AppShell
      header={{ height: 64 }}
      navbar={{ width: 230, breakpoint: 'sm', collapsed: { desktop: collapsed, mobile: true } }}
    >
      <AppShell.Header>
        <Group h="100%" px="md" wrap="nowrap">
          <Tooltip label={collapsed ? '展开导航' : '收起导航'}>
            <ActionIcon
              variant="subtle"
              onClick={() => setCollapsed((value) => !value)}
              aria-label="切换导航栏"
            >
              <Menu size={18} />
            </ActionIcon>
          </Tooltip>
          {!collapsed && (
            <>
              <Title order={1} size="h4">
                ProSkillAPI
              </Title>
              <Text size="sm" c="dimmed">
                技能工坊
              </Text>
            </>
          )}
          <Group ml="auto" wrap="nowrap" gap="xs">
            {/* One directory for the whole app; switching it here reloads every page. */}
            <Tooltip label={root ? `当前插件目录：${root.handle.name}，点击切换` : '选择插件目录'}>
              <ActionIcon
                variant={root ? 'subtle' : 'filled'}
                onClick={openFolder}
                aria-label={root ? `切换插件目录，当前为 ${root.handle.name}` : '选择插件目录'}
              >
                <FolderOpen size={17} />
              </ActionIcon>
            </Tooltip>
            {root && (
              <Text size="xs" c="dimmed" visibleFrom="sm" maw={160} truncate>
                {root.handle.name}
              </Text>
            )}
            {headerActions}
          </Group>
        </Group>
      </AppShell.Header>
      <AppShell.Navbar p={collapsed ? 4 : 'sm'}>
        <ScrollArea>
          {PAGES.map((entry) => (
            <NavLink
              key={entry.id}
              label={entry.label}
              description={collapsed ? undefined : entry.description}
              leftSection={entry.icon}
              active={page === entry.id}
              onClick={() => onNavigate(entry.id)}
            />
          ))}
        </ScrollArea>
      </AppShell.Navbar>
      <AppShell.Main>{children}</AppShell.Main>
    </AppShell>
  );
}
