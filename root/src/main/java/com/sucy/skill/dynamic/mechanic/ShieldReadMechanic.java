package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.combat.shield.ShieldManager;
import com.sucy.skill.dynamic.DynamicSkill;
import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;
import java.util.List;

/** Copies live shield capacity into the current caster's value context. */
@SkillNode(key = "shield read", name = "Shield Read", nameZh = "读取护盾",
        descriptionZh = "读取第一个目标的当前护盾容量和层数，供后续数值或信号节点使用。")
public class ShieldReadMechanic extends MechanicComponent {
    @SkillField(kind = FieldKind.StringValue, label = "Layer Key", labelZh = "护盾键",
            tooltipZh = "留空统计全部层。", defaultValue = "shield")
    private static final String KEY = "key";
    @SkillField(kind = FieldKind.StringValue, label = "Value Key", labelZh = "数值键",
            tooltipZh = "容量写入此键，层数写入此键加 -count。", defaultValue = "shield")
    private static final String VALUE = "value-key";
    @Override public String getKey() { return "shield read"; }
    @Override public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        if (targets.isEmpty()) return false;
        String key = settings.getString(KEY, "shield");
        String valueKey = settings.getString(VALUE, "shield");
        DynamicSkill.getCastData(caster).put(valueKey, ShieldManager.remaining(targets.get(0), key));
        DynamicSkill.getCastData(caster).put(valueKey + "-count", ShieldManager.count(targets.get(0), key));
        return true;
    }
}
