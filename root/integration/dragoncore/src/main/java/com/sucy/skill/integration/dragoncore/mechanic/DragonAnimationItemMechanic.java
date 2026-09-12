/**
 * SkillAPI
 * com.sucy.skill.integration.dragoncore.mechanic.DragonAnimationItemMechanic
 * <p>
 * The MIT License (MIT)
 */
package com.sucy.skill.integration.dragoncore.mechanic;

import com.sucy.skill.dynamic.mechanic.MechanicComponent;
import com.sucy.skill.integration.dragoncore.DragonCoreBridge;
import org.bukkit.entity.LivingEntity;

import java.util.List;

/**
 * Plays a DragonCore model item animation. The bridge absorbs provider method
 * renames so skill configs keep the same "dragon animation item" mechanic key.
 */
public class DragonAnimationItemMechanic extends MechanicComponent {
    private static final String NAME = "name";

    @Override
    public String getKey() {
        return "dragon animation item";
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        if (targets.size() == 0 || !settings.has(NAME)) {
            return false;
        }

        String key = settings.getString(NAME);
        for (LivingEntity target : targets) {
            DragonCoreBridge.setEntityModelItemAnimation(target, key);
        }
        return true;
    }
}
