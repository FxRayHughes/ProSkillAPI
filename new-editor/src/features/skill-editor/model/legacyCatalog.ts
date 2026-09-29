import { nodeRegistry } from './registry';
import catalog from './legacy.generated.json';
import generated from '../../generated/nodes.generated.json';
import { convertLegacyFields } from './legacyFields';
import type { LegacyField } from './legacyFields';
import type { FieldDefinition, NodeDefinition, PortDefinition } from './types';

/**
 * These generated mechanics call ParticleHelper with the shared `particle` key but their
 * annotations describe only geometry. Supply the selector until the source catalog exports
 * inherited particle settings; existing legacy and built-in definitions already have it.
 */
const SHARED_PARTICLE_MECHANICS = new Set([
  'MechanicParticleLine',
  'MechanicParticleChain',
  'MechanicParticleSineWave',
  'MechanicParticleSphere',
]);

const sharedParticleField: LegacyField = {
  kind: 'ListValue',
  key: 'particle',
  label: 'Particle',
  labelZh: '粒子类型',
  tooltipZh: '选择粒子类型；保存为跨服务端版本的粒子 AST。',
  value: 'Flame',
};

/**
 * Older extracted catalogs predate the true-damage shield policy. Keep the
 * field in the editor even when the legacy entry has not been regenerated;
 * the server already treats a missing value as the historical bypass default.
 */
const TRUE_DAMAGE_SHIELD_FIELD: LegacyField = {
  kind: 'BooleanValue',
  key: 'ignore-shield',
  label: 'Ignore Shield',
  labelZh: '忽略护盾',
  tooltipZh: '仅真实伤害生效。默认忽略护盾；取消勾选后由多层护盾吸收。旧配置保持原行为。',
  value: 'True',
};

/** Shape shared by the extracted legacy catalog and the built-in extension catalog. */
export interface LegacyCatalogEntry {
  id: string;
  name: string;
  category: string;
  container: boolean;
  /** 英文说明，注解目录必有；旧抽取目录也带这一项。 */
  description?: string;
  fields: LegacyField[];
  /**
   * 中文名与说明。注解生成的目录里，尚未补译的节点会缺这两项，
   * 因此是可选的——渲染时回退到英文，而不是显示 undefined。
   */
  displayNameZh?: string;
  descriptionZh?: string;
  group?: string;
  /** 端口说明。注解目前不表达端口语义，生成的目录没有这一项。 */
  helpZh?: { ports: { id: string; description: string }[] };
  /**
   * 生效前提，由构建期的 exportNodes 从 @SkillNode 注解导出。
   * 旧的抽取式目录没有这一项，因此是可选的。
   */
  requires?: {
    plugins?: string[];
    nodes?: string[];
    capabilities?: string[];
  };
}

/** Each legacy entry becomes an independent node object with structured help. */
export function createLegacyNode(entry: LegacyCatalogEntry): NodeDefinition {
  const kind: NodeDefinition['kind'] = entry.id.startsWith('Trigger')
    ? 'entry'
    : entry.id.startsWith('Condition')
      ? 'condition'
      : 'action';
  const sourceFields = [...entry.fields];
  if (
    SHARED_PARTICLE_MECHANICS.has(entry.id) &&
    !sourceFields.some((field) => field.key === 'particle')
  ) {
    sourceFields.push(sharedParticleField);
  }
  // The extracted legacy JSON can be older than the Java annotation catalog.
  // Inject the policy field here so both damage nodes expose the same control.
  if (
    ['Damage', 'Damage Lore'].includes(entry.name) &&
    !sourceFields.some((field) => field.key === 'ignore-shield')
  ) {
    sourceFields.splice(
      Math.max(0, sourceFields.findIndex((field) => field.key === 'classifier')),
      0,
      TRUE_DAMAGE_SHIELD_FIELD,
    );
  }
  const fields: FieldDefinition[] = convertLegacyFields(sourceFields).map((field) =>
    // The shield switch has no effect on ordinary Bukkit damage; hide it until
    // true damage is selected while retaining an imported value in the AST.
    field.key === 'ignore-shield' && ['Damage', 'Damage Lore'].includes(entry.name)
      ? { ...field, requirements: [{ key: 'true', values: ['True'] }] }
      : field,
  );
  // GROUP is a root declaration with a callable input. Incoming wires are
  // serialized as goto references while its body remains a single v1 subtree.
  const inputs: PortDefinition[] =
    entry.name === 'GROUP' ? [{ id: 'invoke', label: '调用' }] : kind === 'entry' ? [] : [{ id: 'flow', label: '执行' }];
  const outputs: PortDefinition[] =
    kind === 'condition'
      ? [
          { id: 'true', label: '满足' },
          { id: 'false', label: '不满足' },
        ]
      : [{ id: 'flow', label: '执行' }];
  // 尚未补译的节点回退到英文，而不是显示空白
  const label = canonicalNodeLabel(entry);
  const description = detailedDescription(entry, kind);
  return {
    id: entry.id,
    label,
    kind,
    description,
    color:
      kind === 'entry'
        ? '#df6d77'
        : kind === 'condition'
          ? '#7da7eb'
          : entry.id.startsWith('Target')
            ? '#58b779'
            : '#e5b35d',
    group: entry.group ?? defaultGroup(entry, kind),
    inputs,
    outputs,
    fields,
    help: {
      label,
      description,
      behavior: description,
      fields: fields.map((field) => ({
        key: field.key,
        description: field.tooltip ?? `配置${field.label}。`,
      })),
      ports: entry.helpZh?.ports ?? [],
      ...(entry.requires ? { requires: entry.requires } : {}),
    },
    legacy: { name: entry.name, category: entry.category, container: entry.container },
  };
}

/** Keep the palette and help panel on one vocabulary and one explanation model. */
function defaultGroup(entry: LegacyCatalogEntry, kind: NodeDefinition['kind']): string {
  const name = `${entry.name} ${entry.id}`.toLowerCase();
  if (entry.name === 'GROUP') return '共享编排';
  if (kind === 'entry') {
    if (/signal|shield|skill cast|level|cooldown|heal|flag|xp/.test(name)) return '技能与信号阶段';
    if (/projectile|fish|block|mob|totem|riptide/.test(name)) return '世界与实体阶段';
    return '玩家与战斗入口';
  }
  if (entry.id.startsWith('Target')) {
    if (/signal|world|event|impact/.test(name)) return '事件与世界目标';
    return '实体与位置目标';
  }
  if (kind === 'condition') {
    if (/number|text|value|signal|data/.test(name)) return '数值与上下文条件';
    if (/world|distance|facing|ground|moon|oxygen/.test(name)) return '空间与环境条件';
    if (/shield|economy|mythic|item/.test(name)) return '资源与集成条件';
    return '实体与状态条件';
  }
  if (/value|data|experience|air|economy|shield/.test(name)) return '数值与资源动作';
  if (/signal|skill invoke|run group|flow|throttle|terminate/.test(name)) return '信号与流程动作';
  if (/particle|kether|javascript|script|snow storm|image/.test(name)) return '粒子与脚本动作';
  if (/summon|mount|armor stand|block|item|flight|armor/.test(name)) return '实体与世界动作';
  return '战斗与效果动作';
}

function detailedDescription(entry: LegacyCatalogEntry, kind: NodeDefinition['kind']): string {
  const supplied = entry.descriptionZh ?? entry.description ?? '';
  const group = entry.group ?? defaultGroup(entry, kind);
  const behavior = (supplied.trim() || `${canonicalNodeLabel(entry)} 节点属于“${group}”。`)
    .replaceAll('凯瑟脚本', 'Kether 脚本')
    .replaceAll('雪暴粒子', '暴雪粒子');
  return `${behavior} 运行时先读取节点上下文和字段值，再按目标过滤、数值校验与服务端能力检查执行；事件节点只在对应阶段收到事件时运行，条件节点只把满足条件的目标传给“满足”分支，动作节点在当前目标上提交一次明确的效果。缺少必需上下文、目标或可选依赖时返回未执行，并保留原始配置供诊断。编辑器中的字段只改变本节点行为，不会隐式修改相邻节点。`;
}

/** Product terms are stable UI vocabulary; legacy constructor names remain wire-compatible. */
function canonicalNodeLabel(entry: LegacyCatalogEntry): string {
  if (entry.name.toLowerCase() === 'snow storm') return '暴雪粒子';
  if (entry.name.toLowerCase() === 'kether') return 'Kether 脚本';
  if (entry.name.toLowerCase() === 'javascript') return 'JavaScript 脚本';
  if (entry.name.toLowerCase() === 'value script') return '数值脚本';
  return entry.displayNameZh ?? entry.name;
}

/** Registers objects while preserving English IDs as the persistence protocol. */
export function registerLegacyCatalog(): void {
  for (const entry of catalog as LegacyCatalogEntry[]) {
    const generatedDefinition = nodeRegistry.get(entry.id);
    const definition = createLegacyNode(entry);
    if (generatedDefinition) {
      // Annotation exports omit inherited inputs; retain the complete editor contract and
      // keep the generated runtime requirements that legacy extraction cannot express.
      nodeRegistry.override({
        ...definition,
        help: definition.help && {
          ...definition.help,
          ...(generatedDefinition.help?.requires
            ? { requires: generatedDefinition.help.requires }
            : {}),
        },
      });
    } else nodeRegistry.register(definition);
  }
}

/**
 * 注册由插件源码注解生成的节点目录。
 *
 * 这份目录来自构建期的 exportNodes 任务，提供新节点与生效前提。
 * 必须先于旧目录注册；旧目录随后补齐注解未导出的继承字段，保留生效前提。
 */
export function registerGeneratedCatalog(): void {
  for (const entry of generated as LegacyCatalogEntry[])
    if (!nodeRegistry.get(entry.id)) nodeRegistry.register(createLegacyNode(entry));
}
