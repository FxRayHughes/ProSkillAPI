package com.sucy.skill.dynamic.condition;

import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/** The vanilla attack recharge is a ratio; nonplayers cannot supply it. */
@SkillNode(key = "check attack charge", name = "Check Attack Charge", nameZh = "检查攻击蓄力",
        descriptionZh = "按原版攻击冷却比值 0 至 1 判断玩家是否蓄满力。", container = true)
public final class CheckAttackChargeCondition extends ConditionComponent {
    @SkillField(kind = FieldKind.AttributeValue, label = "Minimum", labelZh = "最小比值", defaultValue = "1")
    private static final String MINIMUM = "minimum";
    @Override public String getKey() { return "check attack charge"; }
    @Override boolean test(LivingEntity caster, int level, LivingEntity target) {
        if (!(target instanceof Player)) return false;
        double min = parseValues(caster, MINIMUM, level, 1);
        return Double.isFinite(min) && min >= 0 && min <= 1 && ((Player) target).getAttackCooldown() >= min;
    }
}
