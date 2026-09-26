import legacyOptions from './particle-options.zh-CN.json';
import availability from './particle-availability.json';
import { isOptionAvailable } from './versionOptions';
import { SERVER_VERSIONS } from '../../../shared/lib/preferences';
import type { ParticleAstValue } from './types';

type ParticleOption = { value: string; label: string; since?: string; until?: string };

const normalizeKey = (value: string): string =>
  value
    .trim()
    .toUpperCase()
    .replaceAll(/[\s-]+/g, '_');

/** Ordered aliases follow the server's SpigotParticles conversion table. */
const families: readonly (readonly string[])[] = [
  ['EXPLOSION_NORMAL', 'EXPLOSION', 'EXPLODE'],
  ['EXPLOSION_LARGE', 'EXPLOSION_HUGE', 'EXPLOSION_EMITTER', 'LARGE_EXPLODE', 'HUGE_EXPLOSION'],
  ['FIREWORKS_SPARK', 'FIREWORK', 'FIREWORK_SPARK'],
  ['WATER_BUBBLE', 'BUBBLE'],
  ['WATER_SPLASH', 'SPLASH'],
  ['WATER_WAKE', 'FISHING'],
  ['SUSPENDED', 'SUSPENDED_DEPTH', 'UNDERWATER', 'SUSPEND', 'DEPTH_SUSPEND'],
  ['CRIT_MAGIC', 'ENCHANTED_HIT', 'MAGIC_CRIT'],
  ['SMOKE_NORMAL', 'SMOKE'],
  ['SMOKE_LARGE', 'LARGE_SMOKE'],
  ['SPELL', 'EFFECT', 'POTION_BREAK'],
  ['SPELL_INSTANT', 'INSTANT_EFFECT', 'INSTANT_SPELL'],
  ['SPELL_MOB', 'SPELL_MOB_AMBIENT', 'ENTITY_EFFECT', 'MOB_SPELL', 'MOB_SPELL_AMBIENT'],
  ['SPELL_WITCH', 'WITCH', 'WITCH_MAGIC'],
  ['DRIP_WATER', 'DRIPPING_WATER'],
  ['DRIP_LAVA', 'DRIPPING_LAVA'],
  ['VILLAGER_ANGRY', 'ANGRY_VILLAGER'],
  ['VILLAGER_HAPPY', 'HAPPY_VILLAGER'],
  ['TOWN_AURA', 'MYCELIUM'],
  ['ENCHANTMENT_TABLE', 'ENCHANT'],
  ['REDSTONE', 'DUST', 'RED_DUST'],
  ['SNOWBALL', 'ITEM_SNOWBALL', 'SNOWBALL_POOF'],
  ['SNOW_SHOVEL', 'POOF'],
  ['SLIME', 'ITEM_SLIME'],
  ['ITEM_CRACK', 'ITEM', 'ICON_CRACK'],
  ['BLOCK_CRACK', 'BLOCK_DUST', 'BLOCK'],
  ['WATER_DROP', 'RAIN'],
  ['MOB_APPEARANCE', 'ELDER_GUARDIAN'],
  ['TOTEM', 'TOTEM_OF_UNDYING'],
  ['FOOTSTEP', 'CLOUD'],
  ['SPIT', 'LLAMA_SPIT'],
];

const legacyByKey = new Map(legacyOptions.map((option) => [normalizeKey(option.value), option]));

/**
 * 1.8 uses SkillAPI's editor keys; later servers accept Bukkit enum names.
 * The version ranges below are snapshots of org.bukkit.Particle in the matching Spigot API,
 * while the legacy names come from the server's ParticleType editor protocol.
 */
export const particleOptions: readonly ParticleOption[] = [
  ...legacyOptions.map((option) => ({ ...option, until: '1.8' })),
  ...availability.map((option) => ({
    ...option,
    label: `${legacyByKey.get(option.value)?.label ?? option.value.replaceAll('_', ' ')} · ${option.value}`,
  })),
];

/** Only fields carrying a particle enum use this shared catalog; other selects keep their own protocol. */
export function isParticleField(key: string): boolean {
  return key === 'particle' || key.endsWith('-particle-type');
}

/** Safe YAML keys avoid dotted version numbers being interpreted as nested config paths. */
export function particleVersionKey(version: string): string {
  return `v${version.replaceAll('.', '_')}`;
}

/** Convert an imported scalar or a new selection into the portable config AST. */
export function makeParticleAst(value: string): ParticleAstValue {
  return {
    kind: 'particle',
    value,
    versions: Object.fromEntries(
      SERVER_VERSIONS.flatMap((version) => {
        const resolved = normalizeParticle(value, version);
        return resolved ? [[particleVersionKey(version), resolved]] : [];
      }),
    ),
  };
}

/** Imported AST mappings remain authoritative so a round-trip does not erase custom variants. */
export function isParticleAst(value: unknown): value is ParticleAstValue {
  if (!value || typeof value !== 'object' || Array.isArray(value)) return false;
  const candidate = value as Record<string, unknown>;
  return (
    candidate.kind === 'particle' &&
    typeof candidate.value === 'string' &&
    candidate.versions !== null &&
    typeof candidate.versions === 'object' &&
    !Array.isArray(candidate.versions) &&
    Object.entries(candidate.versions).every(
      ([key, entry]) => /^v\d+_\d+$/.test(key) && typeof entry === 'string',
    )
  );
}

/** UI preview only; the server picks its own mapping from the persisted AST. */
export function particlePreview(value: ParticleAstValue, version: string): string | undefined {
  return value.versions[particleVersionKey(version)];
}

/**
 * Choose a wire value for one AST variant, not merely a display label. Ambiguous reverse
 * mappings stay absent so the server uses the white fallback instead of changing the effect.
 */
export function normalizeParticle(value: string, version: string): string | undefined {
  const key = normalizeKey(value);
  const family = families.find((names) => names.includes(key));
  if (version === '1.8') {
    const exact = legacyByKey.get(key);
    if (exact) return isOptionAvailable(exact, version) ? exact.value : undefined;
    const matches = legacyOptions.filter(
      (option) =>
        family?.includes(normalizeKey(option.value)) && isOptionAvailable(option, version),
    );
    return matches.length === 1 ? matches[0].value : undefined;
  }
  const available = (candidate: string) =>
    availability.some((option) => option.value === candidate && isOptionAvailable(option, version));
  if (available(key)) return key;
  if (!family) return undefined;
  const matches = family.filter(available);
  // Several old effects collapse into one modern enum. Reversing that collapse when the
  // target has multiple choices would silently change the visual effect, so require a choice.
  return matches.length === 1 ? matches[0] : undefined;
}
