import type { NodeDefinition } from './types';

/** Register definitions before mounting the editor; duplicate IDs fail instead of overwriting. */
export class NodeRegistry {
  private readonly definitions = new Map<string, NodeDefinition>();

  register(definition: NodeDefinition): void {
    if (!definition.id.trim() || this.definitions.has(definition.id)) {
      throw new Error(`Invalid or duplicate node ID: ${definition.id}`);
    }
    for (const entries of [definition.inputs, definition.outputs, definition.fields]) {
      const ids = entries.map((entry) => ('key' in entry ? (entry.id ?? entry.key) : entry.id));
      if (ids.some((id) => !id.trim()) || new Set(ids).size !== ids.length) {
        throw new Error(`Invalid or duplicate ports/fields: ${definition.id}`);
      }
    }
    if (definition.kind === 'entry' && definition.inputs.length) {
      throw new Error('Entry nodes cannot have input ports');
    }
    // Own an immutable snapshot so external plugins cannot mutate existing pin contracts.
    const copy = structuredClone(definition);
    [copy.inputs, copy.outputs, copy.fields].forEach((items) => {
      items.forEach(Object.freeze);
      Object.freeze(items);
    });
    this.definitions.set(copy.id, Object.freeze(copy));
  }

  override(definition: NodeDefinition): void {
    this.definitions.delete(definition.id);
    this.register(definition);
  }

  /** Undefined permits callers to render a recoverable missing-definition state. */
  get(id: string): NodeDefinition | undefined {
    return this.definitions.get(id);
  }
  /** Return a fresh list to protect registry ordering from consumers. */
  list(): NodeDefinition[] {
    return [...this.definitions.values()];
  }
}

export const nodeRegistry = new NodeRegistry();
/** Third-party actions must supply their complete pin and field contracts. */
export function registerActionNode(definition: Omit<NodeDefinition, 'kind'>): void {
  nodeRegistry.register({ ...definition, kind: 'action' });
}
/** Entry input ports are always empty because execution starts here. */
export function registerEntryNode(definition: Omit<NodeDefinition, 'kind' | 'inputs'>): void {
  nodeRegistry.register({ ...definition, kind: 'entry', inputs: [] });
}
