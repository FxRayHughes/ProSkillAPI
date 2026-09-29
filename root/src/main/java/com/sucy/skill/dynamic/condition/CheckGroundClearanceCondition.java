package com.sucy.skill.dynamic.condition;

import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

/** Tests a bounded vertical column without depending on version-specific ray APIs. */
@SkillNode(key = "check ground clearance", name = "Check Ground Clearance", nameZh = "检查头顶净空",
        descriptionZh = "目标上方指定方块数内没有固体方块时满足，最多扫描三十二格。", container = true)
public final class CheckGroundClearanceCondition extends ConditionComponent {
    @SkillField(kind = FieldKind.IntValue, label = "Blocks", labelZh = "净空格数", defaultValue = "2")
    private static final String BLOCKS = "blocks";
    @Override public String getKey() { return "check ground clearance"; }
    @Override boolean test(LivingEntity caster, int level, LivingEntity target) {
        int blocks = settings.getInt(BLOCKS, 2);
        if (target == null || blocks < 1 || blocks > 32) return false;
        Location at = target.getLocation();
        for (int offset = 1; offset <= blocks; offset++)
            if (at.clone().add(0, offset, 0).getBlock().getType().isSolid()) return false;
        return true;
    }
}
