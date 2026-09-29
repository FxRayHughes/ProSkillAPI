package com.sucy.skill.dynamic.condition;

import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

/** Measures angle between caster view and the caster-to-target direction. */
@SkillNode(key = "check facing angle", name = "Check Facing Angle", nameZh = "检查面向角度",
        descriptionZh = "判断施法者是否朝向目标；可只计算水平夹角。", container = true)
public final class CheckFacingAngleCondition extends ConditionComponent {
    @SkillField(kind = FieldKind.AttributeValue, label = "Maximum Degrees", labelZh = "最大夹角", defaultValue = "45")
    private static final String MAX = "maximum";
    @SkillField(kind = FieldKind.BooleanValue, label = "Horizontal", labelZh = "只看水平面", defaultValue = "True")
    private static final String HORIZONTAL = "horizontal";
    @Override public String getKey() { return "check facing angle"; }
    @Override boolean test(LivingEntity caster, int level, LivingEntity target) {
        if (caster == null || target == null || caster == target || !caster.getWorld().equals(target.getWorld()))
            return false;
        double max = parseValues(caster, MAX, level, 45);
        if (!Double.isFinite(max) || max < 0 || max > 180) return false;
        Vector facing = caster.getEyeLocation().getDirection();
        Vector toward = target.getEyeLocation().toVector().subtract(caster.getEyeLocation().toVector());
        if (settings.getBool(HORIZONTAL, true)) { facing.setY(0); toward.setY(0); }
        if (facing.lengthSquared() == 0 || toward.lengthSquared() == 0) return false;
        double cosine = Math.max(-1, Math.min(1, facing.normalize().dot(toward.normalize())));
        return Math.toDegrees(Math.acos(cosine)) <= max;
    }
}
