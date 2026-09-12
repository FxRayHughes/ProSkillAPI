import { Alert, Badge, Button, Card, FileButton, Group, Stack, Text, Title } from '@mantine/core';
import { Download, PlugZap, Trash2 } from 'lucide-react';
import { useState } from 'react';
import { nodeRegistry } from '../../skill-editor/model/registry';
import type { EditorPlugin } from '../model/plugin';
import { collectPluginNodes, parsePlugin, pluginFileName, serializePlugin } from '../model/plugin';
import { readPlugins, writePlugins } from '../model/pluginStore';
import { builtinPlugin } from '../builtin';
import { downloadText } from '../../../shared/lib/download';

/** JSON plugins are declarative and safe: importing never executes arbitrary code. */
export function PluginManagerPage() {
  const [plugins, setPlugins] = useState(readPlugins);
  const [error, setError] = useState('');
  const importPlugin = async (file: File | null) => {
    if (!file) return;
    try {
      const plugin = parsePlugin(await file.text());
      for (const node of plugin.nodes) {
        if (plugin.overrides?.includes(node.id)) nodeRegistry.override(node);
        else nodeRegistry.register(node);
      }
      const next = [...plugins.filter((item) => item.id !== plugin.id), plugin];
      setPlugins(next);
      writePlugins(next);
      setError('');
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : '插件导入失败');
    }
  };
  const remove = (id: string) => {
    const next = plugins.filter((plugin) => plugin.id !== id);
    setPlugins(next);
    writePlugins(next);
  };
  /** 导出的 JSON 与导入格式一致，可原样导回另一台机器。 */
  const exportPlugin = (plugin: EditorPlugin) => {
    const snapshot = collectPluginNodes(plugin, (id) => nodeRegistry.get(id));
    downloadText(pluginFileName(snapshot), serializePlugin(snapshot), 'application/json;charset=utf-8');
  };
  return (
    <Stack p="xl" maw={900}>
      <Group justify="space-between">
        <div>
          <Title order={2}>插件管理</Title>
          <Text c="dimmed" size="sm">
            使用 JSON 注册节点或覆盖已有节点定义。
          </Text>
        </div>
        <Button
          variant="default"
          onClick={() => downloadText('PLUGIN_DEVELOPMENT.md', pluginDevelopmentDoc)}
        >
          下载插件开发文档
        </Button>
        <FileButton accept="application/json,.json" onChange={importPlugin}>
          {(props) => (
            <Button {...props} leftSection={<PlugZap size={16} />}>
              导入插件 JSON
            </Button>
          )}
        </FileButton>
      </Group>
      {error && (
        <Alert color="red" title="导入失败">
          {error}
        </Alert>
      )}
      <Alert color="blue">插件只包含节点定义、参数和展示信息，不会执行脚本或加载代码。</Alert>
      <Card withBorder key={builtinPlugin.id}>
        <Group justify="space-between">
          <div>
            <Group gap="xs">
              <Text fw={600}>
                {builtinPlugin.name}{' '}
                <Text span size="xs" c="dimmed">
                  v{builtinPlugin.version}
                </Text>
              </Text>
              <Badge size="sm" variant="light">
                内置
              </Badge>
            </Group>
            <Text size="xs" c="dimmed">
              {builtinPlugin.id} · {builtinPlugin.nodes.length} 个节点
            </Text>
            <Text size="xs" c="dimmed">
              {builtinPlugin.description}
            </Text>
          </div>
          {/* 内置插件也可导出，便于以它为模板改自己的节点包 */}
          <Button
            variant="subtle"
            leftSection={<Download size={15} />}
            onClick={() => exportPlugin(builtinPlugin)}
          >
            导出
          </Button>
        </Group>
      </Card>
      {plugins.map((plugin) => (
        <Card withBorder key={plugin.id}>
          <Group justify="space-between">
            <div>
              <Text fw={600}>
                {plugin.name}{' '}
                <Text span size="xs" c="dimmed">
                  v{plugin.version}
                </Text>
              </Text>
              <Text size="xs" c="dimmed">
                {plugin.id} · {plugin.nodes.length} 个节点
              </Text>
            </div>
            <Group gap="xs">
              <Button
                variant="subtle"
                leftSection={<Download size={15} />}
                onClick={() => exportPlugin(plugin)}
              >
                导出
              </Button>
              <Button
                color="red"
                variant="subtle"
                leftSection={<Trash2 size={15} />}
                onClick={() => remove(plugin.id)}
              >
                移除
              </Button>
            </Group>
          </Group>
        </Card>
      ))}
      {!plugins.length && <Text c="dimmed">尚未导入其他插件。</Text>}
    </Stack>
  );
}

const pluginDevelopmentDoc = `# ProSkillAPI 插件节点开发\n\n请参阅项目中的 PLUGIN_DEVELOPMENT.md，了解 JSON 插件结构、节点字段、覆盖规则和安全边界。\n`;
