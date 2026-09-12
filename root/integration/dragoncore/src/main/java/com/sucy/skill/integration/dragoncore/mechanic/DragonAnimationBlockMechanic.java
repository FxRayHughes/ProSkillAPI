/**
 * SkillAPI
 * com.sucy.skill.integration.dragoncore.mechanic.DragonAnimationBlockMechanic
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
 * Plays a DragonCore block animation for player viewers. Coordinates remain
 * plain block integers because existing skill configs define fixed block slots.
 */
public class DragonAnimationBlockMechanic extends MechanicComponent {
    private static final String NAME = "name";
    private static final String X = "x";
    private static final String Y = "y";
    private static final String Z = "z";

    @Override
    public String getKey() {
        return "dragon animation block";
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        if (targets.size() == 0 || !settings.has(NAME)) {
            return false;
        }

        String key = settings.getString(NAME);
        int x = (int) parseValues(caster, X, level, 0);
        int y = (int) parseValues(caster, Y, level, 0);
        int z = (int) parseValues(caster, Z, level, 0);
        for (LivingEntity target : targets) {
            if (target instanceof Player) {
                DragonCoreBridge.setBlockAnimation(((Player) target), x, y, z, key);
            }
        }
        return true;
    }
}
