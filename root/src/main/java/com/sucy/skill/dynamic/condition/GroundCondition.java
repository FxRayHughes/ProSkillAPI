package com.sucy.skill.dynamic.condition;

import org.bukkit.entity.LivingEntity;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * SkillAPI © 2018
 * com.sucy.skill.dynamic.condition.Ground
 */
@SkillNode(
        key = "ground",
        name = "Ground",
        nameZh = "检查地面",
        description = "Applies child components when the target is on the ground",
        descriptionZh = "按目标是否站在地面上过滤。注意只有配置值恰好等于“on ground”（忽略大小写）时才要求着地，其余任何值都要求离地，所以填错字会得到相反结果。",
        container = true)
public class GroundCondition extends ConditionComponent {
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Type",
            labelZh = "类型",
            tooltip = "[type] Whether or not the target should be on the ground",
            tooltipZh = "值等于“on ground”（忽略大小写）表示要求目标站在地面；其余任何值（含 Not On Ground）都要求目标离地。",
            options = {"On Ground", "Not On Ground"},
            optionsZh = {"可选值1", "可选值2"},
            defaultValue = "On Ground")
    private static final String type = "type";

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        final boolean onGround = target.isOnGround();
        final boolean wantOnGround = settings.getString(type, "on ground").equalsIgnoreCase("on ground");
        return onGround == wantOnGround;
    }

    @Override
    public String getKey() {
        return "ground";
    }
}
