/**
 * SkillAPI
 * com.sucy.skill.integration.dragoncore.mechanic.DragonAnimationStartMechanic
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
 * Starts a DragonCore animation. This component lives in the DragonCore module
 * because its behavior is meaningless unless the optional provider is enabled.
 */
public class DragonAnimationStartMechanic extends MechanicComponent {
    private static final String NAME = "name";
    private static final String TIME = "time";

    @Override
    public String getKey() {
        return "dragon animation start";
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        if (targets.size() == 0 || !settings.has(NAME)) {
            return false;
        }

        String key = settings.getString(NAME);
        for (LivingEntity target : targets) {
            if (target instanceof Player) {
                DragonCoreBridge.setPlayerAnimation((Player) target, key);
            }
            int time = (int) parseValues(target, TIME, level, 200);
            DragonCoreBridge.setEntityAnimation(target, key, time);
        }
        return true;
    }
}
