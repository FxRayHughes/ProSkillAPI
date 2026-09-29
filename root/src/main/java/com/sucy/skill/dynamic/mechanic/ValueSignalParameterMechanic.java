package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;
import com.sucy.skill.dynamic.DynamicSkill;

/** Reads one specific numeric source and refuses to turn missing state into zero. */
@SkillNode(key = "value signal parameter", name = "Value Signal Parameter", nameZh = "读取信号参数",
        descriptionZh = "来源不存在时不覆盖输出引用键；只读取第一个目标。")
public final class ValueSignalParameterMechanic extends AbstractValueReadMechanic {
    @SkillField(kind = FieldKind.StringValue, label = "Parameter", labelZh = "参数名")
    private static final String PARAMETER = "parameter";
    @Override public String getKey() { return "value signal parameter"; }
    @Override protected Double read(LivingEntity caster, LivingEntity target) {
        return signalNumber(caster);
    }
    private Double signalNumber(LivingEntity caster) {
        Object value = DynamicSkill.getCastData(caster).get("signal-" + settings.getString(PARAMETER, ""));
        if (value == null) return null;
        try { return Double.valueOf(value.toString()); }
        catch (NumberFormatException ex) { return null; }
    }
}
