package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;

import java.util.List;

/** Sets remaining air in ticks, clamped to each target's own maximum air. */
@SkillNode(key = "air set", name = "Air Set", nameZh = "设置氧气",
        descriptionZh = "按目标最大氧气值钳制剩余气泡刻数。")
public final class AirSetMechanic extends MechanicComponent {
    @SkillField(kind = FieldKind.AttributeValue, label = "Air Ticks", labelZh = "氧气刻数", defaultValue = "300")
    private static final String TICKS = "ticks";
    @Override public String getKey() { return "air set"; }
    @Override public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        double requested = parseValues(caster, TICKS, level, 300);
        if (!Double.isFinite(requested) || requested < Integer.MIN_VALUE || requested > Integer.MAX_VALUE) return false;
        for (LivingEntity target : targets)
            target.setRemainingAir(Math.max(0, Math.min(target.getMaximumAir(), (int) Math.round(requested))));
        return !targets.isEmpty();
    }
}
