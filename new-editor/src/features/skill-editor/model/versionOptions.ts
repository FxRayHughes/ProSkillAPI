/** Compare release components numerically: parseFloat would treat 1.20 as 1.2. */
export function compareVersions(left: string, right: string): number {
  const first = left.split('.').map(Number);
  const second = right.split('.').map(Number);
  for (let index = 0; index < Math.max(first.length, second.length); index += 1) {
    const difference = (first[index] ?? 0) - (second[index] ?? 0);
    if (difference !== 0) return difference;
  }
  return 0;
}

/** Inclusive availability keeps the catalog's since/until bounds consistent across controls. */
export function isOptionAvailable(
  option: { since?: string; until?: string },
  version: string,
): boolean {
  return (
    (!option.since || compareVersions(version, option.since) >= 0) &&
    (!option.until || compareVersions(version, option.until) <= 0)
  );
}
