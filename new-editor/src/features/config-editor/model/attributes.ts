import { isMap, isPair, isScalar, isSeq, Scalar } from 'yaml';
import type { YAMLMap } from 'yaml';
import type { ConfigDocument } from './configDocument';

/** One entry of attributes.yml. The map key is the in-game name players and configs use. */
export interface AttributeEntry {
  key: string;
  display: string;
  max: string;
  icon: string;
  iconData: string;
  iconLore: string[];
  /** stat-key to formula, where `a` is the current value and `v` the invested points. */
  stats: { stat: string; formula: string }[];
}

/**
 * stat-keys the plugin understands, taken from the reference table at the top of
 * attributes.yml. Free-form entry stays possible because addons can add their own.
 */
export const STAT_KEYS: readonly { value: string; group: string; label: string }[] = [
  { value: 'health', group: '基础', label: '最大生命值' },
  { value: 'mana', group: '基础', label: '法力值上限' },
  { value: 'mana-regen', group: '基础', label: '法力恢复速度' },
  { value: 'move-speed', group: '基础', label: '移动速度' },
  { value: 'exp', group: '基础', label: '经验获得加成' },
  { value: 'cooldown', group: '基础', label: '冷却缩减' },
  { value: 'physical-damage', group: '攻击', label: '普通攻击伤害加成' },
  { value: 'melee-damage', group: '攻击', label: '近战伤害加成' },
  { value: 'projectile-damage', group: '攻击', label: '远程伤害加成' },
  { value: 'skill-damage', group: '攻击', label: '技能伤害（通用）' },
  { value: 'control-power', group: '攻击', label: '控制强度' },
  { value: 'physical-defense', group: '防御', label: '物理防御' },
  { value: 'melee-defense', group: '防御', label: '近战减伤' },
  { value: 'projectile-defense', group: '防御', label: '远程减伤' },
  { value: 'skill-defense', group: '防御', label: '技能防御（通用）' },
  { value: 'natural-defense', group: '防御', label: '自然伤害防御' },
  { value: 'tenacity', group: '防御', label: '韧性' },
  { value: 'control-resistance', group: '防御', label: '控制抗性' },
  { value: 'crit-rate', group: '战斗扩展', label: '暴击率' },
  { value: 'crit-damage', group: '战斗扩展', label: '暴击伤害加成' },
  { value: 'dodge', group: '战斗扩展', label: '闪避率' },
  { value: 'lifesteal', group: '战斗扩展', label: '吸血率' },
  { value: 'armor-penetration', group: '战斗扩展', label: '百分比破甲' },
  { value: 'armor-penetration-flat', group: '战斗扩展', label: '固定穿透' },
  { value: 'threat-power', group: '战斗扩展', label: '仇恨强度' },
  { value: 'knockback-resist', group: '战斗扩展', label: '防击退' },
  { value: 'heal-power', group: '治疗', label: '治疗强度' },
  { value: 'heal-crit-rate', group: '治疗', label: '治疗暴击率' },
  { value: 'heal-crit-effect', group: '治疗', label: '治疗暴击效果' },
];

/** Formulas the reference table recommends, offered as a starting point. */
export const FORMULA_PRESETS: readonly { value: string; label: string }[] = [
  { value: 'a+v', label: 'a+v — 每点加 1（线性累加）' },
  { value: 'a*0.025+1*v', label: 'a*0.025+1*v — 每点加 2.5%' },
  { value: 'v/(a*0.01+1)', label: 'v/(a*0.01+1) — 百分比减伤' },
  { value: 'v/(a*0.005+1)', label: 'v/(a*0.005+1) — 百分比减伤（较缓）' },
];

function text(value: unknown, fallback = ''): string {
  return value === undefined || value === null ? fallback : String(value);
}

/** Reads the attribute list; every other key of the entry stays in the document untouched. */
export function readAttributes(document: ConfigDocument): AttributeEntry[] {
  if (!isMap(document.contents)) return [];
  const entries: AttributeEntry[] = [];
  for (const item of document.contents.items) {
    if (!isPair(item) || !isScalar(item.key) || !isMap(item.value)) continue;
    const key = String(item.key.value);
    const body = item.value as YAMLMap;
    const read = (name: string) => body.get(name, false);
    const lore = body.get('icon-lore', true);
    const stats = body.get('stats', true);
    entries.push({
      key,
      display: text(read('display'), key),
      max: text(read('max')),
      icon: text(read('icon')),
      iconData: text(read('icon-data')),
      iconLore: isSeq(lore)
        ? lore.items.map((entry) => text(isScalar(entry) ? entry.value : ''))
        : [],
      stats: isMap(stats)
        ? stats.items
            .filter(isPair)
            .filter((pair) => isScalar(pair.key))
            .map((pair) => ({
              stat: String((pair.key as Scalar).value),
              formula: text(isScalar(pair.value) ? pair.value.value : ''),
            }))
        : [],
    });
  }
  return entries;
}

function quoted(value: string): Scalar {
  const scalar = new Scalar(value);
  scalar.type = 'QUOTE_SINGLE';
  return scalar;
}

/**
 * Writes one entry back in place. Only the modelled keys are touched, so `global` blocks and
 * anything else an addon put on the entry survive, as do the file's reference comments.
 */
export function writeAttribute(document: ConfigDocument, entry: AttributeEntry): void {
  const body = document.getIn([entry.key], true);
  if (!isMap(body)) return;
  const set = (name: string, value: string) => {
    // Absent optional keys stay absent rather than being written as empty strings.
    if (value === '' && body.get(name, false) === undefined) return;
    const existing = body.get(name, true);
    if (isScalar(existing)) existing.value = value;
    else body.set(name, quoted(value));
  };
  set('display', entry.display);
  set('max', entry.max);
  set('icon', entry.icon);
  set('icon-data', entry.iconData);
  if (entry.iconLore.length || body.get('icon-lore', false) !== undefined)
    document.setIn([entry.key, 'icon-lore'], document.createNode(entry.iconLore.map(quoted)));
  document.setIn(
    [entry.key, 'stats'],
    document.createNode(
      Object.fromEntries(entry.stats.filter((s) => s.stat).map((s) => [s.stat, quoted(s.formula)])),
    ),
  );
}

/** Renaming the map key must keep the entry's position and comments in the file. */
export function renameAttribute(document: ConfigDocument, from: string, to: string): void {
  if (!isMap(document.contents) || from === to) return;
  for (const item of document.contents.items) {
    if (isPair(item) && isScalar(item.key) && String(item.key.value) === from) {
      item.key.value = to;
      return;
    }
  }
}

export function addAttribute(document: ConfigDocument, key: string): void {
  document.setIn([key], document.createNode({ display: quoted(key), stats: {} }));
}

export function removeAttribute(document: ConfigDocument, key: string): void {
  document.deleteIn([key]);
}
