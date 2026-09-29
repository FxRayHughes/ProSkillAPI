package com.sucy.skill.dynamic.condition;

import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;

/** Tests remaining air in ticks or as a percentage of the entity's capacity. */
@SkillNode(key = "check oxygen", name = "Check Oxygen", nameZh = "检查氧气",
        descriptionZh = "可按剩余刻数或最大氧气百分比检查。", container = true)
public final class CheckOxygenCondition extends ConditionComponent {
    @SkillField(kind = FieldKind.ListValue, label = "Unit", labelZh = "单位",
            options = {"ticks", "percent"}, optionsZh = {"刻数", "百分比"}, defaultValue = "ticks")
    private static final String UNIT = "unit";
    @SkillField(kind = FieldKind.AttributeValue, label = "Minimum", labelZh = "下限", defaultValue = "0")
    private static final String MIN = "minimum";
    @SkillField(kind = FieldKind.AttributeValue, label = "Maximum", labelZh = "上限", defaultValue = "300")
    private static final String MAX = "maximum";
    @Override public String getKey() { return "check oxygen"; }
    @Override boolean test(LivingEntity caster, int level, LivingEntity target) {
        if (target == null || target.getMaximumAir() <= 0) return false;
        double amount = target.getRemainingAir();
        if ("percent".equals(settings.getString(UNIT, "ticks")))
            amount = 100 * amount / target.getMaximumAir();
        double min = parseValues(caster, MIN, level, 0);
        double max = parseValues(caster, MAX, level, 300);
        return Double.isFinite(min) && Double.isFinite(max) && min <= amount && amount <= max;
    }
}
