/**
 * SkillAPI
 * com.sucy.skill.dynamic.target.SingleTarget
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
package com.sucy.skill.dynamic.target;

import com.google.common.collect.ImmutableList;
import com.sucy.skill.api.target.TargetHelper;
import com.sucy.skill.cast.IIndicator;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Applies child components to the closest linear entity of each of the
 * provided targets.
 */
@SkillNode(
        key = "single",
        name = "Single",
        nameZh = "单体选取目标",
        description = "Targets a single unit in front of the current target (the casting player is the default target).",
        descriptionZh = "从每个当前目标的眼睛位置沿视线选中最先命中的那一个生物，也就是直线选取里距离最近的一个。命中后仍要通过阵营、穿墙和数量限制过滤；视线上没有符合条件的生物则该分支不执行。",
        container = true)
public class SingleTarget extends TargetComponent {
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Range",
            labelZh = "范围",
            tooltip = "[range] The max distance away any target can be in blocks",
            tooltipZh = "视线判定的最大距离，单位方块；超出该距离的生物不参与判定。数值随技能等级/属性变化。")
    private static final String RANGE     = "range";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Tolerance",
            labelZh = "容差",
            tooltip = "[tolerance] How much to expand the potential entity's hitbox in all directions, in blocks. This makes it easier to aim",
            tooltipZh = "把候选生物的碰撞箱向各个方向额外扩大的方块数，值越大越容易瞄中（默认 4）。数值随技能等级/属性变化。")
    private static final String TOLERANCE = "tolerance";

    /** {@inheritDoc} */
    @Override
    public void makeIndicators(List<IIndicator> list, Player caster, LivingEntity target, int level) {
        double range = parseValues(caster, RANGE, level, 3.0);
        double angle = parseValues(caster, TOLERANCE, level, 4.0);
        makeConeIndicator(list, target, range, angle);
    }

    /** {@inheritDoc} */
    @Override
    List<LivingEntity> getTargets(
            final LivingEntity caster, final int level, final List<LivingEntity> targets) {

        double range = parseValues(caster, RANGE, level, 5.0);
        double tolerance = parseValues(caster, TOLERANCE, level, 4.0);
        return determineTargets(caster, level, targets, t -> {
            final LivingEntity target = TargetHelper.getLivingTarget(t, range, tolerance);
            return target == null ? ImmutableList.of() : ImmutableList.of(target);
        });
    }

    @Override
    public String getKey() {
        return "single";
    }
}
