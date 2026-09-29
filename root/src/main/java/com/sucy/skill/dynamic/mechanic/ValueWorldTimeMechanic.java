package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;


/** Reads one specific numeric source and refuses to turn missing state into zero. */
@SkillNode(key = "value world time", name = "Value World Time", nameZh = "读取世界时刻",
        descriptionZh = "来源不存在时不覆盖输出引用键；只读取第一个目标。")
public final class ValueWorldTimeMechanic extends AbstractValueReadMechanic {
    @Override public String getKey() { return "value world time"; }
    @Override protected Double read(LivingEntity caster, LivingEntity target) {
        return target == null ? null : (double) target.getWorld().getTime();
    }
}
