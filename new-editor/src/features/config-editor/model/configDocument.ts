import { isMap, isPair, isScalar, isSeq, parseDocument, Scalar as ScalarNode, visit } from 'yaml';
import type { Document, Node, Scalar, YAMLMap, YAMLSeq } from 'yaml';
import { isParticleAst, makeParticleAst } from '../../skill-editor/model/particleCatalog';
import type { ParticleAstValue } from '../../skill-editor/model/types';

/**
 * Config files are hand-maintained and their comments are the documentation, so edits go
 * through the YAML AST instead of a parse/dump cycle that would delete every comment.
 */
export type ConfigDocument = Document.Parsed;

export type ConfigValueKind = 'boolean' | 'number' | 'text' | 'string-list' | 'particle';
export type ConfigValue = boolean | number | string | string[] | ParticleAstValue;

/** The only particle enum in the generic config form is read by IndicatorSettings. */
const INDICATOR_PARTICLE_PATH = ['Casting', 'cast-indicator', 'particle', 'particle'];

export interface ConfigField {
  path: (string | number)[];
  key: string;
  kind: ConfigValueKind;
  value: ConfigValue;
  /** The YAML comments attached to this entry, shown as the field's help text. */
  description: string;
}

export interface ConfigSection {
  path: (string | number)[];
  key: string;
  description: string;
  fields: ConfigField[];
  sections: ConfigSection[];
}

export function parseConfig(source: string): ConfigDocument {
  const document = parseDocument(source);
  if (document.errors.length) throw new Error(`YAML 解析失败：${document.errors[0].message}`);
  return document;
}

/**
 * Sequence indentation and long-line folding match the plugin's files. On save, the
 * casting indicator's legacy particle scalar is upgraded to the shared AST protocol.
 */
export function serializeConfig(document: ConfigDocument): string {
  const indicator = document.getIn(INDICATOR_PARTICLE_PATH, true);
  if (isScalar(indicator) && typeof indicator.value === 'string') {
    // Any config save migrates this remaining legacy scalar, so files edited outside the
    // particle control still use the same portable format as skill components.
    const value = makeParticleAst(indicator.value);
    setValue(
      document,
      {
        path: INDICATOR_PARTICLE_PATH,
        key: 'particle',
        kind: 'particle',
        value,
        description: '',
      },
      value,
    );
  }
  return document.toString({ indentSeq: false, lineWidth: 0 });
}

/**
 * Every comment in the document, normalized and sorted. Comments are the documentation in
 * these hand-maintained files, so callers use this to prove an edit did not drop any.
 * Inline comments may be re-emitted on their own line, which changes order but not content.
 */
export function commentInventory(document: ConfigDocument): string[] {
  const found: string[] = [];
  const collect = (node: { commentBefore?: string | null; comment?: string | null }) => {
    // Blank separator lines inside a block are content too, so they are kept.
    for (const entry of [node.commentBefore, node.comment])
      if (entry !== undefined && entry !== null)
        for (const line of entry.split('\n')) found.push(line.trim());
  };
  collect(document);
  // visit reaches pair keys through the Scalar visitor, so there is no Pair handler here.
  visit(document, {
    Map: (_, node) => collect(node),
    Seq: (_, node) => collect(node),
    Scalar: (_, node) => collect(node),
  });
  return found.sort();
}

/** Comments arrive with a leading space and a "#"-less body; join them into help text. */
function comment(node: { commentBefore?: string | null; comment?: string | null }): string {
  return [node.commentBefore, node.comment]
    .filter((entry): entry is string => Boolean(entry))
    .flatMap((entry) => entry.split('\n'))
    .map((line) => line.trim())
    .filter(Boolean)
    .join('\n');
}

/** The server writes booleans and numbers as quoted strings, so shape is inferred from content. */
function classify(value: unknown): ConfigValueKind {
  if (typeof value === 'boolean') return 'boolean';
  if (typeof value === 'number') return 'number';
  const text = String(value ?? '');
  if (/^(true|false)$/i.test(text)) return 'boolean';
  if (text !== '' && Number.isFinite(Number(text))) return 'number';
  return 'text';
}

function scalarValue(kind: ConfigValueKind, raw: unknown): boolean | number | string {
  const text = String(raw ?? '');
  if (kind === 'boolean') return text.toLowerCase() === 'true';
  if (kind === 'number') return Number(text);
  return text;
}

/** A sequence is editable as a list only when every entry is a scalar. */
function isStringList(node: YAMLSeq): boolean {
  return node.items.every((item) => isScalar(item));
}

/** IndicatorSettings reads this nested particle value directly, outside skill components. */
function isIndicatorParticle(path: (string | number)[]): boolean {
  return path.join('/') === INDICATOR_PARTICLE_PATH.join('/');
}

/** Builds the form tree; the shape follows the file so new plugin settings appear on their own. */
export function readSections(document: ConfigDocument): ConfigSection {
  const root: ConfigSection = { path: [], key: '', description: '', fields: [], sections: [] };
  if (!isMap(document.contents)) return root;
  walk(document.contents, [], root);
  return root;
}

function walk(map: YAMLMap, path: (string | number)[], target: ConfigSection): void {
  for (const item of map.items) {
    if (!isPair(item) || !isScalar(item.key)) continue;
    const key = String(item.key.value);
    const next = [...path, key];
    const description = [comment(item.key as Scalar), comment(item.value as Node)]
      .filter(Boolean)
      .join('\n');
    const value = item.value;
    if (isIndicatorParticle(next) && (isScalar(value) || isMap(value))) {
      const raw = isScalar(value) ? value.value : value.toJSON();
      // Keep hand-edited AST variants authoritative; a legacy scalar is previewed as an AST
      // and is persisted in that form when the user selects a particle.
      if (isParticleAst(raw) || typeof raw === 'string') {
        target.fields.push({
          path: next,
          key,
          kind: 'particle',
          value: isParticleAst(raw) ? raw : makeParticleAst(raw),
          description,
        });
        continue;
      }
    }
    if (isMap(value)) {
      const section: ConfigSection = { path: next, key, description, fields: [], sections: [] };
      walk(value, next, section);
      target.sections.push(section);
    } else if (isSeq(value) && isStringList(value)) {
      target.fields.push({
        path: next,
        key,
        kind: 'string-list',
        value: value.items.map((entry) => String((entry as Scalar).value ?? '')),
        description,
      });
    } else if (isScalar(value) || value === null) {
      const raw = isScalar(value) ? value.value : '';
      const kind = classify(raw);
      target.fields.push({ path: next, key, kind, value: scalarValue(kind, raw), description });
    }
    // Sequences of maps have no safe generic form; they stay untouched in the document.
  }
}

/**
 * Writes a value into the YAML tree. Ordinary scalar edits keep their quoting style;
 * the particle selector replaces its scalar with an AST map while retaining comments.
 * Booleans and numbers become quoted strings because that is what the plugin writes and
 * what its `getString(...).equalsIgnoreCase` checks read.
 */
export function setValue(document: ConfigDocument, field: ConfigField, value: ConfigValue): void {
  if (field.kind === 'particle') {
    const particle = isParticleAst(value)
      ? value
      : typeof value === 'string'
        ? makeParticleAst(value)
        : null;
    if (!particle) throw new Error(`粒子 AST 无效：${field.path.join('.')}`);
    const existing = document.getIn(field.path, true);
    const replacement = document.createNode(particle);
    // Replacing a scalar with a map must keep comments attached to the original YAML node.
    if (isScalar(existing) || isMap(existing)) {
      replacement.commentBefore = existing.commentBefore;
      replacement.comment = existing.comment;
    }
    document.setIn(field.path, replacement);
    return;
  }
  if (Array.isArray(value)) {
    const seq = document.getIn(field.path, true);
    const quote = isSeq(seq) ? quoteStyle(seq.items[0]) : 'QUOTE_SINGLE';
    document.setIn(
      field.path,
      document.createNode(value.map((entry) => quoteScalar(entry, quote))),
    );
    return;
  }
  const text = String(value);
  const existing = document.getIn(field.path, true);
  if (isScalar(existing)) existing.value = text;
  else document.setIn(field.path, quoteScalar(text, 'QUOTE_SINGLE'));
}

type QuoteStyle = Scalar['type'];

/** Match the style already used in the file so a save does not rewrite untouched entries. */
function quoteStyle(item: unknown): QuoteStyle {
  return isScalar(item) && item.type && item.type !== 'PLAIN' ? item.type : 'QUOTE_SINGLE';
}

/** Values such as "perm:10" must stay quoted; unquoted they read as ambiguous YAML. */
function quoteScalar(value: string, type: QuoteStyle): Scalar {
  const scalar = new ScalarNode(value);
  scalar.type = type;
  return scalar;
}
