import { readFileSync, writeFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import vm from 'node:vm';

/** Reuse the editor's reviewed Chinese captions while materializing them into every JSON field. */
function readCaptionMap(pattern) {
  const source = readFileSync(
    new URL('../src/features/skill-editor/model/fieldLabels.ts', import.meta.url),
    'utf8',
  );
  const literal = source.match(pattern)?.[1];
  if (!literal) throw new Error('无法读取中文字段词典');
  return vm.runInNewContext(`(${literal})`);
}
const fieldCaptions = readCaptionMap(/const labels[^=]+=[\s\S]*?(\{[\s\S]*?\n\});/);
const optionCaptions = readCaptionMap(/\(\s*(\{[\s\S]*?\n\s*\}) as Record<string, string>/);
const particleOptions = JSON.parse(
  readFileSync(
    new URL('../src/features/skill-editor/model/particle-options.zh-CN.json', import.meta.url),
    'utf8',
  ),
);

const words = {
  Block: '方块',
  Break: '破坏',
  Place: '放置',
  Cast: '施放',
  Crouch: '下蹲',
  Death: '死亡',
  Kill: '击杀',
  Land: '落地',
  Launch: '发射',
  Move: '移动',
  Physical: '物理',
  Skill: '技能',
  Damage: '伤害',
  Taken: '受到',
  Took: '受到',
  Dealt: '造成',
  Swap: '切换',
  Hand: '手',
  Area: '范围',
  Cone: '扇形',
  Linear: '直线',
  Location: '位置',
  Loc: '位置',
  Nearest: '最近',
  Offset: '偏移',
  Remember: '记忆',
  Forget: '清除记忆',
  Targets: '目标',
  Target: '目标',
  Self: '自身',
  Single: '单体',
  Armor: '盔甲',
  Stand: '架',
  Pose: '姿势',
  Attribute: '属性',
  Biome: '生物群系',
  Chance: '概率',
  Class: '职业',
  Level: '等级',
  Combat: '战斗',
  Data: '数据',
  Direction: '方向',
  Entity: '实体',
  Type: '类型',
  Fire: '火焰',
  Flag: '标记',
  Food: '饥饿度',
  Ground: '地面',
  Health: '生命值',
  Inventory: '背包',
  Item: '物品',
  Held: '手持',
  Remove: '移除',
  Light: '亮度',
  Mana: '法力',
  Mounted: '已骑乘',
  Mounting: '作为坐骑',
  Name: '名称',
  Permission: '权限',
  Potion: '药水',
  Status: '状态',
  Time: '时间',
  Tool: '工具',
  Water: '水中',
  Weather: '天气',
  Command: '命令',
  Delay: '延迟',
  Heal: '治疗',
  Particle: '粒子',
  Sound: '音效',
  Repeat: '重复',
  Speed: '速度',
  Push: '推动',
  Projectile: '投射物',
  Explosion: '爆炸',
  Message: '消息',
  Cooldown: '冷却',
  Value: '数值',
  Warp: '传送',
  Wolf: '狼',
  Cleanup: '清理',
  Environment: '环境',
  Initialize: '初始化',
  Interact: '交互',
  Ceiling: '头顶空间',
  Elevation: '高度差',
  Else: '否则',
  Offhand: '副手',
  Slot: '槽位',
  Buff: '增益',
  Cancel: '取消',
  Channel: '引导',
  Cleanse: '净化',
  Lore: '描述',
  Defense: '防御',
  Disguise: '伪装',
  Durability: '耐久',
  Clear: '清除',
  Toggle: '切换',
  Set: '设置',
  Immunity: '免疫',
  Interrupt: '中断',
  Lightning: '闪电',
  Animation: '动画',
  Effect: '效果',
  Passive: '被动',
  Purge: '驱散',
  Taunt: '嘲讽',
  Add: '相加',
  Copy: '复制',
  Distance: '距离',
  Multiply: '相乘',
  Placeholder: '占位符',
  Random: '随机',
  Edit: '修改',
  Snow: '雪',
  Storm: '风暴',
  Dragon: '末影龙',
  Start: '开始',
  Stop: '停止',
  Script: '脚本',
  Java: '脚本',
  JavaScript: '脚本',
  Kether: '凯瑟脚本',
  Mythic: '神话生物',
  Trigger: '触发',
};
function chineseName(name) {
  return (
    name
      .replace(/^(Trigger|Target|Condition|Mechanic)/, '')
      .replace(/([A-Z])/g, ' $1')
      .trim()
      .split(/\s+/)
      .map((word) => words[word] ?? word)
      .join('') || '技能节点'
  );
}
function chineseHelp(id, category) {
  const name = chineseName(id);
  return category === 'trigger'
    ? `监听${name}事件，事件发生后从该节点开始执行技能。`
    : category === 'target'
      ? `按照${name}规则重新选择目标，并把筛选结果交给后续节点。`
      : category === 'condition'
        ? `读取${name}状态并判断，结果从满足或不满足端口继续。`
        : `执行${name}效果，并将当前目标传递给后续节点。`;
}
function chineseOption(value, field, index) {
  const text = String(value);
  const direct = optionCaptions[text] ?? fieldCaptions[text];
  if (direct) return direct;
  const optionTokens = {
    stone: '石头',
    dirt: '泥土',
    grass: '草方块',
    wood: '木头',
    oak: '橡木',
    spruce: '云杉',
    birch: '桦木',
    jungle: '丛林木',
    acacia: '金合欢木',
    dark: '深色',
    door: '门',
    fence: '栅栏',
    stairs: '楼梯',
    slab: '台阶',
    glass: '玻璃',
    wool: '羊毛',
    sand: '沙子',
    gravel: '沙砾',
    water: '水',
    lava: '熔岩',
    fire: '火焰',
    cloud: '云雾',
    smoke: '烟雾',
    flame: '火焰',
    explosion: '爆炸',
    sound: '音效',
    music: '音乐',
    ambient: '环境',
    hurt: '受伤',
    death: '死亡',
    step: '脚步',
    break: '破坏',
    place: '放置',
    hit: '命中',
    shoot: '射击',
    throw: '投掷',
    use: '使用',
    open: '打开',
    close: '关闭',
    entity: '实体',
    player: '玩家',
    zombie: '僵尸',
    skeleton: '骷髅',
    creeper: '苦力怕',
    item: '物品',
    potion: '药水',
    arrow: '箭',
    bow: '弓',
    sword: '剑',
    axe: '斧',
    pickaxe: '镐',
    helmet: '头盔',
    chestplate: '胸甲',
    leggings: '护腿',
    boots: '靴子',
  };
  const translated = text
    .replace(/_/g, ' ')
    .split(/\s+/)
    .map((part) => optionTokens[part.toLowerCase()])
    .filter(Boolean);
  if (translated.length) return translated.join('');
  const category = /material|block/i.test(field.key)
    ? '方块或材质'
    : /item|tool|armor|hand/i.test(field.key)
      ? '物品'
      : /mob|entity/i.test(field.key)
        ? '实体类型'
        : /biome/i.test(field.key)
          ? '生物群系'
          : /particle|effect/i.test(field.key)
            ? '视觉效果'
            : /sound/i.test(field.key)
              ? '音效'
              : /potion|status/i.test(field.key)
                ? '状态效果'
                : /shape/i.test(field.key)
                  ? '形状'
                  : /direction|position|plane/i.test(field.key)
                    ? '方向'
                    : '可选值';
  if (/material|block/i.test(field.key)) return '其他材质';
  if (/sound/i.test(field.key)) return '其他音效';
  return `${category}${index + 1}`;
}
function chineseField(field, index) {
  const translated =
    fieldCaptions[field.label] ?? chineseName(field.label.replace(/[^A-Za-z]+/g, ''));
  return /[A-Za-z]/.test(translated) ? `参数${index + 1}` : translated;
}

/** Execute trusted repository constructors only; no imported user YAML is ever executable. */
export function extractLegacy() {
  const context = vm.createContext({});
  const run = (source, filename) => vm.runInContext(source, context, { filename, timeout: 5000 });
  const read = (name) => readFileSync(new URL(`../../editor/js/${name}`, import.meta.url), 'utf8');
  run('globalThis.window = globalThis;', 'environment');
  run(read('loader.js'), 'loader.js');
  run(
    `depend = (_, callback) => callback?.();
    globalThis.localStorage = { getItem: () => null };
    globalThis.loadSection = () => { throw new Error('DOM loading is not part of extraction'); };`,
    'adapters',
  );
  for (const name of ['input.js', 'data/1.12.js', 'data/data.js', 'component.js'])
    run(read(name), name);
  run('updateLoader();', 'inheritance');
  const result = run(
    `Object.values({ ...Trigger, ...Object.fromEntries(Object.entries(Target).map(([k,v])=>['target:'+k,v])), ...Object.fromEntries(Object.entries(Condition).map(([k,v])=>['condition:'+k,v])), ...Object.fromEntries(Object.entries(Mechanic).map(([k,v])=>['mechanic:'+k,v])) }).map(entry => {
    const component = new entry.construct();
    return {
      id: entry.construct.name, name: component.name, category: component.type,
      container: component.container, description: component.description,
      fields: component.data.map(field => ({
        kind: field.constructor.name, key: field.key, label: field.name,
        value: field.value ?? (field instanceof StringValue ? '' : undefined),
        base: field.base, scale: field.scale, index: field.index, values: field.values,
        options: typeof field.list === 'function' ? field.list() : field.list,
        requirements: field.requirements, tooltip: field.tooltip,
      })),
    };
  })`,
    'extract',
  );
  // Localization is attached after leaving the VM and stored in every JSON node object.
  return JSON.parse(JSON.stringify(result)).map((entry) => {
    const baseNameZh = chineseName(entry.id);
    const displayNameZh =
      entry.category === 'trigger'
        ? `${baseNameZh}时`
        : entry.category === 'target'
          ? `${baseNameZh}选取目标`
          : entry.category === 'condition'
            ? `检查${baseNameZh}`
            : baseNameZh;
    const descriptionZh = chineseHelp(entry.id, entry.category);
    const fields = entry.fields.map((field, fieldIndex) => {
      const labelZh = chineseField(field, fieldIndex);
      return {
        ...field,
        labelZh,
        tooltipZh: `配置“${labelZh}”，该值会按技能等级传入节点执行逻辑。`,
        optionsZh: (field.options ?? []).map((value, optionIndex) =>
          /particle(?:-type)?$/.test(field.key)
            ? (particleOptions.find((option) => option.value === value) ?? {
                value,
                label: chineseOption(value, field, optionIndex),
              })
            : { value, label: chineseOption(value, field, optionIndex) },
        ),
      };
    });
    return {
      ...entry,
      fields,
      displayNameZh,
      descriptionZh,
      helpZh: {
        label: displayNameZh,
        description: descriptionZh,
        behavior: descriptionZh,
        fields: fields.map((field) => ({ key: field.key, description: field.tooltipZh })),
        ports:
          entry.category === 'trigger'
            ? [{ id: 'flow', description: '事件触发后的执行出口' }]
            : entry.category === 'condition'
              ? [
                  { id: 'flow', description: '接收上一个节点的执行流程' },
                  { id: 'true', description: '条件满足时继续执行' },
                  { id: 'false', description: '条件不满足时继续执行' },
                ]
              : [{ id: 'flow', description: '接收或继续传递执行流程' }],
      },
    };
  });
}

if (process.argv[1] === fileURLToPath(import.meta.url)) {
  const data = extractLegacy();
  writeFileSync(
    new URL('../src/features/skill-editor/model/legacy.generated.json', import.meta.url),
    JSON.stringify(data, null, 2) + '\n',
  );
  console.log(
    `Extracted ${data.length} nodes and ${data.reduce((n, entry) => n + entry.fields.length, 0)} fields`,
  );
}
