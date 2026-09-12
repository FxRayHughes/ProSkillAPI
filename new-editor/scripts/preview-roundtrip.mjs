import { readFileSync } from 'node:fs';
import { createServer } from 'vite';

/**
 * Prints the file the editor would write for a given skill, so a human can compare it with
 * the original. Uses Vite's module runner because the pipeline is TypeScript.
 *
 * Usage: node scripts/preview-roundtrip.mjs <path-to-skill.yml>
 */
const target = process.argv[2];
if (!target) {
  console.error('用法：node scripts/preview-roundtrip.mjs <技能 yml 路径>');
  process.exit(2);
}

const server = await createServer({ server: { middlewareMode: true }, appType: 'custom' });
try {
  const { registerLegacyCatalog } = await server.ssrLoadModule(
    '/src/features/skill-editor/model/legacyCatalog.ts',
  );
  const { registerBuiltinPlugin } = await server.ssrLoadModule(
    '/src/features/plugins/builtin/index.ts',
  );
  const { parseLegacySkill, serializeLegacySkill } = await server.ssrLoadModule(
    '/src/features/skill-editor/io/legacySkill.ts',
  );
  registerLegacyCatalog();
  registerBuiltinPlugin();
  process.stdout.write(serializeLegacySkill(parseLegacySkill(readFileSync(target, 'utf8'))));
} finally {
  await server.close();
}
