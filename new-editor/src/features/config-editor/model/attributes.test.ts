import { describe, expect, it } from 'vitest';
import { readFileSync } from 'node:fs';
import { commentInventory, parseConfig, serializeConfig } from './configDocument';
import {
  addAttribute,
  readAttributes,
  removeAttribute,
  renameAttribute,
  writeAttribute,
} from './attributes';

function fixture(): string {
  return readFileSync(new URL('./__fixtures__/attributes.yml', import.meta.url), 'utf8');
}

describe('attributes.yml editing', () => {
  it('reads every attribute with its stat formulas', () => {
    const entries = readAttributes(parseConfig(fixture()));
    expect(entries).toHaveLength(28);
    const defense = entries.find((entry) => entry.key === '防御力')!;
    expect(defense.display).toBe('防御力');
    expect(defense.max).toBe('999999');
    expect(defense.iconLore).toEqual(['&6减少所受到的伤害']);
    expect(defense.stats).toContainEqual({
      stat: 'physical-defense',
      formula: 'v/(a*0.005+1)',
    });
  });

  it('keeps the reference comments and untouched keys when an entry is edited', () => {
    const source = fixture();
    const document = parseConfig(source);
    const before = commentInventory(document);
    const entry = readAttributes(document).find((item) => item.key === '生命值')!;
    writeAttribute(document, {
      ...entry,
      max: '40',
      stats: [{ stat: 'health', formula: 'a+2*v' }],
    });
    const written = serializeConfig(document);
    expect(commentInventory(parseConfig(written))).toEqual(before);
    // The `global` block is not modelled by the form and must survive verbatim.
    expect(parseConfig(written).getIn(['生命值', 'global', 'condition'])).toBe(' {}');
    const reread = readAttributes(parseConfig(written)).find((item) => item.key === '生命值')!;
    expect(reread.max).toBe('40');
    expect(reread.stats).toEqual([{ stat: 'health', formula: 'a+2*v' }]);
  });

  it('adds, renames and removes entries without disturbing the others', () => {
    const document = parseConfig(fixture());
    addAttribute(document, '测试属性');
    renameAttribute(document, '暴击', '暴击率');
    removeAttribute(document, '闪避');
    const keys = readAttributes(parseConfig(serializeConfig(document))).map((item) => item.key);
    expect(keys).toContain('测试属性');
    expect(keys).toContain('暴击率');
    expect(keys).not.toContain('暴击');
    expect(keys).not.toContain('闪避');
    expect(keys).toHaveLength(28);
  });

  it('renaming keeps the entry in place rather than moving it to the end', () => {
    const document = parseConfig(fixture());
    const index = readAttributes(document).findIndex((item) => item.key === '暴击');
    renameAttribute(document, '暴击', '暴击率');
    expect(readAttributes(document).findIndex((item) => item.key === '暴击率')).toBe(index);
  });
});
