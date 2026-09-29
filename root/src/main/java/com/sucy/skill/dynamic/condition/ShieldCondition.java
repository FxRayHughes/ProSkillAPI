package com.sucy.skill.dynamic.condition;

import com.sucy.skill.combat.shield.ShieldManager;
import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;

/** Tests live capacity, not the original grant amount. */
@SkillNode(key = "check shield", name = "Check Shield", nameZh = "检查护盾",
        descriptionZh = "逐目标检查指定护盾键的当前总容量是否在闭区间内。", container = true)
public class ShieldCondition extends ConditionComponent {
    @SkillField(kind = FieldKind.StringValue, label = "Layer Key", labelZh = "护盾键",
            tooltipZh = "留空检查所有护盾层。", defaultValue = "shield")
    private static final String KEY = "key";
    @SkillField(kind = FieldKind.AttributeValue, label = "Minimum", labelZh = "下限",
            tooltipZh = "当前容量的闭区间下限。", defaultValue = "1")
    private static final String MIN = "minimum";
    @SkillField(kind = FieldKind.AttributeValue, label = "Maximum", labelZh = "上限",
            tooltipZh = "当前容量的闭区间上限。", defaultValue = "999999")
    private static final String MAX = "maximum";
    @Override public String getKey() { return "check shield"; }
    @Override boolean test(LivingEntity caster, int level, LivingEntity target) {
        double amount = ShieldManager.remaining(target, settings.getString(KEY, "shield"));
        return amount >= parseValues(caster, MIN, level, 1)
                && amount <= parseValues(caster, MAX, level, 999999);
    }
}
