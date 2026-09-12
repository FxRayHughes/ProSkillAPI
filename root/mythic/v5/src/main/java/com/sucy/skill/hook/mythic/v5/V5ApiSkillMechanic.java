package com.sucy.skill.hook.mythic.v5;

import com.sucy.skill.hook.mythic.MythicBridges;
import com.sucy.skill.hook.mythic.SkillBridge;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.SkillMechanic;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

/**
 * MythicMobs 5 的 {@code skillapi} 机制：让 MM 怪物施放一个 SkillAPI 技能。
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
public class V5ApiSkillMechanic extends SkillMechanic implements ITargetedEntitySkill, INoTargetSkill {

    private final String skillName;
    private final int level;

    public V5ApiSkillMechanic(String line, MythicLineConfig mlc) {
        super(MythicBukkit.inst().getSkillManager(), null, line, mlc);
        this.skillName = mlc.getString(new String[]{"skill", "s"}, "");
        this.level = mlc.getInteger(new String[]{"level", "l"}, 1);
        // SkillAPI 的技能施放涉及 Bukkit 状态，必须在主线程执行。
        this.setAsyncSafe(false);
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        return doCast(data, asLiving(BukkitAdapter.adapt(target)));
    }

    @Override
    public SkillResult cast(SkillMetadata data) {
        return doCast(data, null);
    }

    private SkillResult doCast(SkillMetadata data, LivingEntity target) {
        final SkillBridge bridge = MythicBridges.skills();
        if (bridge == null) {
            return SkillResult.ERROR;
        }
        if (skillName.isEmpty()) {
            return SkillResult.INVALID_CONFIG;
        }
        final LivingEntity caster = asLiving(BukkitAdapter.adapt(data.getCaster().getEntity()));
        if (caster == null) {
            return SkillResult.INVALID_TARGET;
        }
        return bridge.castAtTarget(caster, target, skillName, level)
                ? SkillResult.SUCCESS : SkillResult.CONDITION_FAILED;
    }

    private static LivingEntity asLiving(final Entity entity) {
        return entity instanceof LivingEntity ? (LivingEntity) entity : null;
    }
}
