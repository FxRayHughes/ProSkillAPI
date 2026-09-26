import {
  isParticleAst,
  isParticleField,
  makeParticleAst,
  particlePreview,
} from '../model/particleCatalog';
import { nodeRegistry } from '../model/registry';
import type { SkillProject } from '../model/types';

/** Preview missing mappings without blocking AST export; the server will play white CLOUD. */
export function particleFallbackIssues(project: SkillProject, version: string): string[] {
  return project.nodes.flatMap((node) => {
    const definition = nodeRegistry.get(node.data.definitionId);
    if (!definition) return [];
    return definition.fields
      .filter((field) => isParticleField(field.key))
      .flatMap((field) => {
        const raw = node.data.values[field.key];
        if (raw === undefined) return [];
        const particle = isParticleAst(raw)
          ? raw
          : typeof raw === 'string'
            ? makeParticleAst(raw)
            : undefined;
        if (particle && particlePreview(particle, version)) return [];
        return [
          `${node.data.legacyKey ?? node.data.label}.${field.key}：${particle?.value ?? String(raw)} 在 ${version} 将使用白色 CLOUD`,
        ];
      });
  });
}
