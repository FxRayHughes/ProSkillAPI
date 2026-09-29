package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.hook.EconomyService;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;

/** Signed Vault transactions report success only after the provider accepts them. */
@SkillNode(key = "economy adjust", name = "Economy Adjust", nameZh = "调整经济余额",
        descriptionZh = "正数存入，负数扣除；Vault 或经济服务缺失时不执行。", requiresPlugins = {"Vault"})
public final class EconomyAdjustMechanic extends MechanicComponent {
    @SkillField(kind = FieldKind.AttributeValue, label = "Delta", labelZh = "余额变化", defaultValue = "1")
    private static final String DELTA = "delta";
    @Override public String getKey() { return "economy adjust"; }
    @Override public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        double amount = parseValues(caster, DELTA, level, 1);
        if (!Double.isFinite(amount) || amount == 0) return false;
        boolean applied = false;
        for (LivingEntity target : targets) if (target instanceof Player)
            applied |= EconomyService.adjust((Player) target, amount);
        return applied;
    }
}
