import { useCallback, useEffect, useRef, useState } from 'react';
import {
  Alert,
  AppShell,
  Badge,
  Drawer,
  Group,
  ScrollArea,
  Stack,
  Tabs,
  Text,
  Title,
} from '@mantine/core';
import { ArrowLeft, Download, PanelLeft, PanelRight, Save, Timer } from 'lucide-react';
import { notifications } from '@mantine/notifications';
import { useReactFlow } from '@xyflow/react';
import { ToolButton } from '../../../shared/ui/ToolButton';
import { downloadText } from '../../../shared/lib/download';
import { enqueueSerial } from '../../../shared/lib/serialQueue';
import { parseLegacySkill, serializeLegacySkill } from '../io/legacySkill';
import { particleFallbackIssues } from '../io/particleDiagnostics';
import { useSkillEditor } from '../model/useSkillEditor';
import { BlueprintCanvas } from './BlueprintCanvas';
import { NodeInspector } from './NodeInspector';
import { NodeLibrary } from './NodeLibrary';
import { SkillMetadata } from './SkillMetadata';
import classes from './Editor.module.css';
import type { AutoSavePreferences } from '../../../shared/lib/preferences';

function describe(error: unknown, fallback: string): string {
  return error instanceof Error ? error.message : fallback;
}

interface FileSignature {
  lastModified: number;
  size: number;
}

function signatureOf(file: File): FileSignature {
  return { lastModified: file.lastModified, size: file.size };
}

function changedSince(before: FileSignature, after: FileSignature): boolean {
  return before.lastModified !== after.lastModified || before.size !== after.size;
}

/** Page composes feature panels; narrow screens reuse them in independent drawers. */
export function EditorPage({
  initialFile,
  themeControl,
  serverVersion,
  autoSave,
  onAutoSaveChange,
  onBack,
}: {
  initialFile?: import('../../workspace/model/types').WorkspaceFile;
  /** Supplied by the app so theme state remains shared across pages. */
  themeControl?: React.ReactNode;
  /** 主页设置的预览版本；服务端运行时独立选择 AST 映射。 */
  serverVersion: string;
  /** Auto-save is app-wide so the settings page and the editor button stay synchronized. */
  autoSave: AutoSavePreferences;
  onAutoSaveChange: (value: AutoSavePreferences) => void;
  onBack?: () => void;
}) {
  const editor = useSkillEditor();
  const { loadProject, markSaved } = editor;
  const standaloneImport = useRef(false);
  useEffect(() => {
    if (initialFile?.project) {
      loadProject(initialFile.project);
      standaloneImport.current = false;
    }
  }, [initialFile, loadProject]);
  const projectRef = useRef(editor.project);
  const dirtyRef = useRef(editor.dirty);
  useEffect(() => {
    projectRef.current = editor.project;
    dirtyRef.current = editor.dirty;
  }, [editor.project, editor.dirty]);
  const [saveStatus, setSaveStatus] = useState<'idle' | 'saving' | 'saved' | 'error'>('idle');
  const [lastSavedAt, setLastSavedAt] = useState<number>();
  const [saveError, setSaveError] = useState('');
  const saveQueue = useRef<{ current: Promise<unknown> }>({ current: Promise.resolve() });
  const fileHandleRef = useRef<FileSystemFileHandle | undefined>(undefined);
  const fileSignatureRef = useRef<FileSignature | undefined>(undefined);
  const previewIssues = particleFallbackIssues(editor.project, serverVersion);
  useEffect(() => {
    let active = true;
    fileHandleRef.current = initialFile?.handle;
    fileSignatureRef.current = undefined;
    if (!initialFile?.handle) return () => undefined;
    // Capture a baseline so automatic saves can refuse to overwrite changes made
    // by another editor or process after this skill was opened.
    void initialFile.handle
      .getFile()
      .then((file) => {
        if (active) fileSignatureRef.current = signatureOf(file);
      })
      .catch(() => {
        // A later save will retry the metadata read; editing must remain usable
        // when a browser denies file metadata access temporarily.
      });
    return () => {
      active = false;
    };
  }, [initialFile]);
  const saveToFolder = useCallback(
    async ({
      silent = false,
      onlyIfDirty = false,
    }: { silent?: boolean; onlyIfDirty?: boolean } = {}) =>
      enqueueSerial(saveQueue.current, async () => {
        // A manually imported file must be exported, never written over the previously opened file.
        if (!initialFile?.handle || standaloneImport.current) return false;
        if (onlyIfDirty && !dirtyRef.current) return false;
        const projectToSave = projectRef.current;
        setSaveStatus('saving');
        setSaveError('');
        try {
          const currentFile = await initialFile.handle.getFile();
          const currentSignature = signatureOf(currentFile);
          if (
            fileHandleRef.current === initialFile.handle &&
            fileSignatureRef.current &&
            changedSince(fileSignatureRef.current, currentSignature)
          ) {
            throw new Error('技能文件已被其他窗口或程序修改，请重新加载后再保存。');
          }
          fileHandleRef.current = initialFile.handle;
          fileSignatureRef.current = currentSignature;
          // Serialization validates the graph, so a rejected structure never truncates the file.
          const content = serializeLegacySkill(projectToSave);
          const writable = await initialFile.handle.createWritable();
          await writable.write(content);
          await writable.close();
          // Refresh the baseline after our own atomic close so the next save does
          // not mistake this write for an external modification.
          try {
            fileSignatureRef.current = signatureOf(await initialFile.handle.getFile());
          } catch {
            fileSignatureRef.current = undefined;
          }
          const stillCurrent = projectRef.current === projectToSave;
          if (stillCurrent) {
            markSaved();
            setLastSavedAt(Date.now());
          }
          setSaveStatus(stillCurrent ? 'saved' : 'idle');
          // Automatic saves avoid a notification for every timer tick; the header still shows time.
          if (!silent) {
            notifications.show({
              color: 'teal',
              title: '保存成功',
              message: `已按 SkillAPI 原生格式写回 ${initialFile.fileName}`,
              closeButtonProps: { 'aria-label': '关闭提示' },
            });
          }
          return true;
        } catch (error) {
          const message = describe(error, '无法写入技能文件');
          setSaveStatus('error');
          setSaveError(message);
          if (silent) {
            notifications.show({
              color: 'red',
              title: '自动保存失败',
              message,
              autoClose: false,
              closeButtonProps: { 'aria-label': '关闭提示' },
            });
          }
          throw error;
        }
      }),
    [markSaved, initialFile],
  );

  // The interval is a safety net for long editing sessions; it never writes a clean project.
  useEffect(() => {
    if (!autoSave.enabled || autoSave.intervalMs <= 0) return undefined;
    const timer = window.setInterval(() => {
      void saveToFolder({ silent: true, onlyIfDirty: true }).catch(() => undefined);
    }, autoSave.intervalMs);
    return () => window.clearInterval(timer);
  }, [autoSave.enabled, autoSave.intervalMs, saveToFolder]);

  // Debounce graph edits so dragging or typing produces one write after the user pauses.
  useEffect(() => {
    if (!autoSave.enabled || !autoSave.saveAfterEdit || !editor.dirty) return undefined;
    const timer = window.setTimeout(() => {
      void saveToFolder({ silent: true, onlyIfDirty: true }).catch(() => undefined);
    }, 800);
    return () => window.clearTimeout(timer);
  }, [autoSave.enabled, autoSave.saveAfterEdit, editor.dirty, editor.project, saveToFolder]);
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
      const project = parseLegacySkill(await file.text());
      editor.loadProject(project);
      standaloneImport.current = true;
      const issues = particleFallbackIssues(project, serverVersion);
      notifications.show({
        color: issues.length ? 'yellow' : 'teal',
        closeButtonProps: { 'aria-label': '关闭提示' },
        title: issues.length ? '导入成功，部分粒子将回退' : '导入成功',
        message: issues.length
          ? `已转换为粒子 AST；${issues.length} 个粒子在 ${serverVersion} 会使用白色 CLOUD，可在节点属性中调整。`
          : '已转换为粒子 AST；导出的文件由服务端按实际版本解析，不会覆盖原文件。',
        autoClose: issues.length ? false : undefined,
      });
    } catch (error) {
      notifications.show({
        color: 'red',
        closeButtonProps: { 'aria-label': '关闭提示' },
        title: '导入失败',
        message: `${describe(error, '无法读取技能文件')}。本编辑器需要 SkillAPI 原生技能 YAML；请按报错修复或转换文件，再重新导入并导出。原文件未修改。`,
        autoClose: false,
      });
    }
    event.target.value = '';
  };
  const statusLabel =
    saveStatus === 'saving'
      ? '保存中'
      : saveStatus === 'error'
        ? `保存失败：${saveError}`
        : editor.dirty
          ? '有未保存修改'
          : lastSavedAt
            ? `已保存 ${new Date(lastSavedAt).toLocaleTimeString()}`
            : '尚未保存';
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
      {previewIssues.length > 0 && (
        <Alert color="yellow" title={`${previewIssues.length} 个粒子在 ${serverVersion} 将回退`}>
          {previewIssues.slice(0, 3).join('；')}
          {previewIssues.length > 3 ? '；请继续检查其他节点' : ''}
        </Alert>
      )}
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
              label={autoSave.enabled ? '关闭自动保存' : '开启自动保存'}
              onClick={() => onAutoSaveChange({ ...autoSave, enabled: !autoSave.enabled })}
            >
              <Timer size={18} />
            </ToolButton>
            <Badge
              color={saveStatus === 'error' ? 'red' : autoSave.enabled ? 'teal' : 'gray'}
              variant="light"
              size="sm"
              title={saveStatus === 'error' ? saveError : undefined}
            >
              {autoSave.enabled ? `自动保存 · ${statusLabel}` : statusLabel}
            </Badge>
            <ToolButton
              label="保存并写回技能文件"
              onClick={async () => {
                try {
                  if (!(await saveToFolder())) {
                    notifications.show({
                      color: 'yellow',
                      closeButtonProps: { 'aria-label': '关闭提示' },
                      title: '未关联文件',
                      message:
                        '当前内容由文件导入或未关联插件目录，请使用“导出技能 YAML”；不会覆盖原文件。',
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
