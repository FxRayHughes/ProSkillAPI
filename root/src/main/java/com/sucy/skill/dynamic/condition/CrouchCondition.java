/**
 * SkillAPI
 * com.sucy.skill.dynamic.condition.CrouchCondition
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Steven Sucy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.sucy.skill.dynamic.condition;

import com.rit.sucy.config.parse.DataSection;
import com.sucy.skill.dynamic.DynamicSkill;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

@SkillNode(
        key = "crouch",
        name = "Crouch",
        nameZh = "检查下蹲",
        description = "Applies child components if the target player(s) are crouching",
        descriptionZh = "要求目标是玩家且潜行状态符合配置。非玩家目标一律不通过。",
        container = true)
public class CrouchCondition extends ConditionComponent {
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Crouching",
            labelZh = "下蹲",
            tooltip = "[crouch] Whether or not the player should be crouching",
            tooltipZh = "只有值恰好等于“false”（忽略大小写）才要求未潜行，其余任何值都要求正在潜行。该值在加载时解析一次。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "True")
    private static final String CROUCH = "crouch";

    private boolean crouch;

    @Override
    public String getKey() {
        return "crouch";
    }

    @Override
    public void load(DynamicSkill skill, DataSection config) {
        super.load(skill, config);
        crouch = !settings.getString(CROUCH, "true").toLowerCase().equals("false");
    }

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        return target instanceof Player && ((Player) target).isSneaking() == crouch;
    }
}
