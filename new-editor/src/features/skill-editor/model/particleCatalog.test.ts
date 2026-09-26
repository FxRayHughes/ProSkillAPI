import { describe, expect, it } from 'vitest';
import { makeParticleAst, normalizeParticle, particleOptions } from './particleCatalog';
import { compareVersions, isOptionAvailable } from './versionOptions';
import { SERVER_VERSIONS } from '../../../shared/lib/preferences';

describe('versioned particle catalog', () => {
  it('compares dotted versions numerically, including 1.20 and 26.2', () => {
    expect(compareVersions('1.20', '1.9')).toBeGreaterThan(0);
    expect(compareVersions('1.21', '1.20')).toBeGreaterThan(0);
    expect(compareVersions('26.2', '26.1')).toBeGreaterThan(0);
  });

  it('exposes legacy keys only on 1.8 and modern Bukkit names on their API versions', () => {
    const values = (version: string) =>
      particleOptions
        .filter((option) => isOptionAvailable(option, version))
        .map((option) => option.value);
    expect(values('1.8')).toContain('Flame');
    expect(values('1.8')).not.toContain('FLAME');
    expect(values('1.20')).toContain('FLAME');
    expect(values('1.20')).toContain('DUST');
    expect(values('1.20')).not.toContain('REDSTONE');
    expect(values('26.2')).toContain('FLAME');
  });

  it('maps known renamed values but refuses unsupported or ambiguous conversions', () => {
    expect(normalizeParticle('Redstone', '1.20')).toBe('DUST');
    expect(normalizeParticle('Block Crack', '26.2')).toBe('BLOCK');
    expect(normalizeParticle('SPELL', '26.2')).toBe('EFFECT');
    expect(normalizeParticle('DUST', '1.8')).toBe('Redstone');
    expect(normalizeParticle('EXPLOSION_EMITTER', '1.8')).toBeUndefined();
    expect(normalizeParticle('EXPLOSION_EMITTER', '1.12')).toBeUndefined();
    expect(normalizeParticle('BLOCK', '1.12')).toBeUndefined();
    expect(normalizeParticle('Dragon Breath', '1.8')).toBeUndefined();
    expect(normalizeParticle('UNKNOWN_PARTICLE', '1.21')).toBeUndefined();
  });

  it('writes a version variant for every option shown in the editor', () => {
    for (const version of SERVER_VERSIONS) {
      const shown = particleOptions.filter((option) => isOptionAvailable(option, version));
      expect(shown.length).toBeGreaterThan(0);
      for (const option of shown)
        expect(makeParticleAst(option.value).versions[`v${version.replace('.', '_')}`]).toBe(
          option.value,
        );
    }
  });

  it('keeps the white CLOUD fallback available on every supported server', () => {
    const cloud = makeParticleAst('Cloud');
    for (const version of SERVER_VERSIONS)
      expect(cloud.versions[`v${version.replace('.', '_')}`], version).toBeTruthy();
  });
});
