package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import com.sucy.skill.hook.EconomyService;

/** Reads one specific numeric source and refuses to turn missing state into zero. */
@SkillNode(key = "value economy", name = "Value Economy", nameZh = "读取经济余额",
        descriptionZh = "来源不存在时不覆盖输出引用键；只读取第一个目标。", requiresPlugins = {"Vault"})
public final class ValueEconomyMechanic extends AbstractValueReadMechanic {
    @Override public String getKey() { return "value economy"; }
    @Override protected Double read(LivingEntity caster, LivingEntity target) {
        return target instanceof Player ? EconomyService.balance((Player) target) : null;
    }
}
