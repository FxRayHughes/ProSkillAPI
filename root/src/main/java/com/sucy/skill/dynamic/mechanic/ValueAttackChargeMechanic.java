package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/** Reads one specific numeric source and refuses to turn missing state into zero. */
@SkillNode(key = "value attack charge", name = "Value Attack Charge", nameZh = "读取攻击蓄力",
        descriptionZh = "来源不存在时不覆盖输出引用键；只读取第一个目标。")
public final class ValueAttackChargeMechanic extends AbstractValueReadMechanic {
    @Override public String getKey() { return "value attack charge"; }
    @Override protected Double read(LivingEntity caster, LivingEntity target) {
        return target instanceof Player ? (double) ((Player) target).getAttackCooldown() : null;
    }
}
