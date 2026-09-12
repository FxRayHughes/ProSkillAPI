package com.sucy.skill.hook;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.attribute.mob.MobAttribute;
import com.sucy.skill.api.attribute.mob.MobAttributeData;
import com.sucy.skill.api.skills.Skill;
import com.sucy.skill.api.skills.SkillCastAPI;
import com.sucy.skill.combat.threat.ThreatManager;
import com.sucy.skill.data.PlayerEquipsUtils;
import com.sucy.skill.dynamic.DynamicSkill;
import com.sucy.skill.hook.mythic.SkillBridge;
import com.sucy.skill.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

/**
 * 把主工程的能力暴露给 MythicMobs 适配模块。
 * <p>
 * 适配模块只依赖 mythic-api，不反向依赖主工程；两者通过本实现相连。
 */
public class SkillBridgeImpl implements SkillBridge {

    /** MythicMobs 的 damageType 机制借用的技能载体，仅用于走 SkillAPI 的伤害管线。 */
    private static final String DAMAGE_SKILL_NAME = "MythicMobs_Cast_Damage";

    /** skillapi 机制把目标存进 cast data 时用的键，技能内用 Remember 选择器读取。 */
    private static final String API_TARGET_KEY = "api-target";

    /**
     * 懒加载：Skill 的构造器会读取语言配置，而本类在 onEnable 早期就被创建，
     * 那时 language 尚未加载，提前 new 会直接让插件 enable 失败。
     */
    private Skill damageSkill;

    private Skill damageSkill() {
        if (damageSkill == null) {
            damageSkill = new Skill(DAMAGE_SKILL_NAME, "Dynamic", Material.ICE, 1) {
            };
        }
        return damageSkill;
    }

    @Override
    public boolean cast(final LivingEntity caster, final String skillName, final int level) {
        final Skill skill = SkillAPI.getSkill(skillName);
        if (skill == null) {
            return false;
        }
        return SkillCastAPI.cast(caster, skill, level);
    }

    @Override
    public boolean castAtTarget(final LivingEntity caster, final LivingEntity target,
                                final String skillName, final int level) {
        final Skill skill = SkillAPI.getSkill(skillName);
        if (skill == null) {
            return false;
        }
        if (target != null) {
            // 写进 cast data，技能内用 Remember 目标选择器（key=api-target）取用。
            DynamicSkill.getCastData(caster).put(API_TARGET_KEY, new ArrayList<>(Arrays.asList(target)));
        }
        if (skill instanceof DynamicSkill) {
            return ((DynamicSkill) skill).cast(caster, level);
        }
        return SkillCastAPI.cast(caster, skill, level);
    }

    @Override
    public void clearThreats(final LivingEntity entity) {
        ThreatManager.clearThreats(entity);
    }

    @Override
    public void shuffleThreats(final LivingEntity entity) {
        ThreatManager.shuffleThreats(entity);
    }

    @Override
    public void damage(final LivingEntity target, final double amount, final LivingEntity source,
                       final String classification, final boolean trueDamage) {
        // MythicMobs 的技能可能在异步线程结算，伤害必须回到主线程施加。
        Bukkit.getScheduler().runTask(SkillAPI.singleton, () -> {
            if (trueDamage) {
                damageSkill().trueDamage(target, amount, source);
            } else {
                damageSkill().damage(target, amount, source, classification, true);
            }
        });
    }

    @Override
    public void applyMobAttribute(final UUID entityId, final String attributeLine) {
        // 怪物属性是可关闭的特性，保留原 MobListener 的开关语义。
        if (!SkillAPI.getSettings().isAttributeMobEnabled()) return;
        final MobAttributeData data = MobAttribute.getData(entityId, true);
        if (data == null) return;
        final Pair<String, Integer> pair = PlayerEquipsUtils.getAttribute(attributeLine);
        if (pair == null || pair.getKey() == null) return;
        data.addAttribute(pair.getKey(), pair.getLast());
    }
}
