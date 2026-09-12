import { dump, load } from 'js-yaml';
import { importLegacyComponents } from './legacyImport';
import { exportLegacyComponents } from './legacyExport';
import { LAYOUT_KEY, readLayout, writeLayout } from './legacyLayout';
import type { SkillMeta, SkillProject } from '../model/types';

/** Keys owned by the editor model; everything else is carried through untouched in `source`. */
const MODELLED_KEYS = new Set([
  'name',
  'type',
  'max-level',
  'skill-req',
  'skill-req-lvl',
  'needs-permission',
  'msg',
  'combo',
  'icon',
  'icon-data',
  'icon-durability',
  'icon-lore',
  'desc',
  'attributes',
  'components',
  LAYOUT_KEY,
]);

const DEFAULT_SERVER_VERSION = '1.16';

function text(value: unknown, fallback = ''): string {
  return value === undefined || value === null ? fallback : String(value);
}
function integer(value: unknown, fallback: number): number {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? Math.trunc(parsed) : fallback;
}
/** Legacy files quote booleans, so "false" must not be read as truthy. */
function flag(value: unknown, fallback = false): boolean {
  if (value === undefined || value === null) return fallback;
  return String(value).toLowerCase() === 'true';
}
function lines(value: unknown): string[] {
  return Array.isArray(value) ? value.map((entry) => text(entry)) : [];
}
/**
 * Values stay strings because the server accepts named formulas, not just numbers.
 * Settings.load stores every key in the section, so list settings such as `incompatible`
 * must survive too; only nested maps are dropped because the format has no use for them.
 */
function attributes(value: unknown): Record<string, string | string[]> {
  if (!value || typeof value !== 'object' || Array.isArray(value)) return {};
  return Object.fromEntries(
    Object.entries(value)
      .filter(([, entry]) => Array.isArray(entry) || typeof entry !== 'object' || entry === null)
      .map(([key, entry]) => [key, Array.isArray(entry) ? lines(entry) : text(entry)]),
  );
}

/** A skill file wraps one section keyed by the skill name; `loaded` marks the aggregate file. */
export function readSkillSection(source: string): {
  name: string;
  section: Record<string, unknown>;
} {
  const raw = load(source);
  if (!raw || typeof raw !== 'object' || Array.isArray(raw))
    throw new Error('技能文件必须是 YAML 对象');
  const document = raw as Record<string, unknown>;
  // An early editor build wrote its own graph format here; the server cannot load those files.
  if (Array.isArray(document.nodes) && Array.isArray(document.edges))
    throw new Error('非 SkillAPI 原生格式（编辑器早期的 nodes/edges 结构），服务器无法加载');
  const sections = Object.entries(document).filter(
    ([key, value]) =>
      key !== 'loaded' && value && typeof value === 'object' && !Array.isArray(value),
  );
  if (!sections.length) throw new Error('文件中没有技能定义');
  if (sections.length > 1) throw new Error('一个文件只能包含一个技能，请拆分技能文件');
  const [name, section] = sections[0] as [string, Record<string, unknown>];
  if (!('components' in section))
    throw new Error('非 SkillAPI 原生格式，缺少 components 段，服务器无法加载');
  return { name, section };
}

/** Parses the native skill section into the editor document without losing unknown keys. */
export function parseLegacySkill(
  source: string,
  serverVersion = DEFAULT_SERVER_VERSION,
): SkillProject {
  const { name, section } = readSkillSection(source);
  const positions = readLayout(section[LAYOUT_KEY]);
  const components = section.components;
  if (components !== null && components !== undefined && typeof components !== 'object')
    throw new Error('components 段必须是名称映射');
  const { nodes, edges } = importLegacyComponents(components ?? {}, positions);
  return {
    schemaVersion: 1,
    source: Object.fromEntries(Object.entries(section).filter(([key]) => !MODELLED_KEYS.has(key))),
    meta: {
      name: text(section.name, name),
      type: text(section.type, '主动技能'),
      maxLevel: integer(section['max-level'], 1),
      skillReq: text(section['skill-req']),
      skillReqLevel: integer(section['skill-req-lvl'], 0),
      needsPermission: flag(section['needs-permission']),
      msg: text(section.msg),
      combo: text(section.combo),
      icon: text(section.icon, 'DIAMOND_SWORD'),
      iconData: integer(section['icon-data'], 0),
      iconDurability: integer(section['icon-durability'], 0),
      iconLore: lines(section['icon-lore']),
      description: lines(section.desc),
      attributes: attributes(section.attributes),
      serverVersion,
    },
    nodes,
    edges,
  };
}

/** Writes the native structure the plugin loads, plus an editor-only layout section. */
export function serializeLegacySkill(project: SkillProject): string {
  const { components, positions } = exportLegacyComponents(project);
  const meta: SkillMeta = project.meta;
  const section: Record<string, unknown> = {
    name: meta.name,
    type: meta.type,
    'max-level': meta.maxLevel,
    'skill-req': meta.skillReq,
    'skill-req-lvl': meta.skillReqLevel,
    'needs-permission': String(meta.needsPermission),
    attributes: meta.attributes,
    msg: meta.msg,
    combo: meta.combo,
    icon: meta.icon,
    'icon-data': meta.iconData,
    'icon-durability': meta.iconDurability,
    'icon-lore': meta.iconLore,
    desc: meta.description,
    // Unmodelled keys keep their original values and land after the known ones.
    ...project.source,
    components,
    [LAYOUT_KEY]: writeLayout(positions),
  };
  return dump({ [meta.name]: section }, { noRefs: true, lineWidth: 120 });
}
