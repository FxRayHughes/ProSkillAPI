import { useEffect, useRef, useState } from 'react';
import { AppShell, Drawer, Group, ScrollArea, Stack, Tabs, Text, Title } from '@mantine/core';
import { ArrowLeft, Download, PanelLeft, PanelRight, Save } from 'lucide-react';
import { notifications } from '@mantine/notifications';
import { useReactFlow } from '@xyflow/react';
import { ToolButton } from '../../../shared/ui/ToolButton';
import { downloadText } from '../../../shared/lib/download';
import { parseLegacySkill, serializeLegacySkill } from '../io/legacySkill';
import { useSkillEditor } from '../model/useSkillEditor';
import { BlueprintCanvas } from './BlueprintCanvas';
import { NodeInspector } from './NodeInspector';
import { NodeLibrary } from './NodeLibrary';
import { SkillMetadata } from './SkillMetadata';
import classes from './Editor.module.css';

function describe(error: unknown, fallback: string): string {
  return error instanceof Error ? error.message : fallback;
}

/** Page composes feature panels; narrow screens reuse them in independent drawers. */
export function EditorPage({
  initialFile,
  themeControl,
  versionControl,
  serverVersion,
  onBack,
}: {
  initialFile?: import('../../workspace/model/types').WorkspaceFile;
  /** Supplied by the app so theme state remains shared across pages. */
  themeControl?: React.ReactNode;
  /** 目标服务端版本选择器，由 app 持有以便跨页面共享。 */
  versionControl?: React.ReactNode;
  /** 全局目标服务端版本，用于过滤枚举选项；不写入技能文件。 */
  serverVersion?: string;
  onBack?: () => void;
}) {
  const editor = useSkillEditor();
  const { setProject } = editor;
  useEffect(() => {
    if (initialFile?.project) setProject(initialFile.project);
  }, [initialFile, setProject]);
  const saveToFolder = async () => {
    if (!initialFile?.handle) return false;
    // Serialization validates the graph, so a rejected structure never truncates the file.
    const content = serializeLegacySkill(editor.project);
    const writable = await initialFile.handle.createWritable();
    await writable.write(content);
    await writable.close();
    // Only report success after close commits the write to the underlying file.
    notifications.show({
      color: 'teal',
      title: '保存成功',
      message: `已按 SkillAPI 原生格式写回 ${initialFile.fileName}`,
      closeButtonProps: { 'aria-label': '关闭提示' },
    });
    return true;
  };
  const { screenToFlowPosition } = useReactFlow();
  const [drawer, setDrawer] = useState<'library' | 'inspector' | null>(null);
  // Share tab selection between the desktop sidebar and mobile drawer.
  const [libraryTab, setLibraryTab] = useState<string | null>('nodes');
  const [inspectorWidth, setInspectorWidth] = useState(360);
  const resizing = useRef(false);
  const startResize = (event: React.PointerEvent<HTMLDivElement>) => {
    event.currentTarget.setPointerCapture(event.pointerId);
    resizing.current = true;
    const startX = event.clientX;
    const startWidth = inspectorWidth;
    const move = (moveEvent: PointerEvent) =>
      setInspectorWidth(Math.max(300, Math.min(560, startWidth + startX - moveEvent.clientX)));
    const stop = () => {
      resizing.current = false;
      window.removeEventListener('pointermove', move);
      window.removeEventListener('pointerup', stop);
    };
    window.addEventListener('pointermove', move);
    window.addEventListener('pointerup', stop, { once: true });
  };
  const importProject = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;
    try {
      editor.setProject(parseLegacySkill(await file.text(), serverVersion));
    } catch (error) {
      notifications.show({
        color: 'red',
        closeButtonProps: { 'aria-label': '关闭提示' },
        title: '导入失败',
        message: describe(error, '无法读取技能文件'),
        autoClose: false,
      });
    }
    event.target.value = '';
  };
  const library = (
    <Tabs value={libraryTab} onChange={setLibraryTab}>
      <Tabs.List grow>
        <Tabs.Tab value="info">基础信息</Tabs.Tab>
        <Tabs.Tab value="nodes">技能节点</Tabs.Tab>
      </Tabs.List>
      <Tabs.Panel value="nodes" p="sm">
        <NodeLibrary
          onAdd={(definition) => {
            editor.addNode(
              definition,
              screenToFlowPosition({ x: window.innerWidth / 2, y: window.innerHeight / 2 }),
            );
            setDrawer(null);
          }}
        />
      </Tabs.Panel>
      <Tabs.Panel value="info" p="sm">
        <SkillMetadata meta={editor.project.meta} onChange={editor.updateMeta} />
      </Tabs.Panel>
    </Tabs>
  );
  const inspector = (
    <Stack p="md">
      <NodeInspector
        node={editor.selected}
        onChange={editor.updateNode}
        serverVersion={serverVersion}
      />
    </Stack>
  );
  return (
    <AppShell
      header={{ height: 64 }}
      navbar={{ width: 250, breakpoint: 'md', collapsed: { mobile: true } }}
      aside={{ width: inspectorWidth, breakpoint: 'md', collapsed: { mobile: true } }}
      padding={0}
      className={classes.editorShell}
    >
      <AppShell.Header>
        <Group h="100%" px="md" justify="space-between" wrap="nowrap">
          <Group gap="xs" wrap="nowrap">
            <ToolButton label="返回技能管理" onClick={() => onBack?.()}>
              <ArrowLeft size={18} />
            </ToolButton>
            {/* 版本是全局设置，放在左上角而不是逐技能的属性面板里 */}
            {versionControl}
          </Group>
          <div className={classes.grow}>
            <Title order={1} size="h4">
              ProSkillAPI
            </Title>
            <Text size="xs" c="dimmed">
              技能蓝图编辑器
            </Text>
          </div>
          <Group gap="xs" wrap="nowrap">
            <Group hiddenFrom="md" gap="xs" wrap="nowrap">
              <ToolButton label="基础信息与技能节点" onClick={() => setDrawer('library')}>
                <PanelLeft size={18} />
              </ToolButton>
              <ToolButton label="节点属性" onClick={() => setDrawer('inspector')}>
                <PanelRight size={18} />
              </ToolButton>
            </Group>
            <ToolButton
              label="保存并写回技能文件"
              onClick={async () => {
                try {
                  if (!(await saveToFolder())) {
                    notifications.show({
                      color: 'yellow',
                      closeButtonProps: { 'aria-label': '关闭提示' },
                      title: '未关联文件',
                      message: '当前技能没有关联文件，请先从技能管理中打开插件目录里的技能。',
                    });
                  }
                } catch (error) {
                  notifications.show({
                    color: 'red',
                    closeButtonProps: { 'aria-label': '关闭提示' },
                    title: '保存失败',
                    message: describe(error, '无法写入技能文件'),
                    autoClose: false,
                  });
                }
              }}
            >
              <Save size={18} />
            </ToolButton>
            <ToolButton
              label="导入技能 YAML"
              onClick={() => document.getElementById('skill-project-import')?.click()}
            >
              <Download size={18} style={{ transform: 'rotate(180deg)' }} />
            </ToolButton>
            <input
              id="skill-project-import"
              hidden
              type="file"
              accept=".yml,.yaml"
              onChange={importProject}
            />
            <ToolButton
              label="导出技能 YAML"
              onClick={() => {
                try {
                  downloadText(
                    `${editor.project.meta.name || 'skill'}.yml`,
                    serializeLegacySkill(editor.project),
                  );
                } catch (error) {
                  notifications.show({
                    color: 'red',
                    closeButtonProps: { 'aria-label': '关闭提示' },
                    title: '导出失败',
                    message: describe(error, '无法导出技能文件'),
                    autoClose: false,
                  });
                }
              }}
            >
              <Download size={18} />
            </ToolButton>
            {themeControl}
          </Group>
        </Group>
      </AppShell.Header>
      <AppShell.Navbar>
        <ScrollArea h="100%">{library}</ScrollArea>
      </AppShell.Navbar>
      <AppShell.Aside className={classes.inspectorAside}>
        <div
          className={classes.resizeHandle}
          role="separator"
          aria-label="调整节点属性栏宽度"
          aria-orientation="vertical"
          tabIndex={0}
          onPointerDown={startResize}
        />
        <ScrollArea h="100%">{inspector}</ScrollArea>
      </AppShell.Aside>
      <AppShell.Main className={classes.editorMain}>
        <BlueprintCanvas editor={editor} />
      </AppShell.Main>
      <Drawer
        opened={drawer !== null}
        onClose={() => setDrawer(null)}
        title={drawer === 'library' ? '技能配置' : '节点属性'}
        position={drawer === 'inspector' ? 'right' : 'left'}
      >
        {drawer === 'library' ? library : inspector}
      </Drawer>
    </AppShell>
  );
}
