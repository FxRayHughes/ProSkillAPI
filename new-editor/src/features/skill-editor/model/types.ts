import type { Edge, Node } from '@xyflow/react';

/** Stable IDs are serialized; labels can change without breaking saved connections. */
export interface PortDefinition {
  id: string;
  label: string;
}
/** Wire keys remain English; variants share a wire key but have distinct UI identities. */
export interface FieldBase {
  key: string;
  id?: string;
  label: string;
  tooltip?: string;
  requirements?: readonly { key: string; values: readonly (string | number)[] }[];
}
export type FieldDefinition = FieldBase &
  (
    | { type: 'text'; default: string }
    | { type: 'number'; default: number; integer?: boolean }
    | { type: 'boolean'; default: boolean }
    | {
        type: 'select';
        default: string | number;
        options: readonly { value: string; label: string; since?: string; until?: string }[];
        numeric?: boolean;
      }
    | {
        type: 'multiselect' | 'string-list';
        default: string[];
        options?: readonly { value: string; label: string; since?: string; until?: string }[];
      }
  );

export type FieldValue = string | number | boolean | string[];

/** Definitions describe editor UI only; server execution requires a corresponding handler. */
export interface NodeDefinition {
  id: string;
  label: string;
  kind: 'entry' | 'action' | 'condition';
  description: string;
  color: string;
  inputs: readonly PortDefinition[];
  outputs: readonly PortDefinition[];
  fields: readonly FieldDefinition[];
  /** Structured, presentation-only documentation for this node's runtime behavior. */
  help?: {
    label: string;
    description: string;
    behavior: string;
    fields: readonly { key: string; description: string }[];
    ports: readonly { id: string; description: string }[];
    /**
     * 生效前提。缺少这些前提时节点通常静默失效——服主会以为技能写错了，
     * 因此编辑器要显式提示，而不是等运行时无反应。
     */
    requires?: {
      /** 需要安装的外部插件，如 MythicMobs、Vault。 */
      plugins?: readonly string[];
      /** 需要先执行的前置节点 key，用于表达通过 cast data 传值的隐式依赖。 */
      nodes?: readonly string[];
      /** 需要的运行时能力，如 SCRIPT_ENGINE。 */
      capabilities?: readonly string[];
    };
  };
  legacy?: { name: string; category: string; container: boolean };
}
export type NodeData = {
  definitionId: string;
  label: string;
  values: Record<string, FieldValue>;
  /** Original component key from the imported file, e.g. "Damage-g"; reused on export. */
  legacyKey?: string;
};
export type SkillNode = Node<NodeData, 'skill'>;
export type SkillEdge = Edge;

/** Mirrors Skill.load / DynamicSkill.load so a round-trip keeps the server contract intact. */
export interface SkillMeta {
  name: string;
  type: string;
  maxLevel: number;
  skillReq: string;
  skillReqLevel: number;
  needsPermission: boolean;
  msg: string;
  combo: string;
  icon: string;
  iconData: number;
  iconDurability: number;
  iconLore: string[];
  description: string[];
  /**
   * The native `attributes` section. Scalars are the flat -base/-scale pairs, including
   * named formulas; list values such as `incompatible` are settings the server also reads,
   * so they are carried here rather than dropped.
   */
  attributes: Record<string, string | string[]>;
  /** Editor-only preference used to filter version specific enum options. */
  serverVersion: string;
}

/**
 * The editor document is the native SkillAPI skill section plus node coordinates.
 * `source` carries every key the editor does not model so saving never drops addon data.
 */
export interface SkillProject {
  schemaVersion: 1;
  source: Record<string, unknown>;
  meta: SkillMeta;
  nodes: SkillNode[];
  edges: SkillEdge[];
}
