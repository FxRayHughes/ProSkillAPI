import { describe, expect, it } from 'vitest';
import { readFileSync } from 'node:fs';
import {
  commentInventory,
  parseConfig,
  readSections,
  serializeConfig,
  setValue,
} from './configDocument';
import type { ConfigSection } from './configDocument';

const FILES = [
  'config.yml',
  'attributes.yml',
  'groups.yml',
  'exp.yml',
  'language.yml',
  'worldGuard.yml',
  'gui.yml',
  'tool.yml',
  'commands.yml',
  'set-bonus.yml',
];

function fixture(name: string): string {
  return readFileSync(new URL(`./__fixtures__/${name}`, import.meta.url), 'utf8');
}
function find(section: ConfigSection, key: string): ConfigSection['fields'][number] | undefined {
  return (
    section.fields.find((field) => field.key === key) ??
    section.sections.map((child) => find(child, key)).find(Boolean)
  );
}
function countFields(section: ConfigSection): number {
  return section.fields.length + section.sections.reduce((n, s) => n + countFields(s), 0);
}
describe('config document', () => {
  for (const name of FILES) {
    it(`${name} keeps every comment and value through a rewrite`, () => {
      const source = fixture(name);
      const document = parseConfig(source);
      const written = serializeConfig(document);
      const reread = parseConfig(written);
      // Inline comments may move onto their own line, so compare content, not placement.
      expect(commentInventory(reread)).toEqual(commentInventory(document));
      expect(reread.toJS()).toEqual(document.toJS());
      // Every comment line of the original must appear in the output. The count can grow,
      // because an inline comment is re-emitted on its own line.
      const hashLines = (text: string) =>
        text
          .split('\n')
          .map((line) => line.trim())
          .filter((line) => line.startsWith('#'));
      const remaining = hashLines(written);
      for (const line of hashLines(source)) {
        const at = remaining.indexOf(line);
        expect(at, `注释丢失：${line}`).toBeGreaterThanOrEqual(0);
        remaining.splice(at, 1);
      }
    });

    it(`${name} inventory covers every comment, so the round-trip check is not vacuous`, () => {
      const source = fixture(name);
      const inventory = commentInventory(parseConfig(source));
      const hashLines = source.split('\n').filter((line) => line.trim().startsWith('#')).length;
      expect(inventory.length).toBeGreaterThanOrEqual(hashLines);
    });

    it(`${name} exposes editable fields`, () => {
      const tree = readSections(parseConfig(fixture(name)));
      expect(countFields(tree)).toBeGreaterThan(0);
    });
  }

  it('classifies quoted booleans and numbers written by the plugin', () => {
    const tree = readSections(parseConfig(fixture('config.yml')));
    expect(find(tree, 'auto-save')).toMatchObject({ kind: 'boolean', value: false });
    expect(find(tree, 'minutes')).toMatchObject({ kind: 'number', value: 30 });
    expect(find(tree, 'main-class-group')).toMatchObject({ kind: 'text', value: 'class' });
    expect(find(tree, 'perm-accounts')).toMatchObject({
      kind: 'string-list',
      value: ['skillapi.account.admin:10'],
    });
  });

  it('surfaces the YAML comment as the field description', () => {
    const tree = readSections(parseConfig(fixture('config.yml')));
    expect(find(tree, 'minutes')?.description).toContain('自动保存间隔');
  });

  it('writes values back as quoted strings the way the plugin reads them', () => {
    const document = parseConfig(fixture('config.yml'));
    const tree = readSections(document);
    setValue(document, find(tree, 'auto-save')!, true);
    setValue(document, find(tree, 'minutes')!, 45);
    setValue(document, find(tree, 'perm-accounts')!, ['a:1', 'b:2']);
    const written = serializeConfig(document);
    expect(written).toMatch(/auto-save: 'true'/);
    expect(written).toMatch(/minutes: '45'/);
    expect(written).toContain("- 'a:1'");
    expect(written).toContain('自动保存间隔');
    // Re-reading must produce the values just written.
    const reread = readSections(parseConfig(written));
    expect(find(reread, 'auto-save')?.value).toBe(true);
    expect(find(reread, 'minutes')?.value).toBe(45);
  });

  it('reads the attribute definitions that drive class and skill forms', () => {
    const tree = readSections(parseConfig(fixture('attributes.yml')));
    const names = tree.sections.map((section) => section.key);
    expect(names).toContain('暴击');
    expect(names).toContain('防御力');
    const crit = tree.sections.find((section) => section.key === '暴击')!;
    expect(find(crit, 'display')?.value).toBe('暴击');
    expect(find(crit, 'crit-rate')?.value).toBe('a+v');
  });

  it('rejects malformed YAML instead of writing a damaged file', () => {
    expect(() => parseConfig('a:\n  - [unclosed\n')).toThrow('YAML 解析失败');
  });
});
