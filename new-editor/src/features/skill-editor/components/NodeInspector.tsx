import { Divider, Paper, Stack, Text, TextInput } from '@mantine/core';
import { nodeRegistry } from '../model/registry';
import { changeField, isFieldVisible, normalizeValues } from '../model/fieldValues';
import type { NodeData, SkillNode } from '../model/types';
import { NodeField } from './NodeField';
import { NodeRequirements } from './NodeRequirements';

/** Old projects may lack newly migrated keys; defaults are resolved without mutating selection. */
export function NodeInspector({
  node,
  onChange,
  serverVersion,
}: {
  node?: SkillNode;
  onChange: (id: string, data: Partial<NodeData>) => void;
  serverVersion?: string;
}) {
  if (!node)
    return (
      <Text c="dimmed" size="sm">
        未选择节点
      </Text>
    );
  const definition = nodeRegistry.get(node.data.definitionId);
  if (!definition) return <Text c="red">节点类型尚未注册</Text>;
  const values = normalizeValues(definition, node.data.values);
  return (
    <Stack gap="lg">
      <Paper className="inspectorHeader" p="sm" withBorder>
        <Text fw={700} size="sm">
          节点属性
        </Text>
        <Stack gap={4} mt={6}>
          <Text size="md" fw={600}>
            {node.data.label}
          </Text>
          <Text size="xs" c="dimmed">
            {definition.description}
          </Text>
        </Stack>
      </Paper>
      <NodeRequirements requires={definition.help?.requires} />
      <Stack gap="sm">
        <Text size="xs" fw={700} c="dimmed" tt="uppercase">
          基础设置
        </Text>
        <TextInput
          label="显示名称"
          value={node.data.label}
          onChange={(event) => onChange(node.id, { label: event.currentTarget.value })}
        />
      </Stack>
      <Divider label="节点参数" labelPosition="center" />
      {definition.fields
        .filter((field) => isFieldVisible(field, values))
        .map((field) => (
          <NodeField
            key={`${node.id}:${field.id ?? field.key}`}
            field={field}
            serverVersion={serverVersion}
            value={values[field.key] ?? field.default}
            onChange={(value) =>
              onChange(node.id, { values: changeField(definition, values, field.key, value) })
            }
          />
        ))}
    </Stack>
  );
}
