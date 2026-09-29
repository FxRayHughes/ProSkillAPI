package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;
import com.sucy.skill.combat.shield.ShieldManager;

/** Reads one specific numeric source and refuses to turn missing state into zero. */
@SkillNode(key = "value shield", name = "Value Shield", nameZh = "读取护盾余量",
        descriptionZh = "来源不存在时不覆盖输出引用键；只读取第一个目标。")
public final class ValueShieldMechanic extends AbstractValueReadMechanic {
    @SkillField(kind = FieldKind.StringValue, label = "Shield Key", labelZh = "护盾键")
    private static final String SHIELD_KEY = "shield-key";
    @Override public String getKey() { return "value shield"; }
    @Override protected Double read(LivingEntity caster, LivingEntity target) {
        return target == null ? null : ShieldManager.remaining(target, settings.getString("shield-key", ""));
    }
}
