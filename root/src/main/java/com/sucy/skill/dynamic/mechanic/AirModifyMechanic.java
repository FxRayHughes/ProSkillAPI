package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;

import java.util.List;

/** Adds a signed air delta without overflowing the target's supported range. */
@SkillNode(key = "air modify", name = "Air Modify", nameZh = "调整氧气",
        descriptionZh = "在当前剩余氧气上加减刻数，并钳制到零与最大值之间。")
public final class AirModifyMechanic extends MechanicComponent {
    @SkillField(kind = FieldKind.AttributeValue, label = "Delta Ticks", labelZh = "变化刻数", defaultValue = "20")
    private static final String DELTA = "delta";
    @Override public String getKey() { return "air modify"; }
    @Override public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        double delta = parseValues(caster, DELTA, level, 20);
        if (!Double.isFinite(delta) || Math.abs(delta) > Integer.MAX_VALUE) return false;
        for (LivingEntity target : targets) {
            long next = (long) target.getRemainingAir() + Math.round(delta);
            target.setRemainingAir((int) Math.max(0, Math.min(target.getMaximumAir(), next)));
        }
        return !targets.isEmpty();
    }
}
