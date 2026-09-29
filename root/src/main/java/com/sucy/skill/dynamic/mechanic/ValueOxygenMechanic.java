package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;


/** Reads one specific numeric source and refuses to turn missing state into zero. */
@SkillNode(key = "value oxygen", name = "Value Oxygen", nameZh = "读取氧气",
        descriptionZh = "来源不存在时不覆盖输出引用键；只读取第一个目标。")
public final class ValueOxygenMechanic extends AbstractValueReadMechanic {
    @Override public String getKey() { return "value oxygen"; }
    @Override protected Double read(LivingEntity caster, LivingEntity target) {
        return target == null ? null : (double) target.getRemainingAir();
    }
}
