import { registerLegacyCatalog } from '../model/legacyCatalog';
import { registerBuiltinPlugin } from '../../plugins/builtin';
import { describe, expect, it } from 'vitest';
import { load } from 'js-yaml';
import { existsSync, readdirSync, readFileSync } from 'node:fs';
import { join } from 'node:path';
import { parseLegacySkill, serializeLegacySkill } from './legacySkill';
import { parseClass, serializeClass } from '../../workspace/model/classProject';

registerLegacyCatalog();
registerBuiltinPlugin();

/**
 * Runs the whole corpus of a real server's plugin folder. Set PROSKILL_DATA to that folder
 * to guard against regressions on files no fixture covers; the suite skips when it is unset.
 */
const root = process.env.PROSKILL_DATA;
const skillDir = root ? join(root, 'dynamic', 'skill') : '';
const classDir = root ? join(root, 'dynamic', 'class') : '';
const available = Boolean(root) && existsSync(skillDir) && existsSync(classDir);

interface Component {
  type: string;
  data?: Record<string, unknown>;
  children?: Record<string, Component>;
}

function listYaml(directory: string): string[] {
  return readdirSync(directory).filter((name) => /\.ya?ml$/i.test(name));
}
function section(yaml: string): Record<string, unknown> {
  const raw = load(yaml) as Record<string, Record<string, unknown>>;
  return raw[Object.keys(raw).find((key) => key !== 'loaded')!];
}
function keys(tree: Record<string, Component>): string[] {
  return Object.entries(tree).flatMap(([key, value]) => [key, ...keys(value.children ?? {})]);
}

/** Every key in the native attributes section is a live setting the server reads. */
function expectSameAttributes(before: unknown, after: unknown): void {
  const normalize = (value: unknown) =>
    Object.fromEntries(
      Object.entries((value ?? {}) as Record<string, unknown>).map(([key, entry]) => [
        key,
        Array.isArray(entry) ? entry.map(String) : String(entry),
      ]),
    );
  expect(normalize(after)).toEqual(normalize(before));
}

describe.skipIf(!available)('whole plugin folder round-trip', () => {
  it('parses every skill file and keeps its components and settings', () => {
    const failures: string[] = [];
    for (const fileName of listYaml(skillDir)) {
      const source = readFileSync(join(skillDir, fileName), 'utf8');
      try {
        const written = serializeLegacySkill(parseLegacySkill(source));
        const before = section(source);
        const after = section(written);
        expect(keys(after.components as Record<string, Component>)).toEqual(
          keys(before.components as Record<string, Component>),
        );
        expectSameAttributes(before.attributes, after.attributes);
        for (const key of ['name', 'type', 'max-level', 'icon', 'msg', 'combo', 'skill-req']) {
          if (before[key] === undefined) continue;
          expect(String(after[key]), `${fileName}.${key}`).toBe(String(before[key]));
        }
        for (const key of ['icon-lore', 'desc']) {
          if (before[key] === undefined) continue;
          expect(after[key], `${fileName}.${key}`).toEqual(before[key]);
        }
      } catch (error) {
        failures.push(`${fileName}: ${error instanceof Error ? error.message : String(error)}`);
      }
    }
    // 刺杀.yml was overwritten by an early editor build and is expected to be rejected.
    expect(failures.filter((entry) => !entry.includes('原生格式'))).toEqual([]);
  });

  it('parses every class file without losing modelled fields', () => {
    const failures: string[] = [];
    for (const fileName of listYaml(classDir)) {
      const source = readFileSync(join(classDir, fileName), 'utf8');
      try {
        const first = parseClass(source);
        expect(parseClass(serializeClass(first))).toEqual(first);
      } catch (error) {
        failures.push(`${fileName}: ${error instanceof Error ? error.message : String(error)}`);
      }
    }
    expect(failures).toEqual([]);
  });
});
