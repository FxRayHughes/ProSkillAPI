import {
  ActionIcon,
  Alert,
  Button,
  Card,
  Group,
  Modal,
  MultiSelect,
  NumberInput,
  ScrollArea,
  Select,
  Stack,
  Switch,
  Text,
  Textarea,
  TextInput,
  Title,
  UnstyledButton,
} from '@mantine/core';
import { notifications } from '@mantine/notifications';
import { FilePlus2, Plus, RefreshCcw, Save, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useWorkspace } from '../model/workspaceContext';
import { SelectPluginFolder } from './SelectPluginFolder';
import { createYamlFile, fileExists, listYamlFiles, writeFile } from '../model/pluginDirectory';
import type { DirectoryEntry } from '../model/pluginDirectory';
import { createClass, parseClass, serializeClass, TREE_TYPES } from '../model/classProject';
import type { ClassProject } from '../model/classProject';

interface ClassFile {
  fileName: string;
  handle: FileSystemFileHandle;
  project?: ClassProject;
  error?: string;
}

function describe(error: unknown, fallback: string): string {
  return error instanceof Error ? error.message : fallback;
}

/** A single unparsable file is listed with its reason instead of hiding the whole folder. */
function toClassFile(entry: DirectoryEntry): ClassFile {
  try {
    return { fileName: entry.fileName, handle: entry.handle, project: parseClass(entry.text) };
  } catch (cause) {
    return {
      fileName: entry.fileName,
      handle: entry.handle,
      error: describe(cause, '无法解析该职业文件'),
    };
  }
}

/** Attribute names the server always reads; custom names appear alongside them. */
const KNOWN_ATTRIBUTES = [
  { key: 'health', label: '生命值' },
  { key: 'mana', label: '法力值' },
];

function attributeNames(attributes: Record<string, string>): string[] {
  const names = new Set(KNOWN_ATTRIBUTES.map((entry) => entry.key));
  for (const key of Object.keys(attributes)) names.add(key.replace(/-(base|scale)$/, ''));
  return [...names];
}
function labelFor(name: string): string {
  return KNOWN_ATTRIBUTES.find((entry) => entry.key === name)?.label ?? name;
}

/** Reads dynamic/class from the plugin folder and writes each class back to its own file. */
export function ClassManagerPage() {
  const { root, restored } = useWorkspace();
  const [files, setFiles] = useState<ClassFile[]>([]);
  const [skillNames, setSkillNames] = useState<string[]>([]);
  const [active, setActive] = useState<ClassProject>();
  const [activeFile, setActiveFile] = useState<string>();
  const [error, setError] = useState('');
  const [creating, setCreating] = useState(false);
  const [newName, setNewName] = useState('');
  const [newAttribute, setNewAttribute] = useState('');

  const [reloads, setReloads] = useState(0);

  useEffect(() => {
    if (!root) return;
    // Switching directories mid-scan must not let the previous result land.
    let cancelled = false;
    // Skill names come from the same folder the server loads, so the picker cannot drift.
    void Promise.all([listYamlFiles(root.classes), listYamlFiles(root.skills)])
      .then(([classes, skills]) => {
        if (cancelled) return;
        setFiles(classes.map(toClassFile));
        setSkillNames(skills.map((entry) => entry.name));
        setActive(undefined);
        setActiveFile(undefined);
      })
      .catch((cause: unknown) => {
        if (!cancelled) setError(describe(cause, '无法读取职业目录'));
      });
    return () => {
      cancelled = true;
    };
  }, [root, reloads]);

  const update = (patch: Partial<ClassProject>) =>
    setActive((current) => (current ? { ...current, ...patch } : current));

  const save = async () => {
    const file = files.find((entry) => entry.fileName === activeFile);
    if (!active || !file) return;
    setError('');
    try {
      await writeFile(file.handle, serializeClass(active));
      setFiles((current) =>
        current.map((entry) =>
          entry.fileName === file.fileName
            ? { ...entry, project: active, error: undefined }
            : entry,
        ),
      );
      notifications.show({
        color: 'teal',
        title: '保存成功',
        message: `职业已写回 ${file.fileName}`,
        closeButtonProps: { 'aria-label': '关闭提示' },
      });
    } catch (cause) {
      setError(describe(cause, '无法写入职业文件'));
    }
  };

  const create = async () => {
    const name = newName.trim();
    if (!name || !root) return;
    setError('');
    try {
      if (await fileExists(root.classes, name)) throw new Error(`职业 ${name} 已存在`);
      const project = createClass(name);
      const handle = await createYamlFile(root.classes, name, serializeClass(project));
      const file: ClassFile = { fileName: `${name}.yml`, handle, project };
      setFiles((current) => [...current, file]);
      setActive(project);
      setActiveFile(file.fileName);
      setCreating(false);
      setNewName('');
    } catch (cause) {
      setError(describe(cause, '无法创建职业'));
    }
  };

  const setAttribute = (key: string, value: string) =>
    active && update({ attributes: { ...active.attributes, [key]: value } });
  const removeAttribute = (name: string) =>
    active &&
    update({
      attributes: Object.fromEntries(
        Object.entries(active.attributes).filter(
          ([key]) => key !== `${name}-base` && key !== `${name}-scale`,
        ),
      ),
    });

  if (!root)
    return (
      <SelectPluginFolder
        restored={restored}
        title="职业管理"
        hint="选择插件目录后，这里会列出 dynamic/class 下的职业。"
      />
    );

  return (
    <ScrollArea h="calc(100dvh - 64px)" offsetScrollbars>
      <Stack p={{ base: 'md', sm: 'xl' }} gap="lg">
        <Group justify="space-between" align="flex-start" wrap="wrap">
          <div>
            <Title order={2}>职业管理</Title>
            <Text c="dimmed" size="sm">
              读取自 {root.handle.name}/dynamic/class
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
              新建职业
            </Button>
          </Group>
        </Group>

        {error && (
          <Alert color="red" title="操作失败" withCloseButton onClose={() => setError('')}>
            {error}
          </Alert>
        )}

        <Group align="flex-start" wrap="wrap" gap="lg">
          <Stack gap="xs" w={{ base: '100%', md: 260 }}>
            <Text fw={600} size="sm">
              职业列表（{files.length}）
            </Text>
            {files.map((file) => (
              <UnstyledButton
                key={file.fileName}
                onClick={() => {
                  if (!file.project) return;
                  setActive(file.project);
                  setActiveFile(file.fileName);
                }}
              >
                <Card
                  withBorder
                  padding="xs"
                  bg={activeFile === file.fileName ? 'blue.9' : undefined}
                >
                  <Text fw={600} lineClamp={1}>
                    {file.project?.name ?? file.fileName}
                  </Text>
                  <Text size="xs" c={file.error ? 'red' : 'dimmed'} lineClamp={2}>
                    {file.error ?? `${file.project!.skills.length} 个技能 · ${file.fileName}`}
                  </Text>
                </Card>
              </UnstyledButton>
            ))}
            {!files.length && (
              <Text c="dimmed" size="sm">
                {root ? 'dynamic/class 中没有职业文件。' : '请先选择 SkillAPI 插件目录。'}
              </Text>
            )}
          </Stack>

          {active && (
            <Card withBorder maw={620} style={{ flex: 1, minWidth: 320 }}>
              <Stack gap="sm">
                <Group justify="space-between">
                  <Text fw={600}>{active.key}</Text>
                  <Button size="xs" leftSection={<Save size={15} />} onClick={save}>
                    保存
                  </Button>
                </Group>

                <TextInput
                  label="职业名称"
                  value={active.name}
                  onChange={(event) => update({ name: event.currentTarget.value })}
                />
                <TextInput
                  label="职业前缀"
                  description="支持 &amp; 颜色码"
                  value={active.prefix}
                  onChange={(event) => update({ prefix: event.currentTarget.value })}
                />
                <TextInput
                  label="动作栏文本"
                  description="留空表示不显示动作栏"
                  value={active.actionBar}
                  onChange={(event) => update({ actionBar: event.currentTarget.value })}
                />
                <Group grow>
                  <TextInput
                    label="所属职业组"
                    description="同一组内只能选择一个职业"
                    value={active.group}
                    onChange={(event) => update({ group: event.currentTarget.value })}
                  />
                  <Select
                    label="父职业"
                    description="转职的来源职业"
                    clearable
                    searchable
                    data={files
                      .map((file) => file.project?.key)
                      .filter((key): key is string => Boolean(key) && key !== active.key)}
                    value={active.parent || null}
                    onChange={(parent) => update({ parent: parent ?? '' })}
                  />
                </Group>
                <Group grow>
                  <NumberInput
                    label="最大等级"
                    min={1}
                    allowDecimal={false}
                    value={active.maxLevel}
                    onChange={(value) => typeof value === 'number' && update({ maxLevel: value })}
                  />
                  <NumberInput
                    label="经验来源"
                    description="按位组合的经验来源掩码"
                    min={0}
                    allowDecimal={false}
                    value={active.expSources}
                    onChange={(value) => typeof value === 'number' && update({ expSources: value })}
                  />
                </Group>
                <Group grow>
                  <TextInput
                    label="法力名称"
                    value={active.mana}
                    onChange={(event) => update({ mana: event.currentTarget.value })}
                  />
                  <NumberInput
                    label="每秒法力回复"
                    min={0}
                    decimalScale={2}
                    value={active.manaRegen}
                    onChange={(value) => typeof value === 'number' && update({ manaRegen: value })}
                  />
                </Group>
                <Select
                  label="技能树排列方式"
                  allowDeselect={false}
                  data={TREE_TYPES.map((entry) => ({ value: entry.value, label: entry.label }))}
                  value={active.tree.toUpperCase().replace(/ /g, '_')}
                  onChange={(tree) => tree && update({ tree })}
                />
                <Switch
                  label="需要权限才能选择"
                  checked={active.needsPermission}
                  onChange={(event) => update({ needsPermission: event.currentTarget.checked })}
                />

                <Text fw={600} size="sm" mt="xs">
                  职业属性
                </Text>
                <Text size="xs" c="dimmed">
                  基础值与每级成长可填写数值或服务端支持的公式，属性名保持与 attributes.yml 一致。
                </Text>
                {attributeNames(active.attributes).map((name) => (
                  <Group key={name} align="flex-end" wrap="nowrap" gap="xs">
                    <TextInput
                      label={`${labelFor(name)} · 基础值`}
                      description={`${name}-base`}
                      value={active.attributes[`${name}-base`] ?? ''}
                      onChange={(event) => setAttribute(`${name}-base`, event.currentTarget.value)}
                      style={{ flex: 1 }}
                    />
                    <TextInput
                      label="每级成长"
                      description={`${name}-scale`}
                      value={active.attributes[`${name}-scale`] ?? ''}
                      onChange={(event) => setAttribute(`${name}-scale`, event.currentTarget.value)}
                      style={{ flex: 1 }}
                    />
                    <ActionIcon
                      variant="subtle"
                      color="red"
                      aria-label={`删除 ${labelFor(name)}`}
                      onClick={() => removeAttribute(name)}
                    >
                      <Trash2 size={16} />
                    </ActionIcon>
                  </Group>
                ))}
                <Group align="flex-end" wrap="nowrap" gap="xs">
                  <TextInput
                    label="新增属性"
                    placeholder="属性名，例如 防御力"
                    value={newAttribute}
                    onChange={(event) => setNewAttribute(event.currentTarget.value)}
                    style={{ flex: 1 }}
                  />
                  <ActionIcon
                    variant="light"
                    aria-label="新增职业属性"
                    disabled={!newAttribute.trim()}
                    onClick={() => {
                      const name = newAttribute.trim();
                      update({
                        attributes: {
                          ...active.attributes,
                          [`${name}-base`]: '0',
                          [`${name}-scale`]: '0',
                        },
                      });
                      setNewAttribute('');
                    }}
                  >
                    <Plus size={16} />
                  </ActionIcon>
                </Group>

                <Text fw={600} size="sm" mt="xs">
                  职业技能（{active.skills.length}）
                </Text>
                <MultiSelect
                  label="可学习的技能"
                  description="顺序会影响技能树的排列，可拖动下方列表调整"
                  searchable
                  data={[...new Set([...skillNames, ...active.skills])]}
                  value={active.skills}
                  onChange={(skills) => update({ skills })}
                />
                <SkillOrder skills={active.skills} onChange={(skills) => update({ skills })} />

                <Text fw={600} size="sm" mt="xs">
                  图标
                </Text>
                <TextInput
                  label="图标材质 ID"
                  value={active.icon}
                  onChange={(event) => update({ icon: event.currentTarget.value })}
                />
                <Group grow>
                  <NumberInput
                    label="图标数据值"
                    min={0}
                    allowDecimal={false}
                    value={active.iconData}
                    onChange={(value) => typeof value === 'number' && update({ iconData: value })}
                  />
                  <NumberInput
                    label="图标耐久值"
                    min={0}
                    allowDecimal={false}
                    value={active.iconDurability}
                    onChange={(value) =>
                      typeof value === 'number' && update({ iconDurability: value })
                    }
                  />
                </Group>
                <Textarea
                  label="图标 Lore"
                  description="每行一条"
                  autosize
                  minRows={2}
                  value={active.iconLore.join('\n')}
                  onChange={(event) =>
                    update({
                      iconLore:
                        event.currentTarget.value === ''
                          ? []
                          : event.currentTarget.value.split('\n'),
                    })
                  }
                />
                <Textarea
                  label="材质黑名单"
                  description="每行一个材质名，禁止该职业使用"
                  autosize
                  minRows={1}
                  value={active.blacklist.join('\n')}
                  onChange={(event) =>
                    update({
                      blacklist:
                        event.currentTarget.value === ''
                          ? []
                          : event.currentTarget.value.split('\n'),
                    })
                  }
                />
              </Stack>
            </Card>
          )}
        </Group>
      </Stack>

      <Modal opened={creating} onClose={() => setCreating(false)} title="新建职业">
        <Stack>
          <TextInput
            label="职业名称"
            description="同时作为文件名与其他配置引用该职业的键"
            value={newName}
            onChange={(event) => setNewName(event.currentTarget.value)}
            onKeyDown={(event) => event.key === 'Enter' && create()}
            data-autofocus
          />
          <Button onClick={create} disabled={!newName.trim()}>
            创建
          </Button>
        </Stack>
      </Modal>
    </ScrollArea>
  );
}

/** Explicit reordering keeps the saved order meaningful without a drag-and-drop dependency. */
function SkillOrder({
  skills,
  onChange,
}: {
  skills: string[];
  onChange: (skills: string[]) => void;
}) {
  const move = (index: number, offset: number) => {
    const target = index + offset;
    if (target < 0 || target >= skills.length) return;
    const next = [...skills];
    [next[index], next[target]] = [next[target], next[index]];
    onChange(next);
  };
  return (
    <Stack gap={4}>
      {skills.map((skill, index) => (
        <Group key={skill} gap="xs" wrap="nowrap">
          <Text size="xs" c="dimmed" w={24}>
            {index + 1}
          </Text>
          <Text size="sm" style={{ flex: 1 }} lineClamp={1}>
            {skill}
          </Text>
          <Button
            size="compact-xs"
            variant="subtle"
            disabled={index === 0}
            onClick={() => move(index, -1)}
          >
            上移
          </Button>
          <Button
            size="compact-xs"
            variant="subtle"
            disabled={index === skills.length - 1}
            onClick={() => move(index, 1)}
          >
            下移
          </Button>
        </Group>
      ))}
    </Stack>
  );
}
