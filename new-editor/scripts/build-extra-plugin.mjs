import { readFileSync, writeFileSync, mkdirSync } from 'node:fs';
import { fileURLToPath } from 'node:url';

/**
 * Builds the built-in extension catalog for components that ship outside the legacy
 * editor's own component.js. Entries use the exact shape of legacy.generated.json so
 * the runtime converts them with the same createLegacyNode/convertLegacyFields code.
 */
const catalogUrl = new URL(
  '../src/features/skill-editor/model/legacy.generated.json',
  import.meta.url,
);
const outputUrl = new URL(
  '../src/features/plugins/builtin/extra-nodes.generated.json',
  import.meta.url,
);

const catalog = JSON.parse(readFileSync(catalogUrl, 'utf8'));

/** Reuse reviewed field definitions (including the 49 particle and 463 material options). */
function borrow(entryId, key, overrides = {}) {
  const entry = catalog.find((item) => item.id === entryId);
  if (!entry) throw new Error(`缺少参考节点：${entryId}`);
  const field = entry.fields.find((item) => item.key === key);
  if (!field) throw new Error(`缺少参考字段：${entryId}.${key}`);
  return { ...structuredClone(field), ...overrides };
}

/** Keys introduced by this repository's Java mechanics need their own captions. */
const captions = {
  'ring-radius': '圆环半径',
  'particle-count': '粒子数量',
  rotation: '起始旋转角度',
  'height-offset': '高度偏移',
  plane: '所在平面',
  sides: '边数',
  'polygon-radius': '外接圆半径',
  density: '每格粒子密度',
  'start-forward': '起点前后偏移',
  'start-upward': '起点上下偏移',
  'start-right': '起点左右偏移',
  'end-forward': '终点前后偏移',
  'end-upward': '终点上下偏移',
  'end-right': '终点左右偏移',
  distance: '冲刺距离',
};

function caption(key) {
  const label = captions[key];
  if (!label) throw new Error(`缺少中文标题：${key}`);
  return label;
}

function attr(key, base, scale = 0, tooltip = '') {
  return {
    kind: 'AttributeValue',
    key,
    label: key,
    base,
    scale,
    tooltip,
    labelZh: caption(key),
    tooltipZh: tooltip || `配置“${caption(key)}”，该值会按技能等级换算。`,
    optionsZh: [],
  };
}

function double(key, value, tooltip = '') {
  return {
    kind: 'DoubleValue',
    key,
    label: key,
    value,
    tooltip,
    labelZh: caption(key),
    tooltipZh: tooltip || `配置“${caption(key)}”。`,
    optionsZh: [],
  };
}

/** ParticleGeometry.radial only accepts these three world-aligned planes. */
function plane() {
  return {
    kind: 'ListValue',
    key: 'plane',
    label: 'plane',
    value: 'XZ',
    options: ['XZ', 'XY', 'YZ'],
    tooltip: '[plane] 图形所在的世界坐标平面',
    labelZh: caption('plane'),
    tooltipZh: '选择图形所在的世界坐标平面，XZ 为水平面。',
    optionsZh: [
      { value: 'XZ', label: 'XZ 水平面' },
      { value: 'XY', label: 'XY 竖直面' },
      { value: 'YZ', label: 'YZ 竖直面' },
    ],
  };
}

/** ParticleHelper.play reads these keys directly for every outline mechanic. */
const particleAppearance = () => [
  borrow('MechanicParticle', 'particle'),
  borrow('MechanicParticle', 'material'),
  borrow('MechanicParticle', 'type'),
  borrow('MechanicParticle', 'visible-radius'),
  borrow('MechanicParticle', 'dx'),
  borrow('MechanicParticle', 'dy'),
  borrow('MechanicParticle', 'dz'),
  borrow('MechanicParticle', 'amount'),
  borrow('MechanicParticle', 'speed'),
];

const iconKey = () => borrow('MechanicParticle', 'icon-key');
const counts = () => borrow('MechanicParticle', 'counts');

const entries = [
  {
    id: 'MechanicParticleRing',
    name: 'Particle Ring',
    category: 'mechanic',
    container: false,
    descriptionZh: '沿圆周均匀绘制粒子轮廓，圆心取自当前目标。',
    fields: [
      iconKey(),
      counts(),
      attr('ring-radius', 1, 0, '[ring-radius] 圆环半径，单位为格'),
      attr('particle-count', 32, 0, '[particle-count] 圆周上的采样点数量'),
      double('rotation', 0, '[rotation] 起始角度，单位为度'),
      double('height-offset', 0, '[height-offset] 相对目标脚下的高度偏移'),
      plane(),
      ...particleAppearance(),
    ],
  },
  {
    id: 'MechanicParticlePolygon',
    name: 'Particle Polygon',
    category: 'mechanic',
    container: false,
    descriptionZh: '绘制正多边形轮廓，边数与半径可随技能等级成长。',
    fields: [
      iconKey(),
      counts(),
      attr('sides', 3, 0, '[sides] 多边形边数，至少为 3'),
      attr('polygon-radius', 1, 0, '[polygon-radius] 外接圆半径，单位为格'),
      attr('density', 10, 0, '[density] 每格长度上的粒子数量'),
      double('rotation', 0, '[rotation] 起始角度，单位为度'),
      double('height-offset', 0, '[height-offset] 相对目标脚下的高度偏移'),
      plane(),
      ...particleAppearance(),
    ],
  },
  {
    id: 'MechanicParticlePointLine',
    name: 'Particle Point Line',
    category: 'mechanic',
    container: false,
    descriptionZh: '在目标朝向坐标系中，于起点与终点之间绘制一条粒子线段。',
    fields: [
      iconKey(),
      counts(),
      attr('density', 10, 0, '[density] 每格长度上的粒子数量'),
      double('start-forward', 0, '[start-forward] 起点沿目标朝向的偏移'),
      double('start-upward', 0, '[start-upward] 起点的垂直偏移'),
      double('start-right', 0, '[start-right] 起点沿目标右手方向的偏移'),
      double('end-forward', 0, '[end-forward] 终点沿目标朝向的偏移'),
      double('end-upward', 0, '[end-upward] 终点的垂直偏移'),
      double('end-right', 0, '[end-right] 终点沿目标右手方向的偏移'),
      ...particleAppearance(),
    ],
  },
  {
    id: 'MechanicDash',
    name: 'Dash',
    category: 'mechanic',
    container: false,
    descriptionZh: '使目标向前冲刺一段距离。本仓库没有对应的服务端实现，字段由现有技能配置反推。',
    fields: [iconKey(), counts(), attr('distance', 10, 0, '[distance] 冲刺距离，单位为格')],
  },
  {
    id: 'TargetThreatLowest',
    name: 'Threat Lowest',
    category: 'target',
    container: true,
    descriptionZh: '选取仇恨值最低的目标。本仓库没有对应的服务端实现，字段由现有技能配置反推。',
    fields: [
      iconKey(),
      borrow('TargetNearest', 'group'),
      borrow('TargetNearest', 'wall'),
      borrow('TargetNearest', 'caster'),
    ],
  },
];

/** Display names follow the legacy catalog's per-category phrasing. */
const displayNames = {
  MechanicParticleRing: '粒子圆环',
  MechanicParticlePolygon: '粒子多边形',
  MechanicParticlePointLine: '粒子连线',
  MechanicDash: '冲刺',
  TargetThreatLowest: '最低仇恨选取目标',
};

const nodes = entries.map((entry) => {
  const displayNameZh = displayNames[entry.id];
  if (!displayNameZh) throw new Error(`缺少中文节点名：${entry.id}`);
  return {
    id: entry.id,
    name: entry.name,
    category: entry.category,
    container: entry.container,
    description: entry.descriptionZh,
    fields: entry.fields,
    displayNameZh,
    descriptionZh: entry.descriptionZh,
    helpZh: {
      label: displayNameZh,
      description: entry.descriptionZh,
      behavior: entry.descriptionZh,
      fields: entry.fields.map((field) => ({
        key: field.key,
        description: field.tooltipZh ?? `配置“${field.labelZh ?? field.key}”。`,
      })),
      ports: [{ id: 'flow', description: '接收或继续传递执行流程' }],
    },
  };
});

mkdirSync(new URL('.', outputUrl), { recursive: true });
writeFileSync(outputUrl, JSON.stringify(nodes, null, 2) + '\n');
if (process.argv[1] === fileURLToPath(import.meta.url)) {
  console.log(
    `Extracted ${nodes.length} extension nodes and ${nodes.reduce((n, entry) => n + entry.fields.length, 0)} fields`,
  );
}
