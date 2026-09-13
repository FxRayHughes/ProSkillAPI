/**
 * SkillAPI
 * com.sucy.skill.dynamic.condition.DirectionCondition
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Steven Sucy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software") to deal
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
import com.sucy.skill.api.target.TargetHelper;
import com.sucy.skill.dynamic.DynamicSkill;
import org.bukkit.entity.LivingEntity;

import java.util.function.BiPredicate;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * A condition for dynamic skills that requires the target or caster to be facing a direction relative to the other
 */
@SkillNode(
        key = "direction",
        name = "Direction",
        nameZh = "检查方向",
        description = "Applies child components when the target or caster is facing the correct direction relative to the other.",
        descriptionZh = "检查施法者与目标之间的朝向关系。类型选“Target”时判断目标是否面朝施法者，选“Caster”（或其他值）时判断施法者是否面朝目标；再按“方向”要求面朝（Towards）或背对（Away）。两个设置都在加载时解析，比较忽略大小写。",
        container = true)
public class DirectionCondition extends ConditionComponent
{
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Type",
            labelZh = "类型",
            tooltip = "[type] The entity to check the direction of",
            tooltipZh = "要检查朝向的一方：Target=看目标是否面朝施法者；其他值（含 Caster）=看施法者是否面朝目标。",
            options = {"Target", "Caster"},
            optionsZh = {"可选值1", "可选值2"},
            defaultValue = "Target")
    private static final String TYPE      = "type";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Direction",
            labelZh = "方向",
            tooltip = "[direction] The direction the chosen entity needs to be looking relative to the other.",
            tooltipZh = "只有值等于“towards”（忽略大小写）才要求面朝对方，其余任何值（含 Away）都要求背对对方。",
            options = {"Away", "Towards"},
            optionsZh = {"方向1", "方向2"},
            defaultValue = "Away")
    private static final String DIRECTION = "direction";

    private BiPredicate<LivingEntity, LivingEntity> test;
    private boolean towards;

    @Override
    public String getKey() {
        return "direction";
    }

    @Override
    public void load(DynamicSkill skill, DataSection config) {
        super.load(skill, config);
        towards = settings.getString(DIRECTION, "Towards").equalsIgnoreCase("towards");
        test = settings.getString(TYPE, "Caster").equalsIgnoreCase("target")
                ? (caster, target) -> TargetHelper.isInFront(target, caster)
                : TargetHelper::isInFront;
    }

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        return test.test(caster, target) == towards;
    }
}
