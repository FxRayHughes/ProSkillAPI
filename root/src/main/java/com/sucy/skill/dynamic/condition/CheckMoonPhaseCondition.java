package com.sucy.skill.dynamic.condition;

import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;

/** Minecraft's lunar cycle has eight whole-day phases; zero is the full moon. */
@SkillNode(key = "check moon phase", name = "Check Moon Phase", nameZh = "检查月相",
        descriptionZh = "按目标世界的八阶段月相检查；零为满月，四为新月。", container = true)
public final class CheckMoonPhaseCondition extends ConditionComponent {
    @SkillField(kind = FieldKind.IntValue, label = "Phase", labelZh = "月相序号",
            tooltipZh = "零为满月，四为新月；范围零到七。", defaultValue = "0")
    private static final String PHASE = "phase";
    @Override public String getKey() { return "check moon phase"; }
    @Override boolean test(LivingEntity caster, int level, LivingEntity target) {
        int desired = settings.getInt(PHASE, 0);
        if (target == null || desired < 0 || desired > 7) return false;
        return (target.getWorld().getFullTime() / 24000L) % 8 == desired;
    }
}
