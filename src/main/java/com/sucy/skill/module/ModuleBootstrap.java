/**
 * SkillAPI
 * com.sucy.skill.module.ModuleBootstrap
 * <p>
 * The MIT License (MIT)
 */
package com.sucy.skill.module;

import com.sucy.skill.SkillAPI;

/**
 * Loads optional runtime modules by class name. The folded module jars are not
 * compile-time dependencies of the main plugin sources, so missing or disabled
 * integrations do not affect the core enable path.
 */
public final class ModuleBootstrap {
    private static final String[] OPTIONAL_MODULES = {
            "com.sucy.skill.integration.dragoncore.DragonCoreModule"
    };

    private ModuleBootstrap() {
        // Utility class: modules are installed during SkillAPI startup only.
    }

    public static void install(SkillAPI plugin) {
        for (String className : OPTIONAL_MODULES) {
            install(plugin, className);
        }
    }

    private static void install(SkillAPI plugin, String className) {
        try {
            Object instance = Class.forName(className).newInstance();
            if (!(instance instanceof SkillModule)) {
                plugin.getLogger().warning(className + " does not implement SkillModule");
                return;
            }
            SkillModule module = (SkillModule) instance;
            if (module.isAvailable(plugin)) {
                module.install(plugin);
                plugin.getLogger().info("Installed optional module: " + module.getName());
            }
        } catch (ClassNotFoundException ignored) {
            // Module jar was not folded into this distribution.
        } catch (Throwable ex) {
            plugin.getLogger().warning("Failed to install optional module " + className + ": " + ex.getMessage());
        }
    }
}
