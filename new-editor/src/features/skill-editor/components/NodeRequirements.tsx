import { Alert, Badge, Group, Stack, Text } from '@mantine/core';

/** 节点生效所需的前提，由构建期从 @SkillNode 注解导出。 */
export interface NodeRequirementsData {
  /** 需要安装的外部插件，如 MythicMobs、Vault。 */
  plugins?: readonly string[];
  /** 需要先执行的前置节点 key。 */
  nodes?: readonly string[];
  /** 需要的运行时能力，如 SCRIPT_ENGINE。 */
  capabilities?: readonly string[];
}

interface NodeRequirementsProps {
  requires?: NodeRequirementsData;
}

/** 运行时能力的中文说明；键与后端 SkillNode.Capability 的常量名一致。 */
const CAPABILITY_LABELS: Record<string, string> = {
  SCRIPT_ENGINE: 'JavaScript 脚本引擎（Java 15 起需自行提供 Nashorn 等实现）',
};

/**
 * 展示节点的生效前提。
 *
 * 缺少前提时节点通常是静默失效的——服主会以为技能配错了而反复排查，
 * 所以这里要显式列出，而不是等运行时没反应。
 */
export function NodeRequirements({ requires }: NodeRequirementsProps) {
  if (!requires) return null;

  const plugins = requires.plugins ?? [];
  const nodes = requires.nodes ?? [];
  const capabilities = requires.capabilities ?? [];
  if (plugins.length === 0 && nodes.length === 0 && capabilities.length === 0) return null;

  return (
    <Alert color="yellow" variant="light" title="生效前提" p="sm">
      <Stack gap={8}>
        {plugins.length > 0 && (
          <Stack gap={4}>
            <Text size="xs" c="dimmed">
              需要安装以下插件，否则本节点不会产生效果：
            </Text>
            <Group gap={6}>
              {plugins.map((plugin) => (
                <Badge key={plugin} color="orange" variant="light" size="sm">
                  {plugin}
                </Badge>
              ))}
            </Group>
          </Stack>
        )}

        {nodes.length > 0 && (
          <Stack gap={4}>
            <Text size="xs" c="dimmed">
              需要先由以下节点写入数据，本节点才能读到：
            </Text>
            <Group gap={6}>
              {nodes.map((node) => (
                <Badge key={node} color="blue" variant="light" size="sm">
                  {node}
                </Badge>
              ))}
            </Group>
          </Stack>
        )}

        {capabilities.length > 0 && (
          <Stack gap={4}>
            <Text size="xs" c="dimmed">
              需要服务端具备以下运行时能力：
            </Text>
            <Stack gap={2}>
              {capabilities.map((capability) => (
                <Text key={capability} size="xs">
                  · {CAPABILITY_LABELS[capability] ?? capability}
                </Text>
              ))}
            </Stack>
          </Stack>
        )}
      </Stack>
    </Alert>
  );
}
