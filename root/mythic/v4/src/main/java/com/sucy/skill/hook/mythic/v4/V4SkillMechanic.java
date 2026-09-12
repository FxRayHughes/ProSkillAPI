package com.sucy.skill.hook.mythic.v4;

import com.sucy.skill.hook.mythic.MythicBridges;
import com.sucy.skill.hook.mythic.SkillBridge;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderInt;
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderString;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

/**
 * MythicMobs 4 的 {@code castskillapi} 机制：让目标施放一个 SkillAPI 技能。
 */
public class V4SkillMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected PlaceholderInt level;
    protected PlaceholderString name;

    public V4SkillMechanic(String line, MythicLineConfig mlc) {
        super(line, mlc);
        this.name = PlaceholderString.of(mlc.getString(new String[]{"name", "n"}, "null"));
        this.level = PlaceholderInt.of(mlc.getString(new String[]{"level", "l"}, "1"));
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (target.isDead()) {
            return false;
        }
        if (target.isLiving() && target.getHealth() <= 0.0d) {
            return false;
        }
        final SkillBridge bridge = MythicBridges.skills();
        if (bridge == null) {
            return false;
        }
        final Entity entity = target.getBukkitEntity();
        if (!(entity instanceof LivingEntity)) {
            return false;
        }
        return bridge.cast((LivingEntity) entity, name.get(data, target), level.get(data, target));
    }
}
