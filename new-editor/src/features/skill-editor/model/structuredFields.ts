/**
 * Codec for flat native YAML maps used by signal contracts and arguments.
 * Keeping this outside React also lets graph validation inspect the same data.
 */
export function parseFieldMap(source: string): Record<string, string | number | boolean> {
  const parsed: unknown = JSON.parse(source);
  if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed))
    throw new Error('请输入键值对象');
  const result: Record<string, string | number | boolean> = {};
  for (const [key, value] of Object.entries(parsed)) {
    if (!key.trim()) throw new Error('参数名不能为空');
    if (typeof value === 'number' && !Number.isFinite(value)) throw new Error('数值必须有限');
    if (!['string', 'number', 'boolean'].includes(typeof value))
      throw new Error(`参数 ${key} 只能是文本、数字或布尔值`);
    result[key] = value as string | number | boolean;
  }
  return result;
}

export function formatFieldMap(value: unknown): string {
  return JSON.stringify(value ?? {}, null, 2);
}
