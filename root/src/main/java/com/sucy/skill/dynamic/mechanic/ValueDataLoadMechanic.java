package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;
import com.sucy.skill.dynamic.data.DataData;
import com.sucy.skill.dynamic.data.DataSkill;
import com.sucy.skill.dynamic.data.DataTagData;

/** Reads one specific numeric source and refuses to turn missing state into zero. */
@SkillNode(key = "value data load", name = "Value Data Load", nameZh = "读取存储数据",
        descriptionZh = "来源不存在时不覆盖输出引用键；只读取第一个目标。")
public final class ValueDataLoadMechanic extends AbstractValueReadMechanic {
    @SkillField(kind = FieldKind.StringValue, label = "Data Key", labelZh = "数据键")
    private static final String DATA_KEY = "data-key";
    @Override public String getKey() { return "value data load"; }
    @Override protected Double read(LivingEntity caster, LivingEntity target) {
        return stored(target);
    }
    private Double stored(LivingEntity target) {
        if (target == null) return null;
        DataData data = DataSkill.getDataData(target.getUniqueId(), false);
        if (data == null) return null;
        DataTagData tag = data.getDataTagData(settings.getString(DATA_KEY, ""));
        return tag == null || (tag.time != -1 && tag.overTime < System.currentTimeMillis())
                ? null : tag.getValue();
    }
}
