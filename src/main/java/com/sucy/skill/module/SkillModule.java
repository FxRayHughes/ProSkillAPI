/**
 * SkillAPI
 * com.sucy.skill.module.SkillModule
 * <p>
 * The MIT License (MIT)
 */
package com.sucy.skill.module;

import com.sucy.skill.SkillAPI;

/**
 * Runtime extension point for optional source modules folded into the plugin
 * jar. Modules decide whether their external plugin/API is available before
 * they register components, which keeps the core registry free of optional
 * provider classes.
 */
public interface SkillModule {
    /**
     * @return human-readable module name for logs
     */
    String getName();

    /**
     * @param plugin running SkillAPI instance
     * @return true when this module should install its runtime hooks/components
     */
    boolean isAvailable(SkillAPI plugin);

    /**
     * Installs module-owned components and hooks. Called before dynamic skills
     * are loaded so existing skill configs can reference optional components.
     */
    void install(SkillAPI plugin);
}
