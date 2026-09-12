import { Badge, Group, Stack, Text } from '@mantine/core';
import type { NodeDefinition } from '../model/types';

interface NodeLibraryTooltipProps {
  definition: NodeDefinition;
}

/** 运行时能力的中文说明；键与后端 SkillNode.Capability 的常量名一致。 */
const CAPABILITY_LABELS: Record<string, string> = {
  SCRIPT_ENGINE: '需 JavaScript 引擎',
};

/**
 * 节点库列表项的悬浮详情。
 *
 * 列表里只能放得下节点名，而说明与生效前提恰恰是选节点时最需要的信息，
 * 所以在这里给出：完整说明、字段数量、以及缺失会导致静默失效的前提。
 */
export function NodeLibraryTooltip({ definition }: NodeLibraryTooltipProps) {
  const requires = definition.help?.requires;
  const plugins = requires?.plugins ?? [];
  const nodes = requires?.nodes ?? [];
  const capabilities = requires?.capabilities ?? [];
  const hasRequirements = plugins.length > 0 || nodes.length > 0 || capabilities.length > 0;

  // 宽度由外层 Tooltip 的 w 控制，这里不再重复约束
  return (
    <Stack gap={6}>
      <Text size="sm" fw={600}>
        {definition.label}
      </Text>

      {definition.description && (
        <Text size="xs" style={{ lineHeight: 1.5 }}>
          {definition.description}
        </Text>
      )}

      <Text size="xs" c="dimmed">
        {definition.fields.length > 0 ? `${definition.fields.length} 个可配置参数` : '无可配置参数'}
      </Text>

      {hasRequirements && (
        <Stack gap={4}>
          {plugins.length > 0 && (
            <Group gap={4} wrap="wrap">
              <Text size="xs" c="dimmed">
                需要插件
              </Text>
              {plugins.map((plugin) => (
                <Badge key={plugin} color="orange" variant="light" size="xs">
                  {plugin}
                </Badge>
              ))}
            </Group>
          )}

          {nodes.length > 0 && (
            <Group gap={4} wrap="wrap">
              <Text size="xs" c="dimmed">
                需前置节点
              </Text>
              {nodes.map((node) => (
                <Badge key={node} color="blue" variant="light" size="xs">
                  {node}
                </Badge>
              ))}
            </Group>
          )}

          {capabilities.length > 0 && (
            <Group gap={4} wrap="wrap">
              <Text size="xs" c="dimmed">
                运行环境
              </Text>
              {capabilities.map((capability) => (
                <Badge key={capability} color="grape" variant="light" size="xs">
                  {CAPABILITY_LABELS[capability] ?? capability}
                </Badge>
              ))}
            </Group>
          )}
        </Stack>
      )}
    </Stack>
  );
}
