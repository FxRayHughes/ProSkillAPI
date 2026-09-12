package com.sucy.skill.dynamic.mechanic;

import org.bukkit.entity.LivingEntity;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * SkillAPI © 2017
 * com.sucy.skill.dynamic.mechanic.HealthSetMechanic
 */
@SkillNode(
        key = "health set",
        name = "Health Set",
        nameZh = "生命值设置",
        description = "Sets the target's health to the specified amount, ignoring resistances, damage buffs, and so on",
        descriptionZh = "直接把每个目标的生命值写成指定数值，不经过伤害或治疗事件，因此无视抗性、伤害加成、治疗加成与护盾类效果。实际值会被限制在 1 到目标最大生命值之间，所以既杀不死目标，也不会超过上限。该节点始终返回成功，即使目标列表为空。")
public class HealthSetMechanic extends MechanicComponent {

    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Health",
            labelZh = "生命值",
            tooltip = "[health] The health to set to",
            tooltipZh = "要设置的生命值，先与 1 取较大值再与目标最大生命值取较小值，故有效区间为 1 至最大生命值。数值随技能等级/属性变化，默认 1。")
    private static final String HEALTH = "health";

    @Override
    public String getKey() {
        return "health set";
    }

    @Override
    public boolean execute(final LivingEntity caster, final int level, final List<LivingEntity> targets) {
        final double health = Math.max(1, parseValues(caster, HEALTH, level, 1));

        for (final LivingEntity target : targets) {
            target.setHealth(Math.min(health, target.getMaxHealth()));
        }

        return true;
    }
}
