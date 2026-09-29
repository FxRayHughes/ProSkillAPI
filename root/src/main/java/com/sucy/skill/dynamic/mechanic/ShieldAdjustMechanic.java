package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.combat.shield.ShieldManager;
import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;
import java.util.List;

/** Changes remaining capacity without recreating the layer or its expiry. */
@SkillNode(key = "shield adjust", name = "Shield Adjust", nameZh = "调整护盾",
        descriptionZh = "对匹配键的护盾层增加或减少当前容量；结果限制在零到各层最大容量。")
public class ShieldAdjustMechanic extends MechanicComponent {
    @SkillField(kind = FieldKind.StringValue, label = "Layer Key", labelZh = "护盾键",
            tooltipZh = "留空修改所有层。", defaultValue = "shield")
    private static final String KEY = "key";
    @SkillField(kind = FieldKind.AttributeValue, label = "Delta", labelZh = "容量变化",
            tooltipZh = "正数恢复容量，负数消耗容量。", defaultValue = "1")
    private static final String DELTA = "delta";
    @Override public String getKey() { return "shield adjust"; }
    @Override public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        double delta = parseValues(caster, DELTA, level, 1);
        boolean changed = false;
        for (LivingEntity target : targets)
            changed |= ShieldManager.adjust(target, settings.getString(KEY, "shield"), delta) > 0;
        return changed;
    }
}
