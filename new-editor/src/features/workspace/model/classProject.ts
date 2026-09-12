import { dump, load } from 'js-yaml';

/** Mirrors RPGClass.load; every key it reads is modelled so the editor never drops one. */
export interface ClassProject {
  /** Keys RPGClass.load does not read are carried through untouched. */
  source: Record<string, unknown>;
  /** YAML section key, equal to the file name and to the name the server registers. */
  key: string;
  name: string;
  prefix: string;
  actionBar: string;
  group: string;
  mana: string;
  parent: string;
  maxLevel: number;
  expSources: number;
  manaRegen: number;
  needsPermission: boolean;
  tree: string;
  blacklist: string[];
  skills: string[];
  /** Flat -base/-scale keys; custom attribute names may be non-ASCII. */
  attributes: Record<string, string>;
  icon: string;
  iconData: number;
  iconDurability: number;
  iconLore: string[];
}

/** DefaultTreeType enum values; getByName uppercases and maps spaces to underscores. */
export const TREE_TYPES = [
  { value: 'REQUIREMENT', label: '按前置需求排列' },
  { value: 'BASIC_HORIZONTAL', label: '基础横向排列' },
  { value: 'BASIC_VERTICAL', label: '基础纵向排列' },
  { value: 'LEVEL_HORIZONTAL', label: '按等级横向排列' },
  { value: 'LEVEL_VERTICAL', label: '按等级纵向排列' },
  { value: 'FLOOD', label: '填充排列' },
] as const;

const MODELLED_KEYS = new Set([
  'name',
  'prefix',
  'action-bar',
  'group',
  'mana',
  'parent',
  'max-level',
  'exp-source',
  'mana-regen',
  'needs-permission',
  'tree',
  'blacklist',
  'skills',
  'attributes',
  'icon',
  'icon-data',
  'icon-durability',
  'icon-lore',
]);

function text(value: unknown, fallback = ''): string {
  return value === undefined || value === null ? fallback : String(value);
}
function integer(value: unknown, fallback: number): number {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? Math.trunc(parsed) : fallback;
}
function decimal(value: unknown, fallback: number): number {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : fallback;
}
/** Legacy files quote booleans, so "false" must not be read as truthy. */
function flag(value: unknown, fallback = false): boolean {
  if (value === undefined || value === null) return fallback;
  return String(value).toLowerCase() === 'true';
}
function lines(value: unknown): string[] {
  return Array.isArray(value) ? value.map((entry) => text(entry)) : [];
}
function attributes(value: unknown): Record<string, string> {
  if (!value || typeof value !== 'object' || Array.isArray(value)) return {};
  return Object.fromEntries(
    Object.entries(value)
      .filter(([, entry]) => !Array.isArray(entry) && typeof entry !== 'object')
      .map(([key, entry]) => [key, text(entry)]),
  );
}

export function parseClass(source: string): ClassProject {
  const raw = load(source);
  if (!raw || typeof raw !== 'object' || Array.isArray(raw))
    throw new Error('职业文件必须是 YAML 对象');
  const sections = Object.entries(raw as Record<string, unknown>).filter(
    ([key, value]) =>
      key !== 'loaded' && value && typeof value === 'object' && !Array.isArray(value),
  );
  if (!sections.length) throw new Error('文件中没有职业定义');
  if (sections.length > 1) throw new Error('一个文件只能包含一个职业，请拆分职业文件');
  const [key, value] = sections[0] as [string, Record<string, unknown>];
  return {
    source: Object.fromEntries(Object.entries(value).filter(([name]) => !MODELLED_KEYS.has(name))),
    key,
    name: text(value.name, key),
    prefix: text(value.prefix),
    actionBar: text(value['action-bar']),
    group: text(value.group, 'class'),
    mana: text(value.mana, 'Mana'),
    parent: text(value.parent),
    maxLevel: integer(value['max-level'], 40),
    expSources: integer(value['exp-source'], 273),
    manaRegen: decimal(value['mana-regen'], 1),
    needsPermission: flag(value['needs-permission']),
    tree: text(value.tree, 'REQUIREMENT'),
    blacklist: lines(value.blacklist),
    skills: lines(value.skills),
    attributes: attributes(value.attributes),
    icon: text(value.icon, 'DIAMOND_SWORD'),
    iconData: integer(value['icon-data'], 0),
    iconDurability: integer(value['icon-durability'], 0),
    iconLore: lines(value['icon-lore']),
  };
}

export function serializeClass(project: ClassProject): string {
  const section: Record<string, unknown> = {
    name: project.name,
    'action-bar': project.actionBar,
    prefix: project.prefix,
    group: project.group,
    mana: project.mana,
    'max-level': project.maxLevel,
    parent: project.parent,
    'needs-permission': String(project.needsPermission),
    attributes: project.attributes,
    'mana-regen': project.manaRegen,
    tree: project.tree,
    blacklist: project.blacklist,
    skills: project.skills,
    icon: project.icon,
    'icon-durability': project.iconDurability,
    'icon-data': project.iconData,
    'icon-lore': project.iconLore,
    'exp-source': project.expSources,
    ...project.source,
  };
  return dump({ [project.key]: section }, { noRefs: true, lineWidth: 120 });
}

export function createClass(key: string): ClassProject {
  return {
    source: {},
    key,
    name: key,
    prefix: `&6${key}`,
    actionBar: '',
    group: 'class',
    mana: 'Mana',
    parent: '',
    maxLevel: 40,
    expSources: 273,
    manaRegen: 1,
    needsPermission: false,
    tree: 'REQUIREMENT',
    blacklist: [],
    skills: [],
    attributes: {
      'health-base': '20',
      'health-scale': '1',
      'mana-base': '20',
      'mana-scale': '1',
    },
    icon: 'DIAMOND_SWORD',
    iconData: 0,
    iconDurability: 0,
    iconLore: ['&d{name}'],
  };
}
