// Consumers use this boundary instead of depending on internal component paths.
export { EditorPage } from './components/EditorPage';
export { nodeRegistry, registerActionNode, registerEntryNode } from './model/registry';
export type { NodeDefinition, FieldDefinition, PortDefinition, SkillProject } from './model/types';
