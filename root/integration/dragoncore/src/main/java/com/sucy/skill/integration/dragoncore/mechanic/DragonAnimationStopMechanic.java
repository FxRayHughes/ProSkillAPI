/**
 * SkillAPI
 * com.sucy.skill.integration.dragoncore.mechanic.DragonAnimationStopMechanic
 * <p>
 * The MIT License (MIT)
 */
package com.sucy.skill.integration.dragoncore.mechanic;

import com.sucy.skill.dynamic.mechanic.MechanicComponent;
import com.sucy.skill.integration.dragoncore.DragonCoreBridge;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Stops a DragonCore animation through the optional integration bridge. Keeping
 * this component out of the core registry avoids exposing unusable mechanics on
 * servers that do not install DragonCore.
 */
public class DragonAnimationStopMechanic extends MechanicComponent {
    private static final String NAME = "name";
    private static final String TIME = "time";

    @Override
    public String getKey() {
        return "dragon animation stop";
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        if (targets.size() == 0 || !settings.has(NAME)) {
            return false;
        }
        String key = settings.getString(NAME);
        for (LivingEntity target : targets) {
            if (target instanceof Player) {
                DragonCoreBridge.removePlayerAnimation((Player) target, key);
            }
            int time = (int) parseValues(target, TIME, level, 200);
            DragonCoreBridge.removeEntityAnimation(target, key, time);
        }
        return true;
    }
}
