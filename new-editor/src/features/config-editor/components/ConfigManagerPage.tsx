import {
  Alert,
  Badge,
  Button,
  Card,
  Group,
  NavLink,
  ScrollArea,
  Stack,
  Text,
  Title,
} from '@mantine/core';
import { notifications } from '@mantine/notifications';
import { RefreshCcw, Save } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useWorkspace } from '../../workspace/model/workspaceContext';
import { listYamlFiles, writeFile } from '../../workspace/model/pluginDirectory';
import type { DirectoryEntry } from '../../workspace/model/pluginDirectory';
import { SelectPluginFolder } from '../../workspace/components/SelectPluginFolder';
import { parseConfig, readSections, serializeConfig, setValue } from '../model/configDocument';
import type { ConfigDocument, ConfigField, ConfigSection } from '../model/configDocument';
import {
  addAttribute,
  readAttributes,
  removeAttribute,
  renameAttribute,
  writeAttribute,
} from '../model/attributes';
import type { AttributeEntry } from '../model/attributes';
import { ConfigSectionForm } from './ConfigSectionForm';
import { AttributesEditor } from './AttributesEditor';

const ATTRIBUTES_FILE = 'attributes.yml';

/** Files the plugin owns but that are not user-editable settings. */
const HIDDEN = new Set(['skills.yml', 'classes.yml']);

interface ConfigFile {
  fileName: string;
  handle: FileSystemFileHandle;
  document?: ConfigDocument;
  error?: string;
}

function describe(error: unknown, fallback: string): string {
  return error instanceof Error ? error.message : fallback;
}

/** A file that fails to parse is listed with its reason rather than hiding the whole folder. */
function toConfigFile(entry: DirectoryEntry): ConfigFile {
  try {
    return { fileName: entry.fileName, handle: entry.handle, document: parseConfig(entry.text) };
  } catch (cause) {
    return {
      fileName: entry.fileName,
      handle: entry.handle,
      error: describe(cause, '无法解析该配置文件'),
    };
  }
}

/**
 * Config files are hand-maintained and heavily commented, so the form is generated from the
 * file itself and every edit is applied to the YAML AST. Comments survive a save.
 */
export function ConfigManagerPage() {
  const { root, restored } = useWorkspace();
  const [files, setFiles] = useState<ConfigFile[]>([]);
  const [active, setActive] = useState<string>();
  const [tree, setTree] = useState<ConfigSection>();
  const [attributes, setAttributes] = useState<AttributeEntry[]>();
  const [selectedAttribute, setSelectedAttribute] = useState<string>();
  const [dirty, setDirty] = useState(false);
  const [error, setError] = useState('');

  const [reloads, setReloads] = useState(0);

  useEffect(() => {
    if (!root) return;
    // Switching directories mid-scan must not let the previous result land.
    let cancelled = false;
    void listYamlFiles(root.handle)
      .then((entries) => {
        if (cancelled) return;
        setFiles(entries.filter((entry) => !HIDDEN.has(entry.fileName)).map(toConfigFile));
        setActive(undefined);
        setTree(undefined);
        setAttributes(undefined);
        setDirty(false);
      })
      .catch((cause: unknown) => {
        if (!cancelled) setError(describe(cause, '无法读取配置目录'));
      });
    return () => {
      cancelled = true;
    };
  }, [root, reloads]);

  const show = (file: ConfigFile) => {
    setActive(file.fileName);
    setDirty(false);
    setError('');
    if (!file.document) {
      setTree(undefined);
      setAttributes(undefined);
      return;
    }
    if (file.fileName === ATTRIBUTES_FILE) {
      const entries = readAttributes(file.document);
      setAttributes(entries);
      setSelectedAttribute(entries[0]?.key);
      setTree(undefined);
    } else {
      setTree(readSections(file.document));
      setAttributes(undefined);
    }
  };

  const current = files.find((file) => file.fileName === active);

  // Every mutation goes through the shared document, so a save writes all pending edits.
  const refresh = (file: ConfigFile) => {
    setDirty(true);
    if (file.fileName === ATTRIBUTES_FILE) setAttributes(readAttributes(file.document!));
    else setTree(readSections(file.document!));
  };

  const editField = (field: ConfigField, value: boolean | number | string | string[]) => {
    if (!current?.document) return;
    setValue(current.document, field, value);
    refresh(current);
  };

  const save = async () => {
    if (!current?.document) return;
    setError('');
    try {
      await writeFile(current.handle, serializeConfig(current.document));
      setDirty(false);
      notifications.show({
        color: 'teal',
        title: '保存成功',
        message: `${current.fileName} 已写回，注释与未编辑的内容保持原样`,
        closeButtonProps: { 'aria-label': '关闭提示' },
      });
    } catch (cause) {
      setError(describe(cause, '无法写入配置文件'));
    }
  };

  if (!root)
    return (
      <SelectPluginFolder
        restored={restored}
        title="配置管理"
        hint="选择插件目录后，这里会列出可视化编辑的配置文件。"
      />
    );

  return (
    <ScrollArea h="calc(100dvh - 64px)" offsetScrollbars>
      <Stack p={{ base: 'md', sm: 'xl' }} gap="lg">
        <Group justify="space-between" align="flex-start" wrap="wrap">
          <div>
            <Title order={2}>配置管理</Title>
            <Text c="dimmed" size="sm">
              直接编辑 {root.handle.name} 根目录下的配置文件，保存时保留全部注释。
            </Text>
          </div>
          <Group gap="xs">
            <Button
              variant="default"
              leftSection={<RefreshCcw size={16} />}
              onClick={() => setReloads((value) => value + 1)}
            >
              重新读取
            </Button>
            <Button leftSection={<Save size={16} />} disabled={!dirty} onClick={save}>
              保存
            </Button>
          </Group>
        </Group>

        {error && (
          <Alert color="red" title="操作失败" withCloseButton onClose={() => setError('')}>
            {error}
          </Alert>
        )}

        <Group align="flex-start" wrap="wrap" gap="lg">
          <Stack gap={2} w={{ base: '100%', md: 230 }}>
            {files.map((file) => (
              <NavLink
                key={file.fileName}
                label={file.fileName}
                description={file.error}
                c={file.error ? 'red' : undefined}
                active={file.fileName === active}
                disabled={Boolean(file.error)}
                onClick={() => show(file)}
                rightSection={
                  file.fileName === ATTRIBUTES_FILE ? (
                    <Badge size="xs" variant="light">
                      专用
                    </Badge>
                  ) : undefined
                }
              />
            ))}
            {!files.length && (
              <Text c="dimmed" size="sm">
                目录下没有可编辑的配置文件。
              </Text>
            )}
          </Stack>

          <Stack style={{ flex: 1, minWidth: 320 }} gap="sm">
            {!active && <Text c="dimmed">从左侧选择一个配置文件。</Text>}
            {dirty && (
              <Alert color="yellow" p="xs">
                有未保存的修改。
              </Alert>
            )}
            {attributes && current && (
              <AttributesEditor
                entries={attributes}
                selected={selectedAttribute}
                onSelect={setSelectedAttribute}
                onChange={(entry) => {
                  writeAttribute(current.document!, entry);
                  refresh(current);
                }}
                onRename={(from, to) => {
                  renameAttribute(current.document!, from, to);
                  setSelectedAttribute(to);
                  refresh(current);
                }}
                onAdd={(key) => {
                  addAttribute(current.document!, key);
                  setSelectedAttribute(key);
                  refresh(current);
                }}
                onRemove={(key) => {
                  removeAttribute(current.document!, key);
                  setSelectedAttribute(undefined);
                  refresh(current);
                }}
              />
            )}
            {tree && (
              <Card withBorder>
                <ConfigSectionForm section={tree} onChange={editField} />
              </Card>
            )}
          </Stack>
        </Group>
      </Stack>
    </ScrollArea>
  );
}
