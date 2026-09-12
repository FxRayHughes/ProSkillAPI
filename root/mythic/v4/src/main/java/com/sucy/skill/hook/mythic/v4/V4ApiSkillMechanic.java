package com.sucy.skill.hook.mythic.v4;

import com.sucy.skill.hook.mythic.MythicBridges;
import com.sucy.skill.hook.mythic.SkillBridge;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.INoTargetSkill;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

/**
 * MythicMobs 4 的 {@code skillapi} 机制：让 MM 怪物施放一个 SkillAPI 技能。
 * <p>
 * 用法：
 * <pre>
 *   Skills:
 *   - skillapi{skill=法术飞弹;level=1} @target
 *   - skillapi{skill=冰霜新星} ~onTimer:100
 * </pre>
 * 带 @target 时目标会以 {@code api-target} 写入施法者的 cast data，
 * 技能内用 Remember 目标选择器（key 填 {@code api-target}）取用。
 */
public class V4ApiSkillMechanic extends SkillMechanic implements ITargetedEntitySkill, INoTargetSkill {

    private final String skillName;
    private final int level;

    public V4ApiSkillMechanic(String line, MythicLineConfig mlc) {
        super(line, mlc);
        this.skillName = mlc.getString(new String[]{"skill", "s"}, "");
        this.level = mlc.getInteger(new String[]{"level", "l"}, 1);
        // SkillAPI 的技能施放涉及 Bukkit 状态，必须在主线程执行。
        this.ASYNC_SAFE = false;
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {
        return cast(data, asLiving(BukkitAdapter.adapt(target)));
    }

    @Override
    public boolean cast(SkillMetadata data) {
        return cast(data, null);
    }

    private boolean cast(SkillMetadata data, LivingEntity target) {
        final SkillBridge bridge = MythicBridges.skills();
        if (bridge == null || skillName.isEmpty()) {
            return false;
        }
        final LivingEntity caster = asLiving(BukkitAdapter.adapt(data.getCaster().getEntity()));
        if (caster == null) {
            return false;
        }
        return bridge.castAtTarget(caster, target, skillName, level);
    }

    private static LivingEntity asLiving(final Entity entity) {
        return entity instanceof LivingEntity ? (LivingEntity) entity : null;
    }
}
