package com.sucy.skill.hook.mythic.v4;

import com.sucy.skill.hook.mythic.MythicBridges;
import com.sucy.skill.hook.mythic.MythicProvider;
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
 * MythicMobs 4 的仇恨操作机制，作用对象始终是施法者自身（怪物）。
 * <p>
 * 用法：
 * <pre>
 *   Skills:
 *   - clearthreats ~onAttack 0.1      # 10% 几率清空自身仇恨表
 *   - shufflethreats ~onTimer:100     # 每 100 tick 乱一次仇恨
 *   - resettarget ~onTimer:200        # 重置 AI 目标并清表
 * </pre>
 */
public class V4ThreatMechanic extends SkillMechanic implements ITargetedEntitySkill, INoTargetSkill {

    /** 清表 / 乱仇恨 / 重置目标。 */
    enum Action {
        CLEAR, SHUFFLE, RESET
    }

    private final Action action;
    private final MythicProvider provider;

    public V4ThreatMechanic(String line, MythicLineConfig mlc, Action action, MythicProvider provider) {
        super(line, mlc);
        this.action = action;
        this.provider = provider;
        this.ASYNC_SAFE = false;
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {
        return apply(data);
    }

    @Override
    public boolean cast(SkillMetadata data) {
        return apply(data);
    }

    private boolean apply(SkillMetadata data) {
        final SkillBridge bridge = MythicBridges.skills();
        if (bridge == null) return false;

        final Entity entity = BukkitAdapter.adapt(data.getCaster().getEntity());
        if (!(entity instanceof LivingEntity)) return false;
        final LivingEntity caster = (LivingEntity) entity;

        switch (action) {
            case CLEAR:
                bridge.clearThreats(caster);
                return true;
            case SHUFFLE:
                bridge.shuffleThreats(caster);
                return true;
            case RESET:
                // 先让 MythicMobs 自己的 AI 放弃目标，再清空 SkillAPI 侧的表
                provider.clearThreatTable(caster);
                bridge.clearThreats(caster);
                return true;
            default:
                return false;
        }
    }
}
