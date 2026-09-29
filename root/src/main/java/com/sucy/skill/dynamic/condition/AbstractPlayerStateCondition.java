package com.sucy.skill.dynamic.condition;

import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/** Distinct state keys share only the expected-state protocol and missing-player rule. */
public abstract class AbstractPlayerStateCondition extends ConditionComponent {
    @SkillField(kind = FieldKind.BooleanValue, label = "Expected State", labelZh = "期望状态", defaultValue = "True")
    private static final String EXPECTED = "expected";
    @Override boolean test(LivingEntity caster, int level, LivingEntity target) {
        if (!(target instanceof Player)) return false;
        Player player = (Player) target;
        boolean actual;
        switch (getKey()) {
            case "check sprint": actual = player.isSprinting(); break;
            case "check glide": actual = player.isGliding(); break;
            case "check flying": actual = player.isFlying(); break;
            case "check blocking": actual = player.isBlocking(); break;
            default: return false;
        }
        return actual == settings.getBool(EXPECTED, true);
    }
}
