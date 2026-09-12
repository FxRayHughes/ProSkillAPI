package com.sucy.skill.hook.mythic.v5;

import com.sucy.skill.hook.mythic.MythicBridges;
import com.sucy.skill.hook.mythic.SkillBridge;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.SkillMechanic;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

/**
 * MythicMobs 5 的 {@code castskillapi} 机制：让目标施放一个 SkillAPI 技能。
 * <p>
 * 与 4.x 的差异：构造器需要 SkillExecutor 与配置文件，castAtEntity 返回 SkillResult。
 */
public class V5SkillMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected PlaceholderInt level;
    protected PlaceholderString name;

    public V5SkillMechanic(String line, MythicLineConfig mlc) {
        super(MythicBukkit.inst().getSkillManager(), null, line, mlc);
        this.name = PlaceholderString.of(mlc.getString(new String[]{"name", "n"}, "null"));
        this.level = PlaceholderInt.of(mlc.getString(new String[]{"level", "l"}, "1"));
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (target.isDead()) {
            return SkillResult.INVALID_TARGET;
        }
        if (target.isLiving() && target.getHealth() <= 0.0d) {
            return SkillResult.INVALID_TARGET;
        }
        final SkillBridge bridge = MythicBridges.skills();
        if (bridge == null) {
            return SkillResult.ERROR;
        }
        final Entity entity = target.getBukkitEntity();
        if (!(entity instanceof LivingEntity)) {
            return SkillResult.INVALID_TARGET;
        }
        final boolean cast = bridge.cast((LivingEntity) entity,
                name.get(data, target), level.get(data, target));
        return cast ? SkillResult.SUCCESS : SkillResult.CONDITION_FAILED;
    }
}
