package com.sucy.skill.dynamic.condition;

import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.hook.EconomyService;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/** An unavailable Vault provider is an unknown balance, never a free purchase. */
@SkillNode(key = "check economy", name = "Check Economy", nameZh = "检查经济余额",
        descriptionZh = "需要 Vault 及经济服务；缺失时条件不通过。", container = true,
        requiresPlugins = {"Vault"})
public final class CheckEconomyCondition extends ConditionComponent {
    @SkillField(kind = FieldKind.AttributeValue, label = "Minimum", labelZh = "最低余额", defaultValue = "0")
    private static final String MINIMUM = "minimum";
    @SkillField(kind = FieldKind.AttributeValue, label = "Maximum", labelZh = "最高余额", defaultValue = "1000000")
    private static final String MAXIMUM = "maximum";
    @Override public String getKey() { return "check economy"; }
    @Override boolean test(LivingEntity caster, int level, LivingEntity target) {
        if (!(target instanceof Player)) return false;
        Double balance = EconomyService.balance((Player) target);
        double min = parseValues(caster, MINIMUM, level, 0);
        double max = parseValues(caster, MAXIMUM, level, 1000000);
        return balance != null && Double.isFinite(balance) && Double.isFinite(min)
                && Double.isFinite(max) && min <= balance && balance <= max;
    }
}
