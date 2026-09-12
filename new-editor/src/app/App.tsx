import './bootstrap';
import { ActionIcon, MantineProvider, Tooltip } from '@mantine/core';
import { Notifications } from '@mantine/notifications';
import { ReactFlowProvider } from '@xyflow/react';
import { EditorPage } from '../features/skill-editor';
import { theme } from './theme';
import { useEffect, useState } from 'react';
import { HomePage } from '../features/workspace/components/HomePage';
import { WorkspaceShell } from '../features/workspace/components/WorkspaceShell';
import type { WorkspacePage } from '../features/workspace/components/WorkspaceShell';
import { SkillManagerPage } from '../features/workspace/components/SkillManagerPage';
import { ClassManagerPage } from '../features/workspace/components/ClassManagerPage';
import { ConfigManagerPage } from '../features/config-editor/components/ConfigManagerPage';
import { WorkspaceProvider } from '../features/workspace/components/WorkspaceProvider';
import type { WorkspaceFile } from '../features/workspace/model/types';
import { PluginManagerPage } from '../features/plugins/components/PluginManagerPage';
import { Moon, Sun } from 'lucide-react';
import {
  readServerVersion,
  readTheme,
  writeServerVersion,
  writeTheme,
} from '../shared/lib/preferences';
import { ServerVersionSelect } from '../shared/components/ServerVersionSelect';

/** Providers belong to the composition root so features remain independently mountable. */
export function App() {
  const [light, setLight] = useState(() => readTheme() === 'light');
  useEffect(() => {
    writeTheme(light ? 'light' : 'dark');
  }, [light]);
  // 目标服务端版本是全局设置：它只影响枚举过滤，不属于任何单个技能。
  const [serverVersion, setServerVersion] = useState(readServerVersion);
  useEffect(() => {
    writeServerVersion(serverVersion);
  }, [serverVersion]);
  const [page, setPage] = useState<WorkspacePage | 'editor'>('home');
  const [opened, setOpened] = useState<WorkspaceFile>();
  // Keep theme controls in each page's toolbar flow instead of overlaying file actions.
  const themeControl = (
    <Tooltip label={light ? '切换深色主题' : '切换浅色主题'}>
      <ActionIcon
        aria-label={light ? '切换深色主题' : '切换浅色主题'}
        onClick={() => setLight((value) => !value)}
      >
        {light ? <Moon size={17} /> : <Sun size={17} />}
      </ActionIcon>
    </Tooltip>
  );
  const versionControl = (
    <ServerVersionSelect value={serverVersion} onChange={setServerVersion} />
  );
  const content =
    page === 'home' ? (
      <HomePage onOpen={setPage} />
    ) : page === 'skills' ? (
      <SkillManagerPage
        onOpen={(file) => {
          setOpened(file);
          setPage('editor');
        }}
      />
    ) : page === 'classes' ? (
      <ClassManagerPage />
    ) : page === 'config' ? (
      <ConfigManagerPage />
    ) : page === 'plugins' ? (
      <PluginManagerPage />
    ) : (
      <EditorPage
        initialFile={opened}
        themeControl={themeControl}
        versionControl={versionControl}
        serverVersion={serverVersion}
        onBack={() => setPage('skills')}
      />
    );
  return (
    <MantineProvider theme={theme} forceColorScheme={light ? 'light' : 'dark'}>
      {/* One host keeps notifications visible across navigation and inherits the active theme. */}
      <Notifications position="bottom-right" limit={3} />
      <WorkspaceProvider>
        <ReactFlowProvider>
          {page === 'editor' ? (
            content
          ) : (
            <WorkspaceShell
              page={page}
              onNavigate={setPage}
              headerActions={
                <>
                  {versionControl}
                  {themeControl}
                </>
              }
            >
              {content}
            </WorkspaceShell>
          )}
        </ReactFlowProvider>
      </WorkspaceProvider>
    </MantineProvider>
  );
}
