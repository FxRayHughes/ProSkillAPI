package com.sucy.skill.dynamic.condition;

import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

/** Compares horizontal or three-dimensional distance without loading blocks. */
@SkillNode(key = "check distance", name = "Check Distance", nameZh = "检查距离",
        descriptionZh = "施法者与目标须在同一世界；可只看水平距离。", container = true)
public final class CheckDistanceCondition extends ConditionComponent {
    @SkillField(kind = FieldKind.AttributeValue, label = "Minimum", labelZh = "最小距离", defaultValue = "0")
    private static final String MIN = "minimum";
    @SkillField(kind = FieldKind.AttributeValue, label = "Maximum", labelZh = "最大距离", defaultValue = "16")
    private static final String MAX = "maximum";
    @SkillField(kind = FieldKind.BooleanValue, label = "Horizontal", labelZh = "只看水平距离", defaultValue = "False")
    private static final String HORIZONTAL = "horizontal";
    @Override public String getKey() { return "check distance"; }
    @Override boolean test(LivingEntity caster, int level, LivingEntity target) {
        if (caster == null || target == null || !caster.getWorld().equals(target.getWorld())) return false;
        double min = parseValues(caster, MIN, level, 0);
        double max = parseValues(caster, MAX, level, 16);
        if (!Double.isFinite(min) || !Double.isFinite(max) || min < 0 || max < min) return false;
        Location a = caster.getLocation();
        Location b = target.getLocation();
        double dx = a.getX() - b.getX();
        double dy = settings.getBool(HORIZONTAL, false) ? 0 : a.getY() - b.getY();
        double dz = a.getZ() - b.getZ();
        double squared = dx * dx + dy * dy + dz * dz;
        return squared >= min * min && squared <= max * max;
    }
}
