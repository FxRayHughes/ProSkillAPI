import { useState } from 'react';
import { addEdge, applyEdgeChanges, applyNodeChanges } from '@xyflow/react';
import type { Connection, EdgeChange, NodeChange, XYPosition } from '@xyflow/react';
import { canConnect, createNode, createProject } from './graph';
import type { NodeData, NodeDefinition, SkillNode, SkillProject } from './types';

/** One state owner keeps the inspector and graph consistent when nodes are removed. */
export function useSkillEditor() {
  const [project, setProject] = useState(createProject);
  const [selectedId, setSelectedId] = useState<string>();
  const copyNodes = async (ids: string[]) => {
    const selected = project.nodes.filter((node) => ids.includes(node.id));
    if (!selected.length) return;
    const edges = project.edges.filter(
      (edge) => ids.includes(edge.source) && ids.includes(edge.target),
    );
    await navigator.clipboard.writeText(
      JSON.stringify({ proSkillClipboard: 1, nodes: selected, edges }),
    );
  };
  const pasteNodes = async (position: XYPosition) => {
    const raw = await navigator.clipboard.readText();
    const payload = JSON.parse(raw) as {
      proSkillClipboard?: number;
      nodes?: SkillNode[];
      edges?: SkillProject['edges'];
    };
    if (payload.proSkillClipboard !== 1 || !payload.nodes?.length) return;
    const ids = new Map(payload.nodes.map((node) => [node.id, crypto.randomUUID()]));
    const nodes = payload.nodes.map((node) => ({
      ...node,
      id: ids.get(node.id)!,
      position: { x: node.position.x + position.x, y: node.position.y + position.y },
      selected: true,
    }));
    const edges = (payload.edges ?? []).map((edge) => ({
      ...edge,
      id: crypto.randomUUID(),
      source: ids.get(edge.source)!,
      target: ids.get(edge.target)!,
    }));
    setProject((current) => ({
      ...current,
      nodes: [...current.nodes.map((node) => ({ ...node, selected: false })), ...nodes],
      edges: [...current.edges, ...edges],
    }));
  };
  return {
    project,
    selectedId,
    setSelectedId,
    setProject,
    copyNodes,
    pasteNodes,
    selected: project.nodes.find((node) => node.id === selectedId),
    onNodesChange: (changes: NodeChange<SkillNode>[]) =>
      setProject((current) => {
        const nodes = applyNodeChanges(changes, current.nodes);
        const ids = new Set(nodes.map((node) => node.id));
        return {
          ...current,
          nodes,
          edges: current.edges.filter((edge) => ids.has(edge.source) && ids.has(edge.target)),
        };
      }),
    onEdgesChange: (changes: EdgeChange[]) =>
      setProject((current) => ({
        ...current,
        edges: applyEdgeChanges(changes, current.edges),
      })),
    onConnect: (connection: Connection) =>
      setProject((current) =>
        canConnect(connection, current)
          ? { ...current, edges: addEdge(connection, current.edges) }
          : current,
      ),
    addNode: (definition: NodeDefinition, position: XYPosition) => {
      // Palette clicks share a center point; offset collisions so earlier nodes remain selectable.
      const available = { ...position };
      while (
        project.nodes.some(
          (node) =>
            Math.abs(node.position.x - available.x) < 240 &&
            Math.abs(node.position.y - available.y) < 140,
        )
      ) {
        available.x += 40;
        available.y += 160;
      }
      const node = createNode(definition, available);
      setProject((current) => ({ ...current, nodes: [...current.nodes, node] }));
      setSelectedId(node.id);
    },
    updateNode: (id: string, data: Partial<NodeData>) =>
      setProject((current) => ({
        ...current,
        nodes: current.nodes.map((node) =>
          node.id === id ? { ...node, data: { ...node.data, ...data } } : node,
        ),
      })),
    updateMeta: (meta: Partial<SkillProject['meta']>) =>
      setProject((current) => ({ ...current, meta: { ...current.meta, ...meta } })),
  };
}
export type SkillEditorController = ReturnType<typeof useSkillEditor>;
