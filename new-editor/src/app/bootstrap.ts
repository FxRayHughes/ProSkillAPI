import {
  registerGeneratedCatalog,
  registerLegacyCatalog,
} from '../features/skill-editor/model/legacyCatalog';
import { applyStoredPlugins } from '../features/plugins/model/pluginStore';

/**
 * Node definitions must exist before any page parses a skill file, so registration runs at
 * module load instead of inside an effect.
 */
// 先拿到注解的运行前提，再用旧目录补齐注解未导出的继承字段。
registerGeneratedCatalog();
registerLegacyCatalog();
applyStoredPlugins();
