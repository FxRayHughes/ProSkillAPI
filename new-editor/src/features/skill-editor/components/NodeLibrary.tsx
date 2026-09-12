import { useState } from 'react';
import {
  Accordion,
  ColorSwatch,
  Group,
  Stack,
  Text,
  TextInput,
  Tooltip,
  UnstyledButton,
} from '@mantine/core';
import { Plus, Search } from 'lucide-react';
import { nodeRegistry } from '../model/registry';
import type { NodeDefinition } from '../model/types';
import classes from './Editor.module.css';
import { NodeLibraryTooltip } from './NodeLibraryTooltip';

/** MIME key is private to this editor; unknown external drags are ignored by the canvas. */
export const NODE_DRAG_TYPE = 'application/proskill-node';

/** Filtering affects the palette only; registered definitions keep stable identities. */
export function NodeLibrary({ onAdd }: { onAdd: (definition: NodeDefinition) => void }) {
  const [query, setQuery] = useState('');
  const nodes = nodeRegistry
    .list()
    .filter((node) => `${node.label} ${node.id}`.toLowerCase().includes(query.toLowerCase()));
  const folders = [
    { id: 'entry', label: '入口节点', nodes: nodes.filter((node) => node.kind === 'entry') },
    {
      id: 'target',
      label: '目标节点',
      nodes: nodes.filter((node) => node.id.startsWith('Target')),
    },
    {
      id: 'condition',
      label: '条件节点',
      nodes: nodes.filter((node) => node.kind === 'condition'),
    },
    {
      id: 'action',
      label: '动作节点',
      nodes: nodes.filter((node) => node.kind === 'action' && !node.id.startsWith('Target')),
    },
  ];
  return (
    <Stack gap="sm">
      <Text fw={600} size="sm">
        节点库
      </Text>
      <TextInput
        aria-label="搜索节点"
        placeholder="搜索节点"
        leftSection={<Search size={16} />}
        value={query}
        onChange={(event) => setQuery(event.currentTarget.value)}
      />
      {/* Categories start collapsed to keep a large legacy node catalog scannable. */}
      <Accordion multiple variant="separated">
        {folders.map((folder) => (
          <Accordion.Item key={folder.id} value={folder.id}>
            <Accordion.Control>
              {folder.label}
              <Text span size="xs" c="dimmed" ml="xs">
                {folder.nodes.length}
              </Text>
            </Accordion.Control>
            <Accordion.Panel>
              <Stack gap={2}>
                {folder.nodes.map((node) => (
                  // 列表只放得下节点名，说明与生效前提放在悬浮详情里。
                  // 开合留一点延迟，避免快速划过列表时闪烁。
                  <Tooltip
                    key={node.id}
                    label={<NodeLibraryTooltip definition={node} />}
                    position="right"
                    withArrow
                    openDelay={350}
                    closeDelay={80}
                    multiline
                    w={360}
                    transitionProps={{ duration: 120 }}
                  >
                    <UnstyledButton
                      className={classes.libraryItem}
                      draggable
                      onDragStart={(event) => event.dataTransfer.setData(NODE_DRAG_TYPE, node.id)}
                      onClick={() => onAdd(node)}
                    >
                      <Group wrap="nowrap" gap="sm">
                        <ColorSwatch color={node.color} size={12} />
                        <div className={classes.grow}>
                          <Text size="sm" fw={500}>
                            {node.label}
                          </Text>
                        </div>
                        <Plus size={15} />
                      </Group>
                    </UnstyledButton>
                  </Tooltip>
                ))}
              </Stack>
            </Accordion.Panel>
          </Accordion.Item>
        ))}
      </Accordion>
      {!nodes.length && (
        <Text c="dimmed" size="sm">
          没有匹配的节点
        </Text>
      )}
    </Stack>
  );
}
