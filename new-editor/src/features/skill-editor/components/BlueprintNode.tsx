import { Badge, Text } from '@mantine/core';
import { Handle, Position } from '@xyflow/react';
import type { NodeProps } from '@xyflow/react';
import { nodeRegistry } from '../model/registry';
import type { SkillNode } from '../model/types';
import classes from './Editor.module.css';

/** Pin IDs match serialized edges; entry nodes deliberately have no target handle. */
export function BlueprintNode({ data, selected }: NodeProps<SkillNode>) {
  const definition = nodeRegistry.get(data.definitionId);
  if (!definition) return <div className={classes.node}>未注册节点：{data.definitionId}</div>;
  const kindLabel =
    definition.kind === 'entry'
      ? '入口'
      : definition.kind === 'condition'
        ? '条件'
        : definition.id.startsWith('Target')
          ? '目标'
          : '动作';
  return (
    <div className={classes.node} data-selected={selected} title={definition.description}>
      <div className={classes.nodeHeader} style={{ borderTopColor: definition.color }}>
        <Badge variant="light" color={definition.color} size="xs">
          {kindLabel}
        </Badge>
        <Text fw={600} size="sm" mt={6}>
          {data.label}
        </Text>
      </div>
      <div className={classes.pins}>
        <div>
          {definition.inputs.map((port) => (
            <div className={classes.pin} key={port.id}>
              <Handle
                type="target"
                position={Position.Left}
                id={port.id}
                title={`${data.label}：输入${port.label}`}
              />
              {port.label}
            </div>
          ))}
        </div>
        <div>
          {definition.outputs.map((port) => (
            <div className={`${classes.pin} ${classes.output}`} key={port.id}>
              {port.label}
              <Handle
                type="source"
                position={Position.Right}
                id={port.id}
                title={`${data.label}：输出${port.label}`}
              />
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
