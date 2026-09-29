package com.sucy.skill.dynamic.condition;

import com.sucy.skill.dynamic.DynamicSkill;
import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;

import java.util.Map;

/** Restricts a branch to a currently executing signal and an optional typed parameter. */
@SkillNode(key = "check signal", name = "Check Signal", nameZh = "检查当前信号",
        descriptionZh = "只在信号接收执行期间满足；可检查参数是否存在。", container = true)
public final class CheckSignalCondition extends ConditionComponent {
    @SkillField(kind = FieldKind.StringValue, label = "Channel", labelZh = "频道", defaultValue = "signal")
    private static final String CHANNEL = "channel";
    @SkillField(kind = FieldKind.StringValue, label = "Parameter", labelZh = "参数名",
            tooltipZh = "留空只检查频道；非空时还要求参数存在。")
    private static final String PARAM = "parameter";
    @Override public String getKey() { return "check signal"; }
    @Override boolean test(LivingEntity caster, int level, LivingEntity target) {
        Map<String, Object> data = DynamicSkill.getCastData(caster);
        if (!settings.getString(CHANNEL, "signal").equals(data.get("signal-channel"))) return false;
        String parameter = settings.getString(PARAM, "");
        return parameter.isEmpty() || data.containsKey("signal-" + parameter);
    }
}
