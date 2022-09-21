package com.sucy.skill.api.skills;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.event.EntityCastSkillEvent;
import com.sucy.skill.api.target.TargetHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

public class SkillCastAPI {

    /**
     * 实体释放技能 [强制]
     *
     * @param caster 释放技能的实体
     * @param skill  技能
     * @param level  技能等级
     */
    public static boolean cast(LivingEntity caster, Skill skill, Integer level) {
        if (skill == null) {
            throw new IllegalArgumentException("Skill cannot be null");
        }
        EntityCastSkillEvent event = new EntityCastSkillEvent(caster, skill, level);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        if (skill instanceof SkillShot) {
            return ((SkillShot) skill).cast(caster, level);
        }
        if (skill instanceof TargetSkill) {
            LivingEntity target = TargetHelper.getLivingTarget(caster, skill.getRange(level));
            if (target == null) {
                return false;
            }
            boolean canAttack = !SkillAPI.getSettings().canAttack(caster, target);
            return ((TargetSkill) skill).cast(caster, target, level, canAttack);
        }
        return false;
    }

}
