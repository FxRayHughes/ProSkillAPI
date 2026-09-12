import {
  registerGeneratedCatalog,
  registerLegacyCatalog,
} from '../features/skill-editor/model/legacyCatalog';
import { applyStoredPlugins } from '../features/plugins/model/pluginStore';

/**
 * Node definitions must exist before any page parses a skill file, so registration runs at
 * module load instead of inside an effect.
 */
// 注解生成的目录是权威来源，先注册；旧的抽取目录只补齐这里缺的条目。
registerGeneratedCatalog();
registerLegacyCatalog();
applyStoredPlugins();
