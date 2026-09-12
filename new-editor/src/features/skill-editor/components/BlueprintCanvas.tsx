import { Background, Controls, MiniMap, ReactFlow, useReactFlow } from '@xyflow/react';
import { useComputedColorScheme } from '@mantine/core';
import { canConnect } from '../model/graph';
import { nodeRegistry } from '../model/registry';
import type { SkillEditorController } from '../model/useSkillEditor';
import { BlueprintNode } from './BlueprintNode';
import { NODE_DRAG_TYPE } from './NodeLibrary';
import classes from './Editor.module.css';

const nodeTypes = { skill: BlueprintNode };

/** Screen coordinates must be transformed by React Flow after zooming or panning. */
export function BlueprintCanvas({ editor }: { editor: SkillEditorController }) {
  const { screenToFlowPosition } = useReactFlow();
  const colorScheme = useComputedColorScheme('light', { getInitialValueInEffect: true });
  return (
    <div
      className={classes.canvas}
      onDragOver={(event) => {
        event.preventDefault();
        event.dataTransfer.dropEffect = 'copy';
      }}
      onDrop={(event) => {
        event.preventDefault();
        const definition = nodeRegistry.get(event.dataTransfer.getData(NODE_DRAG_TYPE));
        if (definition)
          editor.addNode(definition, screenToFlowPosition({ x: event.clientX, y: event.clientY }));
      }}
    >
      <ReactFlow
        nodes={editor.project.nodes}
        edges={editor.project.edges}
        nodeTypes={nodeTypes}
        onNodesChange={editor.onNodesChange}
        onEdgesChange={editor.onEdgesChange}
        onConnect={editor.onConnect}
        isValidConnection={(connection) => canConnect(connection, editor.project)}
        onNodeClick={(_, node) => editor.setSelectedId(node.id)}
        onPaneClick={() => editor.setSelectedId(undefined)}
        multiSelectionKeyCode={['Control', 'Meta']}
        onKeyDown={async (event) => {
          const selected = editor.project.nodes
            .filter((node) => node.selected)
            .map((node) => node.id);
          if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'c') {
            event.preventDefault();
            await editor.copyNodes(selected);
          }
          if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'x') {
            event.preventDefault();
            await editor.copyNodes(selected);
            editor.onNodesChange(selected.map((id) => ({ type: 'remove', id })));
          }
          if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'v') {
            event.preventDefault();
            try {
              await editor.pasteNodes(screenToFlowPosition({ x: 120, y: 120 }));
            } catch {
              /* Ignore unrelated clipboard text. */
            }
          }
        }}
        colorMode={colorScheme}
        fitView
        fitViewOptions={{ maxZoom: 1 }}
        minZoom={0.2}
        maxZoom={2}
      >
        <Background gap={24} />
        <Controls />
        <MiniMap pannable zoomable />
      </ReactFlow>
    </div>
  );
}
