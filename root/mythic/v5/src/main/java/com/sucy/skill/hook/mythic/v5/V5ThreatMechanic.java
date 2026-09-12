package com.sucy.skill.hook.mythic.v5;

import com.sucy.skill.hook.mythic.MythicBridges;
import com.sucy.skill.hook.mythic.MythicProvider;
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
 * MythicMobs 5 的仇恨操作机制，作用对象始终是施法者自身（怪物）。
 * <p>
 * 用法：
 * <pre>
 *   Skills:
 *   - clearthreats ~onAttack 0.1      # 10% 几率清空自身仇恨表
 *   - shufflethreats ~onTimer:100     # 每 100 tick 乱一次仇恨
 *   - resettarget ~onTimer:200        # 重置 AI 目标并清表
 * </pre>
 */
public class V5ThreatMechanic extends SkillMechanic implements ITargetedEntitySkill, INoTargetSkill {

    /** 清表 / 乱仇恨 / 重置目标。 */
    enum Action {
        CLEAR, SHUFFLE, RESET
    }

    private final Action action;
    private final MythicProvider provider;

    public V5ThreatMechanic(String line, MythicLineConfig mlc, Action action, MythicProvider provider) {
        super(MythicBukkit.inst().getSkillManager(), null, line, mlc);
        this.action = action;
        this.provider = provider;
        this.setAsyncSafe(false);
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        return apply(data);
    }

    @Override
    public SkillResult cast(SkillMetadata data) {
        return apply(data);
    }

    private SkillResult apply(SkillMetadata data) {
        final SkillBridge bridge = MythicBridges.skills();
        if (bridge == null) return SkillResult.ERROR;

        final Entity entity = BukkitAdapter.adapt(data.getCaster().getEntity());
        if (!(entity instanceof LivingEntity)) return SkillResult.INVALID_TARGET;
        final LivingEntity caster = (LivingEntity) entity;

        switch (action) {
            case CLEAR:
                bridge.clearThreats(caster);
                return SkillResult.SUCCESS;
            case SHUFFLE:
                bridge.shuffleThreats(caster);
                return SkillResult.SUCCESS;
            case RESET:
                // 先让 MythicMobs 自己的 AI 放弃目标，再清空 SkillAPI 侧的表
                provider.clearThreatTable(caster);
                bridge.clearThreats(caster);
                return SkillResult.SUCCESS;
            default:
                return SkillResult.INVALID_CONFIG;
        }
    }
}
