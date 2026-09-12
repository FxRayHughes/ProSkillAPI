import { readFileSync } from 'node:fs';
import { expect, it } from 'vitest';
import { extractLegacy } from './extract-legacy.mjs';

// This comparison detects omissions when the old editor gains fields or helper options.
it('generated catalog exactly matches every registered legacy constructor', () => {
  const snapshot = JSON.parse(
    readFileSync(
      new URL('../src/features/skill-editor/model/legacy.generated.json', import.meta.url),
      'utf8',
    ),
  );
  expect(snapshot).toEqual(extractLegacy());
});
