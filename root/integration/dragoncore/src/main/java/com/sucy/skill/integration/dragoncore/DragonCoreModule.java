/**
 * SkillAPI
 * com.sucy.skill.integration.dragoncore.DragonCoreModule
 * <p>
 * The MIT License (MIT)
 */
package com.sucy.skill.integration.dragoncore;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.dynamic.ComponentRegistry;
import com.sucy.skill.integration.dragoncore.mechanic.DragonAnimationBlockMechanic;
import com.sucy.skill.integration.dragoncore.mechanic.DragonAnimationItemMechanic;
import com.sucy.skill.integration.dragoncore.mechanic.DragonAnimationStartMechanic;
import com.sucy.skill.integration.dragoncore.mechanic.DragonAnimationStopMechanic;
import com.sucy.skill.module.SkillModule;
import org.bukkit.plugin.Plugin;

/**
 * Optional DragonCore integration module. Its components are registered only
 * when Bukkit has loaded and enabled DragonCore, preserving the old softdepend
 * behavior while keeping the base registry provider-neutral.
 */
public final class DragonCoreModule implements SkillModule {
    @Override
    public String getName() {
        return "DragonCore";
    }

    @Override
    public boolean isAvailable(SkillAPI plugin) {
        Plugin dragonCore = plugin.getServer().getPluginManager().getPlugin("DragonCore");
        return dragonCore != null && dragonCore.isEnabled();
    }

    @Override
    public void install(SkillAPI plugin) {
        ComponentRegistry.registerModuleComponent(new DragonAnimationStartMechanic());
        ComponentRegistry.registerModuleComponent(new DragonAnimationStopMechanic());
        ComponentRegistry.registerModuleComponent(new DragonAnimationBlockMechanic());
        ComponentRegistry.registerModuleComponent(new DragonAnimationItemMechanic());
    }
}
