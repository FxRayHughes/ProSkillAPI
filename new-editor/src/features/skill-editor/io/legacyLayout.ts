import type { XYPosition } from '@xyflow/react';

/** Written into the skill section; Skill.load and DynamicSkill.load ignore unknown keys. */
export const LAYOUT_KEY = 'editor-layout';

interface LayoutDocument {
  version: number;
  nodes: Record<string, XYPosition>;
}

/**
 * Component paths join original component keys with "/". Component keys never contain a
 * slash because DynamicSkill derives the trigger name with `key.replaceAll("-.+", "")`.
 */
export function layoutPath(parentPath: string, key: string): string {
  return parentPath ? `${parentPath}/${key}` : key;
}

/** Malformed or partial layout data degrades to auto-layout rather than failing the import. */
export function readLayout(section: unknown): Map<string, XYPosition> {
  const positions = new Map<string, XYPosition>();
  if (!section || typeof section !== 'object') return positions;
  const nodes = (section as LayoutDocument).nodes;
  if (!nodes || typeof nodes !== 'object' || Array.isArray(nodes)) return positions;
  for (const [path, value] of Object.entries(nodes)) {
    if (!value || typeof value !== 'object') continue;
    const { x, y } = value as XYPosition;
    if (Number.isFinite(x) && Number.isFinite(y)) positions.set(path, { x, y });
  }
  return positions;
}

/** Coordinates are rounded because sub-pixel drift would churn the file diff on every save. */
export function writeLayout(positions: Map<string, XYPosition>): LayoutDocument {
  return {
    version: 1,
    nodes: Object.fromEntries(
      [...positions].map(([path, position]) => [
        path,
        { x: Math.round(position.x), y: Math.round(position.y) },
      ]),
    ),
  };
}
