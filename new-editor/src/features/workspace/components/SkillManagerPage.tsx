import {
  Alert,
  Badge,
  Button,
  Card,
  Group,
  Modal,
  ScrollArea,
  SegmentedControl,
  SimpleGrid,
  Stack,
  Text,
  TextInput,
  Title,
  Tooltip,
} from '@mantine/core';
import { AlertTriangle, FilePlus2, LayoutGrid, List, RefreshCcw } from 'lucide-react';
import { useEffect, useState } from 'react';
import { parseLegacySkill, serializeLegacySkill } from '../../skill-editor/io/legacySkill';
import { createProject } from '../../skill-editor/model/graph';
import type { WorkspaceFile } from '../model/types';
import { useWorkspace } from '../model/workspaceContext';
import { SelectPluginFolder } from './SelectPluginFolder';
import { createYamlFile, fileExists, listYamlFiles } from '../model/pluginDirectory';
import type { DirectoryEntry } from '../model/pluginDirectory';

function describe(error: unknown, fallback: string): string {
  return error instanceof Error ? error.message : fallback;
}

/** A single unparsable file is listed with its reason instead of hiding the whole folder. */
function toWorkspaceFile(entry: DirectoryEntry): WorkspaceFile {
  const base = { name: entry.name, fileName: entry.fileName, handle: entry.handle };
  try {
    return { ...base, project: parseLegacySkill(entry.text) };
  } catch (cause) {
    return { ...base, error: describe(cause, '无法解析该技能文件') };
  }
}

/** Reads dynamic/skill from the plugin folder so what the editor shows is what the server loads. */
export function SkillManagerPage({ onOpen }: { onOpen: (file: WorkspaceFile) => void }) {
  const { root, restored } = useWorkspace();
  const [files, setFiles] = useState<WorkspaceFile[]>([]);
  const [error, setError] = useState('');
  const [view, setView] = useState<'cards' | 'list'>('cards');
  const [creating, setCreating] = useState(false);
  const [newName, setNewName] = useState('');

  const [reloads, setReloads] = useState(0);

  useEffect(() => {
    if (!root) return;
    // Switching directories mid-scan must not let the previous result land.
    let cancelled = false;
    void listYamlFiles(root.skills)
      .then((entries) => {
        if (!cancelled) setFiles(entries.map(toWorkspaceFile));
      })
      .catch((cause: unknown) => {
        if (!cancelled) setError(describe(cause, '无法读取技能目录'));
      });
    return () => {
      cancelled = true;
    };
  }, [root, reloads]);

  const create = async () => {
    const name = newName.trim();
    if (!name || !root) return;
    setError('');
    try {
      if (await fileExists(root.skills, name)) throw new Error(`技能 ${name} 已存在`);
      const project = createProject(name);
      const handle = await createYamlFile(root.skills, name, serializeLegacySkill(project));
      const file: WorkspaceFile = { name, fileName: `${name}.yml`, handle, project };
      setFiles((current) => [...current, file]);
      setCreating(false);
      setNewName('');
      onOpen(file);
    } catch (cause) {
      setError(describe(cause, '无法创建技能'));
    }
  };

  const open = async (file: WorkspaceFile) => {
    if (file.error) return;
    setError('');
    try {
      // Re-read so an external edit since the last scan is not silently overwritten.
      const text = await file.handle.getFile().then((value) => value.text());
      onOpen({ ...file, project: parseLegacySkill(text) });
    } catch (cause) {
      setError(`${file.fileName}：${describe(cause, '无法读取该技能文件')}`);
    }
  };

  const broken = files.filter((file) => file.error).length;

  if (!root)
    return (
      <SelectPluginFolder
        restored={restored}
        title="技能管理"
        hint="选择插件目录后，这里会列出 dynamic/skill 下的技能。"
      />
    );

  return (
    <ScrollArea h="calc(100dvh - 64px)" offsetScrollbars>
      <Stack p={{ base: 'md', sm: 'xl' }} gap="lg">
        <Group justify="space-between" align="flex-start" wrap="wrap">
          <div>
            <Title order={2}>技能管理</Title>
            <Text c="dimmed" size="sm">
              读取自 {root.handle.name}/dynamic/skill
            </Text>
          </div>
          <Group gap="xs" wrap="wrap">
            <Button
              variant="default"
              leftSection={<RefreshCcw size={16} />}
              onClick={() => setReloads((value) => value + 1)}
            >
              刷新
            </Button>
            <Button leftSection={<FilePlus2 size={16} />} onClick={() => setCreating(true)}>
              新建技能
            </Button>
          </Group>
        </Group>

        {error && (
          <Alert color="red" title="文件操作失败" withCloseButton onClose={() => setError('')}>
            {error}
          </Alert>
        )}
        <Text size="sm" c="dimmed">
          技能直接从 dynamic/skill 读取，保存时按 SkillAPI 原生格式写回同一个文件。
        </Text>
        {broken > 0 && (
          <Alert
            color="orange"
            title={`${broken} 个文件无法打开`}
            icon={<AlertTriangle size={18} />}
          >
            这些文件不是服务器能加载的原生格式，或使用了编辑器未注册的组件。编辑器不会自动修改它们。
          </Alert>
        )}

        <Group justify="space-between">
          <Text fw={600}>技能文件（{files.length}）</Text>
          <SegmentedControl
            size="xs"
            value={view}
            onChange={(value) => setView(value as 'cards' | 'list')}
            data={[
              { value: 'cards', label: <LayoutGrid size={15} aria-label="卡片视图" /> },
              { value: 'list', label: <List size={15} aria-label="列表视图" /> },
            ]}
          />
        </Group>

        {view === 'cards' ? (
          <SimpleGrid cols={{ base: 1, sm: 2, lg: 3, xl: 4 }} spacing="sm">
            {files.map((file) => (
              <Card withBorder padding="sm" key={file.fileName}>
                <Stack gap={6}>
                  <Group justify="space-between" wrap="nowrap" gap="xs">
                    <Text fw={600} lineClamp={1} title={file.name}>
                      {file.name}
                    </Text>
                    <SkillStatus file={file} />
                  </Group>
                  <Text size="xs" c="dimmed" lineClamp={1}>
                    {file.project
                      ? `${file.project.meta.type} · ${file.project.nodes.length} 个节点`
                      : file.error}
                  </Text>
                  <Button
                    size="xs"
                    variant="light"
                    fullWidth
                    disabled={Boolean(file.error)}
                    onClick={() => open(file)}
                  >
                    打开编辑
                  </Button>
                </Stack>
              </Card>
            ))}
          </SimpleGrid>
        ) : (
          <Stack gap="xs">
            {files.map((file) => (
              <Card withBorder padding="xs" key={file.fileName}>
                <Group justify="space-between" wrap="nowrap">
                  <Group gap="xs" wrap="nowrap" style={{ minWidth: 0 }}>
                    <Text fw={600} lineClamp={1}>
                      {file.name}
                    </Text>
                    <SkillStatus file={file} />
                  </Group>
                  <Button
                    size="xs"
                    variant="light"
                    disabled={Boolean(file.error)}
                    onClick={() => open(file)}
                  >
                    打开编辑
                  </Button>
                </Group>
              </Card>
            ))}
          </Stack>
        )}
        {!files.length && (
          <Text c="dimmed">
            {root ? 'dynamic/skill 中没有技能文件。' : '请先选择 SkillAPI 插件目录。'}
          </Text>
        )}
      </Stack>

      <Modal opened={creating} onClose={() => setCreating(false)} title="新建技能">
        <Stack>
          <TextInput
            label="技能名称"
            description="同时作为文件名与其他配置引用该技能的键"
            value={newName}
            onChange={(event) => setNewName(event.currentTarget.value)}
            onKeyDown={(event) => event.key === 'Enter' && create()}
            data-autofocus
          />
          <Button onClick={create} disabled={!newName.trim()}>
            创建并打开
          </Button>
        </Stack>
      </Modal>
    </ScrollArea>
  );
}

function SkillStatus({ file }: { file: WorkspaceFile }) {
  if (!file.error) return null;
  return (
    <Tooltip label={file.error} multiline w={280} withArrow>
      <Badge color="red" variant="light" size="sm" style={{ flexShrink: 0 }}>
        格式异常
      </Badge>
    </Tooltip>
  );
}
